package com.lovvi.dao;

import com.lovvi.dto.LovviMatchCrudRequest;
import com.lovvi.dto.LovviMatchCrudRow;
import com.lovvi.infra.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class LovviMatchDAO {

    private final DatabaseConnection db;

    public LovviMatchDAO(DatabaseConnection db) {
        this.db = db;
    }

    public List<LovviMatchCrudRow> listar(int limit) throws SQLException {
        List<LovviMatchCrudRow> matches = new ArrayList<>();
        String sql = "SELECT m.id_match, m.id_usuario_1, CONCAT(u1.nome, ' ', u1.sobrenome) AS usuario_1, " +
                "m.id_usuario_2, CONCAT(u2.nome, ' ', u2.sobrenome) AS usuario_2, m.data_match, " +
                "m.compatibilidade, m.status_match " +
                "FROM lovvi_match m " +
                "JOIN usuario u1 ON u1.id_usuario = m.id_usuario_1 " +
                "JOIN usuario u2 ON u2.id_usuario = m.id_usuario_2 " +
                "ORDER BY m.id_match DESC LIMIT ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matches.add(montarLinha(rs));
                }
            }
        }

        return matches;
    }

    public int inserir(LovviMatchCrudRequest request) throws SQLException {
        String sql = "INSERT INTO lovvi_match (id_usuario_1, id_usuario_2, data_match, compatibilidade, status_match) " +
                "VALUES (?, ?, CURRENT_DATE(), ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, request);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean atualizar(int idMatch, LovviMatchCrudRequest request) throws SQLException {
        String sql = "UPDATE lovvi_match SET id_usuario_1 = ?, id_usuario_2 = ?, compatibilidade = ?, status_match = ? " +
                "WHERE id_match = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            preencher(ps, request);
            ps.setInt(5, idMatch);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(int idMatch) throws SQLException {
        String sql = "DELETE FROM lovvi_match WHERE id_match = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMatch);
            return ps.executeUpdate() > 0;
        }
    }

    public void registrarAcao(int idUsuario, int idCandidato, BigDecimal compatibilidade, String statusMatch) throws SQLException {
        try (Connection conn = db.getConnection()) {
            Integer idMatch = buscarMatchExistente(conn, idUsuario, idCandidato);
            if (idMatch == null) {
                inserirAcao(conn, idUsuario, idCandidato, compatibilidade, statusMatch);
                return;
            }
            atualizarAcao(conn, idMatch, compatibilidade, statusMatch);
        }
    }

    public Set<Integer> listarUsuariosComInteracao(int idUsuario) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT CASE WHEN id_usuario_1 = ? THEN id_usuario_2 ELSE id_usuario_1 END AS id_usuario " +
                "FROM lovvi_match " +
                "WHERE (id_usuario_1 = ? OR id_usuario_2 = ?) AND status_match IN ('aceito', 'recusado')";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_usuario"));
                }
            }
        }

        return ids;
    }

    public List<Integer> listarUsuariosAceitos(int idUsuario) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT CASE WHEN id_usuario_1 = ? THEN id_usuario_2 ELSE id_usuario_1 END AS id_usuario " +
                "FROM lovvi_match " +
                "WHERE (id_usuario_1 = ? OR id_usuario_2 = ?) AND status_match = 'aceito' " +
                "ORDER BY id_match DESC";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_usuario"));
                }
            }
        }

        return ids;
    }

    private Integer buscarMatchExistente(Connection conn, int idUsuario, int idCandidato) throws SQLException {
        String sql = "SELECT id_match FROM lovvi_match " +
                "WHERE (id_usuario_1 = ? AND id_usuario_2 = ?) OR (id_usuario_1 = ? AND id_usuario_2 = ?) " +
                "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idCandidato);
            ps.setInt(3, idCandidato);
            ps.setInt(4, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id_match") : null;
            }
        }
    }

    private void inserirAcao(Connection conn, int idUsuario, int idCandidato, BigDecimal compatibilidade, String statusMatch)
            throws SQLException {
        String sql = "INSERT INTO lovvi_match (id_usuario_1, id_usuario_2, data_match, compatibilidade, status_match) " +
                "VALUES (?, ?, CURRENT_DATE(), ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idCandidato);
            ps.setBigDecimal(3, compatibilidade == null ? BigDecimal.ZERO : compatibilidade);
            ps.setString(4, statusMatch);
            ps.executeUpdate();
        }
    }

    private void atualizarAcao(Connection conn, int idMatch, BigDecimal compatibilidade, String statusMatch) throws SQLException {
        String sql = "UPDATE lovvi_match SET compatibilidade = ?, status_match = ? WHERE id_match = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, compatibilidade == null ? BigDecimal.ZERO : compatibilidade);
            ps.setString(2, statusMatch);
            ps.setInt(3, idMatch);
            ps.executeUpdate();
        }
    }

    private void preencher(PreparedStatement ps, LovviMatchCrudRequest request) throws SQLException {
        ps.setInt(1, request.idUsuario1());
        ps.setInt(2, request.idUsuario2());
        ps.setBigDecimal(3, request.compatibilidade() == null ? BigDecimal.ZERO : request.compatibilidade());
        ps.setString(4, request.statusMatch().trim().toLowerCase());
    }

    private LovviMatchCrudRow montarLinha(ResultSet rs) throws SQLException {
        Date dataMatch = rs.getDate("data_match");
        return new LovviMatchCrudRow(
                rs.getInt("id_match"),
                rs.getInt("id_usuario_1"),
                rs.getString("usuario_1"),
                rs.getInt("id_usuario_2"),
                rs.getString("usuario_2"),
                dataMatch != null ? dataMatch.toLocalDate() : null,
                rs.getBigDecimal("compatibilidade"),
                rs.getString("status_match")
        );
    }
}
