package client.views.recruiter;

import client.views.StartConnection;
import client.views.candidate.CandidateLogin;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.candidate.CandidateSignUpRequest;
import records.recruiter.RecruiterSignUpRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class RecruiterSignUp extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JTextField industryField;
    private JTextField descriptionField;

    public RecruiterSignUp() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (emailField.getText().isEmpty()
                || passwordField.getPassword().length == 0
                || nameField.getText().isEmpty()
                || industryField.getText().isEmpty()
                || descriptionField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.", "Campos Vazios", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ClientConnection clientConnection = ClientConnection.getInstance();

        RecruiterSignUpRequest signUpModel = new RecruiterSignUpRequest(emailField.getText(),
                new String(passwordField.getPassword()),
                nameField.getText(),
                industryField.getText(),
                descriptionField.getText()
        );
        Request<RecruiterSignUpRequest> request = new Request<>(Operations.SIGNUP_RECRUITER, signUpModel);

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
            if(response.status() == Statuses.USER_EXISTS) {
                JOptionPane.showMessageDialog(this, "Email já cadastrado.", "Usuário existente", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dispose();
        RecruiterLogin recruiterLogin = new RecruiterLogin();
        recruiterLogin.setVisible(true);
    }

    private void onCancel() {
        dispose();
        RecruiterLogin recruiterLogin = new RecruiterLogin();
        recruiterLogin.setVisible(true);
    }

    public static void main(String[] args) {
        RecruiterSignUp dialog = new RecruiterSignUp();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
