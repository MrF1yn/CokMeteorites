// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Meteorites;

import org.bukkit.entity.FallingBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.bukkit.World;
import me.olipulse.meteoritespro.MeteoritesPro;
import org.bukkit.Location;
import org.bukkit.Material;

public class MeteoriteCore
{
    private Material material;
    private Location location;
    private MeteoritesPro plugin;
    
    public MeteoriteCore(final Material material, final MeteoritesPro plugin) {
        this.material = material;
        this.plugin = plugin;
    }
    
    public void setLocation(final Location location) {
        this.location = location;
    }
    
    public Location getLocation() {
        return this.location;
    }
    
    public boolean spawnMeteoriteCore(final World world, final Vector vector, final ConfigurationSection coreConfig) {
        final BlockData coreBlockData = Bukkit.createBlockData(this.material);
        final FallingBlock fallingCore = world.spawnFallingBlock(this.location, coreBlockData);
        fallingCore.setCustomName("core");
        fallingCore.setVelocity(vector);
        fallingCore.setHurtEntities(coreConfig.getBoolean("can-hurt-entities"));
        fallingCore.setDropItem(coreConfig.getBoolean("drop-item-when-destroyed"));
        return true;
    }
}
