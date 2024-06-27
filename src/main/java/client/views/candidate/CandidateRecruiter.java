package client.views.candidate;

import records.skill.SkillInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CandidateRecruiter extends JDialog {
    private JPanel contentPane;

    private JButton buttonCancel;
    private JPanel companyPanel;
    private String name;
    private String industry;
    private String email;
    private String description;

    public CandidateRecruiter(String name, String industry, String email, String description) {
        this();
        this.name = name;
        this.industry = industry;
        this.email = email;
        this.description = description;
        showInfo();
    }

    public CandidateRecruiter() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        companyPanel.setLayout(new BoxLayout(companyPanel, BoxLayout.Y_AXIS));
    }

    private void showInfo(){
        JLabel line = new JLabel("----------------------------------------");
        JLabel nameLabel = new JLabel("Name: " + this.name);
        JLabel industryLabel = new JLabel("Industry: " + this.industry);
        JLabel emailLabel = new JLabel("Email: " + this.email);
        JLabel descriptionLabel = new JLabel("Industry: " + this.description);
        companyPanel.add(line);
        companyPanel.add(nameLabel);
        companyPanel.add(industryLabel);
        companyPanel.add(emailLabel);
        companyPanel.add(descriptionLabel);
        companyPanel.revalidate();
        companyPanel.repaint();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        CandidateRecruiter dialog = new CandidateRecruiter();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
