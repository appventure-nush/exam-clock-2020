//package sg.edu.appventure.examclock.connection;
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.qrcode.QRCodeWriter;
//import com.nimbusds.jose.EncryptionMethod;
//import com.nimbusds.jose.jwk.JWK;
//import com.nimbusds.jose.jwk.OctetSequenceKey;
//import com.nimbusds.jose.util.JSONObjectUtils;
//import net.minidev.json.JSONObject;
//import sg.edu.appventure.examclock.model.Key;
//
//import javax.crypto.*;
//import javax.crypto.spec.GCMParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.awt.image.BufferedImage;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.text.ParseException;
//import java.util.Arrays;
//
//public class Encryption {
//    public static final int AES_KEY_SIZE = 256;
//    public static final int GCM_NONCE_LENGTH = 12;
//    public static final int GCM_TAG_LENGTH = 16;
//    private static final byte[] AAD = "ExamClock Encrypted Protocol".getBytes();
//
//    public static byte[] createKey() {
//        try {
//            SecureRandom random = SecureRandom.getInstanceStrong();
//            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//            keyGen.init(AES_KEY_SIZE, random);
//            SecretKey key = keyGen.generateKey();
//            return key.getEncoded();
//        } catch (NoSuchAlgorithmException e) {
//            return null;
//        }
//    }
//
//    public static byte[] encrypt(byte[] encodedKey, byte[] source) {
//        try {
//            SecureRandom random = SecureRandom.getInstanceStrong();
//            SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
//            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//            final byte[] nonce = new byte[GCM_NONCE_LENGTH];
//            random.nextBytes(nonce);
//            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
//            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
////            cipher.updateAAD(AAD);
//            byte[] cipherText = cipher.doFinal(source);
//            byte[] combined = new byte[nonce.length + cipherText.length];
//            System.arraycopy(nonce, 0, combined, 0, GCM_NONCE_LENGTH);
//            System.arraycopy(cipherText, 0, combined, GCM_NONCE_LENGTH, cipherText.length);
//            return combined;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static byte[] decrypt(byte[] encodedKey, byte[] cipherText) {
//        try {
//            byte[] nonce = Arrays.copyOf(cipherText, GCM_NONCE_LENGTH);
//            cipherText = Arrays.copyOfRange(cipherText, GCM_NONCE_LENGTH, cipherText.length);
//            SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
//            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
//            cipher.init(Cipher.DECRYPT_MODE, key, spec);
////            cipher.updateAAD(AAD);
//            return cipher.doFinal(cipherText);
//        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static BufferedImage generateQRCode(Key key, String ip) throws WriterException {
//        return MatrixToImageWriter.toBufferedImage(new QRCodeWriter().encode(new QRCodeContent(key, ip).toString(), BarcodeFormat.QR_CODE, 512, 512));
//    }
//
//    public static JSONObject toJSONObject(byte[] bytes) {
//        try {
//            return JSONObjectUtils.parse(new String(bytes));
//        } catch (ParseException e) {
//            return null;
//        }
//    }
//
//    private static class QRCodeContent extends JSONObject {
//        public QRCodeContent(Key key, String ip) {
//            SecretKey secretKey = new SecretKeySpec(key.key, 0, key.key.length, "AES");
//            JWK jwk = new OctetSequenceKey.Builder(secretKey)
//                    .keyID(key.id)
//                    .algorithm(EncryptionMethod.A256GCM) // indicate the intended key alg (optional)
//                    .build();
//            put("ip", ip);
//            put("id", key.id);
//            put("type", key.type.toString());
//            put("jwk", jwk.toJSONObject());
//            System.out.println("JWK = " + jwk.toJSONObject().toJSONString());
//            System.out.println("KEY = " + new String(Base64.encode(key.key)));
//        }
//    }
//
//    public static byte[] createKeyFromPassword(String password) {
//        int hash = password.hashCode();
//        byte[] key = new byte[AES_KEY_SIZE / 8];
//        for (int i = 0; i < AES_KEY_SIZE / 8; i++) {
//            key[i] = (byte) (Math.sin(i + hash) * 256 - 127);
//        }
//        SecretKey secretKey = new SecretKeySpec(key, 0, key.length, "AES");
//        JWK jwk = new OctetSequenceKey.Builder(secretKey)
//                .keyID("password")
//                .algorithm(EncryptionMethod.A256GCM) // indicate the intended key alg (optional)
//                .build();
//        System.out.println("JWK = " + jwk.toJSONObject().toJSONString());
//        return key;
//    }
//}
