package com.bigfake.remittance.util;
import java.security.*; import java.nio.charset.StandardCharsets;
public final class ChecksumUtils {
    private ChecksumUtils() {}
    public static String sha256(byte[] bytes) {
        try { byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes); StringBuilder out=new StringBuilder();
            for(byte b:digest) out.append(String.format("%02x",b)); return out.toString();
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static String sha256(String value) { return sha256(value.getBytes(StandardCharsets.UTF_8)); }
}
