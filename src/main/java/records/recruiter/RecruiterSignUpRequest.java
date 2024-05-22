package records.recruiter;

public record RecruiterSignUpRequest(String email, String password, String name, String industry, String description) {
}
