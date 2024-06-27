package server.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class UserConnected {
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private Map<String, DefaultListModel<String>> clientLogs;
    private Map<String, Integer> clientRequestCount;

    public UserConnected() {
        clientLogs = new HashMap<>();
        clientRequestCount = new HashMap<>();

        mainPanel = new JPanel(new BorderLayout());
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        mainPanel.add(new JScrollPane(buttonPanel), BorderLayout.CENTER);

        JFrame frame = new JFrame("Users Connected");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(400, 400));
    }

    public void addClientIP(String clientIP) {
        SwingUtilities.invokeLater(() -> {
            if (!clientLogs.containsKey(clientIP)) {
                clientLogs.put(clientIP, new DefaultListModel<>());
                JButton clientButton = new JButton(clientIP);
                clientButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new UserLog(clientIP, clientLogs.get(clientIP));
                    }
                });
                buttonPanel.add(clientButton);
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }
        });
    }

    public void addRequest(String clientIP, String request) {
        SwingUtilities.invokeLater(() -> {
            int count = clientRequestCount.getOrDefault(clientIP, 0) + 1;
            clientRequestCount.put(clientIP, count);
            clientLogs.get(clientIP).addElement("[" + clientIP + " - Request " + count + "]: " + request);
        });
    }

    public void addResponse(String clientIP, String response) {
        SwingUtilities.invokeLater(() -> {
            int count = clientRequestCount.get(clientIP);
            clientLogs.get(clientIP).addElement("[" + clientIP + " - Response " + count + "]: " + response);
        });
    }
}
