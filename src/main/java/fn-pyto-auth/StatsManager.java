package fn_pyto_parkour;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class StatsManager {

    private final Main plugin;
    private File file;
    private YamlConfiguration config;
    private final List<Map.Entry<String, Long>> topList = new ArrayList<>();

    public StatsManager(Main plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "stats.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
        sortTop();
    }

    public void saveScore(Player p, long time) {
        String path = "stats." + p.getName();
        if (!config.contains(path) || config.getLong(path) > time) {
            config.set(path, time);
            try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
            sortTop();
        }
    }

    private void sortTop() {
        topList.clear();
        if (!config.isConfigurationSection("stats")) return;
        Map<String, Long> tempMap = new HashMap<>();
        for (String key : config.getConfigurationSection("stats").getKeys(false)) {
            tempMap.put(key, config.getLong("stats." + key));
        }
        topList.addAll(tempMap.entrySet());
        topList.sort(Map.Entry.comparingByValue());
    }

    public String getTopName(int index) {
        if (index >= topList.size()) return "---";
        return topList.get(index).getKey();
    }

    public String getTopTime(int index) {
        if (index >= topList.size()) return "---";
        long millis = topList.get(index).getValue();
        long sec = millis / 1000;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", sec / 60, sec % 60, ms);
    }
}