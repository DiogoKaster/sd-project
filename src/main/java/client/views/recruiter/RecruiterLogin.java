package client.views.recruiter;

import client.views.LoginOptions;
import client.views.StartConnection;
import client.views.candidate.CandidateHome;
import client.views.candidate.CandidateSignUp;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.recruiter.RecruiterLoginRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class RecruiterLogin extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton buttonSignUp;

    public RecruiterLogin() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        buttonSignUp.addActionListener(e -> goToSignUp());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        RecruiterLoginRequest loginModel = new RecruiterLoginRequest(emailField.getText(), new String(passwordField.getPassword()));
        Request<?> request = new Request<>(Operations.LOGIN_RECRUITER, loginModel);

        clientConnection.send(request);

        try {
            Response<?> response = clientConnection.receive();

            if (response == null){
                JOptionPane.showMessageDialog(null, "Server is Down");
                dispose();
                StartConnection startConnection = new StartConnection();
                startConnection.setVisible(true);
            }

            LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) response.data();

            if (response.status().equals(Statuses.INVALID_LOGIN)){
                JOptionPane.showMessageDialog(null, "Invalid login");
                return;
            }

            if (response.status().equals(Statuses.INVALID_FIELD)) {
                JOptionPane.showMessageDialog(null, "Invalid field");
                return;
            }

            dispose();
            RecruiterHome recruiterHome = new RecruiterHome((String) data.get("token"));
            recruiterHome.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dispose();
    }

    private void goToSignUp() {
        dispose();
        RecruiterSignUp recruiterSignUp = new RecruiterSignUp();
        recruiterSignUp.setVisible(true);
    }


    private void onCancel() {
        dispose();
        LoginOptions loginOptions = new LoginOptions();
        loginOptions.setVisible(true);
    }

    public static void main(String[] args) {
        RecruiterLogin dialog = new RecruiterLogin();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
