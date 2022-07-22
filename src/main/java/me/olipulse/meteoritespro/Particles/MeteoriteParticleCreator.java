// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Particles;

import org.bukkit.plugin.Plugin;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import java.util.ArrayList;
import org.bukkit.entity.FallingBlock;
import org.bukkit.configuration.Configuration;
import me.olipulse.meteoritespro.ExceptionHandling.ConfigException;
import me.olipulse.meteoritespro.MeteoritesPro;
import me.olipulse.meteoritespro.Randomizers.RandomizerClass;

public class MeteoriteParticleCreator
{
    private static RandomizerClass particleRandomizer;
    
    public static RandomizerClass getParticleRandomizer() {
        return MeteoriteParticleCreator.particleRandomizer;
    }
    
    public static boolean initializeParticleRandomizer(final MeteoritesPro plugin) {
        try {
            final Configuration config = (Configuration)plugin.getConfig();
            if (config.getBoolean("enable-meteorite-particles")) {
                (MeteoriteParticleCreator.particleRandomizer = new RandomizerClass(123456789L)).addParticles(plugin, config.getConfigurationSection("possible-meteorite-particle-effects"));
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }
    
    public static void spawnParticle(final MeteoritesPro plugin, final FallingBlock fallingBlock) {
        final int interval = plugin.getConfig().getInt("meteorite-particle-interval");
        final List<Location> locationList = new ArrayList<Location>();
        if (interval > 0) {
            new BukkitRunnable() {
                public void run() {
                    if (fallingBlock.isDead()) {
                        this.cancel();
                    }
                    final MeteoriteParticle particle = MeteoriteParticleCreator.particleRandomizer.getRandomParticle();
                    final Location fallingBlockLocation = fallingBlock.getLocation();
                    final double spread = particle.getSpread();
                    fallingBlock.getWorld().spawnParticle(particle.getParticleType(), fallingBlockLocation, particle.getAmount(), spread, spread, spread, particle.getSpeed(), (Object)null, particle.isForceView());
                    if (locationList.size() > 30 && locationList.get(locationList.size() - 1).equals((Object)fallingBlockLocation)) {
                        this.cancel();
                    }
                    locationList.add(fallingBlockLocation);
                }
            }.runTaskTimer((Plugin)plugin, 1L, (long)interval);
        }
        new BukkitRunnable() {
            public void run() {
            }
        }.runTaskLater((Plugin)plugin, 200L);
    }
}
