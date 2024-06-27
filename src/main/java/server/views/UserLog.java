package server.views;

import javax.swing.*;
import java.awt.*;

public class UserLog {
    private JFrame frame;
    private JList<String> logList;
    private DefaultListModel<String> listModel;

    public UserLog(String clientIP, DefaultListModel<String> logModel) {
        listModel = logModel;
        logList = new JList<>(listModel);

        frame = new JFrame("Log for " + clientIP);
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(logList), BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(400, 400));
    }
}
