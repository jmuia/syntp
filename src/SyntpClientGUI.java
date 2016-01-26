import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by jmuia on 2016-01-24.
 */


public class SyntpClientGUI {
    private SyntpClient syntpClient;
    private JFrame mainFrame;
    private JLabel ipLabel, portLabel, requestLabel, responseLabel, errorLabel;
    private JTextField ipTextField, portTextField;
    private JButton connectionButton, sendButton;
    private JTextArea requestTextArea, responseTextArea;
    /*
        IP address for server
        Port number for server
        connect / disconnect button
        text area to type in text to be sent to server
         text area to display result of the request.
         */

    public SyntpClientGUI(SyntpClient sc) {
        syntpClient = sc;

        mainFrame = new JFrame("SYNTP Client");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(Color.red);
        ipLabel = new JLabel("IP Address: ", SwingConstants.RIGHT);
        portLabel = new JLabel("Port Number: ", SwingConstants.RIGHT);
        ipTextField = new JTextField(16);
        portTextField = new JTextField(16);

        JPanel connectPanel = new JPanel();
        connectPanel.setLayout(new FlowLayout());
        connectPanel.add(ipLabel);
        connectPanel.add(ipTextField);
        connectPanel.add(portLabel);
        connectPanel.add(portTextField);
        connectionButton = new JButton("Connect");
        connectionButton.addActionListener(new ConnectionButtonListener());
        connectPanel.add(connectionButton);

        sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());

        requestTextArea = new JTextArea();
        responseTextArea = new JTextArea();

        mainFrame.setLayout(new GridLayout(5, 1));
        mainFrame.add(errorLabel);
        mainFrame.add(connectPanel);
        mainFrame.add(requestTextArea);
        mainFrame.add(sendButton);
        mainFrame.add(responseTextArea);
        mainFrame.setSize(500, 500);
        mainFrame.setVisible(true);



    }


    private class ConnectionButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (syntpClient.connection == null || syntpClient.connection.isClosed()) {
                System.out.println("connect");
                connect();
            } else {
                System.out.println("disconnect");
                disconnect();
            }
        }
    }

    private void connect() {
        String ipAddress = ipTextField.getText();
        int portNumber;

        try {
            portNumber = Integer.parseInt(portTextField.getText());
        } catch (Exception e) {
            errorLabel.setText(String.format("Invalid port number: \"%s\"", portTextField.getText()));
            return;
        }

        try {
            syntpClient.connect(ipAddress, portNumber);
        } catch (Exception e) {
            System.out.println("not connected ok :(");
            errorLabel.setText(e.toString());
            return;
        }

        System.out.println("connected ok?");

        connectionButton.setText("Disconnect");
        Thread responseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                try {
                    while ((response = syntpClient.in.readLine()) != null) {
                        responseTextArea.append(response + "\n");
                    }
                } catch (IOException e) {
                    errorLabel.setText(e.toString());
                    return;
                }
                errorLabel.setText("Server closed the socket.");
                try {
                    syntpClient.close();
                } catch (Exception e) {
                    errorLabel.setText(e.toString());
                }
                connectionButton.setText("Connect");
            }
        });
        responseThread.start();
    }

    private void disconnect() {
        try {
            syntpClient.close();
        } catch (IOException e) {
            errorLabel.setText(e.toString());
            return;
        }
        connectionButton.setText("Connect");
    }

    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String request = requestTextArea.getText();
            if (syntpClient.connection == null || syntpClient.connection.isClosed()) {
                errorLabel.setText("Need to connect to client first, ya shit");
                return;
            }
            syntpClient.makeRequest(request);
        }
    }

}