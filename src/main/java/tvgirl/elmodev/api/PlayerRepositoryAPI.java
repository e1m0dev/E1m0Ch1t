package tvgirl.elmodev.api;

import java.sql.Timestamp;
import java.util.UUID;

public interface PlayerRepositoryAPI {
    void messageBatch(UUID id, String player, Timestamp timestamp, String message, String world, double x, double y, double z, String ip);
    void flushBatches();
}
