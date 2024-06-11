package server.routes;

import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Statuses;
import records.*;
import server.controllers.CandidateController;
import server.controllers.RecruiterController;
import server.controllers.SkillController;
import server.middlewares.Auth;

import java.util.Objects;

public class Routes {
    public Response<?> getResponse(Request<?> request) {
        Operations operation = request.operation();

        switch(operation) {
            case LOGIN_CANDIDATE -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();
                Response<?> validationResponse = validateFields(operation, data, "email", "password");
                return Objects.requireNonNullElseGet(validationResponse, () -> CandidateController.login(data));
            }
            case SIGNUP_CANDIDATE -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();
                Response<?> validationResponse = validateFields(operation, data, "name", "email", "password");
                return Objects.requireNonNullElseGet(validationResponse, () -> CandidateController.signUp(data));

            }
            case LOGOUT_CANDIDATE -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                return Objects.requireNonNullElseGet(tokenValid, () -> CandidateController.logout(request.token()));
            }
            case LOOKUP_ACCOUNT_CANDIDATE -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                return Objects.requireNonNullElseGet(tokenValid, () -> CandidateController.lookUp(request.token()));
            }
            case UPDATE_ACCOUNT_CANDIDATE -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                if (tokenValid != null) {
                    return tokenValid;
                }

                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();
                Response<?> validationResponse = validateFields(operation, data, "name", "email", "password");
                return Objects.requireNonNullElseGet(validationResponse, () -> CandidateController.update(request.token(), data));
            }
            case DELETE_ACCOUNT_CANDIDATE -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                return Objects.requireNonNullElseGet(tokenValid, () -> CandidateController.delete(request.token()));
            }
            case LOGIN_RECRUITER -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();
                Response<?> validationResponse = validateFields(operation, data, "email", "password");
                return Objects.requireNonNullElseGet(validationResponse, () -> RecruiterController.login(data));

            }
            case SIGNUP_RECRUITER -> {
                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();
                Response<?> validationResponse = validateFields(operation, data, "name", "email", "password", "industry", "description");
                return Objects.requireNonNullElseGet(validationResponse, () -> RecruiterController.signUp(data));

            }
            case LOGOUT_RECRUITER -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                return Objects.requireNonNullElseGet(tokenValid, () -> RecruiterController.logout(request.token()));
            }
            case LOOKUP_ACCOUNT_RECRUITER -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                return Objects.requireNonNullElseGet(tokenValid, () -> RecruiterController.lookUp(request.token()));
            }
            case UPDATE_ACCOUNT_RECRUITER -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                if (tokenValid != null) {
                    return tokenValid;
                }

                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();
                Response<?> validationResponse = validateFields(operation, data, "name", "email", "password", "industry", "description");
                return Objects.requireNonNullElseGet(validationResponse, () -> RecruiterController.update(request.token(), data));

            }
            case DELETE_ACCOUNT_RECRUITER -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                return Objects.requireNonNullElseGet(tokenValid, () -> RecruiterController.delete(request.token()));
            }
            case INCLUDE_SKILL -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                if (tokenValid != null) {
                    return tokenValid;
                }

                LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) request.data();
                return SkillController.includeCandidate(request.token(), data);
            }
            case LOOKUP_SKILLSET -> {
                Response<?> tokenValid = isTokenValid(operation, request.token());
                return Objects.requireNonNullElseGet(tokenValid, () -> SkillController.lookUpSkillSetCandidate(request.token()));
            }

            default -> {
                return new Response<>(operation, Statuses.INVALID_OPERATION);
            }
        }
    }

    private Response<?> isTokenValid(Operations operation, String token) {
        Auth auth = new Auth();

        if (isNullOrEmpty(token)) {
            return new Response<>(operation, Statuses.INVALID_FIELD);
        }

        if (auth.getAuthId(token) == -1) {
            return new Response<>(operation, Statuses.INVALID_TOKEN);
        }

        return null;
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static Response<?> validateFields(Operations operation, LinkedTreeMap<String, ?> data, String... requiredFields) {
        for (String field : requiredFields) {
            Object value = data.get(field);
            if (isNullOrEmpty((String) value)) {
                return new Response<>(operation, Statuses.INVALID_FIELD);
            }
        }
        return null;
    }
}