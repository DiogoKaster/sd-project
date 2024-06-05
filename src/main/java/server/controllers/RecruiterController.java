package server.controllers;

import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Roles;
import enums.Statuses;
import models.DatabaseConnection;
import models.Recruiter;
import records.Response;
import records.recruiter.RecruiterLoginResponse;
import records.recruiter.RecruiterLookupResponse;
import server.middlewares.Auth;

public class RecruiterController {
    private static final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
    private static final Auth auth = new Auth();
    public static Response<?> login(LinkedTreeMap<String, ?> data) {
        try {
            String email = (String) data.get("email");
            String password = (String) data.get("password");

            Recruiter recruiter = databaseConnection.verifyLogin(email, password, Recruiter.class);
            String token = auth.generateToken(recruiter.getId(), Roles.RECRUITER.toString());
            RecruiterLoginResponse responseModel = new RecruiterLoginResponse(token);

            return new Response<>(Operations.LOGIN_RECRUITER, Statuses.SUCCESS, responseModel);
        } catch (Exception e) {
            return new Response<>(Operations.LOGIN_RECRUITER, Statuses.INVALID_LOGIN);
        }
    }

    public static Response<?> signUp(LinkedTreeMap<String, ?> data) {
        Recruiter recruiter = getData(data);
        Recruiter insertedCandidate = databaseConnection.insert(recruiter, Recruiter.class);

        if (insertedCandidate != null) {
            return new Response<>(Operations.SIGNUP_RECRUITER, Statuses.SUCCESS);
        } else {
            return new Response<>(Operations.SIGNUP_RECRUITER, Statuses.USER_EXISTS);
        }
    }

    public static Response<?> lookUp(String token) {
        Recruiter recruiter = databaseConnection.select(auth.getAuthId(token), Recruiter.class);

        if(recruiter != null) {
            RecruiterLookupResponse responseModel = new RecruiterLookupResponse(recruiter);
            return new Response<>(Operations.LOOKUP_ACCOUNT_RECRUITER, Statuses.SUCCESS, responseModel);
        } else {
            return new Response<>(Operations.LOOKUP_ACCOUNT_RECRUITER, Statuses.USER_NOT_FOUND);
        }
    }

    public static Response<?> update(String token, LinkedTreeMap<String, ?> data) {
        Recruiter recruiter = getData(data);
        recruiter.setId(auth.getAuthId(token));
        Recruiter updatedRecruiter = databaseConnection.update(recruiter, Recruiter.class);

        if(updatedRecruiter != null) {
            return new Response<>(Operations.UPDATE_ACCOUNT_RECRUITER, Statuses.SUCCESS);
        } else {
            return new Response<>(Operations.UPDATE_ACCOUNT_RECRUITER, Statuses.INVALID_EMAIL);
        }
    }

    public static Response<?> delete(String token) {
        databaseConnection.delete(auth.getAuthId(token), Recruiter.class);
        return new Response<>(Operations.DELETE_ACCOUNT_RECRUITER, Statuses.SUCCESS);
    }

    public static Response<?> logout(String token) {
        Recruiter recruiter = databaseConnection.select(auth.getAuthId(token), Recruiter.class);

        if (recruiter != null) {
            return new Response<>(Operations.LOGOUT_RECRUITER, Statuses.SUCCESS);
        }
        else {
            return new Response<>(Operations.LOGOUT_RECRUITER, Statuses.USER_NOT_FOUND);
        }
    }

    private static Recruiter getData(LinkedTreeMap<String, ?> data) {
        String name = (String) data.get("name");
        String email = (String) data.get("email");
        String password = (String) data.get("password");
        String industry = (String) data.get("industry");
        String description = (String) data.get("description");

        Recruiter recruiter = new Recruiter();
        recruiter.setName(name);
        recruiter.setEmail(email);
        recruiter.setPassword(password);
        recruiter.setIndustry(industry);
        recruiter.setDescription(description);

        return recruiter;
    }
}
