package info.itsthesky.skar.api;

import java.util.ArrayList;
import java.util.List;

public class WatcherManager {

    private static final List<FileWatcher> WATCHERS = new ArrayList<>();

    public static void add(FileWatcher watcher) {
        watcher.start();
        WATCHERS.add(watcher);
    }

    public static void remove(FileWatcher watcher) {
        WATCHERS.remove(watcher);
    }

    public static List<FileWatcher> getWatchers() {
        return WATCHERS;
    }

    public static void reset() {
        WATCHERS.forEach(FileWatcher::stopThread);
        WATCHERS.clear();
    }
}
