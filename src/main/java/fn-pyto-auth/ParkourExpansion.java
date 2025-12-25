package fn_pyto_parkour;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ParkourExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public ParkourExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "pyparkour"; }

    @Override
    public @NotNull String getAuthor() { return "Pyto"; }

    @Override
    public @NotNull String getVersion() { return "2.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.startsWith("top_name_")) {
            try {
                int index = Integer.parseInt(params.replace("top_name_", "")) - 1;
                return plugin.getStatsManager().getTopName(index);
            } catch (NumberFormatException e) { return "Error"; }
        }
        if (params.startsWith("top_time_")) {
            try {
                int index = Integer.parseInt(params.replace("top_time_", "")) - 1;
                return plugin.getStatsManager().getTopTime(index);
            } catch (NumberFormatException e) { return "Error"; }
        }
        return null;
    }
}