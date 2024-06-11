package records.skill;

public record SkillInfo(String skill, String experience, String id) {
    public SkillInfo(String skill, String experience) {
        this(skill, experience, null);
    }
}