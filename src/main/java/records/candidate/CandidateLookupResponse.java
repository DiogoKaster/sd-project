package records.candidate;

import models.Candidate;

public record CandidateLookupResponse(String email, String password, String name) {
    public CandidateLookupResponse(Candidate candidate){
        this(candidate.getEmail(), candidate.getPassword(), candidate.getName());
    }
}
