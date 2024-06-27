package records.job;

import records.skill.SkillInfo;

import java.util.List;

public record RecruiterLookupJobSetResponse(String jobset_size, List<JobInfo> jobset) {
}
