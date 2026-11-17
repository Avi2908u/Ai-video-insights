package com.Aivideoinsights.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        loadDotEnv();
        normalizeDatabaseUrl();
        ensureDatabaseExists();
        SpringApplication.run(BackendApplication.class, args);
    }

    private static void normalizeDatabaseUrl() {
        String dbUrl = System.getProperty("spring.datasource.url");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DB_URL");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DATABASE_URL");
        }
        if (dbUrl != null && !dbUrl.isBlank()) {
            if (dbUrl.startsWith("postgres://")) {
                dbUrl = "jdbc:postgresql://" + dbUrl.substring(11);
            } else if (dbUrl.startsWith("postgresql://")) {
                dbUrl = "jdbc:postgresql://" + dbUrl.substring(13);
            }
            System.setProperty("spring.datasource.url", dbUrl);
            System.setProperty("DB_URL", dbUrl);
        }
    }

    private static void loadDotEnv() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        File envFile = null;

        // Traverse upwards looking for the root .env file
        for (int i = 0; i < 5 && current != null; i++) {
            File candidate = current.resolve(".env").toFile();
            if (candidate.exists() && candidate.isFile()) {
                envFile = candidate;
                break;
            }
            current = current.getParent();
        }

        if (envFile == null) {
            List<Path> fallbackPaths = List.of(
                    Paths.get(".env"),
                    Paths.get("../.env")
            );
            for (Path path : fallbackPaths) {
                File candidate = path.toFile();
                if (candidate.exists() && candidate.isFile()) {
                    envFile = candidate;
                    break;
                }
            }
        }

        if (envFile != null && envFile.exists() && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        // Always register into Java System Properties so it takes precedence
                        System.setProperty(key, value);

                        // Also mirror Spring property naming conventions
                        if ("YOUTUBE_API_KEY".equals(key)) {
                            System.setProperty("youtube.api.key", value);
                        } else if ("OPENROUTER_API_KEY".equals(key)) {
                            System.setProperty("openrouter.api.key", value);
                        } else if ("DB_URL".equals(key)) {
                            System.setProperty("spring.datasource.url", value);
                        } else if ("DB_USERNAME".equals(key)) {
                            System.setProperty("spring.datasource.username", value);
                        } else if ("DB_PASSWORD".equals(key)) {
                            System.setProperty("spring.datasource.password", value);
                        } else if ("TRANSCRIPT_SERVICE_URL".equals(key)) {
                            System.setProperty("transcript.service.url", value);
                        }
                    }
                }
                System.out.println("[BackendApplication] Successfully loaded credentials and configuration from: " + envFile.getAbsolutePath());
                return;
            } catch (IOException e) {
                System.err.println("[BackendApplication] Failed to read .env file at " + envFile.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        System.out.println("[BackendApplication] No .env file found in search paths. Using existing system environment.");
    }

    private static void ensureDatabaseExists() {
        String dbUrl = System.getProperty("spring.datasource.url", System.getenv("DB_URL"));
        String user = System.getProperty("spring.datasource.username", System.getenv("DB_USERNAME"));
        String pass = System.getProperty("spring.datasource.password", System.getenv("DB_PASSWORD"));

        if (dbUrl == null || !dbUrl.startsWith("jdbc:postgresql://")) {
            return;
        }

        try {
            // Strip "jdbc:" to parse authority and path
            URI cleanUri = URI.create(dbUrl.substring(5));
            String dbName = cleanUri.getPath();
            if (dbName != null && dbName.startsWith("/")) {
                dbName = dbName.substring(1);
            }
            if (dbName == null || dbName.isBlank() || "postgres".equalsIgnoreCase(dbName)) {
                return;
            }

            // Ensure database name is safe (alphanumeric and underscore)
            if (!dbName.matches("^[a-zA-Z0-9_]+$")) {
                return;
            }

            String host = cleanUri.getHost();
            int port = cleanUri.getPort() > 0 ? cleanUri.getPort() : 5432;
            String maintenanceUrl = "jdbc:postgresql://" + host + ":" + port + "/postgres";

            try (Connection conn = DriverManager.getConnection(maintenanceUrl, user, pass)) {
                try (PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT 1 FROM pg_database WHERE datname = ?")) {
                    checkStmt.setString(1, dbName);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("[BackendApplication] Database '" + dbName + "' does not exist. Creating database automatically...");
                            try (Statement createStmt = conn.createStatement()) {
                                createStmt.executeUpdate("CREATE DATABASE " + dbName);
                                System.out.println("[BackendApplication] Database '" + dbName + "' created successfully!");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BackendApplication] Note: Automatic database creation check skipped: " + e.getMessage());
        }
    }
}
