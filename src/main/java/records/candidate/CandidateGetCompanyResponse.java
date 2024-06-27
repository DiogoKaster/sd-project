package records.candidate;

import records.recruiter.RecruiterInfo;

import java.util.List;

public record CandidateGetCompanyResponse(String company_size, List<RecruiterInfo> company) {
}
