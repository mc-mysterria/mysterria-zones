package net.mysterria.zones;

import net.mysterria.zones.commands.ZoneCommand;
import net.mysterria.zones.listeners.SecureZoneListener;
import net.mysterria.zones.manager.ZoneManager;
import net.mysterria.zones.service.ZoneTrackingService;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MysterriaZones extends JavaPlugin implements Listener {

    @Getter
    private static MysterriaZones instance;

    private ZoneManager zoneManager;
    private ZoneTrackingService zoneTrackingService;

    @Nullable
    private Location point1 = null;
    @Nullable
    private Location point2 = null;
    private boolean areaDefined = false;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("GodRefuge plugin enabled!");
        
        zoneManager = new ZoneManager(this);
        zoneTrackingService = new ZoneTrackingService(this);
        
        getServer().getPluginManager().registerEvents(new SecureZoneListener(), this);
        
        if (getServer().getPluginCommand("zone") != null) {
            ZoneCommand zoneCommand = new ZoneCommand(this);
            getServer().getPluginCommand("zone").setExecutor(zoneCommand);
            getServer().getPluginCommand("zone").setTabCompleter(zoneCommand);
        }
        
        zoneTrackingService.startTracking();
        
        loadConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("GodRefuge plugin disabled!");
        
        if (zoneTrackingService != null) {
            zoneTrackingService.stopTracking();
        }
        
        if (zoneManager != null) {
            zoneManager.saveZones();
        }
        
        saveConfigData();
    }

    public void clearProtectedArea() {
        point1 = null;
        point2 = null;
        areaDefined = false;
        saveConfigData();
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();

        if (config.contains("point1") && config.contains("point2")) {
            try {
                point1 = deserializeLocation(config.getConfigurationSection("point1"));
                point2 = deserializeLocation(config.getConfigurationSection("point2"));
                areaDefined = true;
                getLogger().info("Loaded GodRefuge area from config.");
            } catch (IllegalArgumentException | NullPointerException e) {
                getLogger().warning("Failed to load GodRefuge area from config. Points might be invalid or corrupted.");
                point1 = null;
                point2 = null;
                areaDefined = false;
            }
        } else {
            getLogger().info("No GodRefuge area defined in config yet.");
            point1 = null;
            point2 = null;
            areaDefined = false;
        }
    }

    private void saveConfigData() {
        FileConfiguration config = getConfig();

        if (point1 != null && point2 != null) {
            config.set("point1", serializeLocation(point1));
            config.set("point2", serializeLocation(point2));
            getLogger().info("Saved GodRefuge area to config.");
        } else {
            config.set("point1", null);
            config.set("point2", null);
            areaDefined = false;
            getLogger().info("Cleared GodRefuge area from config.");
        }
        saveConfig();
    }

    private Map<String, Object> serializeLocation(Location location) {
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("world", location.getWorld().getName());
        locationMap.put("x", location.getX());
        locationMap.put("y", location.getY());
        locationMap.put("z", location.getZ());
        return locationMap;
    }

    private Location deserializeLocation(org.bukkit.configuration.ConfigurationSection section) {
        if (section == null) return null;
        String worldName = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        if (worldName == null) throw new IllegalArgumentException("World name is missing or invalid in config.");
        return new Location(getServer().getWorld(worldName), x, y, z);
    }
}