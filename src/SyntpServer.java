import java.io.IOException;
import java.net.* ;

public final class SyntpServer {
    public static void main(String argv[]) throws Exception {
        int port;

        if (argv.length != 1) {
            throw new Exception("Must pass a single argument, the port number.");
        }

        try {
            port = Integer.parseInt(argv[0]);
        } catch (Exception e) {
            String msg = String.format("Port must be an integer, got: %s", argv[0]);
            throw new Exception(msg);
        }

        // Establish the listen socket.
        ServerSocket socket = new ServerSocket(port);

        // Process HTTP service requests in an infinite loop.
        while (true) {
            try {
                // Listen for a TCP connection request.
                Socket connection = socket.accept();

                // Construct an object to process the HTTP request message.
                SyntpConnection request = new SyntpConnection(connection);

                // Create a new thread to process the request.
                Thread thread = new Thread(request);

                // Start the thread.
                thread.start();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
