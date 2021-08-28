package info.itsthesky.skar.api;

import de.leonhard.storage.Yaml;
import info.itsthesky.skar.Skar;

import java.io.File;
import java.io.IOException;

public class SkarFile {

    private final String name;
    private final String scriptName;
    private final File file;
    private boolean isAutoReload;

    public SkarFile(File file) {
        this(new Yaml(file));
    }

    public SkarFile(Yaml config) {

        this.name = config.getOrSetDefault("Name", config.getFile().getName());
        this.scriptName = config.getFile().getName().replaceAll("\\.yml", ".sk");
        this.file = config.getFile();
        this.isAutoReload = config.getOrSetDefault("AutoReload", false);

    }

    public String getScriptName() {
        return scriptName;
    }

    public String getName() {
        return name;
    }

    public void setAutoReload(boolean autoReload) {
        isAutoReload = autoReload;
        new Yaml(file).set("AutoReload", autoReload);
        Skar.reload(false);
    }

    public File getFile() {
        return file;
    }

    public boolean isAutoReload() {
        return isAutoReload;
    }

    public static boolean doesSkarFileExist(File script) {
        return getConfigFileFromScript(script).exists();
    }

    public static File getConfigFileFromScript(File script) {
        return new File(Skar.getInstance().getDataFolder(), "skars/" + script.getName().split("\\.")[0] + ".yml");
    }

    public static SkarFile createDefaultSkarFile(File script) {
        final File file = getConfigFileFromScript(script);

        if (file.exists())
            return null;

        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Skar.error("Unable to create default Skar file of " + script.getName());
            return null;
        }

        final Yaml config = new Yaml(file);

        config.set("Name", file.getName());
        config.set("AutoReload", false);

        return new SkarFile(config);
    }
}
