// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Meteorites;

import org.bukkit.util.Vector;
import org.bukkit.Location;
import java.util.Iterator;
import me.olipulse.meteoritespro.ExceptionHandling.ConfigException;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import me.olipulse.meteoritespro.MeteoritesPro;
import me.olipulse.meteoritespro.Randomizers.RandomizerClass;
import java.util.HashMap;

public class MeteoriteCreator
{
    private static HashMap<String, Meteorite> meteoriteList;
    private static RandomizerClass meteoriteRandomizer;
    
    public static boolean initializeMeteorites(final MeteoritesPro plugin) {
        try {
            MeteoriteCreator.meteoriteRandomizer = new RandomizerClass(123456789L);
            for (final String meteoriteStringName : Objects.requireNonNull(plugin.getConfig().getConfigurationSection("meteorites")).getKeys(false)) {
                final ConfigurationSection config = plugin.getConfig().getConfigurationSection("meteorites." + meteoriteStringName);
                if (config != null) {
                    final int outerLayerSize = config.getInt("outer-layer-size");
                    final int innerLayerSize = config.getInt("inner-layer-size");
                    if (outerLayerSize <= innerLayerSize && config.getBoolean("enable-inner-layer")) {
                        throw new ConfigException("Outer layer meteorite size must be greater than inner layer size for meteorite '" + meteoriteStringName + "'");
                    }
                    if (outerLayerSize < 2 || innerLayerSize < 2) {
                        throw new ConfigException("Outer and inner meteorite layer size must be greater than 1 for meteorite '" + meteoriteStringName + "'");
                    }
                    if (config.contains("clean-up-meteorite-blocks-interval", true) && config.getInt("clean-up-meteorite-blocks-interval") < 20 && config.getInt("clean-up-meteorite-blocks-interval") > 0) {
                        throw new ConfigException("invalid 'clean-up-meteorite-blocks-interval' for meteorite: '" + meteoriteStringName + "'. Must be at least 20 but was: " + config.getInt("clean-up-meteorite-blocks-interval"));
                    }
                    final RandomizerClass coreRandomizer = new RandomizerClass(123456789L);
                    if (!coreRandomizer.addMaterials(config.getConfigurationSection("core-block"), plugin)) {
                        throw new ConfigException("Error trying to create meteorite core for meteorite '" + meteoriteStringName + "'");
                    }
                    final MeteoriteCore core = new MeteoriteCore(coreRandomizer.getRandomMaterial(), plugin);
                    final RandomizerClass outerRandomizer = new RandomizerClass(123456789L);
                    if (!outerRandomizer.addMaterials(config.getConfigurationSection("outer-layer-blocks"), plugin)) {
                        throw new ConfigException("Error trying to create meteorite outer layer for meteorite '" + meteoriteStringName + "'");
                    }
                    final MeteoriteLayer outerLayer = new MeteoriteLayer(config.getInt("outer-layer-size"), "outer", outerRandomizer, plugin);
                    Meteorite meteorite;
                    if (config.getBoolean("enable-inner-layer")) {
                        final RandomizerClass innerRandomizer = new RandomizerClass(123456789L);
                        if (!innerRandomizer.addMaterials(config.getConfigurationSection("inner-layer-blocks"), plugin)) {
                            throw new ConfigException("Error trying to create meteorite inner layer for meteorite '" + meteoriteStringName + "'");
                        }
                        final MeteoriteLayer innerLayer = new MeteoriteLayer(config.getInt("inner-layer-size"), "inner", innerRandomizer, plugin);
                        meteorite = new Meteorite(core, outerLayer, innerLayer, config);
                    }
                    else {
                        meteorite = new Meteorite(core, outerLayer, config);
                    }
                    MeteoriteCreator.meteoriteList.put(meteoriteStringName, meteorite);
                    MeteoriteCreator.meteoriteRandomizer.addMeteoriteChance(meteorite, config.getInt("chance"));
                }
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }
    
    public static boolean createMeteorite(final Location location, final Vector vector, final MeteoritesPro plugin, final ConfigurationSection config) {
        final Meteorite meteorite = MeteoriteCreator.meteoriteList.get(config.getName());
        return meteorite.spawnMeteorite(location, vector, plugin);
    }
    
    public static RandomizerClass getMeteoriteRandomizer() {
        return MeteoriteCreator.meteoriteRandomizer;
    }
    
    public static HashMap<String, Meteorite> getMeteoriteList() {
        return MeteoriteCreator.meteoriteList;
    }
    
    static {
        MeteoriteCreator.meteoriteList = new HashMap<String, Meteorite>();
    }
}
