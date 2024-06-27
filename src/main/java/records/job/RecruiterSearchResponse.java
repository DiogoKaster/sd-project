package records.job;

import records.skill.SkillInfo;

import java.util.List;

public record RecruiterSearchResponse(String jobset_size, List<JobInfo> jobset) {
}
