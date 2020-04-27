package sg.edu.appventure.examclock.connection;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Encryption {
    public static final int AES_KEY_SIZE = 128;
    public static final int GCM_NONCE_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
    private static final byte[] AAD = "ExamClock Encrypted Protocol".getBytes();

    public static byte[] createKey() {
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE, random);
            SecretKey key = keyGen.generateKey();
            return key.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static byte[] encrypt(byte[] encodedKey, byte[] source) {
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final byte[] nonce = new byte[GCM_NONCE_LENGTH];
            random.nextBytes(nonce);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            cipher.updateAAD(AAD);
            byte[] cipherText = cipher.doFinal(source);
            byte[] combined = new byte[nonce.length + cipherText.length];
            System.arraycopy(nonce, 0, combined, 0, GCM_NONCE_LENGTH);
            System.arraycopy(cipherText, 0, combined, GCM_NONCE_LENGTH, cipherText.length);
            return combined;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] encodedKey, byte[] cipherText) {
        try {
            byte[] nonce = Arrays.copyOf(cipherText, GCM_NONCE_LENGTH);
            cipherText = Arrays.copyOfRange(cipherText, GCM_NONCE_LENGTH, cipherText.length);
            SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(AAD);
            return cipher.doFinal(cipherText);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage generateQRCode(byte[] key, byte[] content) throws WriterException {
        System.out.println("QR Code Generation key(" + key.length + ") content(" + content.length + ")");
        content = encrypt(key, content);
        assert content != null;
        byte[] combined = new byte[key.length + content.length];
        System.arraycopy(key, 0, combined, 0, key.length);
        System.arraycopy(content, 0, combined, key.length, content.length);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return MatrixToImageWriter.toBufferedImage(qrCodeWriter.encode(new String(Base64.encode(combined)), BarcodeFormat.QR_CODE, 500, 500));
    }

    public static byte[] readQRCode(BufferedImage source) throws NotFoundException, FormatException, ChecksumException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(source)));
        Result qrCodeResult = new QRCodeReader().decode(binaryBitmap);
        String text = qrCodeResult.getText();
        return Base64.decode(text.toCharArray());
    }

    public static byte[] toBytes(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static <T> T toObject(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
