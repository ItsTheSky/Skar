package info.itsthesky.skar.api;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import info.itsthesky.skar.Skar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

/**
 * Every of these method was made and owned by ItsTheSky!
 * @author ItsTheSky
 */
public final class Utils {

    private static final NavigableMap<Long, String> suffixes = new TreeMap<> ();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(Skar.getInstance(), runnable);
    }

    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(Skar.getInstance(), runnable);
    }

    public static String getServerCountry() {

        String result = "";
        try {
            result = readToString("http://proxycheck.io/v2/" + getServerIP() + "?vpn=1&asn=1");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            return result.split("\"country\": \"")[1].split("\",")[0];
        } catch (Exception ex) {
            return null;
        }
    }

    public static Integer rounded(Number n) {
        try {
            return Integer.valueOf(n.toString().split("\\.")[0]);
        } catch (Exception ex) {
            return n.intValue();
        }
    }

    public static String getServerIP() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("google.com", 80));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
        return socket.getLocalAddress().toString().replaceAll("/", "");
    }

    public static String readToString(String targetURL) throws IOException
    {
        URL url = new URL(targetURL);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(url.openStream()));

        StringBuilder stringBuilder = new StringBuilder();

        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null)
        {
            stringBuilder.append(inputLine);
            stringBuilder.append(System.lineSeparator());
        }

        bufferedReader.close();
        return stringBuilder.toString().trim();
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String getFileContent(File file) {
        try {
            return Files.asCharSource(file, Charsets.UTF_8).read();
        } catch (IOException e) {
            Skar.error("Internal error occured: " + e.getMessage());
        }
        return "ERROR";
    }

    public static String colored(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> coloreds(List<String> strings) {
        List<String> c = new ArrayList<>();
        for (String s : strings)
            c.add(colored(s));
        return c;
    }

    public static <T> String[] toStrings(Collection<T> entities, Function<T, String> toString) {
        List<String> l = new ArrayList<>();
        for (T e : entities)
            l.add(toString.apply(e));
        return l.toArray(new String[0]);
    }

    public static int randomInteger(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    @SuppressWarnings("unchecked")
    public static <T> T randomCollectionElement(Collection<T> collection) {
        final T[] array = (T[]) collection.toArray(new Object[0]);
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    // ################### DATA MANAGING ################# //

    public static void setPlayerData(Player player, String key, String value) {
        player.setMetadata(key, new FixedMetadataValue(Skar.getInstance(), value));
    }

    public static String getPlayerData(Player player, String key, String defaultValue) {
        try {
            String value = String.valueOf(player.getMetadata(key).get(0).value());
            return value == null ? defaultValue : value;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static void removePlayerData(Player player, String key) {
        player.removeMetadata(key, Skar.getInstance());
    }
}
