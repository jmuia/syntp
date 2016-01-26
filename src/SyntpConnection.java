import java.io.* ;
import java.net.* ;
import java.util.* ;


final class SyntpConnection implements Runnable {
    final static SyntpVersion MIN_VERSION = new SyntpVersion(0, 0, 1);
    final static SyntpVersion MAX_VERSION = new SyntpVersion(0, 0, 1);
    final static String CRLF = "\r\n";
    final static String GET = "GET";
    final static String SET = "SET";
    final static String REMOVE = "REMOVE";
    Socket socket;

    // Constructor
    public SyntpConnection(Socket socket) throws Exception {
        System.out.println("New connection");
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            handleConnection();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private Pair<String, SyntpVersion> parseRequestLine(String requestLine) throws Exception {
        StringTokenizer tokens = new StringTokenizer(requestLine);
        if (tokens.countTokens() != 2) {
            throw new Exception("Invalid requestLine: # tokens != 2");
        }

        String method = tokens.nextToken();
        if (!method.equals(GET) && !method.equals(SET) && !method.equals(REMOVE)) {
            throw new Exception(String.format("Invalid method: %s", method));
        }

        SyntpVersion syntpVersion = new SyntpVersion(tokens.nextToken());

        return new Pair<>(method, syntpVersion);
    }

    private void handleConnection() throws Exception {
        // Get a reference to the socket's input and output streams.
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // Set up input stream filters.
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String requestLine = br.readLine();
        while (requestLine != null) {
            System.out.println(requestLine);
            try {
                // Get the request line of the SYNTP request message.
                Pair<String, SyntpVersion> parsedRequestLine = parseRequestLine(requestLine);
                SyntpVersion syntpVersion = parsedRequestLine.y;
                if (syntpVersion.compareTo(MAX_VERSION) == 1 || syntpVersion.compareTo(MIN_VERSION) == -1) {
                    throw new Exception(String.format("Server does not support SYNTP-Version: %s", syntpVersion));
                }
                String method = parsedRequestLine.x;

                switch (method) {
                    case GET:
                        processGet(br, os);
                        break;
                    case SET:
                        processSet(br, os);
                        break;
                    case REMOVE:
                        processRemove(br, os);
                        break;
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                requestLine = br.readLine();
            }
        }

        System.out.println("Closing connection...");
        // Close streams and sockets
        os.close();
        br.close();
        socket.close();
    }

    private void processGet(BufferedReader br, DataOutputStream os) throws Exception {
        String word = br.readLine();
        if (word.isEmpty()) {
            throw new Exception("No word in get request");
        }
        HashSet<String> synonyms = SynonymList.getSynonyms(word);
        String response = MAX_VERSION + " 200" + CRLF;
        for (String s: synonyms) {
            response += s + CRLF;
        }
        os.writeBytes(response + CRLF);
    }

    private void processSet(BufferedReader br, DataOutputStream os) throws Exception {
        String a = br.readLine();
        String b = br.readLine();
        if (a.isEmpty() || b.isEmpty()) {
            throw new Exception("Bad set request");
        }
        SynonymList.addSynonym(a, b);
        String response = MAX_VERSION + " 200" + CRLF;
        os.writeBytes(response);
    }

    private void processRemove(BufferedReader br, DataOutputStream os) throws Exception {
        String word = br.readLine();
        if (word.isEmpty()) {
            throw new Exception("No word in remove request");
        }
        SynonymList.removeSynonym(word);
        String response = MAX_VERSION + " 200" + CRLF;
        os.writeBytes(response);
    }
}