package net.mysterria.zones.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.join.Join;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.zones.MysterriaZones;
import net.mysterria.zones.model.Zone;
import org.bukkit.entity.Player;

@Command(name = "zone")
@Permission("myzones.zone")
public class ZoneConfigCommands {

    private final MysterriaZones plugin;

    public ZoneConfigCommands(MysterriaZones plugin) {
        this.plugin = plugin;
    }

    @Execute(name = "setenter")
    public void setEnter(@Context Player player, @Arg String zoneName, @Join String message) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);

        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        zone.setEnterMessage(message);
        plugin.getZoneManager().saveZone(zone);
        player.sendMessage(Component.text("Enter message updated for zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    @Execute(name = "setexit")
    public void setExit(@Context Player player, @Arg String zoneName, @Join String message) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);

        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        zone.setExitMessage(message);
        plugin.getZoneManager().saveZone(zone);
        player.sendMessage(Component.text("Exit message updated for zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    @Execute(name = "setdisplay")
    public void setDisplay(@Context Player player, @Arg String zoneName, @Join String displayName) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);

        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        zone.setDisplayName(displayName);
        plugin.getZoneManager().updateZone(zone);
        player.sendMessage(Component.text("Display name updated to '" + displayName + "' for zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    @Execute(name = "toggle")
    public void toggle(@Context Player player, @Arg String zoneName) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);

        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        zone.setProtection(!zone.isProtection());
        plugin.getZoneManager().updateZone(zone);

        String status = zone.isProtection() ? "enabled" : "disabled";
        player.sendMessage(Component.text("Protection " + status + " for zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    @Execute(name = "priority")
    public void priority(@Context Player player, @Arg String zoneName, @Arg int priority) {
        Zone zone = plugin.getZoneManager().getZone(zoneName);

        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        zone.setPriority(priority);
        plugin.getZoneManager().updateZone(zone);
        player.sendMessage(Component.text("Priority set to " + priority + " for zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }
}
