package tvgirl.elmodev.repository;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import tvgirl.elmodev.E1m0Ch1t;
import tvgirl.elmodev.api.PlayerRepositoryAPI;
import tvgirl.elmodev.state.MessageLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerRepository implements PlayerRepositoryAPI {

    private final E1m0Ch1t plugin;
    private final HikariDataSource dataSource;

    private final Queue<MessageLog> queue = new ConcurrentLinkedQueue<>();
    private BukkitTask worker;

    public PlayerRepository(E1m0Ch1t plugin, HikariDataSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
    }

    private final int BATCH_SIZE = 333;

    public void start() {
        worker = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::flushBatches,
                20L,
                20L
        );
    }

    public void shutdown() {
        if (worker != null) worker.cancel();
        flushAll();
    }

    @Override
    public void messageBatch(UUID id, String player, Timestamp timestamp,
                             String message, String world,
                             double x, double y, double z, String ip) {

        queue.add(new MessageLog(id, player, timestamp, message, world, x, y, z, ip));
    }

    public void flushBatches() {
        List<MessageLog> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 0; i < BATCH_SIZE; i++) {
            MessageLog log = queue.poll();
            if (log == null) break;
            batch.add(log);
        }

        if (!batch.isEmpty()) {
            insertBatch(batch);
        }
    }

    private void flushAll() {
        List<MessageLog> batch = new ArrayList<>();

        MessageLog log;
        while ((log = queue.poll()) != null) {
            batch.add(log);

            if (batch.size() >= BATCH_SIZE) {
                insertBatch(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            insertBatch(batch);
        }
    }

    private void insertBatch(List<MessageLog> batch) {
        String sql = """
                INSERT INTO e1ch_log
                (uuid, player, timestamp, message, world, x, y, z, ip)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (MessageLog log : batch) {
                stmt.setObject(1, log.id());
                stmt.setString(2, log.player());
                stmt.setTimestamp(3, log.timestamp());
                stmt.setString(4, log.message());
                stmt.setString(5, log.world());
                stmt.setDouble(6, log.x());
                stmt.setDouble(7, log.y());
                stmt.setDouble(8, log.z());
                stmt.setString(9, log.IP());

                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            Bukkit.getLogger().warning("Batch | : " + e.getMessage());
        }
    }
}