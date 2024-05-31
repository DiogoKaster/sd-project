package server.controllers;

import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Roles;
import enums.Statuses;
import models.Candidate;
import models.DatabaseConnection;
import records.Response;
import records.candidate.CandidateLoginResponse;
import records.candidate.CandidateLookupResponse;
import server.middlewares.Auth;

public class CandidateController {
    private static final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
    private static final Auth auth = new Auth();
    public static Response<?> login(LinkedTreeMap<String, ?> data) {
        try {
            String email = (String) data.get("email");
            String password = (String) data.get("password");

            Candidate candidate = databaseConnection.verifyLogin(email, password, Candidate.class);
            String token = auth.generateToken(candidate.getId(), Roles.CANDIDATE.toString());
            CandidateLoginResponse responseModel = new CandidateLoginResponse(token);

            return new Response<>(Operations.LOGIN_CANDIDATE, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            return new Response<>(Operations.LOGIN_CANDIDATE, Statuses.INVALID_LOGIN);
        }
    }

    public static Response<?> signUp(LinkedTreeMap<String, ?> data) {
        Candidate candidate = getData(data);
        Candidate insertedCandidate = databaseConnection.insert(candidate, Candidate.class);

        if (insertedCandidate != null) {
            return new Response<>(Operations.SIGNUP_CANDIDATE, Statuses.SUCCESS);
        } else {
            return new Response<>(Operations.SIGNUP_CANDIDATE, Statuses.USER_EXISTS);
        }
    }

    public static Response<?> lookUp(String token) {
        Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

        if(candidate != null) {
            CandidateLookupResponse responseModel = new CandidateLookupResponse(candidate);
            return new Response<>(Operations.LOOKUP_ACCOUNT_CANDIDATE, Statuses.SUCCESS, responseModel);
        } else {
            return new Response<>(Operations.LOOKUP_ACCOUNT_CANDIDATE, Statuses.USER_NOT_FOUND);
        }
    }

    public static Response<?> update(String token, LinkedTreeMap<String, ?> data) {
        Candidate candidate = getData(data);
        candidate.setId(auth.getAuthId(token));

        Candidate updatedCandidate = databaseConnection.update(candidate, Candidate.class);
        if(updatedCandidate != null) {
            return new Response<>(Operations.UPDATE_ACCOUNT_CANDIDATE, Statuses.SUCCESS);
        } else {
            return new Response<>(Operations.UPDATE_ACCOUNT_CANDIDATE, Statuses.INVALID_EMAIL);
        }
    }

    public static Response<?> delete(String token) {
        databaseConnection.delete(auth.getAuthId(token), Candidate.class);
        return new Response<>(Operations.DELETE_ACCOUNT_CANDIDATE, Statuses.SUCCESS);
    }

    public static Response<?> logout(String token) {
        Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

        if (candidate != null) {
            return new Response<>(Operations.LOGOUT_CANDIDATE, Statuses.SUCCESS);
        }
        else {
            return new Response<>(Operations.LOGOUT_CANDIDATE, Statuses.USER_NOT_FOUND);
        }
    }

    private static Candidate getData(LinkedTreeMap<String, ?> data) {
        String name = (String) data.get("name");
        String email = (String) data.get("email");
        String password = (String) data.get("password");

        Candidate candidate = new Candidate();
        candidate.setName(name);
        candidate.setEmail(email);
        candidate.setPassword(password);

        return candidate;
    }
}
