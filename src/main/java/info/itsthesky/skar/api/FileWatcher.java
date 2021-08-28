package info.itsthesky.skar.api;

import ch.njol.skript.ScriptLoader;
import info.itsthesky.skar.Skar;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileWatcher extends Thread {
    private final File file;
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final AtomicBoolean shouldExecute = new AtomicBoolean(true);

    public FileWatcher(File file) {
        this.file = file;
    }

    public boolean isStopped() { return stop.get(); }
    public void stopThread() { stop.set(true); }

    public void doOnChange(File file) {
        Utils.sync(() -> {

            if (file.length() <= 0) return;

            // We create the Skar file datas through the script file
            final SkarFile skar = new SkarFile(SkarFile.getConfigFileFromScript(file));
            // And we can check if the auto relaod is enabled. If it is not, then we return to not execute the reload code.
            if (!skar.isAutoReload()) return;

            if (shouldExecute.get()) {
                shouldExecute.set(false);
                Skar.log("Reloading Script file " + file.getPath() +" ...");
                Skar.debug("File changed: " + file.getPath());
                ScriptLoader.reloadScript(file);
                Skar.success("Reload success! Check above if there's any Skript error.");
                Bukkit.getScheduler().runTaskLater(Skar.getInstance(),
                        () -> shouldExecute.set(true),
                        1);
            }
        });
    }

    @Override
    public void run() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            Path path = file.toPath().getParent();
            path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            while (!isStopped()) {
                WatchKey key;
                try { key = watcher.poll(25, TimeUnit.MILLISECONDS); }
                catch (InterruptedException e) { return; }
                if (key == null) { Thread.yield(); continue; }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        Thread.yield();
                        continue;
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY
                            && filename.toString().equals(file.getName())) {
                            doOnChange(file);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE
                            && filename.toString().equals(file.getName())) {
                        Utils.sync(() -> {

                            List<String> txts = new ArrayList<>();
                            txts.add(Utils.colored("&6┌────────────────────────────────────────────────────────────┤"));
                            txts.add(Utils.colored("&6│ "));
                            txts.add(Utils.colored(">> &6Auto reload"));
                            txts.add(Utils.colored("&6│"));
                            txts.add(Utils.colored("&6│&nSK_Autoreload Files:&r &c Deleted!"));
                            txts.add(Utils.colored("&6│ The script '" + file.getPath() + "' doesn't exist"));
                            txts.add(Utils.colored("&6│ anymore so removed from AutoReload system!"));
                            txts.add(Utils.colored("&6└────────────────────────────────────────────────────────────┤"));
                            Skar.getInstance().getServer().getConsoleSender().sendMessage(txts.toArray(new String[0]));

                            Skar.debug("Deleting file from AutoReload: " + file.getPath());
                            SkarFile.getConfigFileFromScript(file).delete();

                        });
                    }
                    boolean valid = key.reset();
                    if (!valid) { break; }
                }
                Thread.yield();
            }
        } catch (Throwable e) {
            // Log or rethrow the error
        }
    }
}