package com.lovvi.dao;

import com.lovvi.infra.DatabaseConnection;
import com.lovvi.model.Interesse;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
