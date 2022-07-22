// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.EventListeners;

import org.bukkit.inventory.meta.Damageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.NamespacedKey;
import java.util.ArrayList;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Iterator;
import org.bukkit.configuration.Configuration;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import me.olipulse.meteoritespro.Meteorites.Meteorite;
import me.olipulse.meteoritespro.ExceptionHandling.ConfigException;
import java.util.Objects;
import me.olipulse.meteoritespro.Guardians.TreasureGuardianCreator;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.block.Chest;
import org.bukkit.block.Barrel;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.block.Block;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import java.util.Random;
import me.olipulse.meteoritespro.MeteoritesPro;
import org.bukkit.event.Listener;

public class EventListenerClass implements Listener
{
    private MeteoritesPro plugin;
    private Random random;
    private ItemStack treasureChecker;
    private static List<Block> meteoriteBlockList;
    
    public EventListenerClass(final MeteoritesPro plugin) {
        this.random = new Random();
        this.plugin = plugin;
        this.initializeTreasureChecker();
    }
    
    @EventHandler
    public void onMeteoriteFall(final EntityChangeBlockEvent e) {
        final Entity meteoriteBlockEntity = e.getEntity();
        if (meteoriteBlockEntity instanceof FallingBlock) {
            final String lowerCase = meteoriteBlockEntity.getName().toLowerCase();
            switch (lowerCase) {
                case "core": {
                    final ConfigurationSection coreConfig = this.plugin.getConfig().getConfigurationSection("core-settings");
                    assert coreConfig != null;
                    this.handleMeteoriteBlockFall(meteoriteBlockEntity, coreConfig);
                    this.handleMeteoriteCoreFall(meteoriteBlockEntity);
                    EventListenerClass.meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
                    break;
                }
                case "inner": {
                    final ConfigurationSection innerConfig = this.plugin.getConfig().getConfigurationSection("inner-layer-settings");
                    assert innerConfig != null;
                    this.handleMeteoriteBlockFall(meteoriteBlockEntity, innerConfig);
                    EventListenerClass.meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
                    break;
                }
                case "outer": {
                    final ConfigurationSection outerConfig = this.plugin.getConfig().getConfigurationSection("outer-layer-settings");
                    assert outerConfig != null;
                    this.handleMeteoriteBlockFall(meteoriteBlockEntity, outerConfig);
                    EventListenerClass.meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onTreasureInteract(final PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getType() == Material.BARREL) {
                final Barrel treasure = (Barrel)e.getClickedBlock().getState();
                this.checkForTreasure(e, treasure.getInventory(), treasure.getLocation());
            }
            else if (e.getClickedBlock().getType() == Material.CHEST) {
                final Chest treasure2 = (Chest)e.getClickedBlock().getState();
                this.checkForTreasure(e, treasure2.getInventory(), treasure2.getLocation());
            }
        }
    }
    
    private void checkForTreasure(final PlayerInteractEvent e, final Inventory inventory, final Location location) {
        final ItemStack treasureCheck = inventory.getItem(26);
        if (treasureCheck != null && treasureCheck.equals((Object)this.treasureChecker)) {
            inventory.remove(treasureCheck);
            if (this.plugin.getConfig().getBoolean("enable-treasure-guardian")) {
                this.spawnMeteoriteGuardian(location, e.getPlayer());
                e.setCancelled(true);
            }
        }
    }
    
    private void spawnMeteoriteGuardian(final Location treasureLocation, final Player player) {
        final Location playerLocation = player.getLocation();
        final Location guardianLocation = this.getMiddleLocation(playerLocation, treasureLocation);
        TreasureGuardianCreator.getGuardianRandomizer().getRandomGuardian().spawnGuardian(guardianLocation, player);
    }
    
    private Location getMiddleLocation(final Location location1, final Location location2) {
        final Location location3 = new Location(location1.getWorld(), (location1.getX() + location2.getX()) / 2.0, (location1.getY() + location2.getY()) / 2.0, (location1.getZ() + location2.getZ()) / 2.0);
        while (location3.getBlock().getType() != Material.AIR || new Location(location3.getWorld(), location3.getX(), location3.getY() + 1.0, location3.getZ()).getBlock().getType() != Material.AIR || new Location(location3.getWorld(), location3.getX(), location3.getY() + 2.0, location3.getZ()).getBlock().getType() != Material.AIR) {
            location3.add(0.0, 1.0, 0.0);
        }
        return location3;
    }
    
    private void handleMeteoriteBlockFall(final Entity meteoriteBlock, final ConfigurationSection blockConfig) {
        final Location blockLocation = meteoriteBlock.getLocation();
        if (blockConfig.getBoolean("enable-explosion")) {
            meteoriteBlock.getWorld().createExplosion(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ(), (float)blockConfig.getInt("explosion-power"), blockConfig.getBoolean("explosion-sets-fire"), blockConfig.getBoolean("explosion-breaks-blocks"));
        }
        if (blockConfig.getBoolean("enable-lighting-strike")) {
            meteoriteBlock.getWorld().strikeLightning(blockLocation);
        }
    }
    
    private void handleMeteoriteCoreFall(final Entity coreBlock) {
        try {
            final Configuration config = (Configuration)this.plugin.getConfig();
            if (config.contains("enable-meteorite-treasure", true) && config.getBoolean("enable-meteorite-treasure")) {
                final Location treasureLocation = coreBlock.getLocation().add((double)(this.random.nextInt(2) - 1), -1.0, (double)(this.random.nextInt(2) - 1));
                final Material treasureType = Material.getMaterial((String)Objects.requireNonNull(Objects.requireNonNull(config.getString("treasure-barrel-or-chest")).toUpperCase()));
                if (treasureType != Material.BARREL && treasureType != Material.CHEST) {
                    throw new ConfigException("Invalid treasure type: " + config.getString("treasure-barrel-or-chest") + " -> Treasure must be in a barrel or chest!");
                }
                treasureLocation.getBlock().setType(treasureType);
                if (treasureType == Material.BARREL) {
                    final Barrel barrel = (Barrel)treasureLocation.getBlock().getState();
                    final Inventory inventory = barrel.getInventory();
                    this.determineTreasureContent(inventory);
                }
                else {
                    final Chest chest = (Chest)treasureLocation.getBlock().getState();
                    chest.setCustomName("treasure");
                    final Inventory inventory = chest.getBlockInventory();
                    this.determineTreasureContent(inventory);
                }
                EventListenerClass.meteoriteBlockList.add(treasureLocation.getBlock());
            }
            if (config.contains("core-settings.message", true)) {
                String chatMessage = config.getString("core-settings.message");
                assert chatMessage != null;
                if (!chatMessage.equals("")) {
                    chatMessage = Meteorite.setLocationPlaceholders(chatMessage, coreBlock.getLocation());
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));
                }
            }
            if (config.contains("core-settings.commands", true)) {
                for (String command : config.getStringList("core-settings.commands")) {
                    if (!command.equals("")) {
                        command = Meteorite.setLocationPlaceholders(command, coreBlock.getLocation());
                        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), command);
                    }
                }
            }
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(this.plugin, e);
        }
    }
    
    private void determineTreasureContent(final Inventory inventory) {
        try {
            final ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("treasure-content");
            assert config != null;
            for (final String itemName : config.getKeys(false)) {
                if (config.getBoolean(itemName + ".enabled")) {
                    int amount = 1;
                    boolean chanceVerified = true;
                    int count = 0;
                    if (config.contains(itemName + ".chance", true)) {
                        final double chance = config.getDouble(itemName + ".chance");
                        if (chance < 0.0 || chance > 100.0) {
                            throw new ConfigException("Invalid chance for item " + itemName + ": " + chance + " - Chance must be between 0-100");
                        }
                        if (this.random.nextInt(100) + 1 > chance) {
                            chanceVerified = false;
                        }
                    }
                    if (!chanceVerified) {
                        continue;
                    }
                    if (!config.contains(itemName + ".item-type", true)) {
                        throw new ConfigException("You must specify a type for item: " + itemName);
                    }
                    final String itemType = config.getString(itemName + ".item-type");
                    if (itemType == null || Material.getMaterial(itemType) == null) {
                        throw new ConfigException("Invalid type for item " + itemName + ": " + itemType);
                    }
                    if (config.contains(itemName + ".amount", true)) {
                        amount = config.getInt(itemName + ".amount");
                    }
                    final ItemStack item = new ItemStack((Material)Objects.requireNonNull(Material.getMaterial(itemType)), amount);
                    final ItemMeta meta = item.getItemMeta();
                    if (config.contains(itemName + ".display-name", true) && config.getString(itemName + ".display-name") != null) {
                        final String displayName = config.getString(itemName + ".display-name");
                        Objects.requireNonNull(meta).setDisplayName(ChatColor.translateAlternateColorCodes('&', (String)Objects.requireNonNull(displayName)));
                        item.setItemMeta(meta);
                    }
                    if (config.contains(itemName + ".lore", true)) {
                        final List<String> lore = new ArrayList<String>();
                        for (final String loreLine : config.getStringList(itemName + ".lore")) {
                            lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
                        }
                        Objects.requireNonNull(meta).setLore((List)lore);
                        item.setItemMeta(meta);
                    }
                    if (config.contains(itemName + ".unbreakable", true) && config.getBoolean(itemName + ".unbreakable")) {
                        Objects.requireNonNull(meta).setUnbreakable(true);
                        item.setItemMeta(meta);
                    }
                    if (config.contains(itemName + ".enchants", true)) {
                        for (final String enchantLine : Objects.requireNonNull(config.getConfigurationSection(itemName + ".enchants")).getKeys(false)) {
                            if (Enchantment.getByKey(NamespacedKey.minecraft(enchantLine.toLowerCase())) == null) {
                                throw new ConfigException("Invalid enchantment name for item " + itemName + ": " + enchantLine);
                            }
                            Objects.requireNonNull(meta).addEnchant((Enchantment)Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(enchantLine.toLowerCase()))), config.getInt(itemName + ".enchants." + enchantLine), true);
                        }
                        item.setItemMeta(meta);
                    }
                    if (config.contains(itemName + ".custom-model-data", true) && config.getInt(itemName + ".custom-model-data") != 0) {
                        final int customModelData = config.getInt(itemName + ".custom-model-data");
                        Objects.requireNonNull(meta).setCustomModelData(Integer.valueOf(customModelData));
                        item.setItemMeta(meta);
                    }
                    if (config.contains(itemName + ".damage", true) && config.getInt(itemName + ".damage") != 0) {
                        final int damage = config.getInt(itemName + ".damage");
                        Objects.requireNonNull((Damageable)meta).setDamage(damage);
                        item.setItemMeta(meta);
                    }
                    inventory.addItem(new ItemStack[] { item });
                    if (++count >= 25) {
                        break;
                    }
                    continue;
                }
            }
            inventory.setItem(26, this.treasureChecker);
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(this.plugin, e);
        }
    }
    
    private void initializeTreasureChecker() {
        this.treasureChecker = new ItemStack(Material.DIRT, 1);
        final ItemMeta treasureCheckMeta = this.treasureChecker.getItemMeta();
        assert treasureCheckMeta != null;
        treasureCheckMeta.setDisplayName("*");
        treasureCheckMeta.setUnbreakable(true);
        treasureCheckMeta.setCustomModelData(Integer.valueOf(2));
        this.treasureChecker.setItemMeta(treasureCheckMeta);
    }
    
    public static List<Block> getMeteoriteBlockList() {
        return EventListenerClass.meteoriteBlockList;
    }
    
    public static void clearMeteoriteBlockList() {
        EventListenerClass.meteoriteBlockList.clear();
    }
    
    static {
        EventListenerClass.meteoriteBlockList = new ArrayList<Block>();
    }
}
