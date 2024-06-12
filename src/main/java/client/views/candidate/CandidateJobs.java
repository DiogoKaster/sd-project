package client.views.candidate;

import client.views.StartConnection;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.job.CandidateSearchJobRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CandidateJobs extends JDialog {
    private JPanel contentPane;
    private JButton buttonSearch;
    private JButton buttonGoBack;

    private JButton buttonAddSkillToFilter;
    private JTable jobTable;
    private JComboBox<String> skillsDropdown;
    private JPanel skillsFilterPanel;
    private JSpinner experienceSpinner;
    private JRadioButton radioFilterAnd;
    private JRadioButton radioFilterOr;
    private final ButtonGroup buttonFilterType;

    private String token;

    private List<String> skillsFilter;

    public CandidateJobs(String token) {
        this();
        this.token = token;
    }

    public CandidateJobs() {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        getRootPane().setDefaultButton(buttonSearch);

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

        buttonFilterType = new ButtonGroup();
        buttonFilterType.add(radioFilterAnd);
        buttonFilterType.add(radioFilterOr);

        skillsFilter = new ArrayList<>();

        buttonSearch.addActionListener(e -> onSearch());

        buttonGoBack.addActionListener(e -> onGoBack());

        buttonAddSkillToFilter.addActionListener(e -> addSkillToFilter());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onGoBack();
            }
        });

        contentPane.registerKeyboardAction(e -> onGoBack(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onSearch() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        String experience = experienceSpinner.getValue().toString();
        String filterType = getSelectedFilterType();

        if(Integer.parseInt(experience) == 0) {
            experience = null;
        }

        List<String> skillsFilterToSend = (skillsFilter.isEmpty()) ? null : skillsFilter;

        CandidateSearchJobRequest requestModel = new CandidateSearchJobRequest(skillsFilterToSend, experience, filterType);

        Request<?> request = new Request<>(Operations.SEARCH_JOB, this.token, requestModel);

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

            List<?> jobSet = (List<?>) data.get("jobset");
            for (Object job : jobSet) {
                LinkedTreeMap<String, String> jobMap = (LinkedTreeMap<String, String>) job;
                System.out.println(jobMap.get("skill"));
                System.out.println(jobMap.get("experience"));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addSkillToFilter() {
        String selectedSkill = (String) skillsDropdown.getSelectedItem();
        System.out.println("Skill adicionada!");
        this.skillsFilter.add(selectedSkill);
    }

    private void onGoBack() {
        dispose();
        CandidateHome candidateHome = new CandidateHome(this.token);
        candidateHome.setVisible(true);
    }

    private String getSelectedFilterType() {
        for (AbstractButton button : Collections.list(buttonFilterType.getElements())) {
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        CandidateJobs dialog = new CandidateJobs();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
