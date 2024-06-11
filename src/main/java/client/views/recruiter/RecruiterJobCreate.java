package client.views.recruiter;

import client.views.StartConnection;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.skill.RecruiterIncludeJobRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class RecruiterJobCreate extends JDialog {
    private JPanel contentPane;
    private JButton buttonInclude;
    private JButton buttonCancel;
    private JComboBox<String> skillsDropdown;
    private JSpinner experienceSpinner;

    private String token;

    public RecruiterJobCreate(String token) {
        this();
        this.token = token;
    }

    public RecruiterJobCreate() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonInclude);

        buttonInclude.addActionListener(e -> onInclude());

        buttonCancel.addActionListener(e -> onCancel());

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

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onInclude() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        String selectedSkill = (String) skillsDropdown.getSelectedItem();
        String experience = experienceSpinner.getValue().toString();

        RecruiterIncludeJobRequest requestModel = new RecruiterIncludeJobRequest(selectedSkill, experience);
        Request<RecruiterIncludeJobRequest> request = new Request<>(Operations.INCLUDE_JOB, this.token, requestModel);

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
            if (response.status() == Statuses.SKILL_NOT_EXIST) {
                JOptionPane.showMessageDialog(this, "Skill não existe.", "Not Found", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onCancel() {
        dispose();
        RecruiterJobs recruiterJobs = new RecruiterJobs(this.token);
        recruiterJobs.setVisible(true);
    }

    public static void main(String[] args) {
        RecruiterJobCreate dialog = new RecruiterJobCreate();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
