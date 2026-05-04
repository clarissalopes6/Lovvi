package com.lovvi.dao;

import com.lovvi.infra.DatabaseConnection;
import com.lovvi.dto.ColumnInfo;
import com.lovvi.dto.DatabaseOverview;
import com.lovvi.dto.TableDetails;
import com.lovvi.dto.TableSummary;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DatabaseViewerDAO {

    private final DatabaseConnection db;

    public DatabaseViewerDAO(DatabaseConnection db) {
        this.db = db;
    }

    public DatabaseOverview obterVisaoGeral() throws SQLException {
        List<TableSummary> tables = new ArrayList<>();

        try (Connection conn = db.getConnection()) {
            String databaseName = conn.getCatalog();
            for (String tableName : getUserTableNames(conn)) {
                int totalColumns = countColumns(conn, tableName);
                long totalRows = countRows(conn, tableName);
                tables.add(new TableSummary(tableName, totalColumns, totalRows));
            }

            tables.sort(Comparator.comparing(TableSummary::tableName, String.CASE_INSENSITIVE_ORDER));
            return new DatabaseOverview(databaseName, tables);
        }
    }

    public TableDetails obterDetalhesTabela(String tableName, int limit) throws SQLException {
        try (Connection conn = db.getConnection()) {
            String resolvedTableName = resolveTableName(conn, tableName);
            if (resolvedTableName == null) {
                return null;
            }

            List<ColumnInfo> columns = listarColunas(conn, resolvedTableName);
            List<Map<String, Object>> rows = listarLinhas(conn, resolvedTableName, limit);
            return new TableDetails(resolvedTableName, columns, rows);
        }
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

    private long countRows(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(1) FROM " + quoteIdentifier(conn, tableName);

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private String resolveTableName(Connection conn, String requestedName) throws SQLException {
        for (String table : getUserTableNames(conn)) {
            if (table.equalsIgnoreCase(requestedName)) {
                return table;
            }
        }
        return null;
    }

    private List<ColumnInfo> listarColunas(Connection conn, String resolvedTableName) throws SQLException {
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

        return columns;
    }

    private List<Map<String, Object>> listarLinhas(Connection conn, String resolvedTableName, int limit) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT * FROM " + quoteIdentifier(conn, resolvedTableName) + " LIMIT ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(rsMeta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }

        return rows;
    }
}
