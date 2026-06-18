package ao.angogas.backend;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.NoOpResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Value("${local.server.port}")
    protected int port;

    protected RestTemplate restTemplate() {
        HttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate rt = new RestTemplate(factory);
        rt.setErrorHandler(new NoOpResponseErrorHandler());
        return rt;
    }

    protected String base() {
        return "http://localhost:" + port;
    }

    // ── Auth helpers ─────────────────────────────────────────────────────────

    protected String loginAsAdmin() {
        return doLogin("admin@angogas.ao", "Admin@123");
    }

    protected String doLogin(String emailOrPhone, String password) {
        var body = Map.of("emailOrTelefone", emailOrPhone, "password", password);
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/login", body, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = res.getBody();
        if (responseBody == null) {
            throw new IllegalStateException("Login returned null body for: " + emailOrPhone + " (HTTP " + res.getStatusCode() + ")");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        if (data == null) {
            throw new IllegalStateException("Login failed for " + emailOrPhone + " — response: " + responseBody);
        }
        return (String) data.get("accessToken");
    }

    protected void register(String nome, String telefone, String password) {
        var reg = Map.of("nome", nome, "telefone", telefone, "password", password);
        restTemplate().postForEntity(base() + "/api/v1/auth/register", reg, Map.class);
    }

    protected HttpHeaders headers(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    protected HttpEntity<Object> request(Object body, String token) {
        return new HttpEntity<>(body, headers(token));
    }

    // ── Setup helpers ─────────────────────────────────────────────────────────

    protected String createZone(String adminToken, String nome) {
        var body = Map.of("nome", nome, "municipio", "Luanda");
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/zones", HttpMethod.POST, request(body, adminToken), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        return (String) data.get("id");
    }

    protected String createProduct(String adminToken, String nome, double preco, int stock) {
        var body = Map.of("nome", nome, "precoKz", preco, "pesoKg", 13.0, "stock", stock);
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/products", HttpMethod.POST, request(body, adminToken), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        return (String) data.get("id");
    }

    protected String createAddress(String clientToken, String bairro) {
        var body = Map.of("bairro", bairro, "municipio", "Luanda");
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/addresses", HttpMethod.POST, request(body, clientToken), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        return (String) data.get("id");
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> createOrder(String clientToken, String addressId,
                                               String zoneId, String productId, int qty) {
        var body = Map.of(
                "addressId", addressId,
                "zoneId", zoneId,
                "metodoPagamento", "DINHEIRO",
                "items", java.util.List.of(Map.of("productId", productId, "quantidade", qty))
        );
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders", HttpMethod.POST, request(body, clientToken), Map.class);
        return (Map<String, Object>) res.getBody().get("data");
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> createAgent(String adminToken, String zoneId) {
        String phone = "+244" + (900000000L + (long) (Math.random() * 99999999));
        var body = Map.of(
                "nome", "Entregador Teste",
                "telefone", phone,
                "password", "Test@12345",
                "dataNascimento", "1995-01-01",
                "biNumero", "000" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "BA001",
                "cartaConducao", "L" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                "cartaConducaoDesde", "2015-01-01",
                "registoCriminal", true,
                "temSmartphone", true
        );
        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/delivery/admin/agents", HttpMethod.POST, request(body, adminToken), Map.class);
        Map<String, Object> agentData = (Map<String, Object>) res.getBody().get("data");
        String agentId = (String) agentData.get("id");

        // Assign zone
        restTemplate().exchange(base() + "/api/v1/delivery/admin/agents/" + agentId + "/zone",
                HttpMethod.PUT, request(Map.of("zoneId", zoneId), adminToken), Map.class);

        // Toggle disponivel
        String agentToken = doLogin(phone, "Test@12345");
        restTemplate().exchange(base() + "/api/v1/delivery/disponivel",
                HttpMethod.PATCH, request(null, agentToken), Map.class);

        return Map.of("id", agentId, "token", agentToken, "phone", phone);
    }

    protected String uniquePhone() {
        return "+244" + (900000000L + System.nanoTime() % 99999999L);
    }
}
