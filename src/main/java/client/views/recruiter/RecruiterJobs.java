package client.views.recruiter;

import client.views.StartConnection;
import client.views.candidate.CandidateSkill;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import helpers.ClientConnection;
import records.Request;
import records.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

public class RecruiterJobs extends JDialog {
    private JPanel contentPane;
    private JButton buttonGoToCreate;
    private JButton buttonCancel;
    private JPanel jobsPanel;

    private String token;

    public RecruiterJobs(String token) {
        this();
        this.token = token;
        lookUpJobs();
    }

    public RecruiterJobs() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonGoToCreate);

        jobsPanel.setLayout(new BoxLayout(jobsPanel, BoxLayout.Y_AXIS));

        buttonGoToCreate.addActionListener(e -> onGoToCreate());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onGoToCreate() {
        dispose();
        RecruiterJobCreate recruiterJobCreate = new RecruiterJobCreate(this.token);
        recruiterJobCreate.setVisible(true);
    }

    private void onCancel() {
        dispose();
        RecruiterHome recruiterHome = new RecruiterHome(this.token);
        recruiterHome.setVisible(true);
    }

    private void lookUpJobs() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        Request<?> request = new Request<>(Operations.LOOKUP_JOBSET, this.token);

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

            List<?> jobInfoList = (List<?>) data.get("jobset");
            for (Object jobInfo : jobInfoList) {
                LinkedTreeMap<String, String> jobInfoMap = (LinkedTreeMap<String, String>) jobInfo;
                String skill = jobInfoMap.get("skill");
                String experience = jobInfoMap.get("experience");
                String id = jobInfoMap.get("id");

                JButton skillButton = new JButton("Vaga: " + skill);
                skillButton.addActionListener(e -> {
                    dispose();
                    RecruiterJob recruiterJob = new RecruiterJob(this.token, id);
                    recruiterJob.setVisible(true);
                });
                jobsPanel.add(skillButton);
            }

            jobsPanel.revalidate();
            jobsPanel.repaint();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        RecruiterJobs dialog = new RecruiterJobs();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
