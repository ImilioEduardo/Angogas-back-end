package ao.angogas.backend.dto.request.delivery;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateDeliveryAgentRequest {

    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String telefone;

    @Size(min = 6, message = "A password deve ter no mínimo 6 caracteres")
    private String password;

    private Boolean activo;

    // Documentação pessoal
    private LocalDate dataNascimento;

    @Size(max = 30)
    private String biNumero;

    @Size(max = 30)
    private String cartaConducao;

    private LocalDate cartaConducaoDesde;

    private Boolean registoCriminal;

    // Veículo
    @Size(max = 50)
    private String veiculo;

    @Size(max = 20)
    private String matricula;

    @Size(max = 30)
    private String livreteVeiculo;

    @Size(max = 50)
    private String seguroApolice;

    private LocalDate inspecaoValidade;

    // Equipamento
    private Boolean temSmartphone;
}
