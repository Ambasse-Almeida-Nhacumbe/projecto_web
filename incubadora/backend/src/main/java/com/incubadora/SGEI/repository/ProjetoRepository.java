package com.incubadora.SGEI.repository;

import com.incubadora.SGEI.model.Projeto;
import com.incubadora.SGEI.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    List<Projeto> findByUsuario(Usuario usuario);
} 