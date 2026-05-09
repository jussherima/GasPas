package com.gaspas.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_DIR = System.getProperty("user.home") + "/.gaspas";
    private static final String DB_PATH = DB_DIR + "/gaspas.db";
    private Connection connection;

    public void initialize() throws SQLException, java.io.IOException {
        java.nio.file.Path dbDir = java.nio.file.Paths.get(DB_DIR);
        if (!java.nio.file.Files.exists(dbDir)) {
            java.nio.file.Files.createDirectories(dbDir);
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        createTables();
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS platforms (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "name TEXT UNIQUE NOT NULL, " +
                     "data TEXT NOT NULL)";
        connection.createStatement().execute(sql);
    }

    public void insertPlatform(String name, String encryptedData) throws SQLException {
        String sql = "INSERT INTO platforms (name, data) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, encryptedData);
            ps.execute();
        }
    }

    public void updatePlatform(String name, String encryptedData) throws SQLException {
        String sql = "UPDATE platforms SET data = ? WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, encryptedData);
            ps.setString(2, name);
            ps.execute();
        }
    }

    public void deletePlatform(String name) throws SQLException {
        String sql = "DELETE FROM platforms WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.execute();
        }
    }

    public List<String> getAllPlatformNames() throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM platforms ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }
        return names;
    }

    public String getEncryptedData(String name) throws SQLException {
        String sql = "SELECT data FROM platforms WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("data");
                }
            }
        }
        return null;
    }

    public boolean platformExists(String name) throws SQLException {
        String sql = "SELECT 1 FROM platforms WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
