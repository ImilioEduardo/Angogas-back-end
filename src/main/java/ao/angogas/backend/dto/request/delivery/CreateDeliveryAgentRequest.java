package ao.angogas.backend.dto.request.delivery;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDeliveryAgentRequest {

    // Acesso à plataforma
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome não pode exceder 100 caracteres")
    private String nome;

    @Size(max = 150, message = "O email não pode exceder 150 caracteres")
    private String email;

    @Size(max = 20, message = "O telefone não pode exceder 20 caracteres")
    private String telefone;

    @NotBlank(message = "A password é obrigatória")
    @Size(min = 6, message = "A password deve ter no mínimo 6 caracteres")
    private String password;

    // Documentação pessoal
    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve ser no passado")
    private LocalDate dataNascimento;

    @NotBlank(message = "O número do BI/Passaporte é obrigatório")
    @Size(max = 30)
    private String biNumero;

    @NotBlank(message = "O número da carta de condução é obrigatório")
    @Size(max = 30)
    private String cartaConducao;

    @NotNull(message = "A data de emissão da carta de condução é obrigatória")
    @Past(message = "A data de emissão deve ser no passado")
    private LocalDate cartaConducaoDesde;

    private boolean registoCriminal = false;

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
    private boolean temSmartphone = true;
}
