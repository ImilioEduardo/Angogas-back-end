package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.pricing.PricingCalculateRequest;
import ao.angogas.backend.dto.request.pricing.PricingItemRequest;
import ao.angogas.backend.dto.request.pricing.UpdatePlatformSettingsRequest;
import ao.angogas.backend.dto.request.pricing.UpdateProductPricingRequest;
import ao.angogas.backend.dto.response.pricing.PlatformSettingsResponse;
import ao.angogas.backend.dto.response.pricing.PricingBreakdownResponse;
import ao.angogas.backend.dto.response.pricing.ProductPricingResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.model.*;
import ao.angogas.backend.repository.*;
import ao.angogas.backend.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private static final long SETTINGS_ID = 1L;

    private final PlatformSettingsRepository settingsRepository;
    private final ProductPricingRepository productPricingRepository;
    private final DeliveryFinancialsRepository deliveryFinancialsRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;

    // ──────────────────────────────────────────────────────────────────────
    // Cálculo de preço (fórmula §4 do documento de especificação)
    // ──────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PricingBreakdownResponse calculate(PricingCalculateRequest request) {

        PlatformSettings s = getSettingsEntity();

        List<PricingItemRequest> itens = request.resolvedItems();
        if (itens.isEmpty()) {
            throw new BusinessException("É necessário pelo menos um produto no pedido");
        }

        // 1. Validar todos os produtos e calcular subtotal agregado
        BigDecimal subtotalProduto = BigDecimal.ZERO;
        BigDecimal depositoGarrafa = BigDecimal.ZERO;
        int totalBotijas = 0;

        for (PricingItemRequest item : itens) {
            Product product = productRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

            if (!product.isActivo()) {
                throw new BusinessException("Produto indisponível: " + product.getNome());
            }

            totalBotijas += item.getQuantidade();

            subtotalProduto = subtotalProduto.add(
                    product.getPrecoKz().multiply(BigDecimal.valueOf(item.getQuantidade()))
            );

            // Depósito de garrafa por tipo de produto (se cliente não tem garrafa vazia)
            if (!request.isClienteTemGarrafa()) {
                Optional<ProductPricing> ppOpt = productPricingRepository.findByProductId(product.getId());
                depositoGarrafa = depositoGarrafa.add(
                        ppOpt.map(ProductPricing::getDepositoGarrafaKz).orElse(BigDecimal.ZERO)
                );
            }
        }

        if (totalBotijas > s.getQuantidadeMaxBotijas()) {
            throw new BusinessException(
                    "Quantidade máxima por pedido é " + s.getQuantidadeMaxBotijas() + " botijas");
        }

        // 3. Distância e tempo
        double distanciaKm = calcularDistancia(request, s);
        int tempoEstimadoMin = calcularTempo(distanciaKm, s);

        // 4. Taxa base = (custofixo / entregas) × (1 + margemEntrega)
        BigDecimal custofixoPorEntrega = s.getCustofixoMensal()
                .divide(BigDecimal.valueOf(s.getEntregasEstimadasMes()), 4, RoundingMode.HALF_UP);
        BigDecimal taxaBase = custofixoPorEntrega
                .multiply(BigDecimal.ONE.add(s.getMargemEntrega()))
                .setScale(2, RoundingMode.HALF_UP);

        // 5. Taxa distância = precoPorKm × distanciaKm
        BigDecimal taxaDistancia = s.getPrecoPorKm()
                .multiply(BigDecimal.valueOf(distanciaKm))
                .setScale(2, RoundingMode.HALF_UP);

        // 6. Taxa tempo = precoPorMinuto × tempoMin
        BigDecimal taxaTempo = s.getPrecoPorMinuto()
                .multiply(BigDecimal.valueOf(tempoEstimadoMin))
                .setScale(2, RoundingMode.HALF_UP);

        // 7. Subtotal = produto + depósito + taxas de entrega
        BigDecimal subtotal = subtotalProduto
                .add(depositoGarrafa)
                .add(taxaBase)
                .add(taxaDistancia)
                .add(taxaTempo);

        // 8. Comissão da app sobre o subtotal
        BigDecimal comissao = subtotal.multiply(s.getComissaoApp())
                .setScale(2, RoundingMode.HALF_UP);

        // 9. Total ao cliente
        BigDecimal total = subtotal.add(comissao);

        return PricingBreakdownResponse.builder()
                .subtotalProduto(subtotalProduto)
                .taxaBase(taxaBase)
                .taxaDistancia(taxaDistancia)
                .taxaTempo(taxaTempo)
                .depositoGarrafa(depositoGarrafa)
                .subtotal(subtotal)
                .comissao(comissao)
                .total(total)
                .distanciaKm(BigDecimal.valueOf(distanciaKm).setScale(2, RoundingMode.HALF_UP))
                .tempoEstimadoMin(tempoEstimadoMin)
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────
    // Parâmetros da plataforma
    // ──────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PlatformSettingsResponse getSettings() {
        return PlatformSettingsResponse.from(getSettingsEntity());
    }

    @Override
    @Transactional
    public PlatformSettingsResponse updateSettings(UpdatePlatformSettingsRequest req) {
        PlatformSettings s = getSettingsEntity();

        s.setPrecoPorKm(req.getPrecoPorKm());
        s.setPrecoPorMinuto(req.getPrecoPorMinuto());
        s.setComissaoApp(req.getComissaoApp());
        s.setDistanciaMinKm(req.getDistanciaMinKm());
        s.setDistanciaMaxKm(req.getDistanciaMaxKm());
        s.setMargemEntrega(req.getMargemEntrega());
        s.setQuantidadeMaxBotijas(req.getQuantidadeMaxBotijas());
        s.setCustofixoMensal(req.getCustofixoMensal());
        s.setEntregasEstimadasMes(req.getEntregasEstimadasMes());
        s.setArmazemLatitude(req.getArmazemLatitude());
        s.setArmazemLongitude(req.getArmazemLongitude());
        s.setVelocidadeMediaKmh(req.getVelocidadeMediaKmh());

        return PlatformSettingsResponse.from(settingsRepository.save(s));
    }

    // ──────────────────────────────────────────────────────────────────────
    // Precificação por produto
    // ──────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductPricingResponse> listProductPricing() {
        return productPricingRepository.findAllActiveWithProduct()
                .stream()
                .map(ProductPricingResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ProductPricingResponse upsertProductPricing(UpdateProductPricingRequest req) {
        Product product = productRepository.findById(req.getProdutoId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        ProductPricing pp = productPricingRepository.findByProductId(req.getProdutoId())
                .orElse(ProductPricing.builder().product(product).build());

        pp.setCustoSonagazKz(req.getCustoSonagazKz());
        pp.setMargemProduto(req.getMargemProduto());
        pp.setDepositoGarrafaKz(req.getDepositoGarrafaKz());
        pp.setActivo(true);

        return ProductPricingResponse.from(productPricingRepository.save(pp));
    }

    // ──────────────────────────────────────────────────────────────────────
    // Registo financeiro por entrega concluída
    // ──────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void recordDeliveryFinancials(Order order) {
        if (deliveryFinancialsRepository.existsByOrderId(order.getId())) {
            return; // idempotente
        }

        PlatformSettings s = getSettingsEntity();

        // Custo SonaGas dos produtos entregues
        BigDecimal custoProduto = order.getItems().stream()
                .map(item -> {
                    Optional<ProductPricing> pp = productPricingRepository.findByProductId(item.getProduct().getId());
                    BigDecimal custo = pp.map(ProductPricing::getCustoSonagazKz).orElse(BigDecimal.ZERO);
                    return custo.multiply(BigDecimal.valueOf(item.getQuantidade()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal precoCobrado = order.getTotalKz();
        BigDecimal comissao = precoCobrado.multiply(s.getComissaoApp()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxaEntrega = precoCobrado.subtract(custoProduto).subtract(comissao);

        BigDecimal custofixoPorEntrega = s.getCustofixoMensal()
                .divide(BigDecimal.valueOf(s.getEntregasEstimadasMes()), 2, RoundingMode.HALF_UP);
        BigDecimal lucroLiquido = precoCobrado.subtract(custoProduto).subtract(custofixoPorEntrega);

        DeliveryFinancials financials = DeliveryFinancials.builder()
                .order(order)
                .precoCobradoKz(precoCobrado)
                .custoProdutoKz(custoProduto)
                .taxaEntregaKz(taxaEntrega.max(BigDecimal.ZERO))
                .comissaoPlataformaKz(comissao)
                .lucroLiquidoKz(lucroLiquido)
                .build();

        deliveryFinancialsRepository.save(financials);
        log.info("Financials registados para pedido {}: total={} Kz, lucro={} Kz",
                order.getId(), precoCobrado, lucroLiquido);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ──────────────────────────────────────────────────────────────────────

    private PlatformSettings getSettingsEntity() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Parâmetros da plataforma não encontrados na base de dados. " +
                        "Executa a migração V9__pricing_engine.sql"));
    }

    /**
     * Calcula a distância em km entre o armazém e o endereço do cliente.
     * Se o endereço não tiver coordenadas GPS, usa a distância mínima configurada.
     * Lança BusinessException se a distância ultrapassar o máximo definido.
     */
    private double calcularDistancia(PricingCalculateRequest request, PlatformSettings s) {
        double distanciaMin = s.getDistanciaMinKm().doubleValue();
        double distanciaMax = s.getDistanciaMaxKm().doubleValue();

        if (request.getAddressId() == null) {
            return distanciaMin;
        }

        Address address = addressRepository.findById(request.getAddressId()).orElse(null);
        if (address == null || address.getLatitude() == null || address.getLongitude() == null) {
            return distanciaMin;
        }

        double distancia = haversine(
                s.getArmazemLatitude().doubleValue(), s.getArmazemLongitude().doubleValue(),
                address.getLatitude().doubleValue(), address.getLongitude().doubleValue()
        );

        if (distancia > distanciaMax) {
            throw new BusinessException(String.format(
                    "Endereço fora da zona de cobertura (%.1f km). Máximo permitido: %.0f km.",
                    distancia, distanciaMax));
        }

        return Math.max(distancia, distanciaMin);
    }

    /**
     * Estima o tempo de entrega em minutos com base na distância e velocidade média.
     * Mínimo de 5 minutos para tempo de preparação.
     */
    private int calcularTempo(double distanciaKm, PlatformSettings s) {
        double tempoHoras = distanciaKm / s.getVelocidadeMediaKmh().doubleValue();
        int tempoMin = (int) Math.ceil(tempoHoras * 60);
        return Math.max(tempoMin, 5);
    }

    /**
     * Fórmula de Haversine — distância em linha recta entre dois pontos GPS (km).
     * Suficientemente precisa para distâncias urbanas de Luanda.
     */
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }
}
