package ao.angogas.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFlowIntegrationTest extends AbstractIntegrationTest {

    private String adminToken;
    private String clientToken;
    private String zoneId;
    private String productId;
    private String addressId;

    @BeforeEach
    void setUp() {
        adminToken = loginAsAdmin();
        zoneId = createZone(adminToken, "Zona-" + System.nanoTime());
        productId = createProduct(adminToken, "Botijão 13kg", 5000.0, 50);

        String phone = uniquePhone();
        register("Cliente Teste", phone, "Senha@123");
        clientToken = doLogin(phone, "Senha@123");

        addressId = createAddress(clientToken, "Talatona");
    }

    // ── Create order ──────────────────────────────────────────────────────────

    @Test
    void createOrder_validRequest_returnsAguardandoAceitacao() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 2);
        assertThat(order.get("status")).isEqualTo("AGUARDANDO_ACEITACAO");
        assertThat(order.get("totalKz")).isNotNull();
    }

    @Test
    void createOrder_decrementsStock() {
        @SuppressWarnings("unchecked")
        Map<String, Object> before = (Map<String, Object>)
                restTemplate().getForEntity(base() + "/api/v1/products/" + productId, Map.class)
                        .getBody().get("data");
        int stockBefore = (int) before.get("stock");

        createOrder(clientToken, addressId, zoneId, productId, 3);

        @SuppressWarnings("unchecked")
        Map<String, Object> after = (Map<String, Object>)
                restTemplate().getForEntity(base() + "/api/v1/products/" + productId, Map.class)
                        .getBody().get("data");
        int stockAfter = (int) after.get("stock");

        assertThat(stockAfter).isEqualTo(stockBefore - 3);
    }

    @Test
    void createOrder_insufficientStock_returns400() {
        String scarceId = createProduct(adminToken, "Botijão 45kg", 15000.0, 1);

        Map<String, Object> first = createOrder(clientToken, addressId, zoneId, scarceId, 1);
        assertThat(first.get("status")).isEqualTo("AGUARDANDO_ACEITACAO");

        var body = Map.of(
                "addressId", addressId,
                "zoneId", zoneId,
                "metodoPagamento", "DINHEIRO",
                "items", List.of(Map.of("productId", scarceId, "quantidade", 1))
        );
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders", HttpMethod.POST, request(body, clientToken), Map.class);
        assertThat(res.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    @Test
    void createOrder_unknownZone_returns4xx() {
        var body = Map.of(
                "addressId", addressId,
                "zoneId", "00000000-0000-0000-0000-000000000000",
                "metodoPagamento", "DINHEIRO",
                "items", List.of(Map.of("productId", productId, "quantidade", 1))
        );
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders", HttpMethod.POST, request(body, clientToken), Map.class);
        assertThat(res.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    // ── Get order ─────────────────────────────────────────────────────────────

    @Test
    void getOrder_owner_returns200() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId, HttpMethod.GET, request(null, clientToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOrder_anotherClient_returns4xx() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        String otherPhone = uniquePhone();
        register("Outro", otherPhone, "Senha@123");
        String otherToken = doLogin(otherPhone, "Senha@123");

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId, HttpMethod.GET, request(null, otherToken), Map.class);
        assertThat(res.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    // ── List my orders ────────────────────────────────────────────────────────

    @Test
    void listMyOrders_returnsAtLeastOneOrder() {
        createOrder(clientToken, addressId, zoneId, productId, 1);

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/my", HttpMethod.GET, request(null, clientToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        assertThat((int) data.get("totalElements")).isGreaterThanOrEqualTo(1);
    }

    // ── Cancel order ──────────────────────────────────────────────────────────

    @Test
    void cancelOrder_inAguardandoStatus_restoresStock() {
        @SuppressWarnings("unchecked")
        int stockBefore = (int) ((Map<String, Object>)
                restTemplate().getForEntity(base() + "/api/v1/products/" + productId, Map.class)
                        .getBody().get("data")).get("stock");

        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 2);
        String orderId = (String) order.get("id");

        ResponseEntity<Map> cancelRes = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId + "/cancel",
                HttpMethod.DELETE, request(null, clientToken), Map.class);
        assertThat(cancelRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        int stockAfter = (int) ((Map<String, Object>)
                restTemplate().getForEntity(base() + "/api/v1/products/" + productId, Map.class)
                        .getBody().get("data")).get("stock");

        assertThat(stockAfter).isEqualTo(stockBefore);
    }

    @Test
    void cancelOrder_byAnotherClient_returns4xx() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        String otherPhone = uniquePhone();
        register("Outro", otherPhone, "Senha@123");
        String otherToken = doLogin(otherPhone, "Senha@123");

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/" + orderId + "/cancel",
                HttpMethod.DELETE, request(null, otherToken), Map.class);
        assertThat(res.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    @Test
    void adminUpdateStatus_changesOrderStatus() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        var body = Map.of("status", "CONFIRMADO");
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/admin/" + orderId + "/status",
                HttpMethod.PUT, request(body, adminToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> updated = (Map<String, Object>) res.getBody().get("data");
        assertThat(updated.get("status")).isEqualTo("CONFIRMADO");
    }

    @Test
    void adminUpdateStatus_byClient_returns403() {
        Map<String, Object> order = createOrder(clientToken, addressId, zoneId, productId, 1);
        String orderId = (String) order.get("id");

        var body = Map.of("status", "CONFIRMADO");
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/admin/" + orderId + "/status",
                HttpMethod.PUT, request(body, clientToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminListOrders_returns200WithContent() {
        createOrder(clientToken, addressId, zoneId, productId, 1);

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/admin", HttpMethod.GET, request(null, adminToken), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        assertThat((int) data.get("totalElements")).isGreaterThanOrEqualTo(1);
    }
}
