package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.loyalty.AddPointsRequest;
import ao.angogas.backend.dto.request.loyalty.RedeemPointsRequest;
import ao.angogas.backend.dto.response.loyalty.LoyaltyBalanceResponse;
import ao.angogas.backend.dto.response.loyalty.LoyaltyPointResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.model.LoyaltyPoint;
import ao.angogas.backend.model.User;
import ao.angogas.backend.repository.LoyaltyPointRepository;
import ao.angogas.backend.repository.UserRepository;
import ao.angogas.backend.service.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyPointServiceImpl implements LoyaltyPointService {

    private final LoyaltyPointRepository loyaltyPointRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public LoyaltyBalanceResponse getBalance(User user) {
        return buildBalance(user.getId());
    }

    @Override
    @Transactional
    public void addPoints(UUID userId, int pontos, String motivo, UUID orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado"));
        LoyaltyPoint lp = LoyaltyPoint.builder()
                .user(user)
                .pontos(pontos)
                .motivo(motivo)
                .build();
        loyaltyPointRepository.save(lp);
    }

    @Override
    @Transactional
    public LoyaltyBalanceResponse redeem(RedeemPointsRequest request, User user) {
        int saldo = getSaldo(user.getId());
        if (saldo < request.pontos()) {
            throw new BusinessException("Saldo insuficiente. Tens " + saldo + " pontos disponíveis.");
        }
        LoyaltyPoint lp = LoyaltyPoint.builder()
                .user(user)
                .pontos(-request.pontos())
                .motivo(request.motivo())
                .build();
        loyaltyPointRepository.save(lp);
        return buildBalance(user.getId());
    }

    @Override
    @Transactional
    public LoyaltyBalanceResponse addManual(AddPointsRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado"));
        LoyaltyPoint lp = LoyaltyPoint.builder()
                .user(user)
                .pontos(request.pontos())
                .motivo(request.motivo())
                .build();
        loyaltyPointRepository.save(lp);
        return buildBalance(request.userId());
    }

    private int getSaldo(UUID userId) {
        Integer sum = loyaltyPointRepository.sumPontosByUserId(userId);
        return sum != null ? sum : 0;
    }

    private LoyaltyBalanceResponse buildBalance(UUID userId) {
        int saldo = getSaldo(userId);
        var pageable = PageRequest.of(0, 50, Sort.by("criadoEm").descending());
        List<LoyaltyPointResponse> historico = loyaltyPointRepository
                .findByUserId(userId, pageable)
                .map(lp -> new LoyaltyPointResponse(
                        lp.getId(), lp.getPontos(), lp.getMotivo(),
                        lp.getOrder() != null ? lp.getOrder().getId() : null,
                        lp.getCriadoEm()))
                .toList();
        return new LoyaltyBalanceResponse(saldo, historico);
    }
}
