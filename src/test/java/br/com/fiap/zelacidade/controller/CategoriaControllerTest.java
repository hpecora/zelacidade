package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.model.Categoria;
import br.com.fiap.zelacidade.repository.CategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CategoriaRepository repository;

    private Categoria categoria(Long id) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNome("Iluminação pública");
        c.setDescricao("Poste apagado ou piscando");
        c.setPrazoAtendimentoDias(5);
        return c;
    }

    @Test
    @DisplayName("POST /api/categorias cria categoria")
    void criar() throws Exception {
        when(repository.save(any(Categoria.class))).thenReturn(categoria(1L));

        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Iluminação pública","descricao":"Poste apagado ou piscando","prazoAtendimentoDias":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/categorias com prazo fora do intervalo retorna 400")
    void criarPrazoInvalido() throws Exception {
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Buraco","prazoAtendimentoDias":0}
                                """))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("GET /api/categorias/{id} retorna categoria")
    void buscar() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(categoria(1L)));

        mvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Iluminação pública"));
    }

    @Test
    @DisplayName("PUT /api/categorias/{id} atualiza prazo")
    void atualizar() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(categoria(1L)));
        when(repository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(put("/api/categorias/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Iluminação pública","descricao":"Poste apagado","prazoAtendimentoDias":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prazoAtendimentoDias").value(3));
    }

    @Test
    @DisplayName("DELETE /api/categorias/{id} inexistente retorna 404")
    void excluirInexistente() throws Exception {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/categorias/42"))
                .andExpect(status().isNotFound());
        verify(repository, never()).delete(any());
    }
}
