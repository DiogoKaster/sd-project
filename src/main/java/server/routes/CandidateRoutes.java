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

                try {
                    Candidate candidate = databaseConnection.verifyLogin((String) data.get("email"), (String) data.get("password"));
                    String token = auth.generateToken(candidate.getId(), Roles.CANDIDATE.toString());
                    CandidateLoginResponse responseModel = new CandidateLoginResponse(token);
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);

                } catch (Exception e) {
                    return new Response<CandidateLoginResponse>(operation, Statuses.INVALID_LOGIN);
                }
            }
            case SIGNUP_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate sign up.");
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                Candidate candidate = new Candidate();
                candidate.setName((String) data.get("name"));
                candidate.setEmail((String) data.get("email"));
                candidate.setPassword((String) data.get("password"));

                databaseConnection.insert(candidate);

                CandidateSignUpResponse responseModel = new CandidateSignUpResponse();
                return new Response<>(operation, Statuses.SUCCESS, responseModel);

            }
            case LOGOUT_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate logout.");

                String token = request.token();
                Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

                if (candidate != null) {
                    CandidateLogoutResponse responseModel = new CandidateLogoutResponse();
                    return new Response<>(operation, Statuses.SUCCESS, responseModel);

                }
            }
            case LOOKUP_ACCOUNT_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate look up.");

                String token = request.token();
                Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

                CandidateLookupResponse responseModel = new CandidateLookupResponse(candidate);
                return new Response<>(operation, Statuses.SUCCESS, responseModel);

            }
            case UPDATE_ACCOUNT_CANDIDATE -> {
                System.out.println("\n[LOG]: Requested Operation: candidate update.");
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();

                try {
                    if (request.token() != null){
                        String token = request.token();
                        Candidate candidate = new Candidate();
                        candidate.setName((String) data.get("name"));
                        candidate.setEmail((String) data.get("email"));
                        candidate.setPassword((String) data.get("password"));
                        candidate.setId(auth.getAuthId(token));

                        databaseConnection.update(candidate);
                        CandidateUpdateResponse responseModel = new CandidateUpdateResponse();
                        return new Response<>(operation, Statuses.SUCCESS, responseModel);
                    }
                } catch (Exception e) {
                    return new Response<CandidateUpdateResponse>(operation, Statuses.INVALID_EMAIL, null);
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
