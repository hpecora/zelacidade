package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelacidade.model.Cidadao;
import br.com.fiap.zelacidade.repository.CidadaoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cidadaos")
public class CidadaoController {

    private final CidadaoRepository repository;

    public CidadaoController(CidadaoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Cidadao> criar(@Valid @RequestBody Cidadao cidadao) {
        cidadao.setId(null);
        Cidadao salvo = repository.save(cidadao);
        return ResponseEntity.created(URI.create("/api/cidadaos/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    public List<Cidadao> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Cidadao buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidadão", id));
    }

    @PutMapping("/{id}")
    public Cidadao atualizar(@PathVariable Long id, @Valid @RequestBody Cidadao dados) {
        Cidadao atual = buscar(id);
        atual.setNome(dados.getNome());
        atual.setEmail(dados.getEmail());
        atual.setTelefone(dados.getTelefone());
        atual.setBairro(dados.getBairro());
        return repository.save(atual);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        repository.delete(buscar(id));
    }
}
