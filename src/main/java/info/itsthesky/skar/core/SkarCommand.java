package info.itsthesky.skar.core;

import info.itsthesky.skar.Skar;
import info.itsthesky.skar.api.SkarFile;
import info.itsthesky.skar.api.SkarManager;
import info.itsthesky.skar.api.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

public class SkarCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0]) {
            case "help":
                sendHelp(sender);
                return true;
            case "info":
                sendSkarInfo(sender);
                return true;
            case "reload":
                sendReload(sender);
                return true;
            case "create":
                if (args.length == 1) {
                    sendError(sender, "You have to define the name of your desired script file!");
                    return false;
                }
                final String name = args[1].endsWith(".sk") ? args[1] : args[1] + ".sk";
                final File scriptFile = new File("plugins/Skript/scripts/" + name);
                if (scriptFile.exists()) {
                    sendError(sender, "The desired script file already exists!");
                    return false;
                }

                scriptFile.getParentFile().mkdirs();
                try {
                    scriptFile.createNewFile();
                    FileWriter writer = new FileWriter(scriptFile);
                    writer.write("#!──────────────────────────────────────────────────────────────────────────────────────────────────\n");
                    writer.write("#!		THIS FILE WAS CREATED BY SK_AUTORELOAD (SKAR) THE HELPER FOR A BETTER WORKFLOW\n");
                    writer.write("#!──────────────────────────────────────────────────────────────────────────────────────────────────\n");
                    writer.write("\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    sendError(sender, "Unable to create the script file, check the console for further error!");
                    return false;
                }
                sendSuccess(sender, "The file has been created! Enabling AutoReload ...");
                SkarFile file = SkarFile.createDefaultSkarFile(scriptFile);
                file.setAutoReload(true);
                sendSuccess(sender, "The file has been enabled AutoReload!");

                return true;
            case "enable":
                if (args.length == 1) {
                    sendError(sender, "You have to define a script file to enable!");
                    return false;
                }
                String input = args[1].endsWith(".sk") ? args[1] : args[1] + ".sk";
                File script = new File("plugins/Skript/scripts/" + input);
                if (!script.exists()) {
                    sendError(sender, "The file you have specified doesn't exist!");
                    return false;
                }
                file = new SkarFile(SkarFile.getConfigFileFromScript(script));
                if (file.isAutoReload()) {
                    sendError(sender, "This file already has the AutoReload enabled!");
                    return false;
                }
                file.setAutoReload(true);
                sendSuccess(sender, "Successfully enabled AutoReload for the script " + input + "!");
                return true;
            case "disable":
                if (args.length == 1) {
                    sendError(sender, "You have to define a script file to enable!");
                    return false;
                }
                input = args[1].endsWith(".sk") ? args[1] : args[1] + ".sk";
                script = new File("plugins/Skript/scripts/" + input);
                if (!script.exists()) {
                    sendError(sender, "The file you have specified doesn't exist!");
                    return false;
                }
                file = new SkarFile(SkarFile.getConfigFileFromScript(script));
                if (!file.isAutoReload()) {
                    sendError(sender, "This file doesn't have the AutoReload enabled!!");
                    return false;
                }
                file.setAutoReload(false);
                sendSuccess(sender, "Successfully disabled AutoReload for the script " + input + "!");
                return true;
        }

        sendHelp(sender);
        return true;
    }

    private static void sendInfo(CommandSender sender, String msg) {
        sender.sendMessage(new String[] {Utils.colored("&3&l» &b" + msg)});
    }

    private static void sendError(CommandSender sender, String msg) {
        sender.sendMessage(new String[] {Utils.colored("&4&l» &c" + msg)});
    }

    private static void sendSuccess(CommandSender sender, String msg) {
        sender.sendMessage(new String[] {Utils.colored("&2&l» &a" + msg)});
    }

    private static void sendReload(CommandSender sender) {
        final List<String> txts = new ArrayList<>();
        txts.add(Utils.colored("&3~~~~~~~~~~~~~~~~~~~~~~"));
        txts.add(Utils.colored("&6Reloading Skar ..."));
        Skar.reload(false);
        txts.add(Utils.colored("&2Reloaded Skar successfully!"));
        txts.add(Utils.colored("&3~~~~~~~~~~~~~~~~~~~~~~"));
        sender.sendMessage(txts.toArray(new String[0]));
    }

    private static void sendSkarInfo(CommandSender sender) {
        final PluginManager manager = Skar.getInstance().getServer().getPluginManager();
        final List<String> txts = new ArrayList<>();
        txts.add(Utils.colored("&6┌────────────────────────────────────────────────────────────┤"));
        txts.add(Utils.colored("&6│ &a&nSkar v&2" + Skar.getInstance().getDescription().getVersion() + " &aby &2ItsTheSky & Trason"));
        txts.add(Utils.colored("&6│ "));

        txts.add(Utils.colored("&6│ &0&m----------&a&l&nServer Information&0&m----------"));
        txts.add(Utils.colored("&6│ &f&lServer Version: &a&l" + Skar.getInstance().getServer().getVersion()));
        txts.add(Utils.colored("&6│ &f&lBukkit Version: &a&l" + Skar.getInstance().getServer().getBukkitVersion()));
        if (Utils.getServerCountry() != null)
            txts.add(Utils.colored("&6│ &f&lCountry: &a&l" + Utils.getServerCountry()));
        txts.add(Utils.colored("&6│ "));

        txts.add(Utils.colored("&6│ &0&m----------&a&l&nPlugins Information (&2&l&n"+manager.getPlugins().length+"&a&l&n)&0&m----------"));
        for (Plugin pl : Arrays.stream(manager.getPlugins()).map(Plugin::getName).sorted().map(name -> Bukkit.getServer().getPluginManager().getPlugin(name)).collect(Collectors.toList()))
            txts.add(Utils.colored("&6│   &2- &a" + pl.getName() + " (&2v"+pl.getDescription().getVersion()+"&a)"));
        txts.add(Utils.colored("&6│ "));

        txts.add(Utils.colored("&6│ &0&m----------&a&l&nMemory Information &0&m----------"));
        txts.add(Utils.colored("&6│    &fUsed Memory: &a" + Utils.rounded((Runtime.getRuntime().maxMemory()/1000000) - (Runtime.getRuntime().freeMemory()/1000000))  + " &fMB"));
        txts.add(Utils.colored("&6│    &fFree Memory: &a" + Utils.rounded(Runtime.getRuntime().freeMemory()/1000000) + " &fMB"));
        txts.add(Utils.colored("&6│    &fMax Memory: &a" + Utils.rounded(Runtime.getRuntime().maxMemory()/1000000) + " &fMB"));
        txts.add(Utils.colored("&6│ "));

        txts.add(Utils.colored("&6│ &0&m----------&a&l&nSkar File(s) (&2&l&n"+ SkarManager.getMemoriesFiles().size() +"&a&l&n) &0&m----------"));
        txts.add(Utils.colored("&6│    &f&lEnabled (&2"+SkarManager.getAutoReload(true).size()+"&f)"));
        if (SkarManager.getAutoReload(true).size() == 0)
            txts.add(Utils.colored("&6│       &cNone"));
        final List<SkarFile> enabledFiles = SkarManager.getAutoReload(true);
        enabledFiles.sort(Comparator.comparing(SkarFile::getName));
        for (SkarFile file : enabledFiles)
            txts.add(Utils.colored("&6│       &2- &a" + file.getName() + " (&2"+file.getScriptName()+"&a)"));

        txts.add(Utils.colored("&6│ "));
        txts.add(Utils.colored("&6│    &f&lDisabled (&2"+SkarManager.getAutoReload(false).size()+"&f)"));
        if (SkarManager.getAutoReload(false).size() == 0)
            txts.add(Utils.colored("&6│       &cNone"));
        final List<SkarFile> disabledFiles = SkarManager.getAutoReload(false);
        disabledFiles.sort(Comparator.comparing(SkarFile::getName));
        for (SkarFile file : disabledFiles)
            txts.add(Utils.colored("&6│       &2- &a" + file.getName() + " (&2"+file.getScriptName()+"&a)"));

        txts.add(Utils.colored("&6│ "));
        txts.add(Utils.colored("&6└────────────────────────────────────────────────────────────┤"));
        sender.sendMessage(txts.toArray(new String[0]));
    }

    private static void sendHelp(CommandSender sender) {
        final List<String> txts = Arrays.asList(
                Utils.colored("&6┌────────────────────────────────────────────────────────────┤"),
                Utils.colored("&6│ &a&nSkar v&2" + Skar.getInstance().getDescription().getVersion() + " &aby &2ItsTheSky & Trason"),
                Utils.colored("&6│ "),
                Utils.colored("&6│ &b/skar help &7- &3Show this help page"),
                Utils.colored("&6│ &b/skar enable <file> &7- &3Enable the AutoReload of a script file"),
                Utils.colored("&6│ &b/skar disable <file> &7- &3Disable the AutoReload of a script File"),
                Utils.colored("&6│ &b/skar create <file> &7- &3Create a new script file & enable the AutoReload for it"),
                Utils.colored("&6│ &b/skar info &7- &3Show every informations about Skar & your server"),
                Utils.colored("&6│ &b/skar reload &7- &3Reload Skar file configuration system"),
                Utils.colored("&6│ "),
                Utils.colored("&6└────────────────────────────────────────────────────────────┤")
        );
        sender.sendMessage(txts.toArray(new String[0]));
    }
}
