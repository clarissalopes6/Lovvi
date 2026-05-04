package com.lovvi.dao;

import com.lovvi.infra.DatabaseConnection;
import com.lovvi.dto.TesteCrudRequest;
import com.lovvi.dto.TesteCrudRow;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TesteDAO {

    private final DatabaseConnection db;

    public TesteDAO(DatabaseConnection db) {
        this.db = db;
    }

    public List<TesteCrudRow> listar(int limit) throws SQLException {
        List<TesteCrudRow> testes = new ArrayList<>();
        String sql = "SELECT id_teste, nome_teste, descricao FROM teste ORDER BY id_teste DESC LIMIT ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    testes.add(new TesteCrudRow(
                            rs.getInt("id_teste"),
                            rs.getString("nome_teste"),
                            rs.getString("descricao")
                    ));
                }
            }
        }

        return testes;
    }

    public int inserir(TesteCrudRequest request) throws SQLException {
        String sql = "INSERT INTO teste (nome_teste, descricao) VALUES (?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.nomeTeste().trim());
            ps.setString(2, request.descricao());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean atualizar(int idTeste, TesteCrudRequest request) throws SQLException {
        String sql = "UPDATE teste SET nome_teste = ?, descricao = ? WHERE id_teste = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, request.nomeTeste().trim());
            ps.setString(2, request.descricao());
            ps.setInt(3, idTeste);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(int idTeste) throws SQLException {
        String sql = "DELETE FROM teste WHERE id_teste = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTeste);
            return ps.executeUpdate() > 0;
        }
    }
}
