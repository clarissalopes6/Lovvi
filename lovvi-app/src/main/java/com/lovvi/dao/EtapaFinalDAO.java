package com.lovvi.dao;

import com.lovvi.dto.DashboardChart;
import com.lovvi.dto.DashboardData;
import com.lovvi.dto.DashboardMetric;
import com.lovvi.dto.DashboardPoint;
import com.lovvi.dto.TableDetails;
import com.lovvi.infra.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EtapaFinalDAO {

    private final DatabaseConnection db;

    public EtapaFinalDAO(DatabaseConnection db) {
        this.db = db;
    }

    public TableDetails executarRelatorio(String id, String cidade, String tipoPerfil, String categoria, String status, int limit)
            throws SQLException {
        try (Connection conn = db.getConnection()) {
            return switch (id) {
                case "categorias-interesses" -> query(conn,
                        "categorias-interesses",
                        "SELECT i.categoria, COUNT(t.id_usuario) AS total_usuarios " +
                                "FROM interesse i " +
                                "JOIN tem t ON i.id_interesse = t.id_interesse " +
                                "WHERE (? IS NULL OR i.categoria = ?) " +
                                "GROUP BY i.categoria " +
                                "HAVING COUNT(t.id_usuario) > 2 " +
                                "ORDER BY total_usuarios DESC " +
                                "LIMIT ?",
                        Arrays.asList(nullable(categoria), nullable(categoria), limit));
                case "usuarios-por-cidade" -> query(conn,
                        "usuarios-por-cidade",
                        "SELECT u.nome, u.sobrenome, u.cidade, p.descricao, p.tipo_perfil, i.nome_interesse " +
                                "FROM usuario u " +
                                "JOIN perfil p ON u.id_usuario = p.id_usuario " +
                                "JOIN tem t ON u.id_usuario = t.id_usuario " +
                                "JOIN interesse i ON t.id_interesse = i.id_interesse " +
                                "WHERE u.cidade = ? " +
                                "ORDER BY u.nome, i.nome_interesse " +
                                "LIMIT ?",
                        List.of(cidade == null || cidade.isBlank() ? "Recife" : cidade.trim(), limit));
                case "usuarios-sem-perfil" -> query(conn,
                        "usuarios-sem-perfil",
                        "SELECT u.id_usuario, u.nome, u.sobrenome, u.email " +
                                "FROM usuario u " +
                                "LEFT JOIN perfil p ON u.id_usuario = p.id_usuario " +
                                "WHERE p.id_perfil IS NULL " +
                                "ORDER BY u.id_usuario DESC " +
                                "LIMIT ?",
                        List.of(limit));
                case "matches-acima-media" -> query(conn,
                        "matches-acima-media",
                        "SELECT id_usuario_1, id_usuario_2, compatibilidade, status_match " +
                                "FROM lovvi_match " +
                                "WHERE compatibilidade > (SELECT AVG(compatibilidade) FROM lovvi_match) " +
                                "AND (? IS NULL OR status_match = ?) " +
                                "ORDER BY compatibilidade DESC " +
                                "LIMIT ?",
                        Arrays.asList(nullable(status), nullable(status), limit));
                case "view-perfil-completo" -> query(conn,
                        "v_perfil_completo_usuario",
                        "SELECT nome, cidade, tipo_perfil, objetivos, nome_interesse, categoria_interesse " +
                                "FROM v_perfil_completo_usuario " +
                                "WHERE (? IS NULL OR tipo_perfil = ?) " +
                                "AND (? IS NULL OR categoria_interesse = ?) " +
                                "ORDER BY nome, nome_interesse " +
                                "LIMIT ?",
                        Arrays.asList(nullable(tipoPerfil), nullable(tipoPerfil), nullable(categoria), nullable(categoria), limit));
                case "view-usuarios-destaque" -> query(conn,
                        "v_usuarios_destaque",
                        "SELECT nome, email, maior_compatibilidade_alcancada " +
                                "FROM v_usuarios_destaque " +
                                "ORDER BY maior_compatibilidade_alcancada DESC " +
                                "LIMIT ?",
                        List.of(limit));
                default -> null;
            };
        }
    }

    public Integer calcularIdade(LocalDate nascimento) throws SQLException {
        String sql = "SELECT fn_calcular_idade_usuario(?) AS idade";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (nascimento == null) {
                ps.setNull(1, Types.DATE);
            } else {
                ps.setDate(1, Date.valueOf(nascimento));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Number idade = (Number) rs.getObject("idade");
                return idade == null ? null : idade.intValue();
            }
        }
    }

    public BigDecimal calcularCompatibilidade(int idUsuario1, int idUsuario2) throws SQLException {
        String sql = "SELECT fn_calcular_compatibilidade(?, ?) AS compatibilidade";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario1);
            ps.setInt(2, idUsuario2);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("compatibilidade") : null;
            }
        }
    }

    public void atualizarStatusMatch(int idMatch, String status) throws SQLException {
        try (Connection conn = db.getConnection(); CallableStatement cs = conn.prepareCall("{CALL sp_atualizar_status_match(?, ?)}")) {
            cs.setInt(1, idMatch);
            cs.setString(2, status);
            cs.execute();
        }
    }

    public TableDetails listarLogsMatch(int limit) throws SQLException {
        try (Connection conn = db.getConnection()) {
            return query(conn,
                    "log_match",
                    "SELECT id_log, id_match, id_usuario_1, id_usuario_2, status_anterior, status_novo, " +
                            "compatibilidade_anterior, compatibilidade_nova, operacao, data_log " +
                            "FROM log_match ORDER BY id_log DESC LIMIT ?",
                    List.of(limit));
        }
    }

    public DashboardData montarDashboard() throws SQLException {
        try (Connection conn = db.getConnection()) {
            List<DashboardMetric> metrics = new ArrayList<>();
            metrics.add(new DashboardMetric("Usuarios", scalarString(conn, "SELECT COUNT(*) FROM usuario")));
            metrics.add(new DashboardMetric("Matches", scalarString(conn, "SELECT COUNT(*) FROM lovvi_match")));
            metrics.add(new DashboardMetric("Compatibilidade media", scalarDecimal(conn,
                    "SELECT AVG(compatibilidade) FROM lovvi_match") + "%"));
            metrics.add(new DashboardMetric("Matches aceitos", scalarDecimal(conn,
                    "SELECT (SUM(status_match = 'aceito') / COUNT(*)) * 100 FROM lovvi_match") + "%"));

            List<DashboardChart> charts = List.of(
                    chart(conn, "usuarios-cidade", "Usuarios por cidade", "bar",
                            "SELECT cidade AS label, COUNT(*) AS valor FROM usuario GROUP BY cidade ORDER BY valor DESC LIMIT 8"),
                    chart(conn, "matches-status", "Matches por status", "pie",
                            "SELECT status_match AS label, COUNT(*) AS valor FROM lovvi_match GROUP BY status_match ORDER BY valor DESC"),
                    chart(conn, "interesses-categoria", "Interesses por categoria", "bar",
                            "SELECT categoria AS label, COUNT(*) AS valor FROM interesse GROUP BY categoria ORDER BY valor DESC LIMIT 8"),
                    chart(conn, "media-status", "Media de compatibilidade por status", "bar",
                            "SELECT status_match AS label, AVG(compatibilidade) AS valor FROM lovvi_match GROUP BY status_match ORDER BY valor DESC"),
                    chart(conn, "faixa-etaria", "Usuarios por faixa etaria", "bar",
                            "SELECT CASE " +
                                    "WHEN TIMESTAMPDIFF(YEAR, dt_nascimento, CURRENT_DATE()) BETWEEN 18 AND 24 THEN '18-24' " +
                                    "WHEN TIMESTAMPDIFF(YEAR, dt_nascimento, CURRENT_DATE()) BETWEEN 25 AND 34 THEN '25-34' " +
                                    "WHEN TIMESTAMPDIFF(YEAR, dt_nascimento, CURRENT_DATE()) BETWEEN 35 AND 44 THEN '35-44' " +
                                    "ELSE '45+' END AS label, COUNT(*) AS valor " +
                                    "FROM usuario GROUP BY label ORDER BY label")
            );

            return new DashboardData(metrics, charts);
        }
    }

    private Object nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private TableDetails query(Connection conn, String name, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param == null) {
                    ps.setNull(i + 1, Types.VARCHAR);
                } else if (param instanceof Integer value) {
                    ps.setInt(i + 1, value);
                } else {
                    ps.setObject(i + 1, param);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                return new TableDetails(name, List.of(), rows(rs));
            }
        }
    }

    private List<Map<String, Object>> rows(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }

        return rows;
    }

    private String scalarString(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? String.valueOf(rs.getObject(1)) : "0";
        }
    }

    private String scalarDecimal(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            BigDecimal value = rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            if (value == null) {
                value = BigDecimal.ZERO;
            }
            return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
    }

    private DashboardChart chart(Connection conn, String id, String title, String type, String sql) throws SQLException {
        List<DashboardPoint> points = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BigDecimal value = rs.getBigDecimal("valor");
                points.add(new DashboardPoint(
                        rs.getString("label"),
                        value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP)
                ));
            }
        }

        return new DashboardChart(id, title, type, points);
    }
}
