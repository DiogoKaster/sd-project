package server;

import java.io.*;
import java.net.*;
import helpers.Json;
import records.*;
import server.routes.Routes;
import server.views.UserConnected;

public class Server extends Thread {
    private final Socket client;
    private static final UserConnected userConnected = new UserConnected();

    public static void main(String[] args) {
        try {
            startConnection();
        } catch (IOException e) {
            System.exit(1);
        }
    }

    private Server(Socket clientSock) {
        client = clientSock;
        start();
    }

    private static void startConnection() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Which port should be used?");
        int serverPort = Integer.parseInt(br.readLine());
        try (ServerSocket server = new ServerSocket(serverPort, 0)) {
            System.out.println("Connection Socket Created");
            while (true) {
                try {
                    System.out.println("Waiting for Connection");
                    new Server(server.accept());
                } catch (IOException e) {
                    System.err.println("Accept failed.");
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + serverPort);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        System.out.println("New thread started");
        String clientIP = client.getInetAddress().getHostAddress();
        System.out.println(clientIP);

        userConnected.addClientIP(clientIP);

        try (
                client;
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                Json json = Json.getInstance();
                System.out.println("[LOG]: RECEIVING REQUEST: " + request);
                Request<?> clientRequest = json.fromJson(request, Request.class);
                Routes routes = new Routes();

                Response<?> response = routes.getResponse(clientRequest);
                String jsonResponse = json.toJson(response);
                System.out.println("[LOG]: SENDING RESPONSE: " + jsonResponse);
                out.println(jsonResponse);

                userConnected.addRequest(clientIP, request);
                userConnected.addResponse(clientIP, jsonResponse);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
