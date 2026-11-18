package br.com.fiap.zelacidade.repository;

import br.com.fiap.zelacidade.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
