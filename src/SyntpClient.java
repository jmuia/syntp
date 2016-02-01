import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by jmuia on 2016-01-24.
 */


public final class SyntpClient {
    Socket connection;
    PrintWriter out;
    BufferedReader in;

    public void connect(String ipAddress, int portNumber) throws Exception {
        connection = new Socket(ipAddress, portNumber);
        out = new PrintWriter(connection.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    public void makeRequest(String request) {
        out.println(request);
        out.flush();
    }

    public void close() throws IOException {
        connection.close();
        out.close();
        in.close();
    }

    public static void main(String[] argv) throws Exception {
        new SyntpClientGUI(new SyntpClient());
    }

}