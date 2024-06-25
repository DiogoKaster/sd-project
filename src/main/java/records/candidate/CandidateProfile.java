package records.candidate;

import records.skill.SkillInfo;

import java.util.List;

public record CandidateProfile(String idUser, String name, List<SkillInfo> skillList) {
}
