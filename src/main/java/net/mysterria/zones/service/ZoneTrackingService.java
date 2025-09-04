package net.mysterria.zones.service;

import net.mysterria.zones.MysterriaZones;
import net.mysterria.zones.model.Zone;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZoneTrackingService {
    private final MysterriaZones plugin;
    private final Map<UUID, Zone> playerCurrentZones;
    private BukkitRunnable trackingTask;

    public ZoneTrackingService(MysterriaZones plugin) {
        this.plugin = plugin;
        this.playerCurrentZones = new HashMap<>();
    }

    public void startTracking() {
        if (trackingTask != null && !trackingTask.isCancelled()) {
            trackingTask.cancel();
        }

        trackingTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkPlayerZoneChange(player);
                }
            }
        };

        trackingTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void stopTracking() {
        if (trackingTask != null && !trackingTask.isCancelled()) {
            trackingTask.cancel();
        }
        playerCurrentZones.clear();
    }

    private void checkPlayerZoneChange(Player player) {
        Zone currentZone = plugin.getZoneManager().getHighestPriorityZone(player.getLocation());
        Zone previousZone = playerCurrentZones.get(player.getUniqueId());

        if (currentZone != previousZone) {
            if (previousZone != null) {
                onZoneExit(player, previousZone);
            }
            
            if (currentZone != null) {
                onZoneEnter(player, currentZone);
                playerCurrentZones.put(player.getUniqueId(), currentZone);
            } else {
                playerCurrentZones.remove(player.getUniqueId());
            }
        }
    }

    private void onZoneEnter(Player player, Zone zone) {
        Component enterMessage = zone.getEnterComponent();
        Component displayName = zone.getDisplayNameComponent();
        
        player.showTitle(Title.title(
                displayName,
                enterMessage,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))
        ));

        player.playSound(Sound.sound(
                org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME,
                Sound.Source.BLOCK,
                0.8f,
                1.2f
        ));

        player.sendMessage(enterMessage);
    }

    private void onZoneExit(Player player, Zone zone) {
        Component exitMessage = zone.getExitComponent();
        
        player.showTitle(Title.title(
                Component.text(""),
                exitMessage,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
        ));

        player.playSound(Sound.sound(
                org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS,
                Sound.Source.BLOCK,
                0.6f,
                0.8f
        ));

        player.sendMessage(exitMessage);
    }

    public void removePlayer(Player player) {
        playerCurrentZones.remove(player.getUniqueId());
    }

    public Zone getCurrentZone(Player player) {
        return playerCurrentZones.get(player.getUniqueId());
    }
}