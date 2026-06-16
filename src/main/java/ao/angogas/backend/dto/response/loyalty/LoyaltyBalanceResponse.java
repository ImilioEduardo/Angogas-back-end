package ao.angogas.backend.dto.response.loyalty;

import java.util.List;

public record LoyaltyBalanceResponse(
        int saldo,
        List<LoyaltyPointResponse> historico
) {}
