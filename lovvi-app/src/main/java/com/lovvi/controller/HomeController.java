package com.lovvi.controller;

import com.lovvi.dao.UsuarioDAO;
import com.lovvi.model.UsuarioPerfil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.SQLException;

@Controller
public class HomeController {

    private final UsuarioDAO usuarioDAO;

    public HomeController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

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
        model.addAttribute("nomeUsuario", buscarNomeUsuario(idUsuario));
        return "perfil";
    }

    private String buscarNomeUsuario(int idUsuario) {
        try {
            UsuarioPerfil perfil = usuarioDAO.buscarPerfil(idUsuario);
            if (perfil == null) {
                return "Perfil " + idUsuario;
            }
            return (perfil.nome() + " " + perfil.sobrenome()).trim();
        } catch (SQLException e) {
            return "Perfil " + idUsuario;
        }
    }

    @GetMapping("/sobre")
    public String sobre(Model model) {
        model.addAttribute("title", "Sobre - Lovvi App");
        return "sobre";
    }

    @GetMapping("/interface")
    public String interfaceBanco(Model model) {
        prepararInterface(model, "explorador", "Explorador do Banco");
        return "interface";
    }

    @GetMapping("/interface/crud")
    public String interfaceCrud(Model model) {
        prepararInterface(model, "crud", "CRUD do Banco");
        return "interface";
    }

    @GetMapping("/interface/consultas")
    public String interfaceConsultas(Model model) {
        prepararInterface(model, "consultas", "Consultas e Views");
        return "interface";
    }

    @GetMapping("/interface/rotinas")
    public String interfaceRotinas(Model model) {
        prepararInterface(model, "rotinas", "Funcoes, Procedures e Triggers");
        return "interface";
    }

    @GetMapping("/interface/dashboard")
    public String interfaceDashboard(Model model) {
        prepararInterface(model, "dashboard", "Dashboard Estatistico");
        return "interface";
    }

    private void prepararInterface(Model model, String page, String heading) {
        model.addAttribute("title", heading + " - Lovvi");
        model.addAttribute("interfacePage", page);
        model.addAttribute("interfaceHeading", heading);
    }

    @GetMapping("/interface/tudo")
    public String interfaceTudo(Model model) {
        prepararInterface(model, "tudo", "Interface Completa");
        return "interface";
    }
}
