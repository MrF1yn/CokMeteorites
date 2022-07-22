// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Meteorites;

import org.bukkit.plugin.Plugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import org.bukkit.entity.Player;
import java.util.UUID;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.ChatColor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import java.util.Objects;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import me.olipulse.meteoritespro.ExceptionHandling.ConfigException;
import java.util.Random;
import me.olipulse.meteoritespro.MeteoritesPro;
import org.bukkit.scheduler.BukkitScheduler;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.main.Kingdoms;

public class RandomMeteoriteHandler
{
    private static BukkitScheduler scheduler;
    private static int schedulerId;
    private static Runnable runnable;
    
    public static boolean randomMeteoriteHandler(final MeteoritesPro plugin) {
        try {
            final Configuration config = (Configuration)plugin.getConfig();
            RandomMeteoriteHandler.scheduler = plugin.getServer().getScheduler();
            final long delay = plugin.getConfig().getLong("random-meteorite-interval") * 20L;
            final Random random = new Random();
            final double height = config.getDouble("random-meteorite-spawn-height");
            final double maxX = config.getDouble("random-meteorite-max-spawn-x-coord");
            final double maxZ = config.getDouble("random-meteorite-max-spawn-z-coord");
            final double minX = config.getDouble("random-meteorite-min-spawn-x-coord");
            final double minZ = config.getDouble("random-meteorite-min-spawn-z-coord");
            if (maxX <= minX) {
                throw new ConfigException("Max X coordinate for random meteorite may not be smaller than min X coordinate");
            }
            if (maxZ <= minZ) {
                throw new ConfigException("Max Z coordinate for random meteorite may not be smaller than min Z coordinate");
            }
            final Vector randomVector = new Vector();
            final String worldName = config.getString("random-meteorite-world");
            assert worldName != null;
            final World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                throw new ConfigException("Invalid world name for random meteorites.");
            }
            RandomMeteoriteHandler.runnable = new Runnable() {
                Location randomLocation;
                double randomX;
                double randomZ;
                double differenceX;
                double differenceZ;
                boolean WGisEnabled = false;
                WorldGuardPlugin worldGuardPlugin;
                RegionContainer regionContainer;
                RegionManager regionManager;
                ProtectedRegion protectedRegion;
                BlockVector2 blockVector2;
                int safeZoneBufferWG;
                int tryCount = 0;
                boolean GPisEnabled = false;
                GriefPrevention griefPrevention;
                int safeZoneBufferGP;
                boolean foundSafeLocation = false;
                boolean locationIsSafeForGP;
                boolean locationIsSafeForWG;
                
                @Override
                public void run() {
                    if (config.contains("enable-griefprevention-safe-zones", true) && config.getBoolean("enable-griefprevention-safe-zones")) {
                        this.griefPrevention = getGriefPrevention(plugin);
                    }
                    if (this.griefPrevention != null && this.griefPrevention.claimsEnabledForWorld(world)) {
                        this.GPisEnabled = true;
                    }
                    if (config.contains("enable-worldguard-safe-zones", true) && config.getBoolean("enable-worldguard-safe-zones")) {
                        this.worldGuardPlugin = getWorldGuard(plugin);
                    }
                    if (this.worldGuardPlugin != null) {
                        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        this.regionManager = this.regionContainer.get(BukkitAdapter.adapt((World)Objects.requireNonNull(world)));
                        if (this.regionManager != null) {
                            this.WGisEnabled = true;
                        }
                    }
                    if (this.checkAllSafeZones()) {
                        setRandomVector(randomVector, random, plugin);
                        if (!MeteoriteCreator.getMeteoriteRandomizer().getRandomMeteorite().spawnMeteorite(this.randomLocation, randomVector, plugin)) {
                            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritePro] Failed to shoot random meteorite.");
                        }
                    }
                    else {
                        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritePro] Couldn't find safe random meteorite location after " + this.tryCount + " attempts.");
                        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritePro] Make sure there's enough space that isn't protected for the meteorite to spawn in! Are your buffers to high?");
                    }
                    this.tryCount = 0;
                    this.foundSafeLocation = false;
                }
                
                private boolean checkAllSafeZones() {
                    this.setRandomLocation();
                    while (this.tryCount < 20 && !this.foundSafeLocation) {
                        if (this.GPisEnabled && !(this.locationIsSafeForGP = this.checkIfInGPClaim())) {
                            ++this.tryCount;
                            this.checkAllSafeZones();
                            break;
                        }
                        if (Land.getLand(this.randomLocation).isClaimed()) {
                            ++this.tryCount;
                            this.checkAllSafeZones();
                            break;
                        }
                        if (this.WGisEnabled && !(this.locationIsSafeForWG = this.checkIfInWGSafeZone())) {
                            ++this.tryCount;
                            this.checkAllSafeZones();
                            break;
                        }
                        this.foundSafeLocation = true;
                    }
                    return this.tryCount < 20;
                }
                
                private boolean checkIfInGPClaim() {
                    final Claim claim = this.griefPrevention.dataStore.getClaimAt(this.randomLocation, true, (Claim)null);
                    if (claim != null) {
                        return false;
                    }
                    this.safeZoneBufferGP = config.getInt("griefprevention-safe-zone-buffer");
                    final CreateClaimResult bufferClaimResult = this.griefPrevention.dataStore.createClaim(world, (int)this.randomLocation.getX() - this.safeZoneBufferGP, (int)this.randomLocation.getX() + this.safeZoneBufferGP, 0, 256, (int)this.randomLocation.getZ() - this.safeZoneBufferGP, (int)this.randomLocation.getZ() + this.safeZoneBufferGP, (UUID)null, (Claim)null, Long.valueOf(88888888L), (Player)null);
                    if (bufferClaimResult.succeeded) {
                        this.griefPrevention.dataStore.deleteClaim(this.griefPrevention.dataStore.getClaim(88888888L));
                        return true;
                    }
                    return false;
                }
                
                private boolean checkIfInWGSafeZone() {
                    this.safeZoneBufferWG = config.getInt("worldguard-safe-zone-buffer");
                    if (plugin.getConfig().contains("protect-all-worldguard-zones", true) && plugin.getConfig().getBoolean("protect-all-worldguard-zones")) {
                        final List<ProtectedRegion> protectedRegionList = new ArrayList<ProtectedRegion>(this.regionManager.getRegions().values());
                        for (final ProtectedRegion safeZone : protectedRegionList) {
                            if (this.checkIfRandomLocationIsInSafeZonePlusBuffer(safeZone)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    for (final String safeZoneConfigString : config.getStringList("worldguard-safe-zone-names")) {
                        this.protectedRegion = this.regionManager.getRegion(safeZoneConfigString);
                        if (this.protectedRegion == null) {
                            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritePro] Caution, there is no matching world guard region for safe zone: '" + safeZoneConfigString + "'.");
                        }
                        else {
                            if (this.checkIfRandomLocationIsInSafeZonePlusBuffer(this.protectedRegion)) {
                                return false;
                            }
                            continue;
                        }
                    }
                    return true;
                }
                
                private boolean checkIfRandomLocationIsInSafeZonePlusBuffer(final ProtectedRegion safeZone) {
                    final ProtectedCuboidRegion fullSafeZone = new ProtectedCuboidRegion("meteoriteSafeZone123", safeZone.getMinimumPoint().add(-this.safeZoneBufferWG, 0, -this.safeZoneBufferWG), safeZone.getMaximumPoint().add(this.safeZoneBufferWG, 0, this.safeZoneBufferWG));
                    this.blockVector2 = BlockVector2.at(this.randomLocation.getX(), this.randomLocation.getZ());
                    if (fullSafeZone.contains(this.blockVector2)) {
                        plugin.getServer().getConsoleSender().sendMessage("Try " + this.tryCount + ", Meteorite Location " + this.randomLocation.toString() + " was in WG safe zone: " + safeZone.toString());
                        this.regionManager.removeRegion("meteoriteSafeZone123");
                        return true;
                    }
                    return false;
                }
                
                private void setRandomLocation() {
                    this.differenceX = maxX - minX;
                    this.randomX = random.nextInt((int)this.differenceX) + minX;
                    this.differenceZ = maxZ - minZ;
                    this.randomZ = random.nextInt((int)this.differenceZ) + minZ;
                    this.randomLocation = new Location(world, this.randomX, height, this.randomZ);
                }
            };
            if (config.getBoolean("enable-random-meteorites")) {
                RandomMeteoriteHandler.schedulerId = RandomMeteoriteHandler.scheduler.scheduleSyncRepeatingTask((Plugin)plugin, RandomMeteoriteHandler.runnable, delay, delay);
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }
    
    private static WorldGuardPlugin getWorldGuard(final MeteoritesPro meteoritesPro) {
        final Plugin plugin = meteoritesPro.getServer().getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin)plugin;
    }
    
    private static GriefPrevention getGriefPrevention(final MeteoritesPro meteoritesPro) {
        final Plugin plugin = meteoritesPro.getServer().getPluginManager().getPlugin("GriefPrevention");
        if (!(plugin instanceof GriefPrevention)) {
            return null;
        }
        return (GriefPrevention)plugin;
    }
    
    private static void setRandomVector(final Vector randomVector, final Random random, final MeteoritesPro plugin) {
        double speed = 2.0;
        if (plugin.getConfig().contains("meteorite-speed", true)) {
            speed = plugin.getConfig().getDouble("meteorite-speed");
        }
        randomVector.setX((random.nextInt(2000) - 1000) / 1000.0 * speed);
        randomVector.setZ((random.nextInt(2000) - 1000) / 1000.0 * speed);
        randomVector.setY(random.nextInt(3) - 2);
    }
    
    public static BukkitScheduler getScheduler() {
        return RandomMeteoriteHandler.scheduler;
    }
    
    public static int getSchedulerId() {
        return RandomMeteoriteHandler.schedulerId;
    }
    
    public static void shootRandomMeteorite(final MeteoritesPro plugin) {
        RandomMeteoriteHandler.scheduler.runTask((Plugin)plugin, RandomMeteoriteHandler.runnable);
    }
}
