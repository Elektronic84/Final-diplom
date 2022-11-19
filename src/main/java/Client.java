import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final int PORT = 8989;
    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try (Socket clientSocket = new Socket(HOST, PORT);
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                System.out.println("Введите слово для поиска:");
                String input = scanner.nextLine();
                out.println(input);
                String read;
                while ((read = in.readLine()) != null) {
                    System.out.println(read);
                }
            } catch (IOException e) {
                System.out.println("Не могу стартовать сервер");
                e.printStackTrace();
            }
        }
    }
}
