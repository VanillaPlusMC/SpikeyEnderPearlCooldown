package me.spikey.spikeyenderpearlcooldown;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin implements Listener {

    public static Flag pearlFlag;

    public static HashMap<UUID, Timestamp> cooldown;

    public static String message;

    public static long delay;

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            StateFlag flag = new StateFlag("enderPearlCooldown", false);
            registry.register(flag);
            pearlFlag = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("enderPearlCooldown");
            if (existing instanceof StateFlag) {
                pearlFlag = (StateFlag) existing;
            } else {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cooldown = Maps.newHashMap();

        delay = getConfig().getLong("delay");
        message = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message"));

        Bukkit.getPluginManager().registerEvents(this, this);


    }

    public void updateCooldown(UUID uuid) {
        cooldown.put(uuid, Timestamp.from(Instant.now()));
    }

    public boolean isOnCooldown(UUID uuid) {
        if (!cooldown.containsKey(uuid)) return false;
        long dif = Timestamp.from(Instant.now()).getTime() - cooldown.get(uuid).getTime();
        dif = (delay * 1000) - dif;
        return dif > 0;
    }

    public static long getRemainingSeconds(UUID uuid) {
        if (!cooldown.containsKey(uuid)) return -1;
        long dif = Timestamp.from(Instant.now()).getTime() - cooldown.get(uuid).getTime();
        return delay - TimeUnit.MILLISECONDS.toSeconds(dif);
    }

    @EventHandler
    public void pearl(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getPlayer().getItemInHand() == null || !event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.ENDER_PEARL)) return;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(event.getPlayer().getLocation()));

        for (ProtectedRegion region : set.getRegions()) {
            if (!(region.getFlags().get(pearlFlag) == StateFlag.State.ALLOW)) continue;
            if (isOnCooldown(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("enderpearlcooldown.bypass")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(message.formatted(CF.getCoolDownTimeInDays(event.getPlayer().getUniqueId(), 0)));
                return;
            }
            updateCooldown(event.getPlayer().getUniqueId());
        }

    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (isOnCooldown(event.getPlayer().getUniqueId())) return;
        cooldown.remove(event.getPlayer().getUniqueId());
    }
}
