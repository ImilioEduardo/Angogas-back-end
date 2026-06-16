package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.loyalty.AddPointsRequest;
import ao.angogas.backend.dto.request.loyalty.RedeemPointsRequest;
import ao.angogas.backend.dto.response.loyalty.LoyaltyBalanceResponse;
import ao.angogas.backend.model.User;

import java.util.UUID;

public interface LoyaltyPointService {
    LoyaltyBalanceResponse getBalance(User user);
    void addPoints(UUID userId, int pontos, String motivo, UUID orderId);
    LoyaltyBalanceResponse redeem(RedeemPointsRequest request, User user);
    LoyaltyBalanceResponse addManual(AddPointsRequest request);
}
