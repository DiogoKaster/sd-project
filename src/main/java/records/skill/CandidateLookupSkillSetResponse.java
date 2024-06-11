package records.skill;

import java.util.List;

public record CandidateLookupSkillSetResponse(String skillset_size, List<SkillInfo> skillset) {
}
