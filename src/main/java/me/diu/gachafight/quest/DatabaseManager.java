package me.diu.gachafight.quest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private String url;
    private String username;
    private String password;
    private HikariDataSource dataSource;

    public DatabaseManager(String username, String password) {
        this.url = "jdbc:mysql://avonelle.bloom.host:3306/s66551_Quest";
        this.username = username;
        this.password = password;
        connect();
        createQuestProgressTable();
    }

    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setIdleTimeout(60000); // 60 seconds
        config.setConnectionTimeout(60000); // 60 seconds
        config.setValidationTimeout(3000); // 3 seconds
        config.setMaxLifetime(60000); // 60 seconds
        config.setMaximumPoolSize(20); // Maximum 20 connections
        config.setLeakDetectionThreshold(5000); // 5 seconds

        dataSource = new HikariDataSource(config);
    }

    public void disconnect() throws SQLException {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public Connection getConnection()  {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void createQuestProgressTable() {
        String sql = "CREATE TABLE IF NOT EXISTS quest_progress (" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "quest_id INT NOT NULL," +
                "progress INT DEFAULT 0," +
                "start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "slot INT NOT NULL," + // New slot column
                "PRIMARY KEY (player_uuid, quest_id)" +
                ")";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
