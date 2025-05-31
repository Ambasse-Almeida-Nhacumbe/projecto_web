package com.incubadora.SGEI.service;

import com.incubadora.SGEI.model.Mentoria;
import com.incubadora.SGEI.model.Projeto;
import com.incubadora.SGEI.model.Usuario;
import com.incubadora.SGEI.repository.MentoriaRepository;
import com.incubadora.SGEI.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MentoriaService {
    private final MentoriaRepository mentoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public MentoriaService(MentoriaRepository mentoriaRepository, UsuarioRepository usuarioRepository) {
        this.mentoriaRepository = mentoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("hasRole('MENTOR')")
    public List<Mentoria> listarPorMentor(Usuario mentor) {
        return mentoriaRepository.findByMentor(mentor);
    }

    public List<Mentoria> listarPorProjeto(Projeto projeto) {
        return mentoriaRepository.findByProjeto(projeto);
    }

    @PreAuthorize("hasRole('MENTOR')")
    public List<Mentoria> listarPorMentorEPeriodo(Usuario mentor, LocalDateTime inicio, LocalDateTime fim) {
        return mentoriaRepository.findByMentorAndDataHoraBetween(mentor, inicio, fim);
    }

    @Transactional
    @PreAuthorize("hasRole('MENTOR')")
    public Mentoria agendar(Mentoria mentoria) {
        // Verificar se o mentor está disponível no horário
        if (mentoriaRepository.existsByMentorAndDataHora(mentoria.getMentor(), mentoria.getDataHora())) {
            throw new RuntimeException("Mentor já possui mentoria agendada neste horário");
        }

        // Verificar se a data é futura
        if (mentoria.getDataHora().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("A data da mentoria deve ser futura");
        }

        // Verificar se o mentor existe e tem papel MENTOR
        Usuario mentor = usuarioRepository.findById(mentoria.getMentor().getId())
            .orElseThrow(() -> new RuntimeException("Mentor não encontrado"));
        
        if (mentor.getRole() != Usuario.Role.MENTOR) {
            throw new RuntimeException("Usuário não é um mentor");
        }

        mentoria.setStatus(Mentoria.Status.AGENDADA);
        return mentoriaRepository.save(mentoria);
    }

    @Transactional
    @PreAuthorize("hasRole('MENTOR')")
    public Mentoria atualizar(Long id, Mentoria mentoriaAtualizada) {
        return mentoriaRepository.findById(id)
            .map(mentoria -> {
                mentoria.setDataHora(mentoriaAtualizada.getDataHora());
                mentoria.setObservacoes(mentoriaAtualizada.getObservacoes());
                mentoria.setStatus(mentoriaAtualizada.getStatus());
                return mentoriaRepository.save(mentoria);
            })
            .orElseThrow(() -> new RuntimeException("Mentoria não encontrada"));
    }

    @PreAuthorize("hasRole('MENTOR')")
    public Optional<Mentoria> buscarPorId(Long id) {
        return mentoriaRepository.findById(id);
    }

    @Transactional
    @PreAuthorize("hasRole('MENTOR')")
    public void cancelar(Long id) {
        mentoriaRepository.findById(id)
            .map(mentoria -> {
                mentoria.setStatus(Mentoria.Status.CANCELADA);
                return mentoriaRepository.save(mentoria);
            })
            .orElseThrow(() -> new RuntimeException("Mentoria não encontrada"));
    }

    @Transactional
    @PreAuthorize("hasRole('MENTOR')")
    public void concluir(Long id) {
        mentoriaRepository.findById(id)
            .map(mentoria -> {
                mentoria.setStatus(Mentoria.Status.REALIZADA);
                return mentoriaRepository.save(mentoria);
            })
            .orElseThrow(() -> new RuntimeException("Mentoria não encontrada"));
    }
} 