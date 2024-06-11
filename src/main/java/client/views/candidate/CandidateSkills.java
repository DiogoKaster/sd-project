package client.views.candidate;

import client.views.StartConnection;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.skill.CandidateIncludeSkillRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

public class CandidateSkills extends JDialog {
    private JPanel contentPane;
    private JButton buttonGoBack;
    private JComboBox<String> skillsDropdown;
    private JButton buttonInsertSkill;
    private JSpinner experienceSpinner;
    private JPanel skillsPanel;

    private String token;

    public CandidateSkills(String token) {
        this();
        this.token = token;
        lookUpSkillSet();
    }

    public CandidateSkills() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonInsertSkill);

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

        buttonGoBack.addActionListener(e -> onCancel());

        buttonInsertSkill.addActionListener(e -> onOK());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        String selectedSkill = (String) skillsDropdown.getSelectedItem();
        String experience = experienceSpinner.getValue().toString();

        CandidateIncludeSkillRequest includeSkillModel = new CandidateIncludeSkillRequest(selectedSkill, experience);
        Request<CandidateIncludeSkillRequest> request = new Request<>(Operations.INCLUDE_SKILL, this.token, includeSkillModel);

        clientConnection.send(request);

        try {
            Response<?> response = clientConnection.receive();

            if (response == null) {
                JOptionPane.showMessageDialog(null, "Server is Down");
                dispose();
                StartConnection startConnection = new StartConnection();
                startConnection.setVisible(true);
            }

            assert response != null;
            if(response.status() == Statuses.SKILL_EXISTS) {
                JOptionPane.showMessageDialog(this, "Skill já cadastrada.", "Already Exists", JOptionPane.ERROR_MESSAGE);
            }
            if (response.status() == Statuses.SKILL_NOT_EXIST) {
                JOptionPane.showMessageDialog(this, "Skill não existe.", "Not Found", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            skillsPanel.removeAll();
            lookUpSkillSet();
        }
    }

    private void lookUpSkillSet() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        Request<?> request = new Request<>(Operations.LOOKUP_SKILLSET, this.token);

        clientConnection.send(request);

        try {
            Response<?> response = clientConnection.receive();

            if (response == null) {
                JOptionPane.showMessageDialog(null, "Server is Down");
                dispose();
                StartConnection startConnection = new StartConnection();
                startConnection.setVisible(true);
            }

            assert response != null;
            LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) response.data();

            List<?> skillInfoList = (List<?>) data.get("skillset");
            for (Object skillInfo : skillInfoList) {
                LinkedTreeMap<String, String> skillInfoMap = (LinkedTreeMap<String, String>) skillInfo;
                String skill = skillInfoMap.get("skill");
                String experience = skillInfoMap.get("experience");
                String id = skillInfoMap.get("id");

                JButton skillButton = new JButton(skill);
                skillButton.addActionListener(e -> {
                    dispose();
                    CandidateSkill candidateSkill = new CandidateSkill(this.token, skill);
                    candidateSkill.setVisible(true);
                });
                skillsPanel.add(skillButton);
            }

            skillsPanel.revalidate();
            skillsPanel.repaint();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onCancel() {
        dispose();
        CandidateHome candidateHome = new CandidateHome(this.token);
        candidateHome.setVisible(true);
    }

    public static void main(String[] args) {
        CandidateSkills dialog = new CandidateSkills();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
