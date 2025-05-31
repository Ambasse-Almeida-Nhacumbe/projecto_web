package com.incubadora.SGEI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projetos")
public class Projeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    @Column(length = 1000, nullable = false)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDENTE;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @Column(name = "relatorio_path")
    private String relatorioPath;

    @Column(name = "pitch_path")
    private String pitchPath;

    @Column(name = "ultima_atualizacao_relatorio")
    private LocalDateTime ultimaAtualizacaoRelatorio;

    @Column(name = "ultima_atualizacao_pitch")
    private LocalDateTime ultimaAtualizacaoPitch;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mentoria> mentorias = new ArrayList<>();

    public enum Status {
        PENDENTE,
        EM_ANALISE,
        APROVADO,
        REJEITADO
    }

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        ultimaAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) { this.ultimaAtualizacao = ultimaAtualizacao; }
    
    public String getRelatorioPath() { return relatorioPath; }
    public void setRelatorioPath(String relatorioPath) { this.relatorioPath = relatorioPath; }
    
    public String getPitchPath() { return pitchPath; }
    public void setPitchPath(String pitchPath) { this.pitchPath = pitchPath; }
    
    public LocalDateTime getUltimaAtualizacaoRelatorio() { return ultimaAtualizacaoRelatorio; }
    public void setUltimaAtualizacaoRelatorio(LocalDateTime ultimaAtualizacaoRelatorio) { 
        this.ultimaAtualizacaoRelatorio = ultimaAtualizacaoRelatorio; 
    }
    
    public LocalDateTime getUltimaAtualizacaoPitch() { return ultimaAtualizacaoPitch; }
    public void setUltimaAtualizacaoPitch(LocalDateTime ultimaAtualizacaoPitch) { 
        this.ultimaAtualizacaoPitch = ultimaAtualizacaoPitch; 
    }

    public List<Mentoria> getMentorias() {
        return mentorias;
    }

    public void setMentorias(List<Mentoria> mentorias) {
        this.mentorias = mentorias;
    }

    public void addMentoria(Mentoria mentoria) {
        mentorias.add(mentoria);
        mentoria.setProjeto(this);
    }

    public void removeMentoria(Mentoria mentoria) {
        mentorias.remove(mentoria);
        mentoria.setProjeto(null);
    }
} 