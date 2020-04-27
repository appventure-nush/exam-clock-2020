import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import sg.edu.appventure.examclock.connection.Encryption;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class TestEncryption {
    public static void main(String... args) throws WriterException, NotFoundException, FormatException, ChecksumException {
        BufferedImage bufferedImage = Encryption.generateQRCode(Encryption.createKey(), "123.456.7.89".getBytes());
        byte[] bytes = Encryption.readQRCode(bufferedImage);
        byte[] receivedKey = Arrays.copyOf(bytes, Encryption.AES_KEY_SIZE / 8);
        byte[] receivedMessage = Arrays.copyOfRange(bytes, Encryption.AES_KEY_SIZE / 8, bytes.length);
        byte[] original = Encryption.decrypt(receivedKey, receivedMessage);
        System.out.println(new String(original));
    }
}
