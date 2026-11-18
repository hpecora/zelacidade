package br.com.fiap.zelacidade.dto;

import br.com.fiap.zelacidade.model.StatusOcorrencia;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Corpo das requisições de criação/atualização de ocorrência.
 * O cidadão e a categoria são informados pelo ID.
 */
public record OcorrenciaRequest(
        @NotBlank(message = "O título é obrigatório") @Size(max = 100) String titulo,
        @Size(max = 500) String descricao,
        @NotBlank(message = "O endereço é obrigatório") @Size(max = 200) String endereco,
        BigDecimal latitude,
        BigDecimal longitude,
        @NotNull(message = "A prioridade é obrigatória") @Min(1) @Max(5) Integer prioridade,
        StatusOcorrencia status,
        @NotNull(message = "O cidadaoId é obrigatório") Long cidadaoId,
        @NotNull(message = "O categoriaId é obrigatório") Long categoriaId
) {
}
