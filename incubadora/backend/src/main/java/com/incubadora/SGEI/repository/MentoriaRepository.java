package com.incubadora.SGEI.repository;

import com.incubadora.SGEI.model.Mentoria;
import com.incubadora.SGEI.model.Projeto;
import com.incubadora.SGEI.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MentoriaRepository extends JpaRepository<Mentoria, Long> {
    List<Mentoria> findByMentor(Usuario mentor);
    List<Mentoria> findByProjeto(Projeto projeto);
    List<Mentoria> findByMentorAndDataHoraBetween(Usuario mentor, LocalDateTime inicio, LocalDateTime fim);
    boolean existsByMentorAndDataHora(Usuario mentor, LocalDateTime dataHora);
} 