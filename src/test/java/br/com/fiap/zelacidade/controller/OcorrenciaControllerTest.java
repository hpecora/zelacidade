package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.model.Categoria;
import br.com.fiap.zelacidade.model.Cidadao;
import br.com.fiap.zelacidade.model.Ocorrencia;
import br.com.fiap.zelacidade.model.StatusOcorrencia;
import br.com.fiap.zelacidade.repository.CategoriaRepository;
import br.com.fiap.zelacidade.repository.CidadaoRepository;
import br.com.fiap.zelacidade.repository.OcorrenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OcorrenciaController.class)
class OcorrenciaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OcorrenciaRepository ocorrencias;
    @MockBean
    private CidadaoRepository cidadaos;
    @MockBean
    private CategoriaRepository categorias;

    private Cidadao cidadao;
    private Categoria categoria;

    private static final String JSON = """
            {"titulo":"Buraco na rua","descricao":"Buraco grande perto da escola","endereco":"Rua das Flores, 100",
             "latitude":-23.588400,"longitude":-46.632400,"prioridade":4,"status":"RESOLVIDA",
             "cidadaoId":1,"categoriaId":2}
            """;

    @BeforeEach
    void setup() {
        cidadao = new Cidadao();
        cidadao.setId(1L);
        cidadao.setNome("Maria Souza");
        categoria = new Categoria();
        categoria.setId(2L);
        categoria.setNome("Buraco na via");
        when(cidadaos.findById(1L)).thenReturn(Optional.of(cidadao));
        when(categorias.findById(2L)).thenReturn(Optional.of(categoria));
    }

    @Test
    @DisplayName("POST cria ocorrência sempre com status ABERTA (regra de negócio)")
    void criarSempreAberta() throws Exception {
        when(ocorrencias.save(any(Ocorrencia.class))).thenAnswer(inv -> {
            Ocorrencia o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        mvc.perform(post("/api/ocorrencias").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.cidadao.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.categoria.nome").value("Buraco na via"));

        ArgumentCaptor<Ocorrencia> captor = ArgumentCaptor.forClass(Ocorrencia.class);
        verify(ocorrencias).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusOcorrencia.ABERTA);
    }

    @Test
    @DisplayName("POST com cidadão inexistente retorna 404")
    void criarCidadaoInexistente() throws Exception {
        when(cidadaos.findById(1L)).thenReturn(Optional.empty());

        mvc.perform(post("/api/ocorrencias").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isNotFound());
        verify(ocorrencias, never()).save(any());
    }

    @Test
    @DisplayName("POST com prioridade inválida retorna 400")
    void criarPrioridadeInvalida() throws Exception {
        String invalido = JSON.replace("\"prioridade\":4", "\"prioridade\":9");
        mvc.perform(post("/api/ocorrencias").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET filtra ocorrências por status")
    void listarPorStatus() throws Exception {
        Ocorrencia o = new Ocorrencia();
        o.setId(10L);
        o.setStatus(StatusOcorrencia.ABERTA);
        when(ocorrencias.findByStatus(StatusOcorrencia.ABERTA)).thenReturn(List.of(o));

        mvc.perform(get("/api/ocorrencias").param("status", "ABERTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ABERTA"));
    }

    @Test
    @DisplayName("PUT permite mudar o status da ocorrência")
    void atualizarStatus() throws Exception {
        Ocorrencia existente = new Ocorrencia();
        existente.setId(10L);
        existente.setStatus(StatusOcorrencia.ABERTA);
        when(ocorrencias.findById(10L)).thenReturn(Optional.of(existente));
        when(ocorrencias.save(any(Ocorrencia.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(put("/api/ocorrencias/10").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDA"));
    }

    @Test
    @DisplayName("DELETE remove ocorrência")
    void excluir() throws Exception {
        Ocorrencia existente = new Ocorrencia();
        existente.setId(10L);
        when(ocorrencias.findById(10L)).thenReturn(Optional.of(existente));

        mvc.perform(delete("/api/ocorrencias/10"))
                .andExpect(status().isNoContent());
        verify(ocorrencias).delete(existente);
    }
}
