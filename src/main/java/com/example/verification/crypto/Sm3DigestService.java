package com.example.verification.crypto;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.springframework.stereotype.Component;

@Component
public class Sm3DigestService {

    public String digestHex(byte[] data) {
        SM3Digest sm3Digest = new SM3Digest();
        sm3Digest.update(data, 0, data.length);
        byte[] hash = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(hash, 0);
        return toHex(hash);
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
