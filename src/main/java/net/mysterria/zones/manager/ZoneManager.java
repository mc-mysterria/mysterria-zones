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
    private final File zonesFolder;
    private final Logger logger;

    public ZoneManager(MysterriaZones plugin) {
        this.plugin = plugin;
        this.zones = new HashMap<>();
        this.logger = plugin.getLogger();
        this.zonesFolder = new File(plugin.getDataFolder(), "zones");

        if (!zonesFolder.exists()) {
            zonesFolder.mkdirs();
        }

        loadZones();
    }

    public void loadZones() {
        zones.clear();
        File[] zoneFiles = zonesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (zoneFiles == null) {
            logger.info("No zones to load.");
            return;
        }

        for (File zoneFile : zoneFiles) {
            try {
                String zoneName = zoneFile.getName().replace(".yml", "");
                FileConfiguration zoneConfig = YamlConfiguration.loadConfiguration(zoneFile);
                Map<String, Object> zoneData = new HashMap<>();
                for (String key : zoneConfig.getKeys(true)) {
                    zoneData.put(key, zoneConfig.get(key));
                }
                Zone zone = new Zone(zoneData);
                zones.put(zoneName, zone);
                logger.info("Loaded zone: " + zoneName);
            } catch (Exception e) {
                logger.warning("Failed to load zone from " + zoneFile.getName() + ": " + e.getMessage());
            }
        }
        logger.info("Loaded " + zones.size() + " zones.");
    }

    public void saveZone(Zone zone) {
        File zoneFile = new File(zonesFolder, zone.getName() + ".yml");
        FileConfiguration zoneConfig = new YamlConfiguration();
        Map<String, Object> serialized = zone.serialize();
        for (Map.Entry<String, Object> entry : serialized.entrySet()) {
            zoneConfig.set(entry.getKey(), entry.getValue());
        }
        try {
            zoneConfig.save(zoneFile);
            logger.info("Saved zone: " + zone.getName());
        } catch (IOException e) {
            logger.severe("Failed to save zone " + zone.getName() + ": " + e.getMessage());
        }
    }

    public void createZone(String name, Location point1, Location point2) {
        Zone zone = new Zone(name, point1, point2);
        zones.put(name, zone);
        saveZone(zone);
    }

    public boolean deleteZone(String name) {
        if (zones.remove(name) != null) {
            File zoneFile = new File(zonesFolder, name + ".yml");
            if (zoneFile.exists()) {
                zoneFile.delete();
            }
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