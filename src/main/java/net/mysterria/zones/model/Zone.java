package net.mysterria.zones.model;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@Data
public class Zone implements ConfigurationSerializable {
    private String name;
    private String displayName;
    private String worldName;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;
    private String enterMessage;
    private String exitMessage;
    private boolean protection;
    private int priority;

    public Zone(String name, Location point1, Location point2) {
        this.name = name;
        this.displayName = name; // Default to same as internal name
        this.worldName = point1.getWorld().getName();
        this.minX = Math.min(point1.getBlockX(), point2.getBlockX());
        this.minY = Math.min(point1.getBlockY(), point2.getBlockY());
        this.minZ = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.maxX = Math.max(point1.getBlockX(), point2.getBlockX());
        this.maxY = Math.max(point1.getBlockY(), point2.getBlockY());
        this.maxZ = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.enterMessage = "&aWelcome to &e" + displayName + "&a!";
        this.exitMessage = "&cYou left &e" + displayName + "&c!";
        this.protection = true;
        this.priority = 1;
    }

    public Zone(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.displayName = (String) map.getOrDefault("displayName", name);
        this.worldName = (String) map.get("world");
        this.minX = (Integer) map.get("minX");
        this.minY = (Integer) map.get("minY");
        this.minZ = (Integer) map.get("minZ");
        this.maxX = (Integer) map.get("maxX");
        this.maxY = (Integer) map.get("maxY");
        this.maxZ = (Integer) map.get("maxZ");
        this.enterMessage = (String) map.getOrDefault("enterMessage", "&aWelcome to &e" + displayName + "&a!");
        this.exitMessage = (String) map.getOrDefault("exitMessage", "&cYou left &e" + displayName + "&c!");
        this.protection = (Boolean) map.getOrDefault("protection", true);
        this.priority = (Integer) map.getOrDefault("priority", 1);
    }

    public boolean contains(Location location) {
        if (location.getWorld() == null || !location.getWorld().getName().equals(worldName)) {
            return false;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public Component getEnterComponent() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(enterMessage);
    }

    public Component getExitComponent() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(exitMessage);
    }

    public Location getMinPoint() {
        return new Location(Bukkit.getWorld(worldName), minX, minY, minZ);
    }

    public Location getMaxPoint() {
        return new Location(Bukkit.getWorld(worldName), maxX, maxY, maxZ);
    }

    public Component getDisplayNameComponent() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("displayName", displayName);
        map.put("world", worldName);
        map.put("minX", minX);
        map.put("minY", minY);
        map.put("minZ", minZ);
        map.put("maxX", maxX);
        map.put("maxY", maxY);
        map.put("maxZ", maxZ);
        map.put("enterMessage", enterMessage);
        map.put("exitMessage", exitMessage);
        map.put("protection", protection);
        map.put("priority", priority);
        return map;
    }

    public static Zone deserialize(Map<String, Object> map) {
        return new Zone(map);
    }
}