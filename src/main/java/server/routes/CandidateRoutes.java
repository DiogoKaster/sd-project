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
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                String email = (String) data.get("email");
                String password = (String) data.get("password");

                if(isNullOrEmpty(email) || isNullOrEmpty(password)) {
                    return new Response<>(operation, Statuses.INVALID_FIELD);
                }

                try {
                    Candidate candidate = databaseConnection.verifyLogin(email, password);
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
                try {
                    if(request.token() != null) {

                        String token = request.token();
                        Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

                        if (candidate != null) {
                            CandidateLogoutResponse responseModel = new CandidateLogoutResponse();
                            return new Response<>(operation, Statuses.SUCCESS, responseModel);
                        }
                    }
                } catch (Exception e) {
                    return new Response<>(operation, Statuses.USER_NOT_FOUND, new Object());
                }
            }
            case LOOKUP_ACCOUNT_CANDIDATE -> {
                if (isNullOrEmpty(request.token())){
                    return new Response<>(operation, Statuses.INVALID_TOKEN, new Object());
                }
                try {
                    String token = request.token();
                    Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);
                    CandidateLookupResponse responseModel = new CandidateLookupResponse(candidate);

                    return new Response<>(operation, Statuses.SUCCESS, responseModel);
                } catch (Exception e) {
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

                if (request.token() != null) {
                    String token = request.token();
                    Candidate candidate = new Candidate();

                    if (data.containsKey("name")) {
                        candidate.setName(name);
                    }
                    if (data.containsKey("email")) {
                        candidate.setEmail(email);
                    }
                    if (data.containsKey("password")) {
                        candidate.setPassword(password);
                    }

                    candidate.setId(auth.getAuthId(token));

                    Candidate updatedCandidate = databaseConnection.update(candidate, Candidate.class);
                    if(updatedCandidate != null) {
                        CandidateUpdateResponse responseModel = new CandidateUpdateResponse();
                        return new Response<>(operation, Statuses.SUCCESS, responseModel);
                    } else {
                        return new Response<>(operation, Statuses.INVALID_EMAIL, new Object());
                    }
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
                    return new Response<>(operation, Statuses.INVALID_EMAIL, new Object());
                }
            }
        }

        return new Response<>(operation, Statuses.INVALID_OPERATION, new Object());
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
