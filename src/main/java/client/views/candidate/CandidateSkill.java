package client.views.candidate;

import client.views.StartConnection;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.skill.CandidateDeleteSkillRequest;
import records.skill.CandidateLookupSkillRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class CandidateSkill extends JDialog {
    private JPanel contentPane;
    private JButton buttonUpdate;
    private JButton buttonCancel;
    private JButton buttonDelete;
    private JComboBox<String> skillsDropdown;
    private JSpinner experienceSpinner;

    private String token;

    private String oldSkill;

    public CandidateSkill(String token, String oldSkill) {
        this();
        this.token = token;
        this.oldSkill = oldSkill;
        lookUpSkill();
    }
    public CandidateSkill() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonUpdate);

        skillsDropdown.addItem("NodeJs");
        skillsDropdown.addItem("JavaScript");
        skillsDropdown.addItem("Java");
        skillsDropdown.addItem("C");
        skillsDropdown.addItem("HTML");
        skillsDropdown.addItem("CSS");
        skillsDropdown.addItem("React");
        skillsDropdown.addItem("ReactNative");
        skillsDropdown.addItem("TypeScript");
        skillsDropdown.addItem("Ruby");

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        experienceSpinner.setModel(model);

        buttonUpdate.addActionListener(e -> onUpdate());

        buttonDelete.addActionListener(e -> onDelete());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onUpdate() {
        dispose();
    }

    private void lookUpSkill() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        CandidateLookupSkillRequest requestModel = new CandidateLookupSkillRequest(this.oldSkill);
        Request<?> request = new Request<>(Operations.LOOKUP_SKILL, this.token, requestModel);

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
            if (response.status().equals(Statuses.SKILL_NOT_EXIST)){
                JOptionPane.showMessageDialog(null, "User not found");
                dispose();
                CandidateSkills candidateSkills = new CandidateSkills(this.token);
                candidateSkills.setVisible(true);
            }

            LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) response.data();

            System.out.println(data.get("skill"));
            System.out.println(data.get("experience"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onDelete() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        CandidateDeleteSkillRequest requestModel = new CandidateDeleteSkillRequest(this.oldSkill);
        Request<?> request = new Request<>(Operations.DELETE_SKILL, this.token, requestModel);

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
            CandidateSkills candidateSkills = new CandidateSkills(this.token);
            candidateSkills.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onCancel() {
        dispose();
        CandidateSkills candidateSkills = new CandidateSkills(this.token);
        candidateSkills.setVisible(true);
    }

    public static void main(String[] args) {
        CandidateSkill dialog = new CandidateSkill();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
