package records.recruiter;

public record RecruiterUpdateRequest(String email, String password, String name, String industry, String description) {
}
