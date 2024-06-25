package server.controllers;

import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import models.*;
import records.Response;
import records.job.JobInfo;
import records.job.RecruiterLookupJobSetResponse;
import records.skill.SkillInfo;
import server.middlewares.Auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JobController {
    private static final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
    private static final Auth auth = new Auth();

    public static Response<?> include(String token, LinkedTreeMap<String, ?> data) {
        try {
            String skillName = (String) data.get("skill");
            Integer experience = Integer.valueOf((String) data.get("experience"));
            String available = (String) data.get("available");
            String searchable = (String) data.get("searchable");

            Recruiter recruiter = databaseConnection.select(auth.getAuthId(token), Recruiter.class);
            if (recruiter == null) {
                return new Response<>(Operations.INCLUDE_JOB, Statuses.USER_NOT_FOUND);
            }

            Skill skill = databaseConnection.selectByName(skillName, Skill.class);
            if (skill == null) {
                return new Response<>(Operations.INCLUDE_JOB, Statuses.SKILL_NOT_EXIST);
            }

            Job job = new Job();
            job.setRecruiter(recruiter);
            job.setSkill(skill);
            job.setYearsOfExperience(experience);
            job.setAvailable(available);
            job.setSearchable(searchable);

            databaseConnection.insert(job, Job.class);

            return new Response<>(Operations.INCLUDE_JOB, Statuses.SUCCESS);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO DELETEJOB");
            return new Response<>(Operations.INCLUDE_JOB, Statuses.ERROR);
        }
    }

    public static Response<?> lookUpAll(String token) {
        try {
            Recruiter recruiter = databaseConnection.selectWithJobs(auth.getAuthId(token), Recruiter.class);
            if (recruiter == null) {
                return new Response<>(Operations.LOOKUP_JOBSET, Statuses.USER_NOT_FOUND);
            }

            List<SkillInfo> jobInfoList = new ArrayList<>();
            for (Job rj : recruiter.getJobs()) {
                SkillInfo jobInfo = new SkillInfo(
                        rj.getSkill().getName(),
                        rj.getYearsOfExperience().toString(),
                        rj.getId().toString()
                );
                jobInfoList.add(jobInfo);
            }

            String jobsetSize = String.valueOf(jobInfoList.size());
            RecruiterLookupJobSetResponse responseModel = new RecruiterLookupJobSetResponse(jobsetSize, jobInfoList);

            return new Response<>(Operations.LOOKUP_JOBSET, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO LOOKUPJOBSET");
            return new Response<>(Operations.LOOKUP_JOBSET, Statuses.ERROR);
        }
    }

    public static Response<?> lookUp(String token, LinkedTreeMap<String, ?> data) {
        try {
            Integer jobId = Integer.valueOf((String) data.get("id"));

            Recruiter recruiter = databaseConnection.selectWithJobs(auth.getAuthId(token), Recruiter.class);
            if (recruiter == null) {
                return new Response<>(Operations.LOOKUP_JOB, Statuses.USER_NOT_FOUND);
            }

            Job jobInfo = null;
            for (Job rj : recruiter.getJobs()) {
                if (rj.getId().equals(jobId)) {
                    jobInfo = rj;
                    break;
                }
            }

            if (jobInfo == null) {
                return new Response<>(Operations.LOOKUP_JOB, Statuses.SKILL_NOT_EXIST);
            }

            String skill = jobInfo.getSkill().getName();
            String experience = jobInfo.getYearsOfExperience().toString();
            String available = jobInfo.getAvailable();
            String searchable = jobInfo.getSearchable();
            String id = jobInfo.getId().toString();

            JobInfo responseModel = new JobInfo(skill, experience, id, available, searchable);

            return new Response<>(Operations.LOOKUP_JOB, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO LOOKUPJOB");
            return new Response<>(Operations.LOOKUP_JOB, Statuses.ERROR);
        }
    }

    public static Response<?> update(String token, LinkedTreeMap<String, ?> data) {
        try {
            String skill = (String) data.get("skill");
            Integer experience = Integer.valueOf((String) data.get("experience"));
            Integer jobId = Integer.valueOf((String) data.get("id"));

            Recruiter recruiter = databaseConnection.selectWithJobs(auth.getAuthId(token), Recruiter.class);
            if (recruiter == null) {
                return new Response<>(Operations.UPDATE_JOB, Statuses.USER_NOT_FOUND);
            }

            Job job = null;
            for (Job rj : recruiter.getJobs()) {
                if (rj.getId().equals(jobId)) {
                    job = rj;
                    break;
                }
            }

            if (job == null) {
                return new Response<>(Operations.UPDATE_JOB, Statuses.JOB_NOT_FOUND);
            }

            Skill newSkill = databaseConnection.selectByName(skill, Skill.class);
            job.setSkill(newSkill);
            job.setYearsOfExperience(experience);

            databaseConnection.update(job, Job.class);

            return new Response<>(Operations.UPDATE_JOB, Statuses.SUCCESS);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO UPDATEJOB");
            return new Response<>(Operations.UPDATE_JOB, Statuses.ERROR);
        }
    }

    public static Response<?> delete(String token, LinkedTreeMap<String, ?> data) {
        try {
            Integer jobId = Integer.valueOf((String) data.get("id"));

            Recruiter recruiter = databaseConnection.selectWithJobs(auth.getAuthId(token), Recruiter.class);
            if (recruiter == null) {
                return new Response<>(Operations.DELETE_JOB, Statuses.USER_NOT_FOUND);
            }

            Job jobToDelete = null;
            for (Job rj : recruiter.getJobs()) {
                if (rj.getId().equals(jobId)) {
                    jobToDelete = rj;
                    break;
                }
            }

            if (jobToDelete == null) {
                return new Response<>(Operations.DELETE_JOB, Statuses.JOB_NOT_FOUND);
            }

            databaseConnection.delete(jobToDelete.getId(), Job.class);

            return new Response<>(Operations.DELETE_JOB, Statuses.SUCCESS);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO DELETEJOB");
            return new Response<>(Operations.DELETE_JOB, Statuses.ERROR);
        }
    }

    public static Response<?> search(LinkedTreeMap<String, ?> data) {
        try {
            String filterType = data.get("filter") != null ? (String) data.get("filter") : null;
            List<String> skillsFilter = (data.get("skill") != null) ? (List<String>) data.get("skill") : Collections.emptyList();
            Integer experience = data.get("experience") != null ? Integer.parseInt((String) data.get("experience")) : null;

            List<Job> allJobs = databaseConnection.selectAllJobs();
            List<SkillInfo> jobInfoList = new ArrayList<>();

            for (Job job : allJobs) {
                boolean matches = false;

                if (filterType != null && !skillsFilter.isEmpty() && experience != null) {
                    boolean matchesSkills = skillsFilter.contains(job.getSkill().getName());
                    boolean matchesExperience = job.getYearsOfExperience() <= experience;

                    if (filterType.equals("OR")) {
                        matches = matchesSkills || matchesExperience;
                    } else if (filterType.equals("AND")) {
                        matches = matchesSkills && matchesExperience;
                    }
                } else if (!skillsFilter.isEmpty()) {
                    matches = skillsFilter.contains(job.getSkill().getName());
                } else if (experience != null) {
                    matches = job.getYearsOfExperience() <= experience;
                }

                if (matches) {
                    SkillInfo jobInfo = new SkillInfo(
                            job.getSkill().getName(),
                            job.getYearsOfExperience().toString(),
                            job.getId().toString()
                    );
                    jobInfoList.add(jobInfo);
                }
            }

            String jobsetSize = String.valueOf(jobInfoList.size());
            RecruiterLookupJobSetResponse responseModel = new RecruiterLookupJobSetResponse(jobsetSize, jobInfoList);

            return new Response<>(Operations.LOOKUP_JOBSET, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO LOOKUPJOBSET");
            return new Response<>(Operations.LOOKUP_JOBSET, Statuses.ERROR);
        }
    }

    public static Response<?> available(String token, LinkedTreeMap<String, ?> data) {
        try {
            Integer jobId = Integer.valueOf((String) data.get("id"));
            String available = (String) data.get("available");

            Recruiter recruiter = databaseConnection.selectWithJobs(auth.getAuthId(token), Recruiter.class);
            if (recruiter == null) {
                return new Response<>(Operations.SET_JOB_AVAILABLE, Statuses.USER_NOT_FOUND);
            }

            Job job = null;
            for (Job rj : recruiter.getJobs()) {
                if (rj.getId().equals(jobId)) {
                    job = rj;
                    break;
                }
            }

            if (job == null) {
                return new Response<>(Operations.SET_JOB_AVAILABLE, Statuses.JOB_NOT_FOUND);
            }

            job.setAvailable(available);

            databaseConnection.update(job, Job.class);

            return new Response<>(Operations.SET_JOB_AVAILABLE, Statuses.SUCCESS);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO SET_JOB_AVAILABLE");
            return new Response<>(Operations.SET_JOB_AVAILABLE, Statuses.ERROR);
        }
    }

    public static Response<?> searchable(String token, LinkedTreeMap<String, ?> data) {
        try {
            Integer jobId = Integer.valueOf((String) data.get("id"));
            String searchable = (String) data.get("searchable");

            Recruiter recruiter = databaseConnection.selectWithJobs(auth.getAuthId(token), Recruiter.class);
            if (recruiter == null) {
                return new Response<>(Operations.SET_JOB_SEARCHABLE, Statuses.USER_NOT_FOUND);
            }

            Job job = null;
            for (Job rj : recruiter.getJobs()) {
                if (rj.getId().equals(jobId)) {
                    job = rj;
                    break;
                }
            }

            if (job == null) {
                return new Response<>(Operations.SET_JOB_SEARCHABLE, Statuses.JOB_NOT_FOUND);
            }

            job.setSearchable(searchable);

            databaseConnection.update(job, Job.class);

            return new Response<>(Operations.SET_JOB_SEARCHABLE, Statuses.SUCCESS);
        } catch (Exception e) {
            System.out.println("[LOG]: ERRO FEIO DE EXCEPTION NO SET_JOB_SEARCHABLE");
            return new Response<>(Operations.SET_JOB_SEARCHABLE, Statuses.ERROR);
        }
    }
}
