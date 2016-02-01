/**
 * Created by jmuia on 2016-02-01.
 */
final class SyntpLoadTester implements Runnable {
    private SyntpClient client;
    private int numRequests;



    public SyntpLoadTester(SyntpClient client, int numRequests) {
        this.client = client;
        this.numRequests = numRequests;
    }

    public void run() {
        try {
            client.connect("localhost", 5555);
            for (int i = 0; i < numRequests; i++) {

                String request = "SET SYNTP/0.0.1\r\ngavin\r\nthe man";
                client.makeRequest(request);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }



    public static void main(String[] argv) throws Exception {
        int numClients = 10;
        int numRequests = 100;

        if (argv.length == 2) {
            numClients = Integer.parseInt(argv[0]);
            numRequests = Integer.parseInt(argv[1]);
        }

        for (int i = 0; i < numClients; i++) {
            new Thread(new SyntpLoadTester(new SyntpClient(), numRequests)).start();
        }
    }
}
