package client.views.recruiter;

import client.views.StartConnection;
import client.views.candidate.CandidateHome;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.candidate.CandidateUpdateRequest;
import records.recruiter.RecruiterUpdateRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class RecruiterProfile extends JDialog {
    private JPanel contentPane;
    private JButton buttonUpdate;
    private JButton buttonHome;
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField industryField;
    private JTextField descriptionField;

    private String oldName;

    private String oldEmail;

    private String oldPassword;

    private String oldIndustry;

    private String oldDescription;

    private String token;

    public RecruiterProfile(String token) {
        this();
        this.token = token;
        lookUp();
    }
    public RecruiterProfile() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonUpdate);

        buttonUpdate.addActionListener(e -> onUpdate());

        buttonHome.addActionListener(e -> onGoHome());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onGoHome();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onGoHome(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onUpdate() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        RecruiterUpdateRequest currentProfile = getCurrentProfile();

        Request<?> request = new Request<>(Operations.UPDATE_ACCOUNT_RECRUITER, this.token, currentProfile);
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

    private void onGoHome() {
        dispose();
        RecruiterHome recruiterHome = new RecruiterHome(this.token);
        recruiterHome.setVisible(true);
    }

    private void lookUp() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        Request<?> request = new Request<>(Operations.LOOKUP_ACCOUNT_RECRUITER, this.token);

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
            if (response.status().equals(Statuses.USER_NOT_FOUND)){
                JOptionPane.showMessageDialog(null, "User not found");
                dispose();
                RecruiterHome recruiterHome = new RecruiterHome(this.token);
                recruiterHome.setVisible(true);
            }

            LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) response.data();

            oldName = (String) data.get("name");
            oldEmail = (String) data.get("email");
            oldPassword = (String) data.get("password");
            oldIndustry = (String) data.get("industry");
            oldDescription = (String) data.get("description");

            nameField.setText(oldName);
            emailField.setText(oldEmail);
            passwordField.setText(oldPassword);
            industryField.setText(oldIndustry);
            descriptionField.setText(oldDescription);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RecruiterUpdateRequest getCurrentProfile() {
        String currentName = nameField.getText();
        String currentEmail = emailField.getText();
        String currentPassword = new String(passwordField.getPassword());
        String currentIndustry = industryField.getText();
        String currentDescription = descriptionField.getText();

        boolean isNameEmpty = !currentName.isEmpty();
        boolean isEmailEmpty = !currentEmail.isEmpty();
        boolean isPasswordEmpty = !currentPassword.isEmpty();
        boolean isIndustryEmpty = !currentIndustry.isEmpty();
        boolean isDescriptionEmpty = !currentDescription.isEmpty();

        return new RecruiterUpdateRequest(
                isEmailEmpty ? currentEmail : oldEmail,
                isPasswordEmpty ? currentPassword : oldPassword,
                isNameEmpty ? currentName : oldName,
                isIndustryEmpty ? currentIndustry : oldIndustry,
                isDescriptionEmpty ? currentDescription : oldDescription
        );
    }

    public static void main(String[] args) {
        RecruiterProfile dialog = new RecruiterProfile();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
