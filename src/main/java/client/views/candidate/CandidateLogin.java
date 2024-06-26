package client.views.candidate;

import client.views.LoginOptions;
import client.views.StartConnection;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.candidate.CandidateLoginRequest;
import records.Request;
import records.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class CandidateLogin extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField candidateLoginEmailField;
    private JPasswordField candidateLoginPasswordField;
    private JButton buttonSignUpCandidate;

    public CandidateLogin() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        buttonSignUpCandidate.addActionListener(e -> goToSignUpCandidate());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e ->
                onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        CandidateLoginRequest loginModel = new CandidateLoginRequest(candidateLoginEmailField.getText(), new String(candidateLoginPasswordField.getPassword()));
        Request<?> request = new Request<>(Operations.LOGIN_CANDIDATE, loginModel);

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
            CandidateHome candidateHome = new CandidateHome((String) data.get("token"));
            candidateHome.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dispose();
    }

    private void onCancel() {
        dispose();
        LoginOptions loginOptions = new LoginOptions();
        loginOptions.setVisible(true);
    }

    private void goToSignUpCandidate() {
        dispose();
        CandidateSignUp candidateSignUp = new CandidateSignUp();
        candidateSignUp.setVisible(true);
    }

    public static void main(String[] args) {
        CandidateLogin dialog = new CandidateLogin();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
