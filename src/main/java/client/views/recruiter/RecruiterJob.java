package client.views.recruiter;

import client.views.StartConnection;
import client.views.candidate.CandidateSkills;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.skill.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class RecruiterJob extends JDialog {
    private JPanel contentPane;
    private JButton buttonUpdate;
    private JButton buttonGoBack;
    private JComboBox<String> skillsDropdown;
    private JSpinner experienceSpinner;
    private JButton buttonDelete;

    private String token;
    private String id;

    public RecruiterJob(String token, String id) {
        this();
        this.token = token;
        this.id = id;
        lookUpJob();
    }

    public RecruiterJob() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonUpdate);

        buttonUpdate.addActionListener(e -> onUpdate());

        buttonGoBack.addActionListener(e -> onGoBack());

        buttonDelete.addActionListener(e -> onDelete());

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
                onGoBack();
            }
        });

        contentPane.registerKeyboardAction(e -> onGoBack(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onUpdate() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        String selectedSkill = (String) skillsDropdown.getSelectedItem();
        String experience = experienceSpinner.getValue().toString();

        RecruiterUpdateJobRequest requestModel = new RecruiterUpdateJobRequest(this.id, selectedSkill, experience);

        Request<?> request = new Request<>(Operations.UPDATE_JOB, this.token, requestModel);
        clientConnection.send(request);

        try {
            Response<?> response = clientConnection.receive();

            if (response == null){
                JOptionPane.showMessageDialog(null, "Server is Down");
                dispose();
                StartConnection startConnection = new StartConnection();
                startConnection.setVisible(true);
            }

            lookUpJob();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void lookUpJob() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        RecruiterLookupJobRequest requestModel = new RecruiterLookupJobRequest(this.id);
        Request<?> request = new Request<>(Operations.LOOKUP_JOB, this.token, requestModel);

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
            if (response.status().equals(Statuses.JOB_NOT_FOUND)){
                JOptionPane.showMessageDialog(null, "Job not found");
                dispose();
                CandidateSkills candidateSkills = new CandidateSkills(this.token);
                candidateSkills.setVisible(true);
            }

            LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) response.data();
//
//            System.out.println(data.get("id").toString());
//            System.out.println(data.get("skill"));
//            System.out.println(data.get("experience"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onDelete() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        RecruiterDeleteJobRequest requestModel = new RecruiterDeleteJobRequest(this.id);
        Request<?> request = new Request<>(Operations.DELETE_JOB, this.token, requestModel);

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
            RecruiterJobs recruiterJobs = new RecruiterJobs(this.token);
            recruiterJobs.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onGoBack() {
        dispose();
        RecruiterJobs recruiterJobs = new RecruiterJobs(this.token);
        recruiterJobs.setVisible(true);
    }

    public static void main(String[] args) {
        RecruiterJob dialog = new RecruiterJob();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
