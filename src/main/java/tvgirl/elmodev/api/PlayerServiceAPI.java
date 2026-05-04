package tvgirl.elmodev.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import tvgirl.elmodev.state.ChatState;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface PlayerServiceAPI {
    void systemSendAutoBroadcast();
    void playerEnterMessage(Player p, Component raw, BiConsumer<Component, ChatState> consumer);
    void playerSendingMessage(Player p, Component message, ChatState state);
    void beMentioned(Player sender, Player recipient);
    void systemLogMessage(UUID id, String player, Timestamp timestamp, String message, String world, double x, double y, double z, String ip);
}
