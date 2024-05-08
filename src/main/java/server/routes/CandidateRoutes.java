package server.routes;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Roles;
import enums.Statuses;
import models.Candidate;
import models.DatabaseConnection;
import records.*;
import server.middlewares.Auth;

public class CandidateRoutes {
    private final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
    private final Auth auth = new Auth();

    public Response<?> getResponse(Request<?> request) {
        Operations operation = request.operation();

        switch(operation){
            case LOGIN_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate login.");
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String email = (String) data.get("email");
                String password = (String) data.get("password");

                if (email != null && password != null) {
                    try {
                        Candidate candidate = databaseConnection.verifyLogin(email, password);
                        String token = auth.generateToken(candidate.getId(), Roles.CANDIDATE.toString());
                        CandidateLoginResponse responseModel = new CandidateLoginResponse(token);
                        return new Response<>(operation, Statuses.SUCCESS, responseModel);

                    } catch (Exception e) {
                        return new Response<CandidateLoginResponse>(operation, Statuses.INVALID_LOGIN);
                    }
                }
                return new Response<>(operation, Statuses.INVALID_LOGIN);
            }
            case SIGNUP_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate sign up.");
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String name = (String) data.get("name");
                String email = (String) data.get("email");
                String password = (String) data.get("password");

                if (name != null && email != null && password != null) {
                    Candidate candidate = new Candidate();
                    candidate.setName((String) data.get("name"));
                    candidate.setEmail((String) data.get("email"));
                    candidate.setPassword((String) data.get("password"));

                    Candidate insertedCandidate = databaseConnection.insert(candidate, Candidate.class);

                    if (insertedCandidate != null) {
                        CandidateSignUpResponse responseModel = new CandidateSignUpResponse();
                        return new Response<>(operation, Statuses.SUCCESS, responseModel);
                    } else {
                        return new Response<>(operation, Statuses.USER_EXISTS);
                    }
                }
                return new Response<>(operation, Statuses.USER_EXISTS);
            }
            case LOGOUT_CANDIDATE -> {
                try {
                    if(request.token() != null) {
                        System.out.println("\n[LOG]: Requested Operation: candidate logout.");

                        String token = request.token();
                        Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

                        if (candidate != null) {
                            CandidateLogoutResponse responseModel = new CandidateLogoutResponse();
                            return new Response<>(operation, Statuses.SUCCESS, responseModel);
                        }
                    }
                } catch (Exception e) {
                    return new Response<>(operation, Statuses.USER_NOT_FOUND);
                }
            }
            case LOOKUP_ACCOUNT_CANDIDATE -> {
                try {
                    if (request.token() != null) {
                        System.out.println("\n[LOG]: Requested Operation: candidate look up.");

                        String token = request.token();
                        Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

                        CandidateLookupResponse responseModel = new CandidateLookupResponse(candidate);
                        return new Response<>(operation, Statuses.SUCCESS, responseModel);
                    }
                } catch (Exception e) {
                    return new Response<>(operation, Statuses.USER_NOT_FOUND);
                }
            }
            case UPDATE_ACCOUNT_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate update.");
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                if (request.token() != null && data != null) {
                    String token = request.token();
                    Candidate candidate = new Candidate();

                    if (data.containsKey("name")) {
                        candidate.setName((String) data.get("name"));
                    }
                    if (data.containsKey("email")) {
                        candidate.setEmail((String) data.get("email"));
                    }
                    if (data.containsKey("password")) {
                        candidate.setPassword((String) data.get("password"));
                    }

                    candidate.setId(auth.getAuthId(token));

                    Candidate updatedCandidate = databaseConnection.update(candidate, Candidate.class);
                    if(updatedCandidate != null) {
                        CandidateUpdateResponse responseModel = new CandidateUpdateResponse();
                        return new Response<>(operation, Statuses.SUCCESS, responseModel);
                    } else {
                        return new Response<>(operation, Statuses.INVALID_EMAIL, null);
                    }
                }
                else {
                    return new Response<>(operation, Statuses.INVALID_EMAIL, null);
                }
            }
            case DELETE_ACCOUNT_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate delete.");
                try {
                    if (request.token() != null){
                        String token = request.token();
                        databaseConnection.delete(auth.getAuthId(token), Candidate.class);
                        CandidateDeleteResponse responseModel = new CandidateDeleteResponse();
                        return new Response<>(operation, Statuses.SUCCESS, responseModel);
                    }
                } catch (Exception e) {
                    return new Response<CandidateDeleteResponse>(operation, Statuses.INVALID_EMAIL, null);
                }
            }
        }

        return null;
    }
}
