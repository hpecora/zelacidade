package br.com.fiap.zelacidade.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Tipo de problema urbano (ex.: buraco na via, iluminação pública, calçada danificada).
 */
@Entity
@Table(name = "TB_ZC_CATEGORIA")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CATEGORIA")
    private Long id;

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(max = 60)
    @Column(name = "NOME", nullable = false, unique = true, length = 60)
    private String nome;

    @Size(max = 255)
    @Column(name = "DESCRICAO", length = 255)
    private String descricao;

    @NotNull(message = "O prazo de atendimento é obrigatório")
    @Min(1) @Max(90)
    @Column(name = "PRAZO_ATENDIMENTO_DIAS", nullable = false)
    private Integer prazoAtendimentoDias;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getPrazoAtendimentoDias() { return prazoAtendimentoDias; }
    public void setPrazoAtendimentoDias(Integer prazoAtendimentoDias) { this.prazoAtendimentoDias = prazoAtendimentoDias; }
}
