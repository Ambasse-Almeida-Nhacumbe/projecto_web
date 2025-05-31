package com.incubadora.SGEI.controller;

import com.incubadora.SGEI.model.Usuario;
import com.incubadora.SGEI.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
public class AuthController {
    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastroForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute Usuario usuario, BindingResult result) {
        if (result.hasErrors()) {
            return "cadastro";
        }
        try {
            usuarioService.registrar(usuario);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            result.rejectValue("email", "error.usuario", "Email já cadastrado");
            return "cadastro";
        }
    }
} 