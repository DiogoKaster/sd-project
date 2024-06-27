package client.views.recruiter;

import client.views.StartConnection;
import client.views.candidate.CandidateHome;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import helpers.ClientConnection;
import records.Request;
import records.Response;
import records.candidate.CandidateProfile;
import records.job.CandidateSearchJobRequest;
import records.skill.SkillInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class RecruiterCandidates extends JDialog {
    private JPanel contentPane;
    private JButton buttonSearch;
    private JButton buttonGoBack;
    private JComboBox<String> skillsDropdown;
    private JSpinner experienceSpinner;
    private JRadioButton radioFilterAnd;
    private JRadioButton radioFilterOr;
    private JPanel candidatesPanel;
    private JButton buttonAddSkillToFilter;

    private final ButtonGroup buttonFilterType;

    private String token;

    private final List<String> skillsFilter;

    public RecruiterCandidates(String token) {
        this();
        this.token = token;
    }

    public RecruiterCandidates() {
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

        skillsDropdown.addActionListener(e -> updateFilterCheckboxes());
        experienceSpinner.addChangeListener(e -> updateFilterCheckboxes());

        updateFilterCheckboxes();

        candidatesPanel.setLayout(new BoxLayout(candidatesPanel, BoxLayout.Y_AXIS));
    }

    private void onSearch() {
        ClientConnection clientConnection = ClientConnection.getInstance();

        String experience = experienceSpinner.getValue().toString();
        String filterType = getSelectedFilterType();

        if (Integer.parseInt(experience) == 0) {
            experience = null;
        }

        List<String> skillsFilterToSend = (skillsFilter.isEmpty()) ? null : skillsFilter;

        CandidateSearchJobRequest requestModel = new CandidateSearchJobRequest(skillsFilterToSend, experience, filterType);

        Request<?> request = new Request<>(Operations.SEARCH_CANDIDATE, this.token, requestModel);

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

            List<?> candidateList = (List<?>) data.get("profile");

            Map<String, CandidateProfile> candidateProfilesMap = new HashMap<>();
            for (Object candidate : candidateList) {
                LinkedTreeMap<String, String> candidateMap = (LinkedTreeMap<String, String>) candidate;

                String idUser = candidateMap.get("id_user");
                String skill = candidateMap.get("skill");
                String experienceValue = candidateMap.get("experience");
                String skillId = candidateMap.get("id");
                String name = candidateMap.get("name");

                SkillInfo skillInfo = new SkillInfo(skill, experienceValue, skillId);

                CandidateProfile candidateProfile = candidateProfilesMap.get(idUser);
                if (candidateProfile == null) {
                    List<SkillInfo> skillList = new ArrayList<>();
                    skillList.add(skillInfo);
                    candidateProfile = new CandidateProfile(idUser, name, skillList);
                } else {
                    candidateProfile.skillList().add(skillInfo);
                }
                candidateProfilesMap.put(idUser, candidateProfile);
            }

            List<CandidateProfile> candidateProfiles = new ArrayList<>(candidateProfilesMap.values());

            candidatesPanel.removeAll();

            for (CandidateProfile profile : candidateProfiles) {
                JButton candidateButton = new JButton("Candidate: " + profile.name());
                candidateButton.addActionListener(e -> {
                    RecruiterCandidate recruiterCandidate = new RecruiterCandidate(this.token, profile.idUser(), profile.skillList());
                    recruiterCandidate.setVisible(true);
                });
                candidatesPanel.add(candidateButton);
            }
            candidatesPanel.revalidate();
            candidatesPanel.repaint();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addSkillToFilter() {
        String selectedSkill = (String) skillsDropdown.getSelectedItem();
        if (!skillsFilter.contains(selectedSkill)) {
            skillsFilter.add(selectedSkill);
            System.out.println("Skill adicionada: " + selectedSkill);
        }
        updateFilterCheckboxes();
    }

    private String getSelectedFilterType() {
        if (!skillsFilter.isEmpty() && (Integer) experienceSpinner.getValue() > 0) {
            for (AbstractButton button : Collections.list(buttonFilterType.getElements())) {
                if (button.isSelected()) {
                    return button.getText();
                }
            }
        }
        return null;
    }

    private void updateFilterCheckboxes() {
        boolean enableFilters = !skillsFilter.isEmpty() && (Integer) experienceSpinner.getValue() > 0;
        radioFilterAnd.setEnabled(enableFilters);
        radioFilterAnd.setSelected(enableFilters);
        radioFilterOr.setEnabled(enableFilters);
    }

    private void onGoBack() {
        dispose();
        RecruiterHome recruiterHome = new RecruiterHome(this.token);
        recruiterHome.setVisible(true);
    }

    public static void main(String[] args) {
        RecruiterCandidates dialog = new RecruiterCandidates();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
