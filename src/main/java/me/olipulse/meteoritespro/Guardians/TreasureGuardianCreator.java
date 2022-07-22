// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Guardians;

import org.bukkit.configuration.Configuration;
import me.olipulse.meteoritespro.ExceptionHandling.ConfigException;
import me.olipulse.meteoritespro.MeteoritesPro;
import me.olipulse.meteoritespro.Randomizers.RandomizerClass;

public class TreasureGuardianCreator
{
    private static RandomizerClass guardianRandomizer;
    
    public static RandomizerClass getGuardianRandomizer() {
        return TreasureGuardianCreator.guardianRandomizer;
    }
    
    public static boolean initializeGuardianRandomizer(final MeteoritesPro plugin) {
        try {
            final Configuration config = (Configuration)plugin.getConfig();
            if (config.getBoolean("enable-treasure-guardian")) {
                (TreasureGuardianCreator.guardianRandomizer = new RandomizerClass(123456789L)).addGuardians(plugin, config.getConfigurationSection("possible-guardians"));
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }
}
