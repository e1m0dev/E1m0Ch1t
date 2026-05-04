package tvgirl.elmodev.service;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import tvgirl.elmodev.E1m0Ch1t;
import tvgirl.elmodev.api.PlayerServiceAPI;
import tvgirl.elmodev.color.E1m0Color;
import tvgirl.elmodev.repository.PlayerRepository;
import tvgirl.elmodev.state.AMessageState;
import tvgirl.elmodev.state.ChatState;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class PlayerService implements PlayerServiceAPI {

    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final E1m0Color eColor = new E1m0Color();

    private final E1m0Ch1t plugin;
    private final PlayerRepository repo;
    private final FileConfiguration config;

    public PlayerService(E1m0Ch1t plugin, PlayerRepository repo, FileConfiguration config) {
        this.plugin = plugin;
        this.repo = repo;
        this.config = config;
    }

    @Override
    public void playerEnterMessage(Player p, Component raw, BiConsumer<Component, ChatState> consumer) {
        String text = PlainTextComponentSerializer.plainText().serialize(raw);

        ChatState matched = null;
        String chatId = null;

        for (Map.Entry<String, ChatState> entry : plugin.getChatList().entrySet()) {
            if (text.startsWith(entry.getValue().tag())) {
                matched = entry.getValue();
                chatId = entry.getKey();
                break;
            }
        }

        if (matched == null) {
            matched = plugin.getChatList().get("RP");
            chatId = "RP";
        }

        if (!handleCooldown(p, chatId, matched)) return;

        String cleanMessage = text;
        if (!matched.tag().equalsIgnoreCase("DEFAULT")) {
            cleanMessage = text.substring(matched.tag().length());
        }

        String message = config.getString("Settings.chatFormat")
                .replace("%color", matched.color())
                .replace("%chatPrefix", matched.prefix())
                .replace("%nick", p.getName())
                .replace("%donate", "%luckperms_prefix%")
                .replace("%message", cleanMessage);

        List<String> hoverLines = config.getStringList("Settings.messageHover");

        Component finalMessage = eColor.parse(PlaceholderAPI.setPlaceholders(p, message));

        if (!hoverLines.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\n");
            for (String line : hoverLines) {
                joiner.add(PlaceholderAPI.setPlaceholders(p, line));
            }
            finalMessage = finalMessage.hoverEvent(
                    HoverEvent.showText(eColor.parse(joiner.toString()))
            );
        }

        consumer.accept(finalMessage, matched);
    }

    @Override
    public void playerSendingMessage(Player p, Component message, ChatState state) {
        String check = PlainTextComponentSerializer.plainText().serialize(message);
        boolean isLocal = state.radius() > 0;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (isLocal) {
                if (p.getWorld() != target.getWorld()) continue;
                if (p.getLocation().distance(target.getLocation()) > state.radius()) continue;
            } else {
                if (!target.hasPermission(state.permission())) continue;
            }

            target.sendMessage(message);

            if (check.contains("@" + target.getName())) {
                beMentioned(p, target);
            }
        }
    }

    @Override
    public void beMentioned(Player sender, Player recipient) {
        recipient.sendMessage(Component.text("Вас упомянули: " + sender.getName()));
        recipient.playSound(recipient, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    @Override
    public void systemLogMessage(UUID id, String player, Timestamp timestamp, String message, String world, double x, double y, double z, String ip) {
        if (!config.getBoolean("Database.log", true)) return;
        repo.messageBatch(id, player, timestamp, message, world, x, y, z, ip);
    }

    @Override
    public void systemSendAutoBroadcast() {
        if (!config.getBoolean("AutoMessage.enable")) return;

        Map<String, AMessageState> groups = plugin.getBroadcastList();
        if (groups.isEmpty()) return;

        Map<String, Long> lastSend = new HashMap<>();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            for (Map.Entry<String, AMessageState> entry : groups.entrySet()) {
                String id = entry.getKey();
                AMessageState state = entry.getValue();

                long cooldownMs = state.cooldown() * 50L;
                long last = lastSend.getOrDefault(id, 0L);

                if (now - last < cooldownMs) continue;

                for (String msg : state.messagesList()) {
                    Bukkit.broadcast(Component.text(msg));
                }

                lastSend.put(id, now);
            }
        }, 0L, 20L);
    }

    public void startGlobalCooldownTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            cooldowns.values().forEach(map -> map.entrySet().removeIf(entry -> now >= entry.getValue()));
            cooldowns.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }, 20L, 20L);
    }

    private boolean handleCooldown(Player p, String chatId, ChatState state) {
        if (state.cooldown() <= 0) return true;

        UUID id = p.getUniqueId();
        long now = System.currentTimeMillis();

        ConcurrentHashMap<String, Long> playerMap = cooldowns.computeIfAbsent(id, k -> new ConcurrentHashMap<>());

        long expire = playerMap.getOrDefault(chatId, 0L);

        if (now < expire) {
            long left = (expire - now) / 1000;
            long displayTime = left <= 0 ? 1 : left;

            p.sendMessage(eColor.parse(config.getString("Messages.cooldownDown", "&cПодождите %sec сек.")
                    .replace("%sec", String.valueOf(displayTime))));
            return false;
        }

        playerMap.put(chatId, now + (state.cooldown() * 1000L));
        return true;
    }

    public boolean isAdd(String message) {
        Set<String> blocked = plugin.getBlockedAdd();
        String[] words = message.toLowerCase().split("\\s+");

        for (String word : words) {
            if (blocked.contains(word)) {
                return true;
            }
        }

        return false;
    }

    public boolean isWord(String message) {
        Set<String> blocked = plugin.getBlockedWord();
        String[] words = message.toLowerCase().split("\\s+");

        for (String word : words) {
            if (blocked.contains(word)) {
                return true;
            }
        }

        return false;
    }

    public void clearData(UUID uuid) {
        cooldowns.remove(uuid);
    }
}