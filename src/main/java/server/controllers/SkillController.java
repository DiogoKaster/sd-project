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

    public static Response<?> include(String token, LinkedTreeMap<String, ?> data) {
        try {
            String skillName = (String) data.get("skill");
            Integer experience = Integer.valueOf((String) data.get("experience"));

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

    public static Response<?> lookUpAll(String token) {
        try {
            Candidate candidate = databaseConnection.selectWithSkills(auth.getAuthId(token), Candidate.class);
            if (candidate == null) {
                return new Response<>(Operations.LOOKUP_SKILLSET, Statuses.USER_NOT_FOUND);
            }

            List<SkillInfo> skillInfoList = candidate.getCandidateSkills().stream()
                    .map(cs -> new SkillInfo(cs.getSkill().getName(), cs.getYearsOfExperience().toString(), cs.getId().toString()))
                    .collect(Collectors.toList());

            String skillsetSize = String.valueOf(skillInfoList.size());
            CandidateLookupSkillSetResponse responseModel = new CandidateLookupSkillSetResponse(skillsetSize, skillInfoList);

            return new Response<>(Operations.LOOKUP_SKILLSET, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO LOOKUPSKILLSETCANDIDATE");
            return new Response<>(Operations.LOOKUP_SKILLSET, Statuses.ERROR);
        }
    }

    public static Response<?> lookUp(String token, LinkedTreeMap<String, ?> data) {
        try {
            String skillToFind = (String) data.get("skill");

            Candidate candidate = databaseConnection.selectWithSkills(auth.getAuthId(token), Candidate.class);
            if (candidate == null) {
                return new Response<>(Operations.LOOKUP_SKILL, Statuses.USER_NOT_FOUND);
            }

            CandidateSkill skillInfo = candidate.getCandidateSkills().stream()
                    .filter(cs -> cs.getSkill().getName().equals(skillToFind)).findFirst().orElse(null);

            if (skillInfo == null) {
                return new Response<>(Operations.LOOKUP_SKILL, Statuses.SKILL_NOT_EXIST);
            }

            String skill = skillInfo.getSkill().getName();
            String experience = skillInfo.getYearsOfExperience().toString();

            SkillInfo responseModel = new SkillInfo(skill, experience);

            return new Response<>(Operations.LOOKUP_SKILL, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO DELETESKILLCANDIDATE");
            return new Response<>(Operations.LOOKUP_SKILL, Statuses.ERROR);
        }
    }

    public static Response<?> update(String token, LinkedTreeMap<String, ?> data) {
        try {
            String currentSkillName = (String) data.get("skill");
            String newSkillName = (String) data.get("newSkill");
            Integer experience = Integer.valueOf((String) data.get("experience"));

            // Obtém o candidato com suas habilidades
            Candidate candidate = databaseConnection.selectWithSkills(auth.getAuthId(token), Candidate.class);
            if (candidate == null) {
                return new Response<>(Operations.UPDATE_SKILL, Statuses.USER_NOT_FOUND);
            }

            // Verifica se a habilidade atual existe no banco de dados
            Skill currentSkill = databaseConnection.selectByName(currentSkillName, Skill.class);
            if (currentSkill == null) {
                return new Response<>(Operations.UPDATE_SKILL, Statuses.SKILL_NOT_EXIST);
            }

            // Verifica se o candidato possui essa habilidade atual
            CandidateSkill existingCandidateSkill = candidate.getCandidateSkills().stream()
                    .filter(cs -> cs.getSkill().getName().equals(currentSkillName))
                    .findFirst()
                    .orElse(null);

            if (existingCandidateSkill == null) {
                return new Response<>(Operations.UPDATE_SKILL, Statuses.SKILL_NOT_EXIST);
            }

            // Se newSkill for fornecida, verifica se o candidato já possui a nova habilidade
            if (newSkillName != null) {
                Skill newSkill = databaseConnection.selectByName(newSkillName, Skill.class);
                if (newSkill == null) {
                    return new Response<>(Operations.UPDATE_SKILL, Statuses.SKILL_NOT_EXIST);
                }

                boolean newSkillExists = candidate.getCandidateSkills().stream()
                        .anyMatch(cs -> cs.getSkill().getName().equals(newSkillName));

                if (newSkillExists) {
                    return new Response<>(Operations.UPDATE_SKILL, Statuses.SKILL_EXISTS);
                }

                existingCandidateSkill.setSkill(newSkill);
            }

            // Atualiza a experiência da habilidade
            existingCandidateSkill.setYearsOfExperience(experience);
            databaseConnection.update(existingCandidateSkill, CandidateSkill.class);

            return new Response<>(Operations.UPDATE_SKILL, Statuses.SUCCESS);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO UPDATESKILLCANDIDATE");
            return new Response<>(Operations.UPDATE_SKILL, Statuses.ERROR);
        }
    }



    public static Response<?> delete(String token, LinkedTreeMap<String, ?> data) {
        try {
            String skillToFind = (String) data.get("skill");

            Candidate candidate = databaseConnection.selectWithSkills(auth.getAuthId(token), Candidate.class);
            if (candidate == null) {
                return new Response<>(Operations.DELETE_SKILL, Statuses.USER_NOT_FOUND);
            }

            CandidateSkill skillToDelete = candidate.getCandidateSkills().stream()
                    .filter(cs -> cs.getSkill().getName().equals(skillToFind)).findFirst().orElse(null);

            if (skillToDelete == null) {
                return new Response<>(Operations.DELETE_SKILL, Statuses.SKILL_NOT_EXIST);
            }

            databaseConnection.delete(skillToDelete.getId(), CandidateSkill.class);

            return new Response<>(Operations.DELETE_SKILL, Statuses.SUCCESS);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO DELETESKILLCANDIDATE");
            return new Response<>(Operations.DELETE_SKILL, Statuses.ERROR);
        }
    }
}
