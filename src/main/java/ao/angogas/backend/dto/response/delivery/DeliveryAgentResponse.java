package ao.angogas.backend.dto.response.delivery;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class DeliveryAgentResponse {
    private UUID id;
    private UUID userId;
    private String nome;
    private String email;
    private String telefone;
    private String fotoPerfil;

    // Documentação pessoal
    private LocalDate dataNascimento;
    private String biNumero;
    private String cartaConducao;
    private LocalDate cartaConducaoDesde;
    private boolean registoCriminal;

    // Veículo
    private String veiculo;
    private String matricula;
    private String livreteVeiculo;
    private String seguroApolice;
    private LocalDate inspecaoValidade;

    // Equipamento
    private boolean temSmartphone;

    // Operacional
    private UUID zoneId;
    private String zoneName;
    private UUID agencyId;
    private String agencyName;
    private boolean disponivel;
    private boolean activo;
    private BigDecimal avaliacaoMedia;
    private int totalEntregas;
    private OffsetDateTime criadoEm;
}
