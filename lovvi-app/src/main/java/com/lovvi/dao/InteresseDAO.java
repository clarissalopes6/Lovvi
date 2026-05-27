package com.lovvi.dao;

import com.lovvi.infra.DatabaseConnection;
import com.lovvi.dto.InteresseCrudRequest;
import com.lovvi.dto.InteresseCrudRow;
import com.lovvi.model.Interesse;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InteresseDAO {

    private final DatabaseConnection db;

    public InteresseDAO(DatabaseConnection db) {
        this.db = db;
    }

    public List<Interesse> listarTodos() throws SQLException {
        List<Interesse> lista = new ArrayList<>();
        String sql = "SELECT id_interesse, nome_interesse, categoria FROM interesse ORDER BY nome_interesse";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Interesse(
                        rs.getInt("id_interesse"),
                        rs.getString("nome_interesse"),
                        rs.getString("categoria")
                ));
            }
        }

        return lista;
    }

    public List<InteresseCrudRow> listarCrud(int limit) throws SQLException {
        List<InteresseCrudRow> interesses = new ArrayList<>();
        String sql = "SELECT id_interesse, nome_interesse, categoria FROM interesse ORDER BY id_interesse DESC LIMIT ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    interesses.add(new InteresseCrudRow(
                            rs.getInt("id_interesse"),
                            rs.getString("nome_interesse"),
                            rs.getString("categoria")
                    ));
                }
            }
        }

        return interesses;
    }

    public int inserir(InteresseCrudRequest request) throws SQLException {
        String sql = "INSERT INTO interesse (nome_interesse, categoria) VALUES (?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, request);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean atualizar(int idInteresse, InteresseCrudRequest request) throws SQLException {
        String sql = "UPDATE interesse SET nome_interesse = ?, categoria = ? WHERE id_interesse = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            preencher(ps, request);
            ps.setInt(3, idInteresse);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(int idInteresse) throws SQLException {
        String sql = "DELETE FROM interesse WHERE id_interesse = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idInteresse);
            return ps.executeUpdate() > 0;
        }
    }

    private void preencher(PreparedStatement ps, InteresseCrudRequest request) throws SQLException {
        ps.setString(1, request.nomeInteresse().trim());
        ps.setString(2, request.categoria().trim());
    }
}
