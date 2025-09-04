package net.mysterria.zones.commands;

import net.mysterria.zones.MysterriaZones;
import net.mysterria.zones.model.Zone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZoneCommand implements TabExecutor {

    private final MysterriaZones plugin;

    public ZoneCommand(MysterriaZones plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        // Handle legacy command aliases
        if (label.equalsIgnoreCase("godrefuge") || label.equalsIgnoreCase("gr")) {
            if (args.length == 0) {
                sendLegacyUsage(player);
                return true;
            }
            // Convert legacy commands to new format
            String legacyAction = args[0].toLowerCase();
            switch (legacyAction) {
                case "pos1" -> handlePos1(player);
                case "pos2" -> handlePos2(player);
                case "define" -> {
                    if (args.length > 1) {
                        handleCreate(player, new String[]{"create", args[1]});
                    } else {
                        handleCreate(player, new String[]{"create", "main"});
                    }
                }
                case "clear" -> handleClearLegacy(player);
                default -> sendLegacyUsage(player);
            }
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String action = args[0].toLowerCase();
        
        switch (action) {
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "list" -> handleList(player);
            case "info" -> handleInfo(player, args);
            case "setenter" -> handleSetEnter(player, args);
            case "setexit" -> handleSetExit(player, args);
            case "setdisplay" -> handleSetDisplay(player, args);
            case "toggle" -> handleToggle(player, args);
            case "priority" -> handlePriority(player, args);
            case "visualize" -> handleVisualize(player, args);
            case "pos1" -> handlePos1(player);
            case "pos2" -> handlePos2(player);
            default -> sendUsage(player);
        }
        
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /zone create <name>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
        
        if (plugin.getZoneManager().hasZone(zoneName)) {
            player.sendMessage(Component.text("Zone with name '" + zoneName + "' already exists!", NamedTextColor.RED));
            return;
        }

        Location pos1 = plugin.getPoint1();
        Location pos2 = plugin.getPoint2();
        
        if (pos1 == null || pos2 == null) {
            player.sendMessage(Component.text("Please set both positions first using /zone pos1 and /zone pos2", NamedTextColor.RED));
            return;
        }
        
        plugin.getZoneManager().createZone(zoneName, pos1, pos2);
        player.sendMessage(Component.text("Zone '" + zoneName + "' created successfully!", NamedTextColor.GREEN));
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /zone delete <name>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
        
        if (plugin.getZoneManager().deleteZone(zoneName)) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' deleted successfully!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
        }
    }

    private void handleList(Player player) {
        var zones = plugin.getZoneManager().getZoneNames();
        
        if (zones.isEmpty()) {
            player.sendMessage(Component.text("No zones found.", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("Protected Zones:", NamedTextColor.AQUA));
        for (String zoneName : zones) {
            Zone zone = plugin.getZoneManager().getZone(zoneName);
            String status = zone.isProtection() ? "§a✓" : "§c✗";
            player.sendMessage(Component.text("- " + status + " " + zoneName + " (Priority: " + zone.getPriority() + ")", NamedTextColor.WHITE));
        }
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /zone info <name>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
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
    }

    private void handleSetEnter(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /zone setenter <name> <message>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        zone.setEnterMessage(message);
        plugin.getZoneManager().updateZone(zone);
        
        player.sendMessage(Component.text("Enter message updated for zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    private void handleSetExit(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /zone setexit <name> <message>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        zone.setExitMessage(message);
        plugin.getZoneManager().updateZone(zone);
        
        player.sendMessage(Component.text("Exit message updated for zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    private void handleSetDisplay(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /zone setdisplay <name> <display_name>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        zone.setDisplayName(displayName);
        plugin.getZoneManager().updateZone(zone);
        
        player.sendMessage(Component.text("Display name updated to '" + displayName + "' for zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    private void handleToggle(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /zone toggle <name>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
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

    private void handlePriority(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(Component.text("Usage: /zone priority <name> <priority>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        try {
            int priority = Integer.parseInt(args[2]);
            zone.setPriority(priority);
            plugin.getZoneManager().updateZone(zone);
            player.sendMessage(Component.text("Priority set to " + priority + " for zone '" + zoneName + "'!", NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid priority number!", NamedTextColor.RED));
        }
    }

    private void handleVisualize(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /zone visualize <name>", NamedTextColor.RED));
            return;
        }

        String zoneName = args[1];
        Zone zone = plugin.getZoneManager().getZone(zoneName);
        
        if (zone == null) {
            player.sendMessage(Component.text("Zone '" + zoneName + "' not found!", NamedTextColor.RED));
            return;
        }

        visualizeZone(player, zone);
        player.sendMessage(Component.text("Visualizing zone '" + zoneName + "'!", NamedTextColor.GREEN));
    }

    private void handlePos1(Player player) {
        plugin.setPoint1(player.getLocation());
        player.sendMessage(Component.text("First position set to: " + formatLocation(player.getLocation()), NamedTextColor.GREEN));
    }

    private void handlePos2(Player player) {
        plugin.setPoint2(player.getLocation());
        player.sendMessage(Component.text("Second position set to: " + formatLocation(player.getLocation()), NamedTextColor.GREEN));
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

    private void handleClearLegacy(Player player) {
        if (plugin.getZoneManager().hasZone("main")) {
            plugin.getZoneManager().deleteZone("main");
            player.sendMessage(Component.text("Legacy protected area cleared.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("No legacy protected area found.", NamedTextColor.YELLOW));
        }
    }

    private void sendLegacyUsage(Player player) {
        player.sendMessage(Component.text("=== Legacy GodRefuge Commands ===", NamedTextColor.AQUA));
        player.sendMessage(Component.text("/godrefuge pos1 - Set first position", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/godrefuge pos2 - Set second position", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/godrefuge define [name] - Create zone (default: 'main')", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/godrefuge clear - Clear legacy zone", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("", NamedTextColor.WHITE));
        player.sendMessage(Component.text("Use /zone for full zone management features!", NamedTextColor.GREEN));
    }

    private void sendUsage(Player player) {
        player.sendMessage(Component.text("=== Zone Commands ===", NamedTextColor.AQUA));
        player.sendMessage(Component.text("/zone pos1 - Set first position", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone pos2 - Set second position", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone create <name> - Create a new zone", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone delete <name> - Delete a zone", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone list - List all zones", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone info <name> - Show zone information", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone setenter <name> <message> - Set enter message", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone setexit <name> <message> - Set exit message", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone setdisplay <name> <display_name> - Set display name", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone toggle <name> - Toggle zone protection", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone priority <name> <number> - Set zone priority", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/zone visualize <name> - Visualize zone borders", NamedTextColor.YELLOW));
    }

    private String formatLocation(Location loc) {
        return String.format("(%.1f, %.1f, %.1f) in %s", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> completions = Arrays.asList(
                    "create", "delete", "list", "info", "setenter", "setexit", "setdisplay",
                    "toggle", "priority", "visualize", "pos1", "pos2"
            );
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String action = args[0].toLowerCase();
            if (action.equals("delete") || action.equals("info") || action.equals("setenter") || 
                action.equals("setexit") || action.equals("setdisplay") || action.equals("toggle") || 
                action.equals("priority") || action.equals("visualize")) {
                
                return plugin.getZoneManager().getZoneNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }

        return new ArrayList<>();
    }
}