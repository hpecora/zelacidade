package br.com.fiap.zelacidade.repository;

import br.com.fiap.zelacidade.model.Cidadao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CidadaoRepository extends JpaRepository<Cidadao, Long> {
}
