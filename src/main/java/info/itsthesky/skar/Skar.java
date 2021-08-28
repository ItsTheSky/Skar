package info.itsthesky.skar;

import de.leonhard.storage.Yaml;
import info.itsthesky.skar.api.*;
import info.itsthesky.skar.core.SkarCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Skar extends JavaPlugin {

    private static Skar INSTANCE;
    private static final boolean DEBUG = false;

    @Override
    public void onEnable() {

        INSTANCE = this;

        log("~~~~~~~~~~~~~~~~~~~~~~");
        log("Loading Skar v" + getDescription().getVersion() + " by ItsTheSky & Trason ...");
        log("&1");

        new File(getDataFolder(), "skars/").mkdirs();

        try {
            Class.forName("ch.njol.skript.Skript");
        } catch (Exception ex) {
            error("Cannot load Skript! Skript is not installed or didn't loaded correctly! Disabling...");
            error("~~~~~~~~~~~~~~~~~~~~~~");
            getServer().getPluginManager().disablePlugin(INSTANCE);
            return;
        }
        success("Found Skript!");

        log("Loading commands ...");
        getCommand("skar").setExecutor(new SkarCommand());
        success("Successfully loaded commands!");

        log("Loading default Skar files ...");
        reload(true);

        success("Successfully loaded Skar, with a total of &2" + SkarManager.getMemoriesFiles().size() + " Skar file cached&a!");
        log("~~~~~~~~~~~~~~~~~~~~~~");
    }

    @Override
    public void onDisable() {

    }

    public static void reload(boolean debug) {

        SkarManager.reset();
        WatcherManager.reset();

        for (File file : new File("plugins/Skript/scripts/").listFiles()) {
            if (!file.getName().endsWith(".sk")) continue;

            if (!SkarFile.doesSkarFileExist(file)) {
                if (debug)
                    log("Creating default Skar config for " + file.getName() +" ...");
                SkarManager.add(SkarFile.createDefaultSkarFile(file));
                if (debug)
                    success("Success!");
            } else {
                SkarManager.add(new SkarFile(new Yaml(SkarFile.getConfigFileFromScript(file))));
                if (debug)
                    log("Cached " + file.getName() + " successfully!");
            }

            WatcherManager.add(new FileWatcher(file));
        }
    }

    public static Skar getInstance() {
        if (INSTANCE == null)
            throw new IllegalArgumentException("Skar is not running!");
        return INSTANCE;
    }

    // ################### INFOS ################### //

    public static void log(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(Utils.colored("&3[Skar] &b" + message));
    }

    public static void error(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(Utils.colored("&4[Skar] &c" + message));
    }

    public static void debug(String message) {
        if (DEBUG)
            log(message);
    }

    public static void success(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(Utils.colored("&2[Skar] &a" + message));
    }

    public static void warn(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(Utils.colored("&6[Skar] &e" + message));
    }
}
