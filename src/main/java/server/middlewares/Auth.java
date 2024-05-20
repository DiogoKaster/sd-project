package server.middlewares;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;

import java.util.Map;

public class Auth {
    private final Algorithm algorithm = Algorithm.HMAC256("DISTRIBUIDOS");

    public Auth() {
    }

    public String generateToken(int id, String role) {

        return JWT.create()
                .withClaim("id", id)
                .withClaim("role", role)
                .sign(algorithm);
    }

    public int getAuthId(String token) {
        try {
            Map<String, Claim> decoded = JWT.decode(token).getClaims();
            return decoded.get("id").asInt();
        } catch (JWTDecodeException e) {
            System.out.println("[LOG]: Token decode exception");
            return -1;
        }
    }
}
