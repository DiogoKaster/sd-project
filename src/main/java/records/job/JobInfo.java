package records.job;

public record JobInfo(String skill, String experience, String id, String available, String searchable) {
    public JobInfo(String skill, String experience, String id, String available) { this(skill, experience, id, available, null); };
}
