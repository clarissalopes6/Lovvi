package com.lovvi.controller;

import com.lovvi.dao.InteresseDAO;
import com.lovvi.dao.UsuarioDAO;
import com.lovvi.dto.CadastroUsuarioRequest;
import com.lovvi.dto.CadastroUsuarioResult;
import com.lovvi.dto.MatchResultado;
import com.lovvi.model.Interesse;
import com.lovvi.model.UsuarioPerfil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LovviApiController {

    private static final Logger logger = LoggerFactory.getLogger(LovviApiController.class);

    private final InteresseDAO interesseDAO;
    private final UsuarioDAO usuarioDAO;

    public LovviApiController(InteresseDAO interesseDAO, UsuarioDAO usuarioDAO) {
        this.interesseDAO = interesseDAO;
        this.usuarioDAO = usuarioDAO;
    }

    private String normalizeGenero(String genero) {
        if (genero == null) {
            return null;
        }
        return switch (genero.toLowerCase()) {
            case "m", "masculino" -> "Masculino";
            case "f", "feminino" -> "Feminino";
            case "o", "outro" -> "Outro";
            case "nao-binario", "nao binario", "nao_binario" -> "Nao-binario";
            default -> genero;
        };
    }

    private String normalizeTipoPerfil(String tipoPerfil) {
        if (tipoPerfil == null) {
            return null;
        }
        return switch (tipoPerfil.toLowerCase()) {
            case "serio", "relacionamento" -> "relacionamento";
            case "casual" -> "casual";
            case "amizade" -> "amizade";
            default -> tipoPerfil;
        };
    }

    @GetMapping("/interesses")
    public ResponseEntity<List<Interesse>> getAllInterests() {
        try {
            return ResponseEntity.ok(interesseDAO.listarTodos());
        } catch (SQLException e) {
            logger.error("Erro ao buscar interesses", e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @PostMapping("/usuarios/register")
    public ResponseEntity<CadastroUsuarioResult> registerUser(@RequestBody CadastroUsuarioRequest request) {
        if (request == null
                || request.email() == null
                || request.senha() == null
                || request.dtNascimento() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            int userId = usuarioDAO.cadastrar(
                    request,
                    normalizeGenero(request.genero()),
                    normalizeTipoPerfil(request.tipoPerfil())
            );
            return ResponseEntity.ok(new CadastroUsuarioResult(userId));
        } catch (SQLException e) {
            logger.error("Erro ao cadastrar usuario", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/usuarios/{id}/matches")
    public ResponseEntity<List<MatchResultado>> getMatches(@PathVariable("id") int idUsuario) {
        try {
            UsuarioPerfil source = usuarioDAO.buscarPerfil(idUsuario);
            if (source == null) {
                return ResponseEntity.notFound().build();
            }

            List<UsuarioPerfil> allProfiles = usuarioDAO.listarPerfis();
            List<MatchResultado> matches = new ArrayList<>();

            for (UsuarioPerfil candidate : allProfiles) {
                if (candidate.idUsuario() == source.idUsuario()) {
                    continue;
                }

                double score = computeCompatibility(source, candidate);
                if (score <= 0) {
                    continue;
                }

                List<String> commonInterests = usuarioDAO.listarInteressesEmComum(source.idUsuario(), candidate.idUsuario());
                matches.add(new MatchResultado(
                        candidate.idUsuario(),
                        candidate.nome() + " " + candidate.sobrenome(),
                        candidate.cidade(),
                        candidate.tipoPerfil(),
                        Math.min(100.0, Math.round(score * 100.0) / 100.0),
                        commonInterests
                ));
            }

            matches.sort(Comparator.comparingDouble(MatchResultado::compatibilidade).reversed());
            return ResponseEntity.ok(matches.stream().limit(10).collect(Collectors.toList()));
        } catch (SQLException e) {
            logger.error("Erro ao buscar matches do usuario {}", idUsuario, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private double computeCompatibility(UsuarioPerfil a, UsuarioPerfil b) {
        double score = 0.0;
        if (a.tipoPerfil() != null && b.tipoPerfil() != null && a.tipoPerfil().equalsIgnoreCase(b.tipoPerfil())) {
            score += 30;
        }
        if (a.cidade() != null && b.cidade() != null && a.cidade().equalsIgnoreCase(b.cidade())) {
            score += 15;
        }
        if (a.dtNascimento() != null && b.dtNascimento() != null) {
            long ageA = Period.between(a.dtNascimento(), LocalDate.now()).getYears();
            long ageB = Period.between(b.dtNascimento(), LocalDate.now()).getYears();
            long diff = Math.abs(ageA - ageB);
            if (diff <= 5) {
                score += 15;
            } else if (diff <= 10) {
                score += 8;
            }
        }

        Set<Integer> setA = new HashSet<>(a.interesses());
        Set<Integer> setB = new HashSet<>(b.interesses());
        if (!setA.isEmpty() && !setB.isEmpty()) {
            Set<Integer> intersection = new HashSet<>(setA);
            intersection.retainAll(setB);
            double ratio = (double) intersection.size() / Math.max(setA.size(), setB.size());
            score += Math.min(40, ratio * 40);
        }

        return Math.min(100.0, score);
    }
}
