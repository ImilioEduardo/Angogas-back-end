package ao.angogas.backend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email ou telefone é obrigatório")
    private String emailOrTelefone;

    @NotBlank(message = "Password é obrigatória")
    private String password;
}
