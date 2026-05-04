package tvgirl.elmodev.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import tvgirl.elmodev.service.PlayerService;

public class OnPlayersQuit implements Listener {

    private final PlayerService service;

    public OnPlayersQuit(PlayerService service) {
        this.service = service;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        service.clearData(e.getPlayer().getUniqueId());
    }
}
