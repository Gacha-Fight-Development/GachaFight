package me.diu.gachafight.quest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private GachaFight plugin;
    private String url;
    private String username;
    private String password;
    private HikariDataSource dataSource;

    public DatabaseManager(GachaFight plugin, String username, String password) {
        this.plugin = plugin;
        this.url = "jdbc:mysql://avonelle.bloom.host:3306/s66551_Quest";
        this.username = username;
        this.password = password;
    }
    public CompletableFuture<Void> initializeAsync() {
        return CompletableFuture.runAsync(() -> {
            connect();
            createQuestProgressTable();
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
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

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                plugin.getLogger().info("Database connection closed successfully.");
            } catch (Exception e) {
                plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or has been closed.");
        }
        return dataSource.getConnection();
    }

    private void createQuestProgressTable() {
        String sql = "CREATE TABLE IF NOT EXISTS quest_progress (" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "quest_id INT NOT NULL," +
                "progress INT DEFAULT 0," +
                "PRIMARY KEY (player_uuid, quest_id)" +
                ")";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create quest_progress table: " + e.getMessage());
        }
    }

}
