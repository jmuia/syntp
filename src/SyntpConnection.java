import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;


final class SyntpConnection implements Runnable {
    final static SyntpVersion MIN_VERSION = new SyntpVersion(0, 0, 1);
    final static SyntpVersion MAX_VERSION = new SyntpVersion(0, 0, 1);
    final static String CRLF = "\r\n";
    final static String GET = "GET";
    final static String SET = "SET";
    final static String REMOVE = "REMOVE";
    Socket socket;
    BufferedReader br;
    DataOutputStream os;

    // Constructor
    public SyntpConnection(Socket socket) {
        System.out.println("New connection");
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            // Get a reference to the socket's input and output streams.
            InputStream is = socket.getInputStream();
            os = new DataOutputStream(socket.getOutputStream());
            // Set up input stream filters.
            br = new BufferedReader(new InputStreamReader(is));

            try {
                socket.setSoTimeout(2 * 60 * 1000); // 2 minutes
                handleConnection();
            } catch (SocketTimeoutException e) {
                respond(408);
            } finally {
                // Close streams and sockets
                System.out.println("Closing connection...");
                os.close();
                br.close();
                socket.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void respond(int statusCode, String responseBody) throws IOException {
        String responseLine = String.format("%s %s%s", MAX_VERSION, statusCode, CRLF);
        os.writeBytes(responseLine + responseBody);
    }

    private void respond(int statusCode) throws IOException {
        String responseLine = String.format("%s %s%s", MAX_VERSION, statusCode, CRLF);
        os.writeBytes(responseLine);
    }

    private Pair<String, SyntpVersion> parseRequestLine(String requestLine) throws SyntpError {
        StringTokenizer tokens = new StringTokenizer(requestLine);
        if (tokens.countTokens() != 2) {
            throw new SyntpError(400, String.format("Invalid Request-Line: %s", requestLine));
        }

        String method = tokens.nextToken();
        if (!method.equals(GET) && !method.equals(SET) && !method.equals(REMOVE)) {
            throw new SyntpError(400, String.format("Invalid method: %s", method));
        }

        String syntpVersionToken = tokens.nextToken();
        SyntpVersion syntpVersion;
        try {
            syntpVersion = new SyntpVersion(syntpVersionToken);
        } catch (Exception e) {
            throw new SyntpError(400, String.format("Invalid SYNTP-Version: %s", syntpVersionToken));
        }

        return new Pair<>(method, syntpVersion);
    }

    private String parseRequestBody(String method) throws SyntpError {
        String requestBody = "";
        try {
            switch (method) {
                case GET:
                case REMOVE:
                    requestBody += br.readLine() + CRLF;
                    break;
                case SET:
                    requestBody += br.readLine() + CRLF;
                    requestBody += br.readLine() + CRLF;
                    break;
            }
        } catch (IOException e) {
            throw new SyntpError(400, String.format("Request body was invalid for method %s", method));
        }
        return requestBody;
    }

    private void handleConnection() throws IOException {

        String requestLine = br.readLine();
        while (requestLine != null) {
            try {
                // Get the request line of the SYNTP request message.
                Pair<String, SyntpVersion> parsedRequestLine = parseRequestLine(requestLine);
                String method = parsedRequestLine.x;
                String requestBody = parseRequestBody(method);

                SyntpVersion syntpVersion = parsedRequestLine.y;
                if (syntpVersion.compareTo(MAX_VERSION) == 1 || syntpVersion.compareTo(MIN_VERSION) == -1) {
                    throw new SyntpError(505, String.format("Server does not support SYNTP-Version: %s", syntpVersion));
                }

                switch (method) {
                    case GET:
                        processGet(requestBody);
                        break;
                    case SET:
                        processSet(requestBody);
                        break;
                    case REMOVE:
                        processRemove(requestBody);
                        break;
                }

            } catch (SyntpError syntpError) {
                respond(syntpError.statusCode);
            } catch (Exception e) {
                System.out.println(e);
                respond(500);
            } finally {
                requestLine = br.readLine();
            }
        }
    }

    private void processGet(String requestBody) throws IOException, SyntpError {
        StringTokenizer tokenizer = new StringTokenizer(requestBody, CRLF);

        if (tokenizer.countTokens() < 1) {
            throw new SyntpError(400, "No Request-Body provided in GET request.");
        }

        String word = tokenizer.nextToken();

        if (word.isEmpty()) {
            throw new SyntpError(400, "No Request-Body provided in GET request.");
        }

        if (!SynonymList.exists(word)) {
            throw new SyntpError(404, String.format("The word %s does not exist on the server.", word));
        }

        HashSet<String> synonyms = SynonymList.getSynonyms(word);
        String response = "";
        for (String s: synonyms) {
            response += s + CRLF;
        }
        response += CRLF;
        respond(200, response);
    }

    private void processSet(String requestBody) throws IOException, SyntpError {
        StringTokenizer tokenizer = new StringTokenizer(requestBody, CRLF);

        if (tokenizer.countTokens() < 2) {
            throw new SyntpError(400, "Bad SET request.");
        }

        String a = tokenizer.nextToken();
        String b = tokenizer.nextToken();

        if (a.isEmpty() || b.isEmpty()) {
            throw new SyntpError(400, "Bad SET request.");
        }

        SynonymList.addSynonym(a, b);
        respond(200);
    }

    private void processRemove(String requestBody) throws IOException, SyntpError {
        StringTokenizer tokenizer = new StringTokenizer(requestBody, CRLF);

        if (tokenizer.countTokens() < 1) {
            throw new SyntpError(400, "No Request-Body provided in REMOVE request.");
        }

        String word = tokenizer.nextToken();

        if (word.isEmpty()) {
            throw new SyntpError(400, "No Request-Body provided in REMOVE request.");
        }

        if (!SynonymList.exists(word)) {
            throw new SyntpError(404, String.format("The word %s does not exist on the server.", word));
        }

        SynonymList.removeSynonym(word);
        respond(200);
    }
}