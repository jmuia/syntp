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
    private JLabel errorLabel;
    private JTextField ipTextField, portTextField;
    private JButton connectionButton, sendButton;
    private JTextArea requestTextArea, responseTextArea;

    public SyntpClientGUI(SyntpClient sc) {
        syntpClient = sc;

        mainFrame = new JFrame("SYNTP Client");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        /* Error Label */
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(Color.red);
        errorLabel.setBackground(Color.CYAN);
        mainFrame.add(errorLabel, gbc);

        /* Connect Options */
        gbc.gridy = 1;

        ipTextField = new JTextField(16);
        portTextField = new JTextField(16);
        connectionButton = new JButton("Connect");
        connectionButton.addActionListener(new ConnectionButtonListener());

        JPanel connectPanel = new JPanel();
        connectPanel.setLayout(new GridLayout(1, 5));
        connectPanel.add(new JLabel("IP Address: ", SwingConstants.RIGHT));
        connectPanel.add(ipTextField);
        connectPanel.add(new JLabel("Port Number: ", SwingConstants.RIGHT));
        connectPanel.add(portTextField);
        connectPanel.add(connectionButton);

        mainFrame.add(connectPanel, gbc);

        /* Request button / Response label */
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        sendButton = new JButton("Send Request");
        sendButton.addActionListener(new SendButtonListener());

        mainFrame.add(sendButton, gbc);

        gbc.gridx = 1;
        mainFrame.add(new JLabel("Response"), gbc);

        /* Response / Request text areas */
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        requestTextArea = new JTextArea();
        requestTextArea.setLineWrap(true);
        mainFrame.add(new JScrollPane(requestTextArea), gbc);

        gbc.gridx = 1;
        responseTextArea = new JTextArea();
        responseTextArea.setLineWrap(true);
        mainFrame.add(new JScrollPane(responseTextArea), gbc);

        mainFrame.setSize(500, 500);
        mainFrame.setVisible(true);

        new SpringLayout();

    }


    private class ConnectionButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (syntpClient.connection == null || syntpClient.connection.isClosed()) {
                connect();
            } else {
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
            errorLabel.setText(e.toString());
            return;
        }

        connectionButton.setText("Disconnect");
        errorLabel.setText("");

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
                errorLabel.setText("You are not connected to a SYNTP server.");
                return;
            }
            syntpClient.makeRequest(request);
        }
    }

}