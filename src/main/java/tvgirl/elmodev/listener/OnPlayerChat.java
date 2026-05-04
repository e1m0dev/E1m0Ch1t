package tvgirl.elmodev.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tvgirl.elmodev.E1m0Ch1t;
import tvgirl.elmodev.color.E1m0Color;
import tvgirl.elmodev.service.PlayerService;

import java.sql.Timestamp;
import java.util.UUID;

public class OnPlayerChat implements Listener {
    private final E1m0Ch1t plugin;
    private final PlayerService service;
    private E1m0Color color = new E1m0Color();


    public OnPlayerChat(E1m0Ch1t plugin, PlayerService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        String msg = PlainTextComponentSerializer.plainText().serialize(e.message());

        if(!e.getPlayer().hasPermission(plugin.getConfig().getString("Permissions.bypassAdd"))) {
            if(service.isAdd(msg)) {
                e.getPlayer().sendMessage(color.parse(plugin.getConfig().getString("Messages.Errors.blockAdd")));
                e.setCancelled(true);
                return;
            }
        }

        if(!e.getPlayer().hasPermission(plugin.getConfig().getString("Permissions.bypassWord"))) {
            if(service.isWord(msg)) {
                e.getPlayer().sendMessage(color.parse(plugin.getConfig().getString("Messages.Errors.blockWord")));
                e.setCancelled(true);
                return;
            }
        }

        String p = e.getPlayer().getName();
        UUID id = e.getPlayer().getUniqueId();
        String w = e.getPlayer().getWorld().toString();
        Timestamp stamp = new Timestamp(System.currentTimeMillis());

        double x = e.getPlayer().getLocation().getX();
        double y = e.getPlayer().getLocation().getY();
        double z = e.getPlayer().getLocation().getZ();

        String ip = e.getPlayer().getAddress().toString();

        e.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            service.playerEnterMessage(e.getPlayer(), e.message(), (msh, state) ->
                    service.playerSendingMessage(e.getPlayer(), msh, state)
            );
        });

        service.systemLogMessage(id, p, stamp, msg, w, x, y, z, ip);
    }

}
