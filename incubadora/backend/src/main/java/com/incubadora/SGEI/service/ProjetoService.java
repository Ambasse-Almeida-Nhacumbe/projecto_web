package com.incubadora.SGEI.service;

import com.incubadora.SGEI.model.Projeto;
import com.incubadora.SGEI.model.Usuario;
import com.incubadora.SGEI.repository.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjetoService {
    private final ProjetoRepository projetoRepository;
    private final FileStorageService fileStorageService;

    public ProjetoService(ProjetoRepository projetoRepository, FileStorageService fileStorageService) {
        this.projetoRepository = projetoRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Projeto> listarTodos() {
        return projetoRepository.findAll();
    }

    public List<Projeto> listarPorUsuario(Usuario usuario) {
        return projetoRepository.findByUsuario(usuario);
    }

    public Optional<Projeto> buscarPorId(Long id) {
        return projetoRepository.findById(id);
    }

    @Transactional
    public Projeto salvar(Projeto projeto) {
        if (projeto.getId() == null) {
            projeto.setDataCriacao(LocalDateTime.now());
        }
        projeto.setUltimaAtualizacao(LocalDateTime.now());
        return projetoRepository.save(projeto);
    }

    @Transactional
    public void deletar(Long id) {
        Projeto projeto = projetoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        // Deletar arquivos associados
        if (projeto.getRelatorioPath() != null) {
            fileStorageService.deleteFile(projeto.getRelatorioPath());
        }
        if (projeto.getPitchPath() != null) {
            fileStorageService.deleteFile(projeto.getPitchPath());
        }

        projetoRepository.deleteById(id);
    }

    @Transactional
    public Projeto atualizarStatus(Long id, Projeto.Status novoStatus) {
        Projeto projeto = projetoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        projeto.setStatus(novoStatus);
        projeto.setUltimaAtualizacao(LocalDateTime.now());
        return projetoRepository.save(projeto);
    }

    public boolean verificarPermissaoAcesso(Projeto projeto, Usuario usuario) {
        return projeto.getUsuario().getId().equals(usuario.getId()) ||
               usuario.getRole() == Usuario.Role.ADMIN ||
               usuario.getRole() == Usuario.Role.MENTOR;
    }
} 