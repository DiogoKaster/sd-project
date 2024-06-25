package client.views.recruiter;

import records.skill.SkillInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

        buttonChoose.addActionListener(e -> onOK());

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

    private void onOK() {
        // add your code here
        dispose();
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
