package client.views.recruiter;

import client.views.StartConnection;
import client.views.candidate.CandidateLogin;
import client.views.candidate.CandidateProfile;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class RecruiterHome extends JDialog {
    private JPanel contentPane;
    private JButton buttonLookUp;
    private JButton buttonLogout;
    private JButton buttonDelete;

    private String token;

    public RecruiterHome(String token) {
        this();
        this.token = token;
    }
    public RecruiterHome() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonLookUp);

        buttonLookUp.addActionListener(e -> onLookUp());

        buttonLogout.addActionListener(e -> onLogout());

        buttonDelete.addActionListener(e -> onDelete());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onLogout();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onLogout(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onLookUp() {
        dispose();
        RecruiterProfile recruiterProfile = new RecruiterProfile(this.token);
        recruiterProfile.setVisible(true);
    }

    private void onLogout() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        Request<?> request = new Request<>(Operations.LOGOUT_RECRUITER, this.token);

        clientConnection.send(request);

        try {
            Response<?> response = clientConnection.receive();

            if (response == null){
                JOptionPane.showMessageDialog(null, "Server is Down");
                dispose();
                StartConnection startConnection = new StartConnection();
                startConnection.setVisible(true);
            }

            dispose();
            RecruiterLogin recruiterLogin = new RecruiterLogin();
            recruiterLogin.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onDelete() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        Request<?> request = new Request<>(Operations.DELETE_ACCOUNT_RECRUITER, this.token);

        clientConnection.send(request);

        try {
            Response<?> response = clientConnection.receive();

            if (response == null){
                JOptionPane.showMessageDialog(null, "Server is Down");
                dispose();
                StartConnection startConnection = new StartConnection();
                startConnection.setVisible(true);
            }

            assert response != null;
            if (!(response.status().equals(Statuses.SUCCESS))) {
                JOptionPane.showMessageDialog(null, "Cannot Delete!");
                return;
            }

            dispose();
            RecruiterLogin recruiterLogin = new RecruiterLogin();
            recruiterLogin.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        RecruiterHome dialog = new RecruiterHome();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
