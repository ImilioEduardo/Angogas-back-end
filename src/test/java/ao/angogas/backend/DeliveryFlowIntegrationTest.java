package ao.angogas.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryFlowIntegrationTest extends AbstractIntegrationTest {

    private String adminToken;
    private String clientToken;
    private String agentToken;
    private String zoneId;
    private String productId;
    private String addressId;

    @BeforeEach
    void setUp() {
        adminToken = loginAsAdmin();
        zoneId = createZone(adminToken, "Zona-Delivery-" + System.nanoTime());
        productId = createProduct(adminToken, "Botijão 13kg Delivery", 5000.0, 100);

        String clientPhone = uniquePhone();
        register("Cliente Entrega", clientPhone);
        clientToken = doLogin(clientPhone, "Senha@123");
        addressId = createAddress(clientToken, "Maianga");

        Map<String, Object> agentInfo = createAgent(adminToken, zoneId);
        agentToken = (String) agentInfo.get("token");
    }

    // ── Accept / Reject ───────────────────────────────────────────────────────

    @Test
    void accept_aguardandoOrder_movesToConfirmado() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.POST, request(null, agentToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> accepted = (Map<String, Object>) res.getBody().get("data");
        assertThat(accepted.get("status")).isEqualTo("CONFIRMADO");
        assertThat(accepted.get("entregadorNome")).isNotNull();
    }

    @Test
    void accept_alreadyAcceptedOrder_returns400() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        restTemplate().exchange(base() + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.POST, request(null, agentToken), Map.class);

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.POST, request(null, agentToken), Map.class);
        assertThat(res.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    @Test
    void reject_aguardandoOrder_returns200() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId + "/reject",
                HttpMethod.POST, request(null, agentToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void accept_byClientRole_returns403() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.POST, request(null, clientToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── Status progression ────────────────────────────────────────────────────

    @Test
    void statusFlow_confirmedToEntregue_allStepsSucceed() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");
        String codigoEntrega = (String) order.get("codigoEntrega");

        restTemplate().exchange(base() + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.POST, request(null, agentToken), Map.class);

        for (String status : new String[]{"A_PREPARAR", "A_CAMINHO"}) {
            ResponseEntity<Map> res = restTemplate().exchange(
                    base() + "/api/v1/delivery/orders/" + orderId + "/status",
                    HttpMethod.PUT, request(Map.of("status", status), agentToken), Map.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        ResponseEntity<Map> entregueRes = restTemplate().exchange(
                base() + "/api/v1/delivery/orders/" + orderId + "/status",
                HttpMethod.PUT,
                request(Map.of("status", "ENTREGUE", "codigoEntrega", codigoEntrega), agentToken),
                Map.class);
        assertThat(entregueRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> final$ = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId,
                HttpMethod.GET, request(null, clientToken), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> finalOrder = (Map<String, Object>) final$.getBody().get("data");
        assertThat(finalOrder.get("status")).isEqualTo("ENTREGUE");
    }

    // ── GPS location ──────────────────────────────────────────────────────────

    @Test
    void updateLocation_validPayload_returns200() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        restTemplate().exchange(base() + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.POST, request(null, agentToken), Map.class);

        var body = Map.of("latitude", -8.9064, "longitude", 13.1943);
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/delivery/orders/" + orderId + "/location",
                HttpMethod.POST, request(body, agentToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getLastLocation_afterUpdate_returnsCoordinates() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        restTemplate().exchange(base() + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.POST, request(null, agentToken), Map.class);

        restTemplate().exchange(base() + "/api/v1/delivery/orders/" + orderId + "/location",
                HttpMethod.POST, request(Map.of("latitude", -8.9064, "longitude", 13.1943), agentToken), Map.class);

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/tracking/orders/" + orderId + "/location",
                HttpMethod.GET, request(null, clientToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> location = (Map<String, Object>) res.getBody().get("data");
        assertThat(location).isNotNull();
        assertThat(location.get("latitude")).isNotNull();
    }

    // ── Available orders ──────────────────────────────────────────────────────

    @Test
    void availableOrders_includesClienteTelefone() {
        createOrder(clientToken, addressId, zoneId, productId, 1);

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/delivery/available-orders",
                HttpMethod.GET, request(null, agentToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        assertThat((int) data.get("totalElements")).isGreaterThanOrEqualTo(1);

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> content =
                (java.util.List<Map<String, Object>>) data.get("content");
        assertThat(content).isNotEmpty();
        assertThat(content.get(0)).containsKey("clienteTelefone");
    }

    // ── My orders list ────────────────────────────────────────────────────────

    @Test
    void myOrders_afterAccept_includesOrder() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        restTemplate().exchange(base() + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.POST, request(null, agentToken), Map.class);

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/delivery/my-orders",
                HttpMethod.GET, request(null, agentToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        assertThat((int) data.get("totalElements")).isGreaterThanOrEqualTo(1);
    }

    // ── Toggle disponivel ─────────────────────────────────────────────────────

    @Test
    void toggleDisponivel_byAgent_returns200() {
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/delivery/disponivel",
                HttpMethod.PATCH, request(null, agentToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void toggleDisponivel_byClientRole_returns403() {
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/delivery/disponivel",
                HttpMethod.PATCH, request(null, clientToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
