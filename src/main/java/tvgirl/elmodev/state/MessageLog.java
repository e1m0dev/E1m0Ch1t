package tvgirl.elmodev.state;

import java.sql.Timestamp;
import java.util.UUID;

public record MessageLog(
        UUID id,
        String player,
        Timestamp timestamp,
        String message,
        String world,
        double x,
        double y,
        double z,
        String IP)
{
    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String player() {
        return player;
    }

    @Override
    public Timestamp timestamp() {
        return timestamp;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String world() {
        return world;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public String IP() {
        return IP;
    }
}
