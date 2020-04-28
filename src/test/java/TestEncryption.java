import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import sg.edu.appventure.examclock.connection.Base64;

import java.util.Arrays;

public class TestEncryption {
    public static void main(String... args) throws WriterException, NotFoundException, FormatException, ChecksumException {
//        BufferedImage bufferedImage = Encryption.generateQRCode(Encryption.createKey(), "123.456.7.89".getBytes());
//        byte[] bytes = Encryption.readQRCode(bufferedImage);
//        byte[] receivedKey = Arrays.copyOf(bytes, Encryption.AES_KEY_SIZE / 8);
//        byte[] receivedMessage = Arrays.copyOfRange(bytes, Encryption.AES_KEY_SIZE / 8, bytes.length);
//        byte[] original = Encryption.decrypt(receivedKey, receivedMessage);
//        System.out.println(new String(original));

        byte[] data = {64, -88, -122, -67, 28, 101, 10, 54, 76, 83, -65, 12, 38, -92, -93, 5, -40, -127, 82, 53, -56, -4, -103, 46, 16, 78, 31, -89, 48, 30, 100, -98, 55, 49, 125, 32, 57, -112, 6, 44, 45, 89, -10, 60, -31, 0};
        char[] encode = Base64.encode(data);
        System.out.println("First Test\n\nEncode " + data.length + "=>" + encode.length);
        System.out.println(Arrays.toString(data));
        System.out.println(Arrays.toString(encode));
        byte[] decode = Base64.decode(encode);
        System.out.println("\nDecode " + encode.length + "=>" + decode.length);
        System.out.println(Arrays.toString(encode));
        System.out.println(Arrays.toString(decode));
    }
}
