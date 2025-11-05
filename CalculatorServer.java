//CalculatorServer
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class CalculatorServer {

    // --- 요청 한 줄 처리: 의미 기반 응답 ---
    // 요청: "ADD 10 20" / "DIV 8 0" / "BYE"
    // 응답: 성공 -> "value=<int>"
    //       실패 -> "Incorrect: <ERROR_CODE>"
    private static String handle(String line) {
        if (line == null) return "Incorrect: BAD_SYNTAX";
        line = line.trim();
        if (line.isEmpty()) return "Incorrect: BAD_SYNTAX";
        if (line.equalsIgnoreCase("BYE")) return "BYE";

        String[] t = line.split("\\s+");
        if (t.length != 3) return "Incorrect: ARG_COUNT";

        String cmd = t[0].toUpperCase();
        int a, b;
        try {
            a = Integer.parseInt(t[1]);
            b = Integer.parseInt(t[2]);
        } catch (NumberFormatException e) {
            return "Incorrect: BAD_SYNTAX";
        }

        long result;
        switch (cmd) {
            case "ADD": result = (long)a + b; break;
            case "SUB": result = (long)a - b; break;
            case "MUL": result = (long)a * b; break;
            case "DIV":
                if (b == 0) return "Incorrect: DIV_BY_ZERO";
                result = a / b; // 정수 나눗셈
                break;
            default: return "Incorrect: UNSUPPORTED_OP";
        }
        return "value=" + result;
    }

    // --- 클라이언트 1명을 담당하는 작업자 (Runnable) ---
    private static class ClientTask implements Runnable {
        private final Socket socket;
        ClientTask(Socket s) { this.socket = s; }

        @Override public void run() {
            System.out.println("[Server] Connected: " + socket.getRemoteSocketAddress());
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                String line;
                while ((line = in.readLine()) != null) {
                    String resp = handle(line);
                    if ("BYE".equals(resp)) {
                        out.write("BYE\n"); out.flush();
                        break;
                    }
                    out.write(resp + "\n");
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("[Server] I/O error: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException ignore) {}
                System.out.println("[Server] Closed: " + socket.getRemoteSocketAddress());
            }
        }
    }

    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 1234; // HW 기본 포트 예시
        System.out.println("[Server] Listening on port " + port);

        ExecutorService pool = Executors.newFixedThreadPool(20); // ThreadPool + Runnable
        try (ServerSocket listener = new ServerSocket(port)) {
            while (true) {
                Socket s = listener.accept();
                pool.execute(new ClientTask(s));
            }
        } catch (IOException e) {
            System.out.println("[Server] Fatal: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
}

