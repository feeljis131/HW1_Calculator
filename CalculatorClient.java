//CalculatorClient
import java.io.*;
import java.net.*;
import java.util.*;

public class CalculatorClient {
    private static class HostPort {
        final String host; final int port;
        HostPort(String h, int p) { host = h; port = p; }
    }

    // server_info.dat 에서 host/port 읽기 (없으면 기본값)
    private static HostPort loadConfigOrDefault(String path) {
        String host = "127.0.0.1";
        int port = 1234;
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            p.load(new InputStreamReader(fis));
            host = p.getProperty("host", host).trim();
            String ps = p.getProperty("port");
            if (ps != null) port = Integer.parseInt(ps.trim());
            System.out.println("[Client] Loaded " + path + " -> " + host + ":" + port);
        } catch (FileNotFoundException e) {
            System.out.println("[Client] No " + path + ". Use default " + host + ":" + port);
        } catch (Exception e) {
            System.out.println("[Client] Config error. Use default " + host + ":" + port + " (" + e.getMessage() + ")");
        }
        return new HostPort(host, port);
    }

    public static void main(String[] args) {
        HostPort cfg = loadConfigOrDefault("server_info.dat");
        String host = (args.length > 0) ? args[0] : cfg.host;
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : cfg.port;

        System.out.println("[Client] Connect to " + host + ":" + port);
        System.out.println("형식: <CMD> <A> <B>   (CMD: ADD|SUB|MUL|DIV)");
        System.out.println("종료: BYE\n");

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             Scanner sc = new Scanner(System.in)) {

            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) break;
                String msg = sc.nextLine().trim();
                if (msg.isEmpty()) continue;

                out.write(msg + "\n");
                out.flush();

                String reply = in.readLine();
                if (reply == null) {
                    System.out.println("[Client] Server closed.");
                    break;
                }
                System.out.println(reply);

                if ("BYE".equalsIgnoreCase(msg) || "BYE".equalsIgnoreCase(reply)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[Client] Error: " + e.getMessage());
        }
        System.out.println("[Client] Bye.");
    }
}