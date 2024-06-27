package server.views;

import javax.swing.*;
import java.awt.*;

public class UserConnected extends JDialog {
    private JPanel contentPane;
    private JPanel usersPanel;

    private List usersConnected;

    public UserConnected() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
    }

    public void updateUsers(String newIp) {
        usersConnected.add(newIp);
    }

    public static void main(String[] args) {
        UserConnected dialog = new UserConnected();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
