package info.itsthesky.skar.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SkarManager {

    private static final List<SkarFile> MEMORIES_FILES = new ArrayList<>();

    public static void reset() {
        MEMORIES_FILES.clear();
    }

    public static void add(SkarFile file) {
        if (MEMORIES_FILES.contains(file))
            return;
        MEMORIES_FILES.add(file);
    }

    public static List<SkarFile> getMemoriesFiles() {
        return MEMORIES_FILES;
    }

    public static void remove(SkarFile file) {
        if (!MEMORIES_FILES.contains(file))
            return;
        file.getFile().delete();
        MEMORIES_FILES.remove(file);
    }

    public static boolean isCached(SkarFile file) {
        return MEMORIES_FILES.contains(file);
    }

    public static List<SkarFile> getAutoReload(boolean autoReload) {
        if (autoReload) {
            return MEMORIES_FILES
                    .stream()
                    .filter(SkarFile::isAutoReload)
                    .collect(Collectors.toList());
        } else {
            return MEMORIES_FILES
                    .stream()
                    .filter(skar -> !skar.isAutoReload())
                    .collect(Collectors.toList());
        }
    }

    public static SkarFile search(File file) {
        Optional<SkarFile> op = MEMORIES_FILES
                .stream()
                .filter(skar -> skar.getFile().compareTo(file) == 0)
                .findAny();
        return op.orElse(null);
    }

    public static boolean isAutoReload(File file) {
        final SkarFile skar = search(file);
        if (skar == null)
            return false;
        return skar.isAutoReload();
    }
}
