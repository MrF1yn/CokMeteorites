// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Meteorites;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.FallingBlock;
import org.bukkit.block.data.BlockData;
import java.util.Iterator;
import me.olipulse.meteoritespro.Particles.MeteoriteParticleCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.bukkit.World;
import me.olipulse.meteoritespro.MeteoritesPro;
import me.olipulse.meteoritespro.Randomizers.RandomizerClass;

public class MeteoriteLayer
{
    private int diameter;
    private String innerOrOuterMeteorLayer;
    private RandomizerClass randomizer;
    private MeteoritesPro plugin;
    
    public MeteoriteLayer(final int diameter, final String innerOrOuterMeteorLayer, final RandomizerClass randomizer, final MeteoritesPro plugin) {
        this.diameter = diameter;
        this.innerOrOuterMeteorLayer = innerOrOuterMeteorLayer;
        this.randomizer = randomizer;
        this.plugin = plugin;
    }
    
    public boolean spawnMeteoriteLayer(final World world, final Vector vector, final MeteoriteCore core, final ConfigurationSection layerConfig) {
        for (final Location meteorLayerBlockLocation : this.generateSphere(core.getLocation(), this.diameter, true)) {
            final BlockData meteoriteLayerBlockData = Bukkit.createBlockData(this.randomizer.getRandomMaterial());
            final FallingBlock fallingMeteorLayerBlock = world.spawnFallingBlock(meteorLayerBlockLocation, meteoriteLayerBlockData);
            fallingMeteorLayerBlock.setCustomName(this.innerOrOuterMeteorLayer);
            fallingMeteorLayerBlock.setVelocity(vector);
            fallingMeteorLayerBlock.setHurtEntities(layerConfig.getBoolean("can-hurt-entities"));
            fallingMeteorLayerBlock.setDropItem(layerConfig.getBoolean("drop-item-when-destroyed"));
            if (this.plugin.getConfig().contains("enable-meteorite-particles", true) && this.plugin.getConfig().getBoolean("enable-meteorite-particles")) {
                MeteoriteParticleCreator.spawnParticle(this.plugin, fallingMeteorLayerBlock);
            }
        }
        return true;
    }
    
    public List<Location> generateSphere(final Location centerBlock, final int radius, final boolean hollow) {
        final List<Location> circleBlocks = new ArrayList<Location>();
        final int bx = centerBlock.getBlockX();
        final int by = centerBlock.getBlockY();
        final int bz = centerBlock.getBlockZ();
        for (int x = bx - radius; x <= bx + radius; ++x) {
            for (int y = by - radius; y <= by + radius; ++y) {
                for (int z = bz - radius; z <= bz + radius; ++z) {
                    final double distance = (bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y);
                    if (distance < radius * radius && (!hollow || distance >= (radius - 1) * (radius - 1))) {
                        final Location l = new Location(centerBlock.getWorld(), (double)x, (double)y, (double)z);
                        circleBlocks.add(l);
                    }
                }
            }
        }
        return circleBlocks;
    }
}
