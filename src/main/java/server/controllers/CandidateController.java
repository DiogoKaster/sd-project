package server.controllers;

import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Roles;
import enums.Statuses;
import models.Candidate;
import models.CandidateSkill;
import models.DatabaseConnection;
import models.Job;
import records.Response;
import records.candidate.CandidateInfo;
import records.candidate.CandidateLoginResponse;
import records.candidate.CandidateLookupResponse;
import records.candidate.CandidateSearchResponse;
import records.job.RecruiterLookupJobSetResponse;
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
                System.out.println(candidate.getName());
                for (CandidateSkill candidateSkill : candidate.getCandidateSkills()) {
                    System.out.println(candidateSkill.getSkill());
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
                                candidate.getId().toString()
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
}
