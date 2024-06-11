package server.controllers;

import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import models.Candidate;
import models.CandidateSkill;
import models.DatabaseConnection;
import models.Skill;
import records.Response;
import records.skill.CandidateLookupSkillSetResponse;
import records.skill.SkillInfo;
import server.middlewares.Auth;

import java.util.List;
import java.util.stream.Collectors;

public class SkillController {
    private static final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
    private static final Auth auth = new Auth();

    public static Response<?> includeCandidate(String token, LinkedTreeMap<String, ?> data) {
        try {
            String skillName = (String) data.get("skill");
            Integer experience = ((Number) data.get("experience")).intValue();

            Candidate candidate = databaseConnection.selectWithSkills(auth.getAuthId(token), Candidate.class);
            if (candidate == null) {
                return new Response<>(Operations.INCLUDE_SKILL, Statuses.USER_NOT_FOUND);
            }

            Skill skill = databaseConnection.selectByName(skillName, Skill.class);
            if (skill == null) {
                return new Response<>(Operations.INCLUDE_SKILL, Statuses.SKILL_NOT_EXIST);
            }

            boolean skillExists = candidate.getCandidateSkills().stream()
                    .anyMatch(cs -> cs.getSkill().getName().equals(skillName));

            if (skillExists) {
                return new Response<>(Operations.INCLUDE_SKILL, Statuses.SKILL_EXISTS);
            }

            CandidateSkill candidateSkill = new CandidateSkill();
            candidateSkill.setCandidate(candidate);
            candidateSkill.setSkill(skill);
            candidateSkill.setYearsOfExperience(experience);

            databaseConnection.insert(candidateSkill, CandidateSkill.class);

            return new Response<>(Operations.INCLUDE_SKILL, Statuses.SUCCESS);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO INCLUDECANDIDATE");
            return new Response<>(Operations.INCLUDE_SKILL, Statuses.ERROR);
        }
    }

    public static Response<?> lookUpSkillSetCandidate(String token) {
        try {
            Candidate candidate = databaseConnection.selectWithSkills(auth.getAuthId(token), Candidate.class);
            if (candidate == null) {
                return new Response<>(Operations.LOOKUP_SKILLSET, Statuses.USER_NOT_FOUND);
            }

            List<SkillInfo> skillInfoList = candidate.getCandidateSkills().stream()
                    .map(cs -> new SkillInfo(cs.getSkill().getName(), cs.getYearsOfExperience().toString()))
                    .collect(Collectors.toList());

            String skillsetSize = String.valueOf(skillInfoList.size());
            CandidateLookupSkillSetResponse data = new CandidateLookupSkillSetResponse(skillsetSize, skillInfoList);

            return new Response<>(Operations.LOOKUP_SKILLSET, Statuses.SUCCESS, data);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO LOOKUPSKILLSETCANDIDATE");
            return new Response<>(Operations.LOOKUP_SKILLSET, Statuses.ERROR);
        }
    }
}
