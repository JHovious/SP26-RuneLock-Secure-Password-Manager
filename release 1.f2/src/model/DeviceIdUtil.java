package model;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Generates and stores a per-device ID.
 * Stored locally in "device.id" and never synced.
 * @Author: Justin Hovious
 */
public class DeviceIdUtil {

    static String FILE = "device.id";
    private static String cached = null;

    public static synchronized String getDeviceId() {
        if (cached != null) return cached;

        Path p = Paths.get(FILE);

        try {
            if (Files.exists(p)) {
                cached = Files.readString(p).trim();
                if (!cached.isEmpty()) return cached;
            }

            cached = UUID.randomUUID().toString();
            Files.writeString(p, cached);
            return cached;

        } catch (IOException e) {
            cached = UUID.randomUUID().toString();
            return cached;
        }
    }
}
