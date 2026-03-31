package com.lovvi.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DatabaseConnection {

    @Value("${db.url}")
    private String url;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Value("${db.driver}")
    private String driver;

    private final Object initLock = new Object();
    private volatile boolean h2Initialized = false;

    private boolean isH2() {
        return url != null && url.startsWith("jdbc:h2:");
    }

    private void initializeH2IfNeeded(Connection conn) throws SQLException {
        if (!isH2() || h2Initialized) {
            return;
        }

        synchronized (initLock) {
            if (h2Initialized) {
                return;
            }

            try (Statement st = conn.createStatement()) {
                st.execute("RUNSCRIPT FROM 'classpath:sql/schema.sql'");
                st.execute("RUNSCRIPT FROM 'classpath:sql/data.sql'");
                h2Initialized = true;
            }
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado: " + driver, e);
        }

        Connection conn = DriverManager.getConnection(url, username, password);
        initializeH2IfNeeded(conn);
        return conn;
    }
}
