package ao.angogas.backend.websocket;

import ao.angogas.backend.dto.request.delivery.UpdateLocationRequest;
import ao.angogas.backend.dto.response.delivery.OrderTrackingResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TrackingMessageController {

    @MessageMapping("/tracking/{orderId}")
    @SendTo("/topic/tracking/{orderId}")
    public OrderTrackingResponse handleLocation(
            UpdateLocationRequest request,
            @DestinationVariable String orderId) {
        return OrderTrackingResponse.builder()
                .orderId(UUID.fromString(orderId))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .registadoEm(OffsetDateTime.now())
                .build();
    }
}
