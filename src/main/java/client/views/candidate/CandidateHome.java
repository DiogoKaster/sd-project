package client.views.candidate;

import client.views.StartConnection;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import models.CandidateSkill;
import records.Request;
import records.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class CandidateHome extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonLogout;
    private String token;

    private JLabel userName;
    private JButton buttonDelete;
    private JButton buttonSkills;

    public CandidateHome(String token) {
        this();
        this.token = token;
    }
    public CandidateHome() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonLogout.addActionListener(e -> onCancel());

        buttonDelete.addActionListener(e -> onDelete());

        buttonSkills.addActionListener(e -> onGoToSkills());

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
        dispose();
        CandidateProfile candidateProfile = new CandidateProfile(this.token);
        candidateProfile.setVisible(true);
    }
    private void onCancel() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        Request<?> request = new Request<>(Operations.LOGOUT_CANDIDATE, this.token);

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
            CandidateLogin candidateLogin = new CandidateLogin();
            candidateLogin.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onDelete() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        Request<?> request = new Request<>(Operations.DELETE_ACCOUNT_CANDIDATE, this.token);

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
            CandidateLogin candidateLogin = new CandidateLogin();
            candidateLogin.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onGoToSkills() {
        dispose();
        CandidateSkills candidateSkills = new CandidateSkills(this.token);
        candidateSkills.setVisible(true);
    }

    public static void main(String[] args) {
        CandidateHome dialog = new CandidateHome();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
