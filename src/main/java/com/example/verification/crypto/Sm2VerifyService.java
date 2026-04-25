package com.example.verification.crypto;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;

@Component
public class Sm2VerifyService {

    public boolean verifyHex(String publicKeyHex, String digestHex, String signatureHex) {
        try {
            X9ECParameters parameters = GMNamedCurves.getByName("sm2p256v1");
            ECDomainParameters domainParameters = new ECDomainParameters(
                    parameters.getCurve(), parameters.getG(), parameters.getN(), parameters.getH());

            byte[] publicKeyBytes = hexToBytes(publicKeyHex);
            ECPoint point = parameters.getCurve().decodePoint(publicKeyBytes);
            ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(point, domainParameters);

            byte[] digestBytes = hexToBytes(digestHex);
            byte[] signatureDer = toDerSignature(hexToBytes(signatureHex));

            SM2Signer signer = new SM2Signer();
            signer.init(false, publicKeyParameters);
            signer.update(digestBytes, 0, digestBytes.length);
            return signer.verifySignature(signatureDer);
        } catch (Exception ex) {
            return false;
        }
    }

    private byte[] toDerSignature(byte[] signatureBytes) throws IOException {
        if (signatureBytes.length == 64) {
            byte[] rBytes = new byte[32];
            byte[] sBytes = new byte[32];
            System.arraycopy(signatureBytes, 0, rBytes, 0, 32);
            System.arraycopy(signatureBytes, 32, sBytes, 0, 32);
            BigInteger r = new BigInteger(1, rBytes);
            BigInteger s = new BigInteger(1, sBytes);
            ASN1EncodableVector vector = new ASN1EncodableVector();
            vector.add(new ASN1Integer(r));
            vector.add(new ASN1Integer(s));
            return new DERSequence(vector).getEncoded();
        }
        return signatureBytes;
    }

    private byte[] hexToBytes(String hex) {
        String normalized = hex.startsWith("04") || hex.length() % 2 == 0 ? hex : "0" + hex;
        int len = normalized.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(normalized.charAt(i), 16) << 4)
                    + Character.digit(normalized.charAt(i + 1), 16));
        }
        return data;
    }
}
