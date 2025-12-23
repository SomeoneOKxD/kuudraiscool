package someoneok.kic.utils;

import net.minecraft.util.Vec3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GeneralUtils {
    public static boolean vecEquals(Vec3 a, Vec3 b) {
        return a.distanceTo(b) < 0.05;
    }

    public static float round2(float value) {
        return Math.round(value * 100f) / 100f;
    }

    public static String compressJson(String json) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(byteStream)) {
            gzip.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return Base64.getEncoder().encodeToString(byteStream.toByteArray());
    }

    public static String decompressJson(String base64) throws IOException {
        byte[] data = Base64.getDecoder().decode(base64);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return out.toString("UTF-8");
        }
    }
}
