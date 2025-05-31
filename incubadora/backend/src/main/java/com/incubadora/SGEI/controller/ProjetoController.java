package com.incubadora.SGEI.controller;

import com.incubadora.SGEI.model.Projeto;
import com.incubadora.SGEI.model.Usuario;
import com.incubadora.SGEI.service.ProjetoService;
import com.incubadora.SGEI.service.UsuarioService;
import com.incubadora.SGEI.service.FileStorageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.time.LocalDateTime;
import java.util.Map;
import java.nio.file.Path;
import java.io.IOException;

@Controller
@RequestMapping("/")
public class ProjetoController {
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;
    private final FileStorageService fileStorageService;

    public ProjetoController(ProjetoService projetoService, UsuarioService usuarioService, FileStorageService fileStorageService) {
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/projetos")
    public String listarProjetos(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = getUsuarioAutenticado(userDetails);
        model.addAttribute("usuario", usuario);
        model.addAttribute("projetos", projetoService.listarTodos());
        return "projetos";
    }

    @GetMapping("/detalhe-projeto/{id}")
    public String detalheProjeto(@PathVariable Long id, Model model, 
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getUsuarioAutenticado(userDetails);
            model.addAttribute("usuario", usuario);
            
            return projetoService.buscarPorId(id)
                .map(projeto -> {
                    model.addAttribute("projeto", projeto);
                    model.addAttribute("statusDisponiveis", Projeto.Status.values());
                    return "detalhe-projeto";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Projeto não encontrado");
                    return "redirect:/projetos";
                });
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao carregar projeto: " + e.getMessage());
            return "redirect:/projetos";
        }
    }

    @GetMapping("/submissao")
    public String submissaoProjeto(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = getUsuarioAutenticado(userDetails);
            model.addAttribute("projeto", new Projeto());
            model.addAttribute("usuario", usuario);
            return "submissao";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
            return "redirect:/projetos";
        }
    }

    @PostMapping("/submissao")
    public String submeterProjeto(@Valid @ModelAttribute Projeto projeto,
                                 BindingResult result,
                                 @RequestParam(required = false) MultipartFile relatorio,
                                 @RequestParam(required = false) MultipartFile pitch,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("usuario", getUsuarioAutenticado(userDetails));
                return "submissao";
            }

            Usuario usuario = getUsuarioAutenticado(userDetails);
            projeto.setUsuario(usuario);
            projeto.setStatus(Projeto.Status.PENDENTE);
            projeto.setDataCriacao(LocalDateTime.now());
            projeto.setUltimaAtualizacao(LocalDateTime.now());

            // Salvar arquivos se fornecidos
            if (relatorio != null && !relatorio.isEmpty()) {
                String relatorioPath = fileStorageService.salvarRelatorio(relatorio);
                projeto.setRelatorioPath(relatorioPath);
            }

            if (pitch != null && !pitch.isEmpty()) {
                String pitchPath = fileStorageService.salvarPitch(pitch);
                projeto.setPitchPath(pitchPath);
            }

            projetoService.salvar(projeto);
            redirectAttributes.addFlashAttribute("success", "Projeto submetido com sucesso!");
            return "redirect:/projetos";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao submeter projeto: " + e.getMessage());
            model.addAttribute("usuario", getUsuarioAutenticado(userDetails));
            return "submissao";
        }
    }

    @PostMapping("/api/projetos/{id}/status")
    @ResponseBody
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, 
                                           @RequestBody Map<String, String> payload,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (!userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MENTOR"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Sem permissão para atualizar status"));
            }

            String novoStatus = payload.get("status");
            if (novoStatus == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Status não fornecido"));
            }

            projetoService.atualizarStatus(id, Projeto.Status.valueOf(novoStatus));
            return ResponseEntity.ok(Map.of("message", "Status atualizado com sucesso"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Status inválido"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao atualizar status: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/projetos/{id}")
    @ResponseBody
    public ResponseEntity<?> deletarProjeto(@PathVariable Long id) {
        try {
            projetoService.deletar(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/projetos/{id}/relatorio")
    @ResponseBody
    public ResponseEntity<?> uploadRelatorio(@PathVariable Long id,
                                           @RequestParam("file") MultipartFile file,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Projeto projeto = projetoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

            Usuario usuario = getUsuarioAutenticado(userDetails);
            if (!projeto.getUsuario().getId().equals(usuario.getId()) && 
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Sem permissão para upload"));
            }

            String relatorioPath = fileStorageService.salvarRelatorio(file);
            projeto.setRelatorioPath(relatorioPath);
            projetoService.salvar(projeto);

            return ResponseEntity.ok(Map.of("message", "Relatório enviado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao enviar relatório: " + e.getMessage()));
        }
    }

    @PostMapping("/api/projetos/{id}/pitch")
    @ResponseBody
    public ResponseEntity<?> uploadPitch(@PathVariable Long id,
                                       @RequestParam("file") MultipartFile file,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Projeto projeto = projetoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

            Usuario usuario = getUsuarioAutenticado(userDetails);
            if (!projeto.getUsuario().getId().equals(usuario.getId()) && 
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Sem permissão para upload"));
            }

            String pitchPath = fileStorageService.salvarPitch(file);
            projeto.setPitchPath(pitchPath);
            projetoService.salvar(projeto);

            return ResponseEntity.ok(Map.of("message", "Pitch enviado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro ao enviar pitch: " + e.getMessage()));
        }
    }

    @GetMapping("/api/projetos/{id}/download/{tipo}")
    public ResponseEntity<Resource> downloadArquivo(@PathVariable Long id,
                                                  @PathVariable String tipo,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Projeto projeto = projetoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

            String filePath = tipo.equals("relatorio") ? projeto.getRelatorioPath() : projeto.getPitchPath();
            if (filePath == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.carregarArquivo(filePath);
            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Usuario getUsuarioAutenticado(UserDetails userDetails) {
        return usuarioService.buscarPorEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
} 