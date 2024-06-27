package client.views.recruiter;

import client.views.StartConnection;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.candidate.ChooseCandidateRequest;
import records.skill.SkillInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

public class RecruiterCandidate extends JDialog {
    private JPanel contentPane;
    private JButton buttonChoose;
    private JButton buttonCancel;
    private JPanel skillsPanel;
    private String token;
    private String userId;
    private List<SkillInfo> skillList;

    public RecruiterCandidate(String token, String userId, List<SkillInfo> skillList) {
        this();
        this.token = token;
        this.userId = userId;
        this.skillList = skillList;
        displaySkills();
    }

    public RecruiterCandidate() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonChoose);

        buttonChoose.addActionListener(e -> onChoose());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        skillsPanel.setLayout(new BoxLayout(skillsPanel, BoxLayout.Y_AXIS));
    }

    private void displaySkills() {
        for (SkillInfo skill : this.skillList) {
            JLabel line = new JLabel("----------------------------------------");
            JLabel skillLabel = new JLabel("Skill: " + skill.skill());
            JLabel experienceLabel = new JLabel("Experience: " + skill.experience());
            skillsPanel.add(line);
            skillsPanel.add(skillLabel);
            skillsPanel.add(experienceLabel);
        }
        skillsPanel.revalidate();
        skillsPanel.repaint();
    }

    private void onChoose() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        ChooseCandidateRequest requestModel = new ChooseCandidateRequest(this.userId);
        Request<ChooseCandidateRequest> request = new Request<>(Operations.CHOOSE_CANDIDATE, this.token, requestModel);

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
            if (response.status() != Statuses.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Candidato não pode ser escolhido.", "Not Found", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        RecruiterCandidate dialog = new RecruiterCandidate();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
