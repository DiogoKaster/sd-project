package records.candidate;

import java.util.List;

public record CandidateSearchResponse(String profile_size, List<CandidateInfo> profile) {
}
