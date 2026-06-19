package ao.angogas.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "zones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;

    @Column(columnDefinition = "TEXT")
    private String coordenadas;
}
