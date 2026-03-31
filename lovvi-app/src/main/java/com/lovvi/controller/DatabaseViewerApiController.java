package com.lovvi.controller;

import com.lovvi.infra.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interface")
public class DatabaseViewerApiController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseViewerApiController.class);
    private final DatabaseConnection db;

    public DatabaseViewerApiController(DatabaseConnection db) {
        this.db = db;
    }

    record TableSummary(String tableName, int totalColumns, long totalRows) {
    }

    record ColumnInfo(String name, String type, int size, boolean nullable) {
    }

    record TableDetails(String tableName, List<ColumnInfo> columns, List<Map<String, Object>> rows) {
    }

    record DatabaseOverview(String databaseName, List<TableSummary> tables) {
    }

        record CrudResult(boolean success, String message, Integer id) {
        }

        record UsuarioCrudRequest(
            String nome,
            String sobrenome,
            String email,
            String senha,
            String cidade,
            String genero,
            LocalDate dtNascimento
        ) {
        }

        record UsuarioCrudRow(
            int idUsuario,
            String nome,
            String sobrenome,
            String email,
            String cidade,
            String genero,
            LocalDate dtNascimento
        ) {
        }

        record TesteCrudRequest(String nomeTeste, String descricao) {
        }

        record TesteCrudRow(int idTeste, String nomeTeste, String descricao) {
        }

        private boolean isBlank(String value) {
        return value == null || value.isBlank();
        }

    private String quoteIdentifier(Connection conn, String identifier) throws SQLException {
        String quote = conn.getMetaData().getIdentifierQuoteString();
        if (quote == null || quote.isBlank()) {
            return identifier;
        }
        return quote + identifier + quote;
    }

    private boolean isUserTable(String schema, String table) {
        if (table == null || table.isBlank()) {
            return false;
        }
        if (schema == null) {
            return true;
        }
        String normalized = schema.toUpperCase();
        return !normalized.equals("INFORMATION_SCHEMA")
                && !normalized.equals("PG_CATALOG")
                && !normalized.equals("SYS")
                && !normalized.equals("MYSQL")
                && !normalized.equals("PERFORMANCE_SCHEMA");
    }

    private List<String> getUserTableNames(Connection conn) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String table = rs.getString("TABLE_NAME");
                String schema = rs.getString("TABLE_SCHEM");
                if (isUserTable(schema, table)) {
                    tableNames.add(table);
                }
            }
        }

        tableNames.sort(String.CASE_INSENSITIVE_ORDER);
        return tableNames;
    }

    private int countColumns(Connection conn, String tableName) throws SQLException {
        int count = 0;
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, "%")) {
            while (rs.next()) {
                count++;
            }
        }

        return count;
    }

    private long countRows(Connection conn, String tableName) {
        String sql;
        try {
            sql = "SELECT COUNT(1) FROM " + quoteIdentifier(conn, tableName);
        } catch (SQLException e) {
            return -1;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException ignored) {
        }

        return -1;
    }

    private String resolveTableName(Connection conn, String requestedName) throws SQLException {
        for (String table : getUserTableNames(conn)) {
            if (table.equalsIgnoreCase(requestedName)) {
                return table;
            }
        }
        return null;
    }

    @GetMapping("/overview")
    public ResponseEntity<DatabaseOverview> getOverview() {
        List<TableSummary> tables = new ArrayList<>();

        try (Connection conn = db.getConnection()) {
            String databaseName = conn.getCatalog();
            for (String tableName : getUserTableNames(conn)) {
                int totalColumns = countColumns(conn, tableName);
                long totalRows = countRows(conn, tableName);
                tables.add(new TableSummary(tableName, totalColumns, totalRows));
            }

            tables.sort(Comparator.comparing(TableSummary::tableName, String.CASE_INSENSITIVE_ORDER));
            return ResponseEntity.ok(new DatabaseOverview(databaseName, tables));
        } catch (SQLException e) {
            logger.error("Erro ao montar visão geral do banco", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tables/{tableName}")
    public ResponseEntity<TableDetails> getTableDetails(
            @PathVariable("tableName") String tableName,
            @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));

        try (Connection conn = db.getConnection()) {
            String resolvedTableName = resolveTableName(conn, tableName);
            if (resolvedTableName == null) {
                return ResponseEntity.notFound().build();
            }

            List<ColumnInfo> columns = new ArrayList<>();
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, resolvedTableName, "%")) {
                while (rs.next()) {
                    columns.add(new ColumnInfo(
                            rs.getString("COLUMN_NAME"),
                            rs.getString("TYPE_NAME"),
                            rs.getInt("COLUMN_SIZE"),
                            rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable
                    ));
                }
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            String sql = "SELECT * FROM " + quoteIdentifier(conn, resolvedTableName) + " LIMIT ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, safeLimit);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData rsMeta = rs.getMetaData();
                    int columnCount = rsMeta.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String column = rsMeta.getColumnLabel(i);
                            row.put(column, rs.getObject(i));
                        }
                        rows.add(row);
                    }
                }
            }

            return ResponseEntity.ok(new TableDetails(resolvedTableName, columns, rows));
        } catch (SQLException e) {
            logger.error("Erro ao montar detalhes da tabela {}", tableName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioCrudRow>> listUsuarios(
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 300));
        List<UsuarioCrudRow> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario, nome, sobrenome, email, cidade, genero, dt_nascimento " +
                "FROM usuario ORDER BY id_usuario DESC LIMIT ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(new UsuarioCrudRow(
                            rs.getInt("id_usuario"),
                            rs.getString("nome"),
                            rs.getString("sobrenome"),
                            rs.getString("email"),
                            rs.getString("cidade"),
                            rs.getString("genero"),
                            rs.getDate("dt_nascimento") != null ? rs.getDate("dt_nascimento").toLocalDate() : null
                    ));
                }
            }

            return ResponseEntity.ok(usuarios);
        } catch (SQLException e) {
            logger.error("Erro ao listar usuarios", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/usuarios")
    public ResponseEntity<CrudResult> createUsuario(@RequestBody UsuarioCrudRequest request) {
        if (request == null || isBlank(request.nome()) || isBlank(request.sobrenome()) ||
                isBlank(request.email()) || isBlank(request.senha()) || isBlank(request.cidade()) ||
                isBlank(request.genero()) || request.dtNascimento() == null) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Dados obrigatorios do usuario ausentes.", null));
        }

        String sql = "INSERT INTO usuario (nome, sobrenome, email, senha, cidade, genero, dt_nascimento) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.nome().trim());
            ps.setString(2, request.sobrenome().trim());
            ps.setString(3, request.email().trim());
            ps.setString(4, request.senha());
            ps.setString(5, request.cidade().trim());
            ps.setString(6, request.genero().trim());
            ps.setDate(7, java.sql.Date.valueOf(request.dtNascimento()));
            ps.executeUpdate();

            Integer createdId = null;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    createdId = rs.getInt(1);
                }
            }

            return ResponseEntity.ok(new CrudResult(true, "Usuario inserido com sucesso.", createdId));
        } catch (SQLException e) {
            logger.error("Erro ao inserir usuario", e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao inserir usuario.", null));
        }
    }

    @PutMapping("/usuarios/{idUsuario}")
    public ResponseEntity<CrudResult> updateUsuario(
            @PathVariable("idUsuario") int idUsuario,
            @RequestBody UsuarioCrudRequest request
    ) {
        if (request == null || isBlank(request.nome()) || isBlank(request.sobrenome()) ||
                isBlank(request.email()) || isBlank(request.senha()) || isBlank(request.cidade()) ||
                isBlank(request.genero()) || request.dtNascimento() == null) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Dados obrigatorios do usuario ausentes.", null));
        }

        String sql = "UPDATE usuario SET nome = ?, sobrenome = ?, email = ?, senha = ?, cidade = ?, genero = ?, " +
                "dt_nascimento = ? WHERE id_usuario = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, request.nome().trim());
            ps.setString(2, request.sobrenome().trim());
            ps.setString(3, request.email().trim());
            ps.setString(4, request.senha());
            ps.setString(5, request.cidade().trim());
            ps.setString(6, request.genero().trim());
            ps.setDate(7, java.sql.Date.valueOf(request.dtNascimento()));
            ps.setInt(8, idUsuario);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Usuario nao encontrado.", null));
            }

            return ResponseEntity.ok(new CrudResult(true, "Usuario atualizado com sucesso.", idUsuario));
        } catch (SQLException e) {
            logger.error("Erro ao atualizar usuario {}", idUsuario, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao atualizar usuario.", null));
        }
    }

    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<CrudResult> deleteUsuario(@PathVariable("idUsuario") int idUsuario) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Usuario nao encontrado.", null));
            }

            return ResponseEntity.ok(new CrudResult(true, "Usuario removido com sucesso.", idUsuario));
        } catch (SQLException e) {
            logger.error("Erro ao remover usuario {}", idUsuario, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao remover usuario.", null));
        }
    }

    @GetMapping("/testes")
    public ResponseEntity<List<TesteCrudRow>> listTestes(
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 300));
        List<TesteCrudRow> testes = new ArrayList<>();
        String sql = "SELECT id_teste, nome_teste, descricao FROM teste ORDER BY id_teste DESC LIMIT ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    testes.add(new TesteCrudRow(
                            rs.getInt("id_teste"),
                            rs.getString("nome_teste"),
                            rs.getString("descricao")
                    ));
                }
            }

            return ResponseEntity.ok(testes);
        } catch (SQLException e) {
            logger.error("Erro ao listar testes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/testes")
    public ResponseEntity<CrudResult> createTeste(@RequestBody TesteCrudRequest request) {
        if (request == null || isBlank(request.nomeTeste())) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Nome do teste e obrigatorio.", null));
        }

        String sql = "INSERT INTO teste (nome_teste, descricao) VALUES (?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.nomeTeste().trim());
            ps.setString(2, request.descricao());
            ps.executeUpdate();

            Integer createdId = null;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    createdId = rs.getInt(1);
                }
            }

            return ResponseEntity.ok(new CrudResult(true, "Teste inserido com sucesso.", createdId));
        } catch (SQLException e) {
            logger.error("Erro ao inserir teste", e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao inserir teste.", null));
        }
    }

    @PutMapping("/testes/{idTeste}")
    public ResponseEntity<CrudResult> updateTeste(
            @PathVariable("idTeste") int idTeste,
            @RequestBody TesteCrudRequest request
    ) {
        if (request == null || isBlank(request.nomeTeste())) {
            return ResponseEntity.badRequest().body(new CrudResult(false, "Nome do teste e obrigatorio.", null));
        }

        String sql = "UPDATE teste SET nome_teste = ?, descricao = ? WHERE id_teste = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, request.nomeTeste().trim());
            ps.setString(2, request.descricao());
            ps.setInt(3, idTeste);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Teste nao encontrado.", null));
            }

            return ResponseEntity.ok(new CrudResult(true, "Teste atualizado com sucesso.", idTeste));
        } catch (SQLException e) {
            logger.error("Erro ao atualizar teste {}", idTeste, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao atualizar teste.", null));
        }
    }

    @DeleteMapping("/testes/{idTeste}")
    public ResponseEntity<CrudResult> deleteTeste(@PathVariable("idTeste") int idTeste) {
        String sql = "DELETE FROM teste WHERE id_teste = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTeste);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                return ResponseEntity.status(404).body(new CrudResult(false, "Teste nao encontrado.", null));
            }

            return ResponseEntity.ok(new CrudResult(true, "Teste removido com sucesso.", idTeste));
        } catch (SQLException e) {
            logger.error("Erro ao remover teste {}", idTeste, e);
            return ResponseEntity.internalServerError().body(new CrudResult(false, "Erro ao remover teste.", null));
        }
    }
}
