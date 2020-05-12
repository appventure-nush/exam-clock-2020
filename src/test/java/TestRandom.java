import sg.edu.appventure.examclock.connection.Encryption;

public class TestRandom {
    public static void main(String... args) {
        byte[] keyFromPassword = Encryption.createKeyFromPassword("Hello World!");
        byte[] encrypt = Encryption.encrypt(keyFromPassword, "This is encrypted!".getBytes());
        byte[] decrypt = Encryption.decrypt(keyFromPassword, encrypt);
        System.out.println(new String(decrypt));
    }
}
