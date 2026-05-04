package tvgirl.elmodev.database;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private final HikariDataSource dataSource;

    public DatabaseManager(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createLogTables() {
        String sql = """
        CREATE TABLE IF NOT EXISTS e1ch_log (
            id BIGSERIAL PRIMARY KEY,
            uuid UUID NOT NULL,
            player VARCHAR(16),
            timestamp TIMESTAMP NOT NULL,
            message TEXT NOT NULL,
            world VARCHAR(32),
            x DOUBLE PRECISION,
            y DOUBLE PRECISION,
            z DOUBLE PRECISION,
            ip TEXT
        );
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.execute();

        } catch (SQLException e) {
            Bukkit.getLogger().warning("LTABLES | : " + e.getMessage());
        }
    }

    public void createIndexes() {
        String[] indexes = {
                "CREATE INDEX IF NOT EXISTS idx_player ON e1ch_log(player)",
                "CREATE INDEX IF NOT EXISTS idx_uuid ON e1ch_log(uuid)",
                "CREATE INDEX IF NOT EXISTS idx_timestamp ON e1ch_log(timestamp)",
                "CREATE INDEX IF NOT EXISTS idx_player_time ON e1ch_log(player, timestamp DESC)"
        };

        try (Connection conn = dataSource.getConnection()) {
            for (String sql : indexes) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.execute();
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Index creation failed: " + e.getMessage());
        }
    }
}
