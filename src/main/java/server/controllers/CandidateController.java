package server.controllers;

import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Roles;
import enums.Statuses;
import models.*;
import records.Response;
import records.candidate.*;
import records.job.RecruiterLookupJobSetResponse;
import records.recruiter.RecruiterInfo;
import records.skill.SkillInfo;
import server.middlewares.Auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CandidateController {
    private static final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
    private static final Auth auth = new Auth();
    public static Response<?> login(LinkedTreeMap<String, ?> data) {
        try {
            String email = (String) data.get("email");
            String password = (String) data.get("password");

            Candidate candidate = databaseConnection.verifyLogin(email, password, Candidate.class);
            String token = auth.generateToken(candidate.getId(), Roles.CANDIDATE.toString());
            CandidateLoginResponse responseModel = new CandidateLoginResponse(token);

            return new Response<>(Operations.LOGIN_CANDIDATE, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            return new Response<>(Operations.LOGIN_CANDIDATE, Statuses.INVALID_LOGIN);
        }
    }

    public static Response<?> signUp(LinkedTreeMap<String, ?> data) {
        Candidate candidate = getData(data);
        Candidate insertedCandidate = databaseConnection.insert(candidate, Candidate.class);

        if (insertedCandidate != null) {
            return new Response<>(Operations.SIGNUP_CANDIDATE, Statuses.SUCCESS);
        } else {
            return new Response<>(Operations.SIGNUP_CANDIDATE, Statuses.USER_EXISTS);
        }
    }

    public static Response<?> lookUp(String token) {
        Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

        if(candidate != null) {
            CandidateLookupResponse responseModel = new CandidateLookupResponse(candidate);
            return new Response<>(Operations.LOOKUP_ACCOUNT_CANDIDATE, Statuses.SUCCESS, responseModel);
        } else {
            return new Response<>(Operations.LOOKUP_ACCOUNT_CANDIDATE, Statuses.USER_NOT_FOUND);
        }
    }

    public static Response<?> update(String token, LinkedTreeMap<String, ?> data) {
        Candidate candidate = getData(data);
        candidate.setId(auth.getAuthId(token));

        Candidate updatedCandidate = databaseConnection.update(candidate, Candidate.class);
        if(updatedCandidate != null) {
            return new Response<>(Operations.UPDATE_ACCOUNT_CANDIDATE, Statuses.SUCCESS);
        } else {
            return new Response<>(Operations.UPDATE_ACCOUNT_CANDIDATE, Statuses.INVALID_EMAIL);
        }
    }

    public static Response<?> delete(String token) {
        databaseConnection.delete(auth.getAuthId(token), Candidate.class);
        return new Response<>(Operations.DELETE_ACCOUNT_CANDIDATE, Statuses.SUCCESS);
    }

    public static Response<?> logout(String token) {
        Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

        if (candidate != null) {
            return new Response<>(Operations.LOGOUT_CANDIDATE, Statuses.SUCCESS);
        }
        else {
            return new Response<>(Operations.LOGOUT_CANDIDATE, Statuses.USER_NOT_FOUND);
        }
    }

    private static Candidate getData(LinkedTreeMap<String, ?> data) {
        String name = (String) data.get("name");
        String email = (String) data.get("email");
        String password = (String) data.get("password");

        Candidate candidate = new Candidate();
        candidate.setName(name);
        candidate.setEmail(email);
        candidate.setPassword(password);

        return candidate;
    }

    public static Response<?> search(LinkedTreeMap<String, ?> data) {
        try {
            String filterType = data.get("filter") != null ? (String) data.get("filter") : null;
            List<String> skillsFilter = (data.get("skill") != null) ? (List<String>) data.get("skill") : Collections.emptyList();
            Integer experience = data.get("experience") != null ? Integer.parseInt((String) data.get("experience")) : null;

            List<Candidate> allCandidates = databaseConnection.selectWithSkills();
            List<CandidateInfo> candidateInfoList = new ArrayList<>();

            for (Candidate candidate : allCandidates) {
                for (CandidateSkill candidateSkill : candidate.getCandidateSkills()) {
                    boolean matches = false;

                    if (filterType != null && !skillsFilter.isEmpty() && experience != null) {

                        boolean matchesSkills = skillsFilter.contains(candidateSkill.getSkill().getName());
                        boolean matchesExperience = candidateSkill.getYearsOfExperience() <= experience;

                        if (filterType.equals("OR")) {
                            matches = matchesSkills || matchesExperience;
                        } else if (filterType.equals("AND")) {
                            matches = matchesSkills && matchesExperience;
                        }

                    } else if (!skillsFilter.isEmpty()) {
                        matches = skillsFilter.contains(candidateSkill.getSkill().getName());
                    } else if (experience != null) {
                        matches = candidateSkill.getYearsOfExperience() <= experience;
                    }

                    if (matches) {
                        CandidateInfo candidateInfo = new CandidateInfo(
                                candidateSkill.getSkill().getName(),
                                candidateSkill.getYearsOfExperience().toString(),
                                candidateSkill.getId().toString(),
                                candidate.getId().toString(),
                                candidate.getName()
                        );
                        candidateInfoList.add(candidateInfo);
                    }
                }
            }

            String candidateInfoSize = String.valueOf(candidateInfoList.size());
            CandidateSearchResponse responseModel = new CandidateSearchResponse(candidateInfoSize, candidateInfoList);

            return new Response<>(Operations.SEARCH_CANDIDATE, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO SEARCH_CANDIDATE");
            return new Response<>(Operations.SEARCH_CANDIDATE, Statuses.ERROR);
        }
    }

    public static Response<?> choose(String token, LinkedTreeMap<String, ?> data) {
        try {
            Integer userId = Integer.valueOf((String) data.get("id_user"));
            Candidate candidate = databaseConnection.selectWithChosen(userId, Candidate.class);
            Recruiter recruiter = databaseConnection.selectWithChosen(auth.getAuthId(token), Recruiter.class);

//            boolean alreadyChosen = false;
//            for (ChosenCandidate chosenCandidate : recruiter.getChosenCandidates()) {
//                if (chosenCandidate.getCandidate().getId().equals(candidate.getId())) {
//                    alreadyChosen = true;
//                    break;
//                }
//            }
//
//            if (alreadyChosen) {
//                return new Response<>(Operations.CHOOSE_CANDIDATE, Statuses.ERROR);
//            }

            ChosenCandidate newChosenCandidate = new ChosenCandidate();
            newChosenCandidate.setCandidate(candidate);
            newChosenCandidate.setRecruiter(recruiter);

            databaseConnection.insert(newChosenCandidate, ChosenCandidate.class);

            return new Response<>(Operations.CHOOSE_CANDIDATE, Statuses.SUCCESS);
        } catch (Exception e) {
            return new Response<>(Operations.CHOOSE_CANDIDATE, Statuses.ERROR);
        }
    }

    public static Response<?> companies(String token) {
        try {
            Candidate candidate = databaseConnection.selectWithChosen(auth.getAuthId(token), Candidate.class);

            List<RecruiterInfo> recruiterInfoList = new ArrayList<>();
            for (ChosenCandidate chosenCandidate : candidate.getChosenCandidates()) {
                String recruiterInfoName = chosenCandidate.getRecruiter().getName();
                String recruiterInfoIndustry = chosenCandidate.getRecruiter().getIndustry();
                String recruiterInfoEmail = chosenCandidate.getRecruiter().getEmail();
                String recruiterInfoDescription = chosenCandidate.getRecruiter().getDescription();

                RecruiterInfo recruiterInfo = new RecruiterInfo(recruiterInfoName, recruiterInfoIndustry, recruiterInfoEmail, recruiterInfoDescription);
                recruiterInfoList.add(recruiterInfo);
            }

            String recruiterInfoListSize = String.valueOf(recruiterInfoList.size());
            CandidateGetCompanyResponse responseModel = new CandidateGetCompanyResponse(recruiterInfoListSize, recruiterInfoList);

            return new Response<>(Operations.GET_COMPANY, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            return new Response<>(Operations.GET_COMPANY, Statuses.ERROR);
        }
    }
}
