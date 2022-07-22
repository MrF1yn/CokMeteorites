// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Meteorites;

import org.bukkit.Material;
import org.bukkit.World;
import java.util.Iterator;
import org.bukkit.plugin.Plugin;
import java.util.Collection;
import me.olipulse.meteoritespro.EventListeners.EventListenerClass;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import me.olipulse.meteoritespro.MeteoritesPro;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import java.util.List;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.configuration.ConfigurationSection;

public class Meteorite
{
    private MeteoriteCore core;
    private MeteoriteLayer outerLayer;
    private MeteoriteLayer innerLayer;
    private ConfigurationSection config;
    BukkitScheduler scheduler;
    private int schedulerId;
    List<Block> blockList;
    
    public Meteorite(final MeteoriteCore core, final MeteoriteLayer outerLayer, final ConfigurationSection config) {
        this.scheduler = Bukkit.getScheduler();
        this.blockList = new ArrayList<Block>();
        this.core = core;
        this.outerLayer = outerLayer;
        this.innerLayer = null;
        this.config = config;
    }
    
    public Meteorite(final MeteoriteCore core, final MeteoriteLayer outerLayer, final MeteoriteLayer innerLayer, final ConfigurationSection config) {
        this.scheduler = Bukkit.getScheduler();
        this.blockList = new ArrayList<Block>();
        this.core = core;
        this.outerLayer = outerLayer;
        this.innerLayer = innerLayer;
        this.config = config;
    }
    
    public boolean spawnMeteorite(final Location location, final Vector vector, final MeteoritesPro plugin) {
        final ConfigurationSection coreConfig = plugin.getConfig().getConfigurationSection("core-settings");
        final ConfigurationSection innerConfig = plugin.getConfig().getConfigurationSection("inner-layer-settings");
        final ConfigurationSection outerConfig = plugin.getConfig().getConfigurationSection("outer-layer-settings");
        this.core.setLocation(location);
        if (this.config.contains("chat-message", true)) {
            String chatMessage = this.config.getString("chat-message");
            assert chatMessage != null;
            if (!chatMessage.equals("")) {
                chatMessage = setLocationPlaceholders(chatMessage, this.core.getLocation());
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));
            }
        }
        if (this.config.contains("meteorite-spawn-commands", true)) {
            for (String command : this.config.getStringList("meteorite-spawn-commands")) {
                if (!command.equals("")) {
                    command = setLocationPlaceholders(command, this.core.getLocation());
                    Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), command);
                }
            }
        }
        final World world = location.getWorld();
        if (this.config.contains("clean-up-meteorite-blocks-interval", true) && this.config.getInt("clean-up-meteorite-blocks-interval") > 0) {
            this.schedulerId = this.scheduler.scheduleSyncDelayedTask((Plugin)plugin, () -> {
                this.blockList.addAll(new ArrayList<Block>(EventListenerClass.getMeteoriteBlockList()));
                EventListenerClass.clearMeteoriteBlockList();
                return;
            }, 380L);
            this.schedulerId = this.scheduler.scheduleSyncDelayedTask((Plugin)plugin, this::cleanUpMeteoriteBlocks, (long)(this.config.getInt("clean-up-meteorite-blocks-interval") * 20));
        }
        else {
            this.schedulerId = this.scheduler.scheduleSyncDelayedTask((Plugin)plugin, EventListenerClass::clearMeteoriteBlockList, 380L);
        }
        return this.core.spawnMeteoriteCore(world, vector, coreConfig) && (this.innerLayer == null || this.innerLayer.spawnMeteoriteLayer(world, vector, this.core, innerConfig)) && this.outerLayer.spawnMeteoriteLayer(world, vector, this.core, outerConfig);
    }
    
    public static String setLocationPlaceholders(String string, final Location location) {
        string = string.replaceAll("%locationX%", String.valueOf(Math.round(location.getX())));
        string = string.replaceAll("%locationZ%", String.valueOf(Math.round(location.getZ())));
        string = string.replaceAll("%locationY%", String.valueOf(Math.round(location.getY())));
        return string;
    }
    
    public void cleanUpMeteoriteBlocks() {
        for (final Block meteoriteBlock : this.blockList) {
            meteoriteBlock.setType(Material.AIR);
        }
        this.blockList.clear();
    }
}
