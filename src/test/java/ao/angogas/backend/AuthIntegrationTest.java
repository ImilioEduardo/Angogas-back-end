package ao.angogas.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends AbstractIntegrationTest {

    // ── Register ─────────────────────────────────────────────────────────────

    @Test
    void register_validCliente_returns201() {
        var body = Map.of("nome", "Cliente Teste", "telefone", uniquePhone(), "password", "Senha@123");
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/register", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        assertThat(data).containsKey("accessToken");
    }

    @Test
    void register_duplicatePhone_returnsConflict() {
        String phone = uniquePhone();
        var body = Map.of("nome", "Dup Cliente", "telefone", phone, "password", "Senha@123");
        restTemplate().postForEntity(base() + "/api/v1/auth/register", body, Map.class);

        ResponseEntity<Map> res2 = restTemplate().postForEntity(base() + "/api/v1/auth/register", body, Map.class);
        assertThat(res2.getStatusCode().value()).isGreaterThanOrEqualTo(400);
    }

    @Test
    void register_missingNome_returns400() {
        var body = Map.of("telefone", uniquePhone(), "password", "Senha@123");
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/register", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_adminCredentials_returnsAccessToken() {
        var body = Map.of("emailOrTelefone", "admin@angogas.ao", "password", "Admin@123");
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/login", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        assertThat(data.get("accessToken")).isNotNull().asString().isNotBlank();
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) data.get("user");
        assertThat(user.get("role")).isEqualTo("ADMIN");
    }

    @Test
    void login_wrongPassword_returns401() {
        var body = Map.of("emailOrTelefone", "admin@angogas.ao", "password", "wrong");
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/login", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_unknownUser_returns401() {
        var body = Map.of("emailOrTelefone", "naoexiste@x.com", "password", "Senha@123");
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/login", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Test
    void refresh_validToken_returnsNewAccessToken() {
        String phone = uniquePhone();
        var regBody = Map.of("nome", "Refresh User", "telefone", phone, "password", "Senha@123");
        ResponseEntity<Map> regRes = restTemplate().postForEntity(base() + "/api/v1/auth/register", regBody, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> regData = (Map<String, Object>) regRes.getBody().get("data");
        String refreshToken = (String) regData.get("refreshToken");

        var refreshBody = Map.of("refreshToken", refreshToken);
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/refresh", refreshBody, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
        assertThat(data.get("accessToken")).isNotNull().asString().isNotBlank();
    }

    @Test
    void refresh_invalidToken_returns401() {
        var body = Map.of("refreshToken", "invalid-token");
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/refresh", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_validRefreshToken_returns200() {
        String phone = uniquePhone();
        var regBody = Map.of("nome", "Logout User", "telefone", phone, "password", "Senha@123");
        ResponseEntity<Map> regRes = restTemplate().postForEntity(base() + "/api/v1/auth/register", regBody, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> regData = (Map<String, Object>) regRes.getBody().get("data");
        String refreshToken = (String) regData.get("refreshToken");

        var body = Map.of("refreshToken", refreshToken);
        ResponseEntity<Map> res = restTemplate().postForEntity(base() + "/api/v1/auth/logout", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── Protected endpoint requires auth ──────────────────────────────────────

    @Test
    void protectedEndpoint_withoutToken_returns401() {
        ResponseEntity<Map> res = restTemplate().getForEntity(base() + "/api/v1/orders/my", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() {
        String phone = uniquePhone();
        register("Auth User", phone);
        String token = doLogin(phone, "Senha@123");

        ResponseEntity<Map> res = restTemplate().exchange(
                base() + "/api/v1/orders/my", HttpMethod.GET, request(null, token), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
