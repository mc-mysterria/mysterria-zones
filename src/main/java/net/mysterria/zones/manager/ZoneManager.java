package net.mysterria.zones.manager;

import net.mysterria.zones.MysterriaZones;
import net.mysterria.zones.model.Zone;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ZoneManager {
    private final MysterriaZones plugin;
    private final Map<String, Zone> zones;
    private final File zonesFile;
    private FileConfiguration zonesConfig;
    private final Logger logger;

    public ZoneManager(MysterriaZones plugin) {
        this.plugin = plugin;
        this.zones = new HashMap<>();
        this.logger = plugin.getLogger();
        this.zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        loadZones();
    }

    public void loadZones() {
        zones.clear();
        
        if (!zonesFile.exists()) {
            try {
                zonesFile.createNewFile();
                zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
                zonesConfig.set("zones", new HashMap<String, Object>());
                zonesConfig.save(zonesFile);
                logger.info("Created new zones.yml file");
            } catch (IOException e) {
                logger.severe("Failed to create zones.yml: " + e.getMessage());
                return;
            }
        } else {
            zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
        }

        ConfigurationSection zonesSection = zonesConfig.getConfigurationSection("zones");
        if (zonesSection != null) {
            for (String zoneName : zonesSection.getKeys(false)) {
                try {
                    ConfigurationSection zoneSection = zonesSection.getConfigurationSection(zoneName);
                    if (zoneSection != null) {
                        Map<String, Object> zoneData = new HashMap<>();
                        for (String key : zoneSection.getKeys(true)) {
                            zoneData.put(key, zoneSection.get(key));
                        }
                        Zone zone = new Zone(zoneData);
                        zones.put(zoneName, zone);
                        logger.info("Loaded zone: " + zoneName);
                    }
                } catch (Exception e) {
                    logger.warning("Failed to load zone " + zoneName + ": " + e.getMessage());
                }
            }
        }
        
        logger.info("Loaded " + zones.size() + " zones");
    }

    public void saveZones() {
        try {
            if (zonesConfig == null) {
                zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
            }
            
            zonesConfig.set("zones", null);
            ConfigurationSection zonesSection = zonesConfig.createSection("zones");
            
            for (Map.Entry<String, Zone> entry : zones.entrySet()) {
                ConfigurationSection zoneSection = zonesSection.createSection(entry.getKey());
                Map<String, Object> serialized = entry.getValue().serialize();
                for (Map.Entry<String, Object> dataEntry : serialized.entrySet()) {
                    zoneSection.set(dataEntry.getKey(), dataEntry.getValue());
                }
            }
            
            zonesConfig.save(zonesFile);
            logger.info("Saved " + zones.size() + " zones to zones.yml");
        } catch (IOException e) {
            logger.severe("Failed to save zones.yml: " + e.getMessage());
        }
    }

    public void createZone(String name, Location point1, Location point2) {
        Zone zone = new Zone(name, point1, point2);
        zones.put(name, zone);
        saveZones();
    }

    public boolean deleteZone(String name) {
        if (zones.remove(name) != null) {
            saveZones();
            return true;
        }
        return false;
    }

    public Zone getZone(String name) {
        return zones.get(name);
    }

    public Collection<Zone> getAllZones() {
        return zones.values();
    }

    public Set<String> getZoneNames() {
        return zones.keySet();
    }

    public List<Zone> getZonesAtLocation(Location location) {
        List<Zone> foundZones = new ArrayList<>();
        for (Zone zone : zones.values()) {
            if (zone.contains(location)) {
                foundZones.add(zone);
            }
        }
        foundZones.sort((z1, z2) -> Integer.compare(z2.getPriority(), z1.getPriority()));
        return foundZones;
    }

    public Zone getHighestPriorityZone(Location location) {
        return getZonesAtLocation(location).stream()
                .findFirst()
                .orElse(null);
    }

    public boolean hasZone(String name) {
        return zones.containsKey(name);
    }

    public void updateZone(Zone zone) {
        zones.put(zone.getName(), zone);
        saveZones();
    }
}