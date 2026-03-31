package com.lovvi.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovvi.infra.DatabaseConnection;

@RestController
@RequestMapping("/api")
public class LovviApiController {

    private static final Logger logger = LoggerFactory.getLogger(LovviApiController.class);
    private final DatabaseConnection db;

    public LovviApiController(DatabaseConnection db) {
        this.db = db;
    }

    record Interest(int idInteresse, String nomeInteresse, String categoria) {
    }

    record UserProfile(int idUsuario, String nome, String sobrenome, String email, String cidade, String genero,
                       LocalDate dtNascimento, String descricao, String preferencias, String objetivos,
                       String tipoPerfil, BigDecimal altura, List<Integer> interesses) {
    }

    record MatchResponse(int idUsuario, String nomeCompleto, String cidade, String tipoPerfil, double compatibilidade, List<String> interesses) {
    }

    record UserRegistrationRequest(
            String nome,
            String sobrenome,
            String email,
            String senha,
            String cidade,
            String genero,
            LocalDate dtNascimento,
            String descricao,
            String preferencias,
            String objetivos,
            String tipoPerfil,
            BigDecimal altura,
            List<Integer> interesses
    ) {
    }

    record UserRegistrationResult(int idUsuario) {
    }

    private boolean validateInterests(Connection conn, List<Integer> interestIds) throws SQLException {
        if (interestIds == null || interestIds.isEmpty()) {
            return true;
        }

        String placeholders = interestIds.stream().map(i -> "?").collect(Collectors.joining(","));
        String sql = "SELECT COUNT(1) AS total FROM interesse WHERE id_interesse IN (" + placeholders + ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < interestIds.size(); i++) {
                ps.setInt(i + 1, interestIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") == interestIds.size();
                }
            }
        }

        return false;
    }

    private String normalizeGenero(String genero) {
        if (genero == null) return null;
        return switch (genero.toLowerCase()) {
            case "m", "masculino" -> "Masculino";
            case "f", "feminino" -> "Feminino";
            case "o", "outro" -> "Outro";
            case "nao-binario", "nao binario", "nao_binario" -> "Nao-binario";
            default -> genero;
        };
    }

    private String normalizeTipoPerfil(String tipoPerfil) {
        if (tipoPerfil == null) return null;
        return switch (tipoPerfil.toLowerCase()) {
            case "serio", "relacionamento" -> "relacionamento";
            case "casual" -> "casual";
            case "amizade" -> "amizade";
            default -> tipoPerfil;
        };
    }

    @GetMapping("/interesses")
    public ResponseEntity<List<Interest>> getAllInterests() {
        List<Interest> lista = new ArrayList<>();
        String sql = "SELECT id_interesse, nome_interesse, categoria FROM interesse ORDER BY nome_interesse";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Interest(rs.getInt("id_interesse"), rs.getString("nome_interesse"), rs.getString("categoria")));
            }
            return ResponseEntity.ok(lista);
        } catch (SQLException e) {
            logger.error("Erro ao buscar interesses", e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @PostMapping("/usuarios/register")
    public ResponseEntity<UserRegistrationResult> registerUser(@RequestBody UserRegistrationRequest request) {
        if (request == null || request.email() == null || request.senha() == null) {
            return ResponseEntity.badRequest().build();
        }

        String insertUsuario = "INSERT INTO usuario (nome, sobrenome, email, senha, cidade, genero, dt_nascimento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertPerfil = "INSERT INTO perfil (descricao, preferencias, objetivos, tipo_perfil, altura, id_usuario) VALUES (?, ?, ?, ?, ?, ?)";
        String insertTem = "INSERT INTO tem (id_usuario, id_interesse) VALUES (?, ?)";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            String valorGenero = normalizeGenero(request.genero());
            String valorTipoPerfil = normalizeTipoPerfil(request.tipoPerfil());

            if (request.interesses() != null && !validateInterests(conn, request.interesses())) {
                logger.warn("Interesses inválidos enviados para o cadastro: {}", request.interesses());
                return ResponseEntity.badRequest().build();
            }

            int userId;
            try (PreparedStatement ps = conn.prepareStatement(insertUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, request.nome());
                ps.setString(2, request.sobrenome());
                ps.setString(3, request.email());
                ps.setString(4, request.senha());
                ps.setString(5, request.cidade());
                ps.setString(6, valorGenero != null ? valorGenero : request.genero());
                ps.setDate(7, Date.valueOf(request.dtNascimento()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        return ResponseEntity.internalServerError().build();
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertPerfil)) {
                ps.setString(1, request.descricao());
                ps.setString(2, request.preferencias());
                ps.setString(3, request.objetivos());
                ps.setString(4, valorTipoPerfil != null ? valorTipoPerfil : request.tipoPerfil());
                ps.setBigDecimal(5, request.altura());
                ps.setInt(6, userId);
                ps.executeUpdate();
            }

            if (request.interesses() != null) {
                try (PreparedStatement ps = conn.prepareStatement(insertTem)) {
                    for (Integer idInteresse : request.interesses()) {
                        ps.setInt(1, userId);
                        ps.setInt(2, idInteresse);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return ResponseEntity.ok(new UserRegistrationResult(userId));
        } catch (SQLException e) {
            logger.error("Erro ao cadastrar usuário", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/usuarios/{id}/matches")
    public ResponseEntity<List<MatchResponse>> getMatches(@PathVariable("id") int idUsuario) {
        UserProfile source = getUserProfile(idUsuario);
        if (source == null) {
            return ResponseEntity.notFound().build();
        }

        List<UserProfile> allProfiles = getAllProfiles();
        List<MatchResponse> matches = new ArrayList<>();

        for (UserProfile candidate : allProfiles) {
            if (candidate.idUsuario() == source.idUsuario()) continue;
            double score = computeCompatibility(source, candidate);
            if (score <= 0) continue;
            List<String> commonInterests = getCommonInterestNames(source.idUsuario(), candidate.idUsuario());
            matches.add(new MatchResponse(
                    candidate.idUsuario(),
                    candidate.nome() + " " + candidate.sobrenome(),
                    candidate.cidade(),
                    candidate.tipoPerfil(),
                    Math.min(100.0, Math.round(score * 100.0) / 100.0),
                    commonInterests
            ));
        }

        matches.sort(Comparator.comparingDouble(MatchResponse::compatibilidade).reversed());
        return ResponseEntity.ok(matches.stream().limit(10).collect(Collectors.toList()));
    }

    private UserProfile getUserProfile(int idUsuario) {
        String sql = "SELECT u.id_usuario, u.nome, u.sobrenome, u.email, u.cidade, u.genero, u.dt_nascimento, " +
                "p.descricao, p.preferencias, p.objetivos, p.tipo_perfil, p.altura " +
                "FROM usuario u " +
                "LEFT JOIN perfil p ON u.id_usuario = p.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    List<Integer> interesses = getInterestIdsByUserId(idUsuario);
                    return new UserProfile(
                            rs.getInt("id_usuario"),
                            rs.getString("nome"),
                            rs.getString("sobrenome"),
                            rs.getString("email"),
                            rs.getString("cidade"),
                            rs.getString("genero"),
                            Objects.requireNonNull(rs.getDate("dt_nascimento")).toLocalDate(),
                            rs.getString("descricao"),
                            rs.getString("preferencias"),
                            rs.getString("objetivos"),
                            rs.getString("tipo_perfil"),
                            rs.getBigDecimal("altura"),
                            interesses
                    );
                }
            }
        } catch (SQLException e) {
        }
        return null;
    }

    private List<UserProfile> getAllProfiles() {
        List<UserProfile> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nome, u.sobrenome, u.email, u.cidade, u.genero, u.dt_nascimento, " +
                "p.descricao, p.preferencias, p.objetivos, p.tipo_perfil, p.altura " +
                "FROM usuario u " +
                "LEFT JOIN perfil p ON u.id_usuario = p.id_usuario";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("id_usuario");
                List<Integer> interesses = getInterestIdsByUserId(userId);
                lista.add(new UserProfile(
                        userId,
                        rs.getString("nome"),
                        rs.getString("sobrenome"),
                        rs.getString("email"),
                        rs.getString("cidade"),
                        rs.getString("genero"),
                        Objects.requireNonNull(rs.getDate("dt_nascimento")).toLocalDate(),
                        rs.getString("descricao"),
                        rs.getString("preferencias"),
                        rs.getString("objetivos"),
                        rs.getString("tipo_perfil"),
                        rs.getBigDecimal("altura"),
                        interesses
                ));
            }
        } catch (SQLException e) {
        }
        return lista;
    }

    private List<Integer> getInterestIdsByUserId(int idUsuario) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_interesse FROM tem WHERE id_usuario = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_interesse"));
                }
            }
        } catch (SQLException e) {
        }
        return ids;
    }

    private List<String> getCommonInterestNames(int sourceId, int targetId) {
        List<String> names = new ArrayList<>();
        String sql = "SELECT i.nome_interesse FROM interesse i " +
                "JOIN tem t1 ON i.id_interesse = t1.id_interesse " +
                "JOIN tem t2 ON i.id_interesse = t2.id_interesse " +
                "WHERE t1.id_usuario = ? AND t2.id_usuario = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sourceId);
            ps.setInt(2, targetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("nome_interesse"));
                }
            }
        } catch (SQLException e) {
        }
        return names;
    }

    private double computeCompatibility(UserProfile a, UserProfile b) {
        double score = 0.0;
        if (a.tipoPerfil() != null && b.tipoPerfil() != null && a.tipoPerfil().equalsIgnoreCase(b.tipoPerfil())) {
            score += 30;
        }
        if (a.cidade() != null && b.cidade() != null && a.cidade().equalsIgnoreCase(b.cidade())) {
            score += 15;
        }

        long ageA = Period.between(a.dtNascimento(), LocalDate.now()).getYears();
        long ageB = Period.between(b.dtNascimento(), LocalDate.now()).getYears();
        long diff = Math.abs(ageA - ageB);
        if (diff <= 5) score += 15;
        else if (diff <= 10) score += 8;

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
