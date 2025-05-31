package com.incubadora.SGEI.controller;

import com.incubadora.SGEI.model.Mentoria;
import com.incubadora.SGEI.model.Usuario;
import com.incubadora.SGEI.service.MentoriaService;
import com.incubadora.SGEI.service.ProjetoService;
import com.incubadora.SGEI.service.UsuarioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/mentoria")
public class MentoriaController {
    private final MentoriaService mentoriaService;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public MentoriaController(MentoriaService mentoriaService, ProjetoService projetoService, UsuarioService usuarioService) {
        this.mentoriaService = mentoriaService;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarMentorias(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario mentor = usuarioService.buscarPorEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        model.addAttribute("mentorias", mentoriaService.listarPorMentor(mentor));
        model.addAttribute("usuario", mentor);
        return "mentoria/lista";
    }

    @GetMapping("/agendar/{projetoId}")
    public String formAgendar(@PathVariable Long projetoId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario mentor = usuarioService.buscarPorEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return projetoService.buscarPorId(projetoId)
            .map(projeto -> {
                Mentoria mentoria = new Mentoria();
                mentoria.setProjeto(projeto);
                mentoria.setMentor(mentor);
                
                model.addAttribute("mentoria", mentoria);
                model.addAttribute("projeto", projeto);
                model.addAttribute("usuario", mentor);
                return "mentoria/form";
            })
            .orElseGet(() -> {
                model.addAttribute("error", "Projeto não encontrado");
                return "redirect:/projetos";
            });
    }

    @PostMapping("/agendar")
    public String agendar(@Valid @ModelAttribute Mentoria mentoria,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("usuario", usuarioService.buscarPorEmail(userDetails.getUsername()).orElse(null));
            return "mentoria/form";
        }

        try {
            mentoriaService.agendar(mentoria);
            redirectAttributes.addFlashAttribute("success", "Mentoria agendada com sucesso!");
            return "redirect:/mentoria";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuarioService.buscarPorEmail(userDetails.getUsername()).orElse(null));
            return "mentoria/form";
        }
    }

    @GetMapping("/{id}")
    public String detalhesMentoria(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario mentor = usuarioService.buscarPorEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return mentoriaService.buscarPorId(id)
            .map(mentoria -> {
                model.addAttribute("mentoria", mentoria);
                model.addAttribute("usuario", mentor);
                return "mentoria/detalhes";
            })
            .orElseGet(() -> {
                model.addAttribute("error", "Mentoria não encontrada");
                return "redirect:/mentoria";
            });
    }

    @PostMapping("/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelarMentoria(@PathVariable Long id) {
        try {
            mentoriaService.cancelar(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/concluir")
    @ResponseBody
    public ResponseEntity<?> concluirMentoria(@PathVariable Long id) {
        try {
            mentoriaService.concluir(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/disponibilidade")
    @ResponseBody
    public ResponseEntity<?> verificarDisponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHora,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario mentor = usuarioService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            boolean disponivel = !mentoriaService.listarPorMentorEPeriodo(
                mentor,
                dataHora.minusHours(1),
                dataHora.plusHours(1)
            ).isEmpty();

            return ResponseEntity.ok().body(disponivel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 