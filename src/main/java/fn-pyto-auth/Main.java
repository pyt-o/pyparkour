package fn_pyto_parkour;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Main extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private final Map<UUID, Long> timers = new HashMap<>();
    private final Map<UUID, Location> lastCheckpoint = new HashMap<>();
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        statsManager = new StatsManager(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ParkourExpansion(this).register();
        }

        getCommand("parkour").setExecutor(this);
        getCommand("parkour").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : timers.keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        long timeMillis = System.currentTimeMillis() - timers.get(uuid);
                        String msg = getConfig().getString("messages.actionbar-timer", "&6Czas: &e%time%")
                                .replace("%time%", formatTime(timeMillis));
                        p.sendActionBar(Component.text(color(msg)));
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);
        getLogger().info("PyParkour 2.3 (Permissions Update) wlaczony!");
    }

    public StatsManager getStatsManager() { return statsManager; }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            
            if (sender.hasPermission("pyparkour.use")) {
                suggestions.add("help");
                suggestions.add("wyjscie");
            }
            
            if (sender.hasPermission("pyparkour.admin")) {
                suggestions.add("setup");
                suggestions.add("setvoid");
                suggestions.add("reload");
            }
            return suggestions;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        
        if (!p.hasPermission("pyparkour.use")) {
            p.sendMessage(color("&cBrak uprawnien do uzywania parkoura!"));
            return true;
        }

        
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(p);
            return true;
        }

        
        if (args[0].equalsIgnoreCase("wyjscie") || args[0].equalsIgnoreCase("leave")) {
            if (timers.containsKey(p.getUniqueId())) {
                timers.remove(p.getUniqueId());
                lastCheckpoint.remove(p.getUniqueId());
                p.sendMessage(color(getConfig().getString("messages.leave", "&cPrzerwano.")));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                p.teleport(p.getWorld().getSpawnLocation());
            } else {
                p.sendMessage(color(getConfig().getString("messages.not-playing", "&cNie grasz!")));
            }
            return true;
        }

       
        if (!p.hasPermission("pyparkour.admin")) {
            p.sendMessage(color("&cKomenda dostepna tylko dla administratorow!"));
            return true;
        }

        if (args[0].equalsIgnoreCase("setup") || args[0].equalsIgnoreCase("set")) {
            p.getInventory().addItem(
                    createItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, "&a&lSTART &7(Postaw mnie)"),
                    createItem(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, "&c&lMETA &7(Postaw mnie)"),
                    createItem(Material.STONE_PRESSURE_PLATE, "&e&lCHECKPOINT &7(Postaw mnie)")
            );
            p.sendMessage(color("&aOtrzymales narzedzia!"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            statsManager.load();
            p.sendMessage(color("&aPrzeladowano!"));
            return true;
        }

        if (args[0].equalsIgnoreCase("setvoid")) {
            getConfig().set("void-y", (int) p.getLocation().getY());
            saveConfig();
            p.sendMessage(color("&cUstawiono void na Y=" + (int)p.getLocation().getY()));
        }
        return true;
    }

    

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL || event.getClickedBlock() == null) return;
        Player p = event.getPlayer();

        
        if (!p.hasPermission("pyparkour.use")) return;

        Location blockLoc = event.getClickedBlock().getLocation();

        if (compareLoc(blockLoc, getConfig().getLocation("data.start"))) {
            if (!timers.containsKey(p.getUniqueId())) {
                timers.put(p.getUniqueId(), System.currentTimeMillis());
                lastCheckpoint.put(p.getUniqueId(), p.getLocation());
                p.sendMessage(color(getConfig().getString("messages.start", "&aStart!")));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            }
        } else if (compareLoc(blockLoc, getConfig().getLocation("data.end"))) {
            if (timers.containsKey(p.getUniqueId())) {
                long totalTime = System.currentTimeMillis() - timers.get(p.getUniqueId());
                stopParkour(p, totalTime);
            }
        } else {
            List<?> checkpoints = getConfig().getList("data.checkpoints");
            if (checkpoints != null) {
                for (Object obj : checkpoints) {
                    if (obj instanceof Location) {
                        if (compareLoc(blockLoc, (Location) obj)) {
                            lastCheckpoint.put(p.getUniqueId(), p.getLocation());
                            p.sendActionBar(Component.text(color(getConfig().getString("messages.checkpoint", "&aCheck!"))));
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                            break;
                        }
                    }
                }
            }
        }
    }

   
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        String name = item.getItemMeta().getDisplayName();

        
        if ((name.contains("START") || name.contains("META") || name.contains("CHECKPOINT"))) {
            if (!p.hasPermission("pyparkour.admin")) {
                p.sendMessage(color("&cBrak uprawnien do tworzenia parkoura!"));
                event.setCancelled(true);
                return;
            }

            Location loc = event.getBlock().getLocation();
            if (name.contains("START")) {
                getConfig().set("data.start", loc); saveConfig(); p.sendMessage(color("&aUstawiono START"));
            } else if (name.contains("META")) {
                getConfig().set("data.end", loc); saveConfig(); p.sendMessage(color("&cUstawiono METE"));
            } else if (name.contains("CHECKPOINT")) {
                List<Location> list = (List<Location>) getConfig().getList("data.checkpoints");
                if (list == null) list = new ArrayList<>();
                list.add(loc);
                getConfig().set("data.checkpoints", list); saveConfig(); p.sendMessage(color("&eDodano CHECKPOINT"));
            }
        }
    }

   
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (!timers.containsKey(p.getUniqueId())) return;
        if (p.getLocation().getY() < getConfig().getInt("void-y")) {
            Location safe = lastCheckpoint.get(p.getUniqueId());
            if (safe != null) {
                p.teleport(safe);
                p.sendMessage(color(getConfig().getString("messages.fail", "&cSpadles!")));
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            } else {
                timers.remove(p.getUniqueId());
                p.teleport(p.getWorld().getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        timers.remove(event.getPlayer().getUniqueId());
        lastCheckpoint.remove(event.getPlayer().getUniqueId());
    }

    private void stopParkour(Player p, long time) {
        timers.remove(p.getUniqueId());
        lastCheckpoint.remove(p.getUniqueId());
        statsManager.saveScore(p, time);
        String formatted = formatTime(time);
        p.sendMessage(color(getConfig().getString("messages.finish", "&aKoniec!").replace("%time%", formatted)));
        String broadcast = getConfig().getString("messages.broadcast").replace("%player%", p.getName()).replace("%time%", formatted);
        Bukkit.broadcast(Component.text(color(broadcast)));
        playSound(p, "finish");
        spawnFirework(p.getLocation());
    }

    private void sendHelp(Player p) {
        String header = getConfig().getString("messages.help-header", "&6Pomoc");
        p.sendMessage(color(header));
        for (String line : getConfig().getStringList("messages.help-player")) p.sendMessage(color(line));
        if (p.hasPermission("pyparkour.admin")) {
            for (String line : getConfig().getStringList("messages.help-admin")) p.sendMessage(color(line));
        }
    }

    private boolean compareLoc(Location l1, Location l2) {
        if (l1 == null || l2 == null) return false;
        return l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
    }

    private String formatTime(long millis) {
        long sec = millis / 1000;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", sec / 60, sec % 60, ms);
    }

    private void spawnFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.ORANGE).with(FireworkEffect.Type.BALL_LARGE).build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        item.setItemMeta(meta);
        return item;
    }

    private void playSound(Player p, String key) {
        try {
            String soundName = getConfig().getString("sounds." + key);
            if (soundName == null) return;
            p.playSound(p.getLocation(), Sound.valueOf(soundName), 1f, 1f);
        } catch (Exception e) {}
    }

    private String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
