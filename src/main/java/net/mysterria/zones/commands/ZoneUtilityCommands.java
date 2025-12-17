package net.mysterria.zones.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.zones.MysterriaZones;
import net.mysterria.zones.model.Zone;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

@Command(name = "zone")
@Permission("myzones.zone")
public class ZoneUtilityCommands {

    private final MysterriaZones plugin;

    public ZoneUtilityCommands(MysterriaZones plugin) {
        this.plugin = plugin;
    }

    @Execute(name = "info")
    public void info(@Context Player player, @Arg String zoneName) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);

        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("=== Zone Info: " + zoneName + " ===", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Display Name: " + zone.getDisplayName(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("World: " + zone.getWorldName(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Min Point: " + zone.getMinX() + ", " + zone.getMinY() + ", " + zone.getMinZ(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Max Point: " + zone.getMaxX() + ", " + zone.getMaxY() + ", " + zone.getMaxZ(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Protection: " + (zone.isProtection() ? "Enabled" : "Disabled"), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Priority: " + zone.getPriority(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Enter Message: " + zone.getEnterMessage(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Exit Message: " + zone.getExitMessage(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Banished Players: " + zone.getBanishedPlayers().size(), NamedTextColor.YELLOW));
    }

    @Execute(name = "visualize")
    public void visualize(@Context Player player, @Arg String zoneName) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);

        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        visualizeZone(player, zone);
        player.sendMessage(Component.text("Visualizing zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    private void visualizeZone(Player player, Zone zone) {
        for (int x = zone.getMinX(); x <= zone.getMaxX(); x += 4) {
            for (int y = zone.getMinY(); y <= zone.getMaxY(); y += 4) {
                for (int z = zone.getMinZ(); z <= zone.getMaxZ(); z += 4) {
                    if (x == zone.getMinX() || x == zone.getMaxX() ||
                        y == zone.getMinY() || y == zone.getMaxY() ||
                        z == zone.getMinZ() || z == zone.getMaxZ()) {
                        player.spawnParticle(Particle.DUST, x + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.LIME, 1.5f));
                    }
                }
            }
        }
    }
}
