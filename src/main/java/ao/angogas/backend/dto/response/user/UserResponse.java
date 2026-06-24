package ao.angogas.backend.dto.response.user;

import ao.angogas.backend.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private String nome;
    private String email;
    private String telefone;
    private UserRole role;
    private boolean activo;
    private boolean emailVerificado;
    private String fotoPerfil;
    private OffsetDateTime criadoEm;
}
