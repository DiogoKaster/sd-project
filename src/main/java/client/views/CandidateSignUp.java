package client.views;

import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.CandidateSignUpRequest;
import records.Request;
import records.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class CandidateSignUp extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField emailField;
    private JTextField nameField;
    private JPasswordField passwordField;

    public CandidateSignUp() {
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
        if (emailField.getText().isEmpty() || passwordField.getPassword().length == 0 || nameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.", "Campos Vazios", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ClientConnection clientConnection = ClientConnection.getInstance();

        CandidateSignUpRequest signUpModel = new CandidateSignUpRequest(emailField.getText(), new String(passwordField.getPassword()), nameField.getText());
        Request<CandidateSignUpRequest> request = new Request<>(Operations.SIGNUP_CANDIDATE, signUpModel);

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
        CandidateLogin candidateLogin = new CandidateLogin();
        candidateLogin.setVisible(true);
    }

    private void onCancel() {
        dispose();
        CandidateLogin candidateLogin = new CandidateLogin();
        candidateLogin.setVisible(true);
    }

    public static void main(String[] args) {
        CandidateSignUp dialog = new CandidateSignUp();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
