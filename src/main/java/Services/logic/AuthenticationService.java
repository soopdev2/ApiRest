package Services.logic;

import Entity.InfoTrack;
import Entity.Utente;
import Utils.JPAUtil;
import Utils.Utils;
import static Utils.Utils.calcolaScadenza;
import static Utils.Utils.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Produces;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.Path;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Date;
import java.util.MissingResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/oauth2")
public class AuthenticationService {

    public static final String CLIENT_SECRET = config.getString("CLIENT_SECRET");
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class.getName());
    JPAUtil jpaUtil = new JPAUtil();

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToken(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret) {

        if (!"client_credentials".equals(grantType)) {
            InfoTrack infoTrack = new InfoTrack("CREATE",
                    "Authentication service - API - (/token)",
                    400,
                    "Errore - generazione token non effettuata.",
                    "API chiamata dall'utenza con clientId " + clientId + ".",
                    "Errore 400 - BAD_REQUEST - Grant type diverso da client_credentials.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid grant_type" + " " + grantType).build();
        }

        if (isValidCredentials(clientId, clientSecret)) {
            try {
                String accessToken = generateToken(CLIENT_SECRET, "access_token");
                //String refreshToken = generateRefreshToken(CLIENT_SECRET, "refresh_token");
                Instant expirationInstant = getExpirationInstantFromToken(accessToken, clientSecret);
                //Instant expirationInstant2 = getExpirationInstantFromToken(refreshToken, clientSecret);

                //String jsonResponse = "{\"access_token\":\"" + accessToken + "\", \"expiration_date\":\"" + formatInstant(expirationInstant) + "\", \"refresh_token\":\"" + refreshToken + "\", \"refresh_token_expiration_date\":\"" + formatInstant(expirationInstant2) + "\"}";
                String jsonResponseSenzaRefresh = "{\"access_token\":\"" + accessToken + "\", \"expiration_date\":\"" + formatInstant(expirationInstant) + "\"}";

                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Authentication service - API - (/token)",
                        200,
                        "Token generato con successo.",
                        "API chiamata dall'utenza con clienId " + clientId + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.ok().entity(jsonResponseSenzaRefresh).type(MediaType.APPLICATION_JSON).build();
            } catch (Exception e) {

                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Authentication service - API - (/token)",
                        500,
                        "Errore - Token non generato.",
                        "API chiamata dall'utenza con clientId " + clientId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore durante la generazione del token").build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("CREATE",
                    "Authentication service - API - (/token)",
                    401,
                    "Credenziali errate o utenza non autorizzata.",
                    "API chiamata dall'utenza con clientId " + clientId + ".",
                    "L'utenza che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla oppure credenziali errate.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

//    @POST
//    @Path("/refreshToken")
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response refreshToken(
//            @FormParam("client_id") String clientId,
//            @FormParam("client_secret") String clientSecret,
//            @FormParam("refresh_token") String refreshToken) {
//
//        if (isValidRefreshToken(clientId, clientSecret, refreshToken)) {
//            try {
//                String newAccessToken = generateToken(CLIENT_SECRET, "access_token");
//                String newRefreshToken = generateRefreshToken(CLIENT_SECRET, "refresh_token");
//                Instant expirationInstantAccessToken = getExpirationInstantFromToken(newAccessToken, clientSecret);
//                Instant expirationInstantRefreshToken = getExpirationInstantFromToken(newRefreshToken, clientSecret);
//
//                String jsonResponse = "{\"access_token\":\"" + newAccessToken + "\", \"expiration_date\":\"" + formatInstant(expirationInstantAccessToken) + "\", \"refresh_token\":\"" + newRefreshToken + "\", \"refresh_token_expiration_date\":\"" + formatInstant(expirationInstantRefreshToken) + "\"}";
//
//                return Response.ok().entity(jsonResponse).type(MediaType.APPLICATION_JSON).build();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error during token generation").build();
//            }
//        } else {
//            return Response.status(Response.Status.UNAUTHORIZED).build();
//        }
//    }
//    @GET
//    @Path("/auth")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response authenticate(@HeaderParam("Authorization") String authorizationHeader) {
//        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            return Response.status(Response.Status.UNAUTHORIZED)
//                    .entity("{\"message\":\"Missing or invalid Authorization header. Expected Bearer token.\"}")
//                    .build();
//        }
//
//        String token = authorizationHeader.substring("Bearer ".length());
//
//        try {
//            if (isValidToken(token) && isAccessToken(token)) {
//                return Response.ok().entity("{\"message\":\"Authentication successful. Token is valid.\"}")
//                        .build();
//            } else {
//                return Response.status(Response.Status.UNAUTHORIZED)
//                        .entity("{\"message\":\"Invalid or expired access token.\"}")
//                        .build();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity("{\"message\":\"Error during token validation.\"}")
//                    .build();
//        }
//    }
//    private boolean isValidRefreshToken(String clientId, String clientSecret, String refreshToken) {
//        EntityManagerFactory entityManagerFactory = null;
//        EntityManager entityManager = null;
//
//        try {
//            entityManagerFactory = Persistence.createEntityManagerFactory("gestionale_questionario");
//            entityManager = entityManagerFactory.createEntityManager();
//
//            TypedQuery<Utente> query = entityManager.createQuery(
//                    "SELECT u FROM Utente u WHERE u.username = :username", Utente.class
//            )
//                    .setParameter("username", clientId);
//
//            Utente utente = query.getSingleResult();
//
//            if (utente != null) {
//                Instant expirationInstant = getExpirationInstantFromToken(refreshToken, clientSecret);
//                if (expirationInstant != null && expirationInstant.isBefore(Instant.now())) {
//                    return false;
//                }
//
//                if (utente.getRefreshToken() != null && utente.getRefreshToken().equals(refreshToken)) {
//                    String newAccessToken = generateToken(CLIENT_SECRET, "access_token");
//                    String newRefreshToken = generateRefreshToken(CLIENT_SECRET, "refresh_token");
//                    utente.setRefreshToken(newRefreshToken);
//
//                    entityManager.getTransaction().begin();
//                    entityManager.merge(utente);
//                    entityManager.getTransaction().commit();
//
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (entityManager != null) {
//                entityManager.close();
//            }
//            if (entityManagerFactory != null) {
//                entityManagerFactory.close();
//            }
//        }
//
//        return false;
//    }
    public static boolean isValidCredentials(String clientId, String clientSecret) {
        int i = 1;

        while (true) {
            try {
                String storedClientId = config.getString("client_id_" + i);
                String storedClientSecret = config.getString("client_secret_" + i);

                if (storedClientId.equalsIgnoreCase(clientId) && storedClientSecret.equals(clientSecret)) {
                    return true;
                }

                i++;
            } catch (MissingResourceException e) {
                break;
            }
        }

        return false;
    }

    public static String generateToken(String clientSecret, String tokenType) {
        String scadenza = config.getString("scadenza_token");
        LocalDateTime scadenza_token = calcolaScadenza(scadenza);
        Key key = Keys.hmacShaKeyFor(clientSecret.getBytes());
        Instant now = Instant.now();
        Instant scadenzaInstant = scadenza_token.atZone(ZoneId.systemDefault()).toInstant();
        Date scadenza_token_date = Date.from(scadenzaInstant);

        String token = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(scadenza_token_date)
                .claim("token_type", tokenType)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    public static String generateRefreshToken(String clientSecret, String tokenType) {
        Key key = Keys.hmacShaKeyFor(clientSecret.getBytes());
        String scadenza = config.getString("scadenza_refreshToken");
        LocalDateTime scadenza_refresh_token = calcolaScadenza(scadenza);
        Instant scadenzaInstant = scadenza_refresh_token.atZone(ZoneId.systemDefault()).toInstant();
        Date scadenza_refresh_token_date = Date.from(scadenzaInstant);
        Instant now = Instant.now();

        String token = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(scadenza_refresh_token_date)
                .claim("token_type", tokenType)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    public String formatInstant(Instant instant) {
        return DateTimeFormatter.ISO_DATE_TIME
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    public static Instant getExpirationInstantFromToken(String token, String clientSecret) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(CLIENT_SECRET.getBytes())
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();
            Date expirationDate = claims.getExpiration();

            return expirationDate.toInstant();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isValidToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(CLIENT_SECRET.getBytes())
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();

            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isAccessToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(CLIENT_SECRET.getBytes())
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();

            return claims.containsKey("token_type") && "access_token".equals(claims.get("token_type"));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void saveRefreshTokenToDatabase(String clientId, String refreshToken) {
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;

        try {
            entityManagerFactory = Persistence.createEntityManagerFactory("gestionale_questionario");
            entityManager = entityManagerFactory.createEntityManager();

            TypedQuery<Utente> query = entityManager.createQuery(
                    "SELECT u FROM Utente u WHERE u.username = :username", Utente.class
            )
                    .setParameter("username", clientId);

            Utente utente = query.getSingleResult();

            if (utente != null) {
                utente.setRefreshToken(refreshToken);
                entityManager.getTransaction().begin();
                entityManager.merge(utente);
                entityManager.getTransaction().commit();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
            }
        }
    }

    public static String Auth(String clientId) {
        try {
            String accessToken = generateToken(CLIENT_SECRET, "access_token");
            String refreshToken = generateRefreshToken(CLIENT_SECRET, "refresh_token");
            saveRefreshTokenToDatabase(clientId, refreshToken);
            return accessToken;
        } catch (Exception e) {
            e.printStackTrace();
            Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error during token generation").build();
        }

        return null;
    }
}
