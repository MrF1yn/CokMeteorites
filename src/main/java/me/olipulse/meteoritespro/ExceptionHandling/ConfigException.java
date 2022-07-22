// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.ExceptionHandling;

import org.bukkit.ChatColor;
import me.olipulse.meteoritespro.MeteoritesPro;

public class ConfigException extends Exception
{
    public ConfigException(final String message) {
        super(message);
    }
    
    public static void handleConfigException(final MeteoritesPro plugin, final ConfigException e) {
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritesPro] " + e.getMessage());
    }
}
