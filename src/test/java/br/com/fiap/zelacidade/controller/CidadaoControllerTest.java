package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.model.Cidadao;
import br.com.fiap.zelacidade.repository.CidadaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CidadaoController.class)
class CidadaoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CidadaoRepository repository;

    private Cidadao cidadao(Long id) {
        Cidadao c = new Cidadao();
        c.setId(id);
        c.setNome("Maria Souza");
        c.setEmail("maria@email.com");
        c.setTelefone("11999990000");
        c.setBairro("Vila Mariana");
        return c;
    }

    private static final String JSON_VALIDO = """
            {"nome":"Maria Souza","email":"maria@email.com","telefone":"11999990000","bairro":"Vila Mariana"}
            """;

    @Test
    @DisplayName("POST /api/cidadaos cria cidadão e retorna 201")
    void criar() throws Exception {
        when(repository.save(any(Cidadao.class))).thenReturn(cidadao(1L));

        mvc.perform(post("/api/cidadaos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/cidadaos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Souza"));
    }

    @Test
    @DisplayName("POST /api/cidadaos com e-mail inválido retorna 400")
    void criarInvalido() throws Exception {
        String invalido = """
                {"nome":"","email":"nao-e-email","bairro":"Centro"}
                """;
        mvc.perform(post("/api/cidadaos").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes.email").exists())
                .andExpect(jsonPath("$.detalhes.nome").exists());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("GET /api/cidadaos lista cidadãos")
    void listar() throws Exception {
        when(repository.findAll()).thenReturn(List.of(cidadao(1L), cidadao(2L)));

        mvc.perform(get("/api/cidadaos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/cidadaos/{id} inexistente retorna 404")
    void buscarInexistente() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/cidadaos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/cidadaos/{id} atualiza cidadão")
    void atualizar() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(cidadao(1L)));
        when(repository.save(any(Cidadao.class))).thenAnswer(inv -> inv.getArgument(0));

        String novo = """
                {"nome":"Maria S. Lima","email":"maria@email.com","telefone":"11999990000","bairro":"Moema"}
                """;
        mvc.perform(put("/api/cidadaos/1").contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria S. Lima"))
                .andExpect(jsonPath("$.bairro").value("Moema"));
    }

    @Test
    @DisplayName("DELETE /api/cidadaos/{id} remove cidadão e retorna 204")
    void excluir() throws Exception {
        Cidadao c = cidadao(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        mvc.perform(delete("/api/cidadaos/1"))
                .andExpect(status().isNoContent());
        verify(repository).delete(c);
    }
}
