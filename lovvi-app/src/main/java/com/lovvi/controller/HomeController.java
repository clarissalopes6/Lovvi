package com.lovvi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    public String home(Model model) {
        model.addAttribute("title", "Lovvi - Plataforma de Relacionamento");
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Entrar - Lovvi");
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("title", "Cadastro - Lovvi");
        return "cadastro";
    }

    @GetMapping("/perfil/{id}")
    public String perfil(@PathVariable("id") int idUsuario, Model model) {
        model.addAttribute("title", "Meu Perfil - Lovvi");
        model.addAttribute("idUsuario", idUsuario);
        return "perfil";
    }

    @GetMapping("/sobre")
    public String sobre(Model model) {
        model.addAttribute("title", "Sobre - Lovvi App");
        return "sobre";
    }

    @GetMapping("/interface")
    public String interfaceBanco(Model model) {
        model.addAttribute("title", "Interface do Banco - Lovvi");
        return "interface";
    }
}
