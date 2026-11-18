package br.com.fiap.zelacidade.controller;

import br.com.fiap.zelacidade.dto.OcorrenciaRequest;
import br.com.fiap.zelacidade.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelacidade.model.Ocorrencia;
import br.com.fiap.zelacidade.model.StatusOcorrencia;
import br.com.fiap.zelacidade.repository.CategoriaRepository;
import br.com.fiap.zelacidade.repository.CidadaoRepository;
import br.com.fiap.zelacidade.repository.OcorrenciaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaRepository ocorrencias;
    private final CidadaoRepository cidadaos;
    private final CategoriaRepository categorias;

    public OcorrenciaController(OcorrenciaRepository ocorrencias,
                                CidadaoRepository cidadaos,
                                CategoriaRepository categorias) {
        this.ocorrencias = ocorrencias;
        this.cidadaos = cidadaos;
        this.categorias = categorias;
    }

    @PostMapping
    public ResponseEntity<Ocorrencia> criar(@Valid @RequestBody OcorrenciaRequest req) {
        Ocorrencia nova = new Ocorrencia();
        aplicar(nova, req);
        // Toda ocorrência nova começa como ABERTA, independentemente do que foi enviado
        nova.setStatus(StatusOcorrencia.ABERTA);
        Ocorrencia salva = ocorrencias.save(nova);
        return ResponseEntity.created(URI.create("/api/ocorrencias/" + salva.getId())).body(salva);
    }

    @GetMapping
    public List<Ocorrencia> listar(@RequestParam(required = false) StatusOcorrencia status) {
        return status == null ? ocorrencias.findAll() : ocorrencias.findByStatus(status);
    }

    @GetMapping("/{id}")
    public Ocorrencia buscar(@PathVariable Long id) {
        return ocorrencias.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrência", id));
    }

    @PutMapping("/{id}")
    public Ocorrencia atualizar(@PathVariable Long id, @Valid @RequestBody OcorrenciaRequest req) {
        Ocorrencia atual = buscar(id);
        aplicar(atual, req);
        if (req.status() != null) {
            atual.setStatus(req.status());
        }
        return ocorrencias.save(atual);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        ocorrencias.delete(buscar(id));
    }

    private void aplicar(Ocorrencia o, OcorrenciaRequest req) {
        o.setTitulo(req.titulo());
        o.setDescricao(req.descricao());
        o.setEndereco(req.endereco());
        o.setLatitude(req.latitude());
        o.setLongitude(req.longitude());
        o.setPrioridade(req.prioridade());
        o.setCidadao(cidadaos.findById(req.cidadaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidadão", req.cidadaoId())));
        o.setCategoria(categorias.findById(req.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", req.categoriaId())));
    }
}
