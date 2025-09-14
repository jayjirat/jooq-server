package jooq.server.myserver.utils;

public class LogUtil {
    public static void logMemory(String tag) {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        long free = runtime.freeMemory();

        System.out.println("[" + tag + "] Used: " + used / 1024 / 1024 + " MB, "
                + "Free: " + free / 1024 / 1024 + " MB, "
                + "Max: " + max / 1024 / 1024 + " MB");
    }

}
