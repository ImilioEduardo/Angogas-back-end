package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.subscription.CreateSubscriptionRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.subscription.SubscriptionResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.exception.UnauthorizedException;
import ao.angogas.backend.model.Subscription;
import ao.angogas.backend.model.User;
import ao.angogas.backend.repository.SubscriptionRepository;
import ao.angogas.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request, User user) {
        subscriptionRepository.findByUserIdAndActivaTrue(user.getId())
                .ifPresent(s -> { throw new BusinessException("Já tens uma subscrição activa"); });

        Subscription sub = Subscription.builder()
                .user(user)
                .plano(request.plano())
                .precoKz(request.precoKz())
                .inicio(request.inicio())
                .fim(request.fim())
                .renovacaoAuto(request.renovacaoAuto())
                .build();

        return toResponse(subscriptionRepository.save(sub));
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getMy(User user) {
        return subscriptionRepository.findByUserIdAndActivaTrue(user.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma subscrição activa"));
    }

    @Override
    @Transactional
    public void cancel(UUID id, User user) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscrição não encontrada"));
        if (!sub.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Acesso negado");
        }
        sub.setActiva(false);
        subscriptionRepository.save(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubscriptionResponse> listAll(Pageable pageable) {
        return PageResponse.from(subscriptionRepository.findAll(pageable).map(this::toResponse));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getUser().getId(),
                s.getPlano(),
                s.getPrecoKz(),
                s.getInicio(),
                s.getFim(),
                s.isActiva(),
                s.isRenovacaoAuto(),
                s.getCriadoEm()
        );
    }
}
