package records.skill;

import java.util.List;

public record RecruiterLookupJobSetResponse(String jobset_size, List<SkillInfo> jobset) {
}
