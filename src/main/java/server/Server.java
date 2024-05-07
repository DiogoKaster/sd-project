package server;
import java.io.*;
import java.net.*;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.google.gson.internal.LinkedTreeMap;
import enums.Operations;
import enums.Roles;
import enums.Statuses;
import helpers.Json;
import models.Candidate;
import models.DatabaseConnection;
import records.*;
import server.middlewares.Auth;

public class Server extends Thread{
    private final Socket client;
    public static void main(String[] args)   {
        try {
            Server.startConnection();
        } catch (IOException e) {
            System.exit(1);
        }
    }
    private Server(Socket clientSock){
        client = clientSock;
        start();
    }
    private static void startConnection() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Which port should be used?");
        int serverPort = Integer.parseInt(br.readLine());
        try(ServerSocket server = new ServerSocket(serverPort, 0)){
            System.out.println("Connection Socket Created");
            while(true){
                try {
                    System.out.println("Waiting for Connection");
                    new Server(server.accept());
                } catch (IOException e) {
                    System.err.println("Accept failed.");
                    System.exit(1);
                }
            }
        }
        catch (IOException e){
            System.err.println("Could not listen on port: "+serverPort);
            System.exit(1);
        }
    }

    @Override
    public void run(){
        System.out.println("New thread started");

        try(
                client;
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))
        ){
            String request;
            while((request = in.readLine()) != null){

                Json json = Json.getInstance();
                Request<?> clientRequest = json.fromJson(request, Request.class);
                Operations operation = clientRequest.operation();

                Response<?> response;
                DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
                Auth auth = new Auth();

                switch(operation){
                    case LOGIN_CANDIDATE -> {
                        System.out.println("\n[LOG]: Requested Operation: candidate login.");
                        LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) clientRequest.data();

                        try {
                            Candidate candidate = databaseConnection.verifyLogin((String) data.get("email"), (String) data.get("password"));
                            String token = auth.generateToken(candidate.getId(), Roles.CANDIDATE.toString());
                            CandidateLoginResponse responseModel = new CandidateLoginResponse(token);
                            response = new Response<>(operation, Statuses.SUCCESS, responseModel);
                            String jsonResponse = json.toJson(response);
                            System.out.println("[LOG]: SENDING RESPONSE: " + jsonResponse);
                            out.println(jsonResponse);
                        } catch (Exception e) {
                            response = new Response<CandidateLoginResponse>(operation, Statuses.INVALID_LOGIN);
                            String jsonResponse = json.toJson(response);
                            out.println(jsonResponse);
                        }
                    }
                    case SIGNUP_CANDIDATE -> {
                        System.out.println("\n[LOG]: Requested Operation: candidate sign up.");
                        LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) clientRequest.data();

                        Candidate candidate = new Candidate();
                        candidate.setName((String) data.get("name"));
                        candidate.setEmail((String) data.get("email"));
                        candidate.setPassword((String) data.get("password"));

                        databaseConnection.insert(candidate);

                        CandidateSignUpResponse responseModel = new CandidateSignUpResponse();
                        response = new Response<>(operation, Statuses.SUCCESS, responseModel);
                        String jsonResponse = json.toJson(response);

                        System.out.println("[LOG]: SENDING RESPONSE: " + jsonResponse);
                        out.println(jsonResponse);
                    }
                    case LOGOUT_CANDIDATE -> {
                        System.out.println("\n[LOG]: Requested Operation: candidate logout.");
                        String token = clientRequest.token();
                        Map<String, Claim> decoded = JWT.decode(token).getClaims();
                        int id = decoded.get("id").asInt();
                        Candidate candidate = databaseConnection.select(id, Candidate.class);

                        if (candidate != null) {
                            CandidateLogoutResponse responseModel = new CandidateLogoutResponse();
                            response = new Response<>(operation, Statuses.SUCCESS, responseModel);
                            String jsonResponse = json.toJson(response);

                            System.out.println("[LOG]: SENDING RESPONSE: " + jsonResponse);
                            out.println(jsonResponse);
                        }
                    }
                    case LOOKUP_ACCOUNT_CANDIDATE -> {
                        System.out.println("\n[LOG]: Requested Operation: candidate look up.");
                        String token = clientRequest.token();

                        Candidate candidate = databaseConnection.select(auth.getAuthId(token), Candidate.class);

                        CandidateLookupResponse responseModel = new CandidateLookupResponse(candidate);
                        response = new Response<>(operation, Statuses.SUCCESS, responseModel);
                        String jsonResponse = json.toJson(response);

                        System.out.println("[LOG]: SENDING RESPONSE: " + jsonResponse);
                        out.println(jsonResponse);
                    }
                    case UPDATE_ACCOUNT_CANDIDATE -> {
                        System.out.println("\n[LOG]: Requested Operation: candidate update.");
                        LinkedTreeMap<String, ?> data = (LinkedTreeMap<String, ?>) clientRequest.data();

                        try {
                            if (clientRequest.token() != null){
                                String token = clientRequest.token();
                                Candidate candidate = new Candidate();
                                candidate.setName((String) data.get("name"));
                                candidate.setEmail((String) data.get("email"));
                                candidate.setPassword((String) data.get("password"));
                                candidate.setId(auth.getAuthId(token));

                                databaseConnection.update(candidate);
                                CandidateUpdateResponse responseModel = new CandidateUpdateResponse();
                                response = new Response<>(operation, Statuses.SUCCESS, responseModel);
                                String jsonResponse = json.toJson(response);

                                System.out.println("[LOG]: SENDING RESPONSE: " + jsonResponse);
                                out.println(jsonResponse);
                            }
                        } catch (Exception e) {
                            response = new Response<CandidateUpdateResponse>(operation, Statuses.INVALID_EMAIL, null);
                            String jsonResponse = json.toJson(response);
                            out.println(jsonResponse);
                        }
                    }
                    case DELETE_ACCOUNT_CANDIDATE -> {
                        System.out.println("\n[LOG]: Requested Operation: candidate delete.");
                        try {
                            if (clientRequest.token() != null){
                                String token = clientRequest.token();
                                databaseConnection.delete(auth.getAuthId(token), Candidate.class);
                                CandidateDeleteResponse responseModel = new CandidateDeleteResponse();
                                response = new Response<>(operation, Statuses.SUCCESS, responseModel);
                                String jsonResponse = json.toJson(response);

                                System.out.println("[LOG]: SENDING RESPONSE: " + jsonResponse);
                                out.println(jsonResponse);
                            }
                        } catch (Exception e) {
                            response = new Response<CandidateDeleteResponse>(operation, Statuses.INVALID_EMAIL, null);
                            String jsonResponse = json.toJson(response);
                            out.println(jsonResponse);
                        }
                    }
                }
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
