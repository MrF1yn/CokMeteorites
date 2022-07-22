// 
// Decompiled by Procyon v0.5.36
// 

package me.olipulse.meteoritespro.Commands;

import java.util.Iterator;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import me.olipulse.meteoritespro.Meteorites.MeteoriteCreator;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.ChatColor;
import me.olipulse.meteoritespro.ExceptionHandling.ConfigException;
import me.olipulse.meteoritespro.Meteorites.RandomMeteoriteHandler;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.olipulse.meteoritespro.MeteoritesPro;
import org.bukkit.command.CommandExecutor;

public class CommandClass implements CommandExecutor
{
    private MeteoritesPro plugin;
    private final String PERMISSIONPREFIX = "meteoritespro.";
    private final String ADMINPERMISSION = "meteoritespro.admin";
    private final String CHATPREFIX = "&9[&6MeteoritesPro&9] ";
    private double meteoriteSpeed;
    
    public CommandClass(final MeteoritesPro plugin) {
        this.meteoriteSpeed = 2.0;
        this.plugin = plugin;
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (command.getName().equalsIgnoreCase("mp") || command.getName().equalsIgnoreCase("meteoritespro")) {
            Player player = null;
            if (sender instanceof Player) {
                player = ((Player)sender).getPlayer();
            }
            switch (args.length) {
                case 0: {
                    if (sender.hasPermission("meteoritespro.default") || sender.hasPermission("meteoritespro.admin")) {
                        this.sendPlayerHelp(sender);
                        break;
                    }
                    this.sendPlayerNoPerm(sender);
                    break;
                }
                case 1: {
                    final String lowerCase = args[0].toLowerCase();
                    switch (lowerCase) {
                        case "help": {
                            if (sender.hasPermission("meteoritespro.default") || sender.hasPermission("meteoritespro.admin")) {
                                this.sendPlayerHelp(sender);
                                break;
                            }
                            this.sendPlayerNoPerm(sender);
                            break;
                        }
                        case "discord": {
                            if (sender.hasPermission("meteoritespro.default") || sender.hasPermission("meteoritespro.admin")) {
                                this.sendPlayerDiscord(sender);
                                break;
                            }
                            this.sendPlayerNoPerm(sender);
                            break;
                        }
                        case "reload": {
                            if (!sender.hasPermission("meteoritespro.reload")) {
                                if (!sender.hasPermission("meteoritespro.admin")) {
                                    this.sendPlayerNoPerm(sender);
                                    break;
                                }
                            }
                            try {
                                this.plugin.reloadConfig();
                                if (RandomMeteoriteHandler.getScheduler() != null) {
                                    RandomMeteoriteHandler.getScheduler().cancelTask(RandomMeteoriteHandler.getSchedulerId());
                                }
                                if (!this.plugin.initializePluginRandomizers() || !RandomMeteoriteHandler.randomMeteoriteHandler(this.plugin)) {
                                    throw new ConfigException("There was an error reloading the plugin. Check the console for the error message!");
                                }
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &aSuccessfully reloaded the plugin"));
                            }
                            catch (ConfigException e) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &c" + e.getMessage()));
                            }
                            break;
                        }
                        case "shoot": {
                            if (!sender.hasPermission("meteoritespro.shoot") && !sender.hasPermission("meteoritespro.admin")) {
                                this.sendPlayerNoPerm(sender);
                                break;
                            }
                            if (player != null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cPlease specify the meteorite you want to shoot"));
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cYour available meteorite names are: &a" + this.getAvailableMeteoriteNames() + "."));
                                break;
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cYou must be in game to execute this command"));
                            break;
                        }
                        case "shootrandom": {
                            if (sender.hasPermission("meteoritespro.shootrandom") || sender.hasPermission("meteoritespro.admin")) {
                                RandomMeteoriteHandler.shootRandomMeteorite(this.plugin);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &7Shooting &9Meteorite &7. . . "));
                                break;
                            }
                            break;
                        }
                        case "start": {
                            if (!sender.hasPermission("meteoritespro.start") && !sender.hasPermission("meteoritespro.admin")) {
                                this.sendPlayerNoPerm(sender);
                                break;
                            }
                            if (!this.plugin.getConfig().getBoolean("enable-random-meteorites")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cRandom meteorites are disabled in the config.yml"));
                                break;
                            }
                            if (RandomMeteoriteHandler.getScheduler().isCurrentlyRunning(RandomMeteoriteHandler.getSchedulerId()) || RandomMeteoriteHandler.getScheduler().isQueued(RandomMeteoriteHandler.getSchedulerId())) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cRandom meteorites are already falling"));
                                break;
                            }
                            RandomMeteoriteHandler.randomMeteoriteHandler(this.plugin);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &aRandom meteorites will now fall"));
                            break;
                        }
                        case "stop": {
                            if (!sender.hasPermission("meteoritespro.stop") && !sender.hasPermission("meteoritespro.admin")) {
                                this.sendPlayerNoPerm(sender);
                                break;
                            }
                            if (RandomMeteoriteHandler.getScheduler().isCurrentlyRunning(RandomMeteoriteHandler.getSchedulerId()) || RandomMeteoriteHandler.getScheduler().isQueued(RandomMeteoriteHandler.getSchedulerId())) {
                                RandomMeteoriteHandler.getScheduler().cancelTask(RandomMeteoriteHandler.getSchedulerId());
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &aRandom meteorites have stopped falling"));
                                break;
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cRandom meteorites are not falling"));
                            break;
                        }
                        default: {
                            this.sendPlayerUnknownCommand(sender);
                            break;
                        }
                    }
                    break;
                }
                case 2: {
                    final String lowerCase2 = args[0].toLowerCase();
                    switch (lowerCase2) {
                        case "shoot": {
                            if (!sender.hasPermission("meteoritespro.shoot") && !sender.hasPermission("meteoritespro.admin")) {
                                this.sendPlayerNoPerm(sender);
                                break;
                            }
                            if (player == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cYou must be in game to execute this command"));
                                break;
                            }
                            if (this.plugin.getConfig().contains("meteorites", true) && Objects.requireNonNull(this.plugin.getConfig().getConfigurationSection("meteorites")).getKeys(false).contains(args[1])) {
                                final ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("meteorites." + args[1]);
                                if (config != null) {
                                    if (config.contains("meteorite-speed", true)) {
                                        this.meteoriteSpeed = config.getDouble("meteorite-speed");
                                        if (this.meteoriteSpeed > 5.0) {
                                            this.meteoriteSpeed = 5.0;
                                        }
                                        else if (this.meteoriteSpeed < 0.0) {
                                            this.meteoriteSpeed = 1.0;
                                        }
                                    }
                                    if (MeteoriteCreator.createMeteorite(this.getPlayerLocationForMeteorite(player, 10), this.calculateMeteoriteVectorFromPlayersView(player, this.meteoriteSpeed), this.plugin, config)) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &7Shooting &9Meteorite &7. . . "));
                                    }
                                    else {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cFailed to shoot meteorite. Check the console for the error!"));
                                    }
                                }
                                break;
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cInvalid meteorite name: '" + args[1] + "'"));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cYour available meteorite names are: &a" + this.getAvailableMeteoriteNames() + "."));
                            break;
                        }
                        default: {
                            this.sendPlayerUnknownCommand(sender);
                            break;
                        }
                    }
                    break;
                }
                default: {
                    if (sender.hasPermission("meteoritespro.default") || sender.hasPermission("meteoritespro.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cToo many arguments"));
                        this.sendPlayerHelp(sender);
                        break;
                    }
                    this.sendPlayerNoPerm(sender);
                    break;
                }
            }
        }
        return true;
    }
    
    private void sendPlayerUnknownCommand(final CommandSender sender) {
        if (sender.hasPermission("meteoritespro.default") || sender.hasPermission("meteoritespro.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cUnknown command"));
            this.sendPlayerHelp(sender);
        }
        else {
            this.sendPlayerNoPerm(sender);
        }
    }
    
    private Location getPlayerLocationForMeteorite(final Player player, final int amountOfBlocksAbovePlayer) {
        final Location location = player.getLocation();
        location.add(0.0, (double)amountOfBlocksAbovePlayer, 0.0);
        double x = (int)location.getX();
        double z = (int)location.getZ();
        if (x >= 0.0) {
            x += 0.5;
        }
        else {
            x -= 0.5;
        }
        if (z >= 0.0) {
            z += 0.5;
        }
        else {
            z -= 0.5;
        }
        location.setX(x);
        location.setZ(z);
        return location;
    }
    
    private Vector calculateMeteoriteVectorFromPlayersView(final Player player, final double speed) {
        return new Vector(player.getLocation().getDirection().getX() * speed, player.getLocation().getDirection().getY() * speed, player.getLocation().getDirection().getZ() * speed);
    }
    
    private void sendPlayerHelp(final CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m--------------------&9&l<< &6MeteoritesPro&9&l >>&r&8&m--------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mp reload &3- &7Reload the plugin's configuration"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mp shoot <name> &3- &7Shoot a meteorite in the direction you're facing"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mp shootrandom &3- &7Shoot a random meteorite in your random area"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mp stop &3- &7Random meteorites stop falling"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mp start &3- &7Random meteorites start falling"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mp help &3- &7Open this menu"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mp discord &3- &7Join our support Discord server"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------------------------"));
    }
    
    private void sendPlayerDiscord(final CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &3https://discord.gg/47YEbMm &e&l<< &e(Click me)"));
    }
    
    private void sendPlayerNoPerm(final CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[&6MeteoritesPro&9] &cYou have no permission to perform that command"));
    }
    
    private String getAvailableMeteoriteNames() {
        final StringBuilder meteoriteNames = new StringBuilder();
        for (final String meteoriteStringName : Objects.requireNonNull(this.plugin.getConfig().getConfigurationSection("meteorites")).getKeys(false)) {
            meteoriteNames.append("'").append(meteoriteStringName).append("', ");
        }
        meteoriteNames.delete(meteoriteNames.length() - 2, meteoriteNames.length());
        return meteoriteNames.toString();
    }
}
