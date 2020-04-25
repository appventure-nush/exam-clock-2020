import java.io.IOException;
import java.net.InetAddress;

public class LanTest {
    public static void main(String... args) throws IOException {
        System.out.println("Checking subnet");
        checkHosts("192.168.0");
    }

    public static void checkHosts(String subnet) throws IOException {
        int timeout = 1000;
        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;
            if (InetAddress.getByName(host).isReachable(timeout)) {
                System.out.println(host + " is reachable");
            }
        }
    }
}
