package server.routes;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Roles;
import enums.Statuses;
import models.Candidate;
import models.DatabaseConnection;
import models.Recruiter;
import records.*;
import records.candidate.*;
import records.recruiter.*;
import server.middlewares.Auth;

public class Routes {
    private final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
    private final Auth auth = new Auth();

    public Response<?> getResponse(Request<?> request) {
        Operations operation = request.operation();

        switch(operation){
            case LOGIN_CANDIDATE -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String email = (String) data.get("email");
                String password = (String) data.get("password");

                if(isNullOrEmpty(email) || isNullOrEmpty(password)) {
                    return new Response<>(operation, Statuses.INVALID_FIELD);
                }

                try {
                    Candidate candidate = databaseConnection.verifyLogin(email, password, Candidate.class);
                    String token = auth.generateToken(candidate.getId(), Roles.CANDIDATE.toString());
                    CandidateLoginResponse responseModel = new CandidateLoginResponse(token);

                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } catch (Exception e) {
                    return new Response<>(operation, Statuses.INVALID_LOGIN, new Object());
                }

            }
            case SIGNUP_CANDIDATE -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String name = (String) data.get("name");
                String email = (String) data.get("email");
                String password = (String) data.get("password");

                if (isNullOrEmpty(name) || isNullOrEmpty(email) || isNullOrEmpty(password)) {
                    return new Response<>(operation, Statuses.INVALID_FIELD);
                }

                Candidate candidate = new Candidate();
                candidate.setName((String) data.get("name"));
                candidate.setEmail((String) data.get("email"));
                candidate.setPassword((String) data.get("password"));

                Candidate insertedCandidate = databaseConnection.insert(candidate, Candidate.class);

                if (insertedCandidate != null) {
                    CandidateSignUpResponse responseModel = new CandidateSignUpResponse();
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } else {
                    return new Response<>(operation, Statuses.USER_EXISTS, new Object());
                }
            }
            case LOGOUT_CANDIDATE -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                if(tokenValid != null) {
                    return tokenValid;
                }
                Candidate candidate = databaseConnection.select(auth.getAuthId(request.token()), Candidate.class);

                if (candidate != null) {
                    CandidateLogoutResponse responseModel = new CandidateLogoutResponse();
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                }
                else {
                    return new Response<>(operation, Statuses.USER_NOT_FOUND, new Object());
                }
            }
            case LOOKUP_ACCOUNT_CANDIDATE -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                if(tokenValid != null) {
                    return tokenValid;
                }
                Candidate candidate = databaseConnection.select(auth.getAuthId(request.token()), Candidate.class);

                if(candidate != null) {
                    CandidateLookupResponse responseModel = new CandidateLookupResponse(candidate);
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } else {
                    return new Response<>(operation, Statuses.USER_NOT_FOUND);
                }
            }
            case UPDATE_ACCOUNT_CANDIDATE -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String name = (String) data.get("name");
                String email = (String) data.get("email");
                String password = (String) data.get("password");

                if((isNullOrEmpty(name) && isNullOrEmpty(email) && isNullOrEmpty(password))) {
                    return new Response<>(operation, Statuses.INVALID_FIELD, new Object());
                }

                Response<?> tokenValid = isTokenValid(operation, request.token());
                if(tokenValid != null) {
                    return tokenValid;
                }

                Candidate candidate = new Candidate();

                if (data.containsKey("name") && !(isNullOrEmpty(name))) {
                    candidate.setName(name);
                }
                if (data.containsKey("email") && !(isNullOrEmpty(email))) {
                    candidate.setEmail(email);
                }
                if (data.containsKey("password") && !(isNullOrEmpty(password))) {
                    candidate.setPassword(password);
                }

                candidate.setId(auth.getAuthId(request.token()));

                Candidate updatedCandidate = databaseConnection.update(candidate, Candidate.class);
                if(updatedCandidate != null) {
                    CandidateUpdateResponse responseModel = new CandidateUpdateResponse();
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } else {
                    return new Response<>(operation, Statuses.INVALID_EMAIL, new Object());
                }
            }
            case DELETE_ACCOUNT_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate delete.");

                Response<?> tokenValid = isTokenValid(operation, request.token());
                if(tokenValid != null) {
                    return tokenValid;
                }

                databaseConnection.delete(auth.getAuthId(request.token()), Candidate.class);
                CandidateDeleteResponse responseModel = new CandidateDeleteResponse();
                return new Response<>(operation, Statuses.SUCCESS, responseModel);
            }
            case LOGIN_RECRUITER -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String email = (String) data.get("email");
                String password = (String) data.get("password");

                if(isNullOrEmpty(email) || isNullOrEmpty(password)) {
                    return new Response<>(operation, Statuses.INVALID_FIELD);
                }

                try {
                    Recruiter recruiter = databaseConnection.verifyLogin(email, password, Recruiter.class);
                    String token = auth.generateToken(recruiter.getId(), Roles.RECRUITER.toString());
                    RecruiterLoginResponse responseModel = new RecruiterLoginResponse(token);

                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } catch (Exception e) {
                    return new Response<>(operation, Statuses.INVALID_LOGIN);
                }
            }
            case SIGNUP_RECRUITER -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String name = (String) data.get("name");
                String email = (String) data.get("email");
                String password = (String) data.get("password");
                String industry = (String) data.get("industry");
                String description = (String) data.get("description");

                if (isNullOrEmpty(name)
                        || isNullOrEmpty(email)
                        || isNullOrEmpty(password)
                        || isNullOrEmpty(industry)
                        || isNullOrEmpty(description)) {
                    return new Response<>(operation, Statuses.INVALID_FIELD);
                }

                Recruiter recruiter = new Recruiter();
                recruiter.setName(name);
                recruiter.setEmail(email);
                recruiter.setPassword(password);
                recruiter.setIndustry(industry);
                recruiter.setDescription(description);

                Recruiter insertedCandidate = databaseConnection.insert(recruiter, Recruiter.class);

                if (insertedCandidate != null) {
                    RecruiterSignUpResponse responseModel = new RecruiterSignUpResponse();
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } else {
                    return new Response<>(operation, Statuses.USER_EXISTS);
                }
            }
            case LOGOUT_RECRUITER -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                if(tokenValid != null) {
                    return tokenValid;
                }
                Recruiter recruiter = databaseConnection.select(auth.getAuthId(request.token()), Recruiter.class);

                if (recruiter != null) {
                    RecruiterLogoutResponse responseModel = new RecruiterLogoutResponse();
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                }
                else {
                    return new Response<>(operation, Statuses.USER_NOT_FOUND);
                }
            }
            case LOOKUP_ACCOUNT_RECRUITER -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                if(tokenValid != null) {
                    return tokenValid;
                }
                Recruiter recruiter = databaseConnection.select(auth.getAuthId(request.token()), Recruiter.class);

                if(recruiter != null) {
                    RecruiterLookupResponse responseModel = new RecruiterLookupResponse(recruiter);
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } else {
                    return new Response<>(operation, Statuses.USER_NOT_FOUND);
                }
            }
            case UPDATE_ACCOUNT_RECRUITER -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String name = (String) data.get("name");
                String email = (String) data.get("email");
                String password = (String) data.get("password");
                String industry = (String) data.get("industry");
                String description = (String) data.get("description");

                if((isNullOrEmpty(name) &&
                        isNullOrEmpty(email) &&
                        isNullOrEmpty(password) &&
                        isNullOrEmpty(industry) &&
                        isNullOrEmpty(description))) {
                    return new Response<>(operation, Statuses.INVALID_FIELD);
                }

                Response<?> tokenValid = isTokenValid(operation, request.token());
                if(tokenValid != null) {
                    return tokenValid;
                }

                Recruiter recruiter = new Recruiter();

                if (data.containsKey("name") && !(isNullOrEmpty(name))) {
                    recruiter.setName(name);
                }
                if (data.containsKey("email") && !(isNullOrEmpty(email))) {
                    recruiter.setEmail(email);
                }
                if (data.containsKey("password") && !(isNullOrEmpty(password))) {
                    recruiter.setPassword(password);
                }
                if (data.containsKey("industry") && !(isNullOrEmpty(industry))) {
                    recruiter.setIndustry(industry);
                }
                if (data.containsKey("description") && !(isNullOrEmpty(description))) {
                    recruiter.setDescription(description);
                }

                recruiter.setId(auth.getAuthId(request.token()));

                Recruiter updatedRecruiter = databaseConnection.update(recruiter, Recruiter.class);
                if(updatedRecruiter != null) {
                    RecruiterUpdateResponse responseModel = new RecruiterUpdateResponse();
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } else {
                    return new Response<>(operation, Statuses.INVALID_EMAIL);
                }
            }
            case DELETE_ACCOUNT_RECRUITER -> {
                System.out.println("\n[LOG]: Requested Operation: candidate delete.");

                Response<?> tokenValid = isTokenValid(operation, request.token());
                if(tokenValid != null) {
                    return tokenValid;
                }

                databaseConnection.delete(auth.getAuthId(request.token()), Recruiter.class);
                CandidateDeleteResponse responseModel = new CandidateDeleteResponse();
                return new Response<>(operation, Statuses.SUCCESS, responseModel);
            }
        }

        return new Response<>(operation, Statuses.INVALID_OPERATION);
    }

    private Response<?> isTokenValid(Operations operation, String token) {
        if (isNullOrEmpty(token)) {
            return new Response<>(operation, Statuses.INVALID_FIELD, new Object());
        }

        if (auth.getAuthId(token) == -1) {
            return new Response<>(operation, Statuses.INVALID_TOKEN, new Object());
        }

        return null;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
