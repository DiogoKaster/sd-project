package client.views;

import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.CandidateLookupRequest;
import records.CandidateUpdateRequest;
import records.Request;
import records.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Objects;

public class CandidateProfile extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField passwordField;

    private String oldName;
    private String oldEmail;
    private String oldPassword;
    private String token;

    public CandidateProfile(String token) {
        this();
        this.token = token;
        lookUp();
    }

    public CandidateProfile() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

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

        CandidateUpdateRequest currentProfile = getCurrentProfile();

        Request<CandidateUpdateRequest> request = new Request<>(Operations.UPDATE_ACCOUNT_CANDIDATE, this.token, currentProfile);
        clientConnection.send(request);

        try {
            Response<?> response = clientConnection.receive();

            if (response.status().equals(Statuses.INVALID_FIELD)){
                JOptionPane.showMessageDialog(null, "Invalid field");
                return;
            }

            if (response.status().equals(Statuses.INVALID_EMAIL)) {
                JOptionPane.showMessageDialog(null, "Invalid email");
                return;
            }

            lookUp();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onCancel() {
        dispose();
        CandidateHome candidateHome = new CandidateHome(this.token);
        candidateHome.setVisible(true);
    }

    private void lookUp() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        Request<?> request = new Request<>(Operations.LOOKUP_ACCOUNT_CANDIDATE, this.token);

        clientConnection.send(request);

        try {
            Response<?> response = clientConnection.receive();

            if (response.status().equals(Statuses.USER_NOT_FOUND)){
                JOptionPane.showMessageDialog(null, "User not found");
                dispose();
                CandidateHome candidateHome = new CandidateHome(this.token);
                candidateHome.setVisible(true);
            }

            LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) response.data();

            oldName = (String) data.get("name");
            oldEmail = (String) data.get("email");
            oldPassword = (String) data.get("password");

            nameField.setText((String) data.get("name"));
            emailField.setText((String) data.get("email"));
            passwordField.setText((String) data.get("password"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CandidateUpdateRequest getCurrentProfile() {
        String currentName = nameField.getText();
        String currentEmail = emailField.getText();
        String currentPassword = passwordField.getText();

        boolean isNameEmpty = !currentName.isEmpty();
        boolean isEmailEmpty = !currentEmail.isEmpty();
        boolean isPasswordEmpty = !currentPassword.isEmpty();

        return new CandidateUpdateRequest(
                isEmailEmpty ? currentEmail : oldEmail,
                isPasswordEmpty ? currentPassword : oldPassword,
                isNameEmpty ? currentName : oldName
        );
    }


    public static void main(String[] args) {
        CandidateProfile dialog = new CandidateProfile();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
