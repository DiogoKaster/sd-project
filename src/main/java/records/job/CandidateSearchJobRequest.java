package records.job;

import java.util.List;

public record CandidateSearchJobRequest(List<String> skill, String experience, String filter) {
    public CandidateSearchJobRequest(String experience) {
        this(null, experience, null);
    }

    public CandidateSearchJobRequest(List<String> skill) {
        this(skill, null, null);
    }
}
