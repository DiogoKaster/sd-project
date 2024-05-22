package records.recruiter;

import models.Recruiter;

public record RecruiterLookupResponse(String email, String password, String name, String industry, String description) {
    public RecruiterLookupResponse(Recruiter recruiter){
        this(recruiter.getEmail(), recruiter.getPassword(), recruiter.getName(), recruiter.getIndustry(), recruiter.getDescription());
    }
}
