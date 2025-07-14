/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Enbas.Controllers;

import Entity.InfoTrack;
import Entity.Utente;
import Services.Filter.Secured;
import Utils.JPAUtil;
import Utils.Utils;
import com.google.gson.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Salvatore
 */
@Path("/utente")
public class UtenteController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UtenteController.class.getName());
    JPAUtil jpaUtil = new JPAUtil();
    
    @POST
    @Path("/findById")
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@FormParam("userId") Long userId, @FormParam("selectedUserId") Long selectedUserId, @HeaderParam("Authorization") String authorizationHeader) {
        try {
            Utente utente = jpaUtil.findUserByUserId(userId.toString());
            if (utente.getRuolo().getId() == 1) {
                Utente utenteSelezionato = jpaUtil.findUserByUserId(selectedUserId.toString());
                if (utenteSelezionato == null) {
                    InfoTrack infoTrack = new InfoTrack("READ",
                            "Utente controller - API - (/findById)",
                            404,
                            "Errore - Utente con id " + selectedUserId + "non trovato.",
                            "API chiamata dall'utente con id " + userId + ".",
                            "Errore 404 - NOT_FOUND",
                            Utils.formatLocalDateTime(LocalDateTime.now()));
                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\":\"Utente non trovato\"}")
                            .build();
                }
                JsonObject json = new JsonObject();
                json.addProperty("id", utenteSelezionato.getId());
                if (utenteSelezionato.getNome() != null) {
                    json.addProperty("nome", utenteSelezionato.getNome());
                }
                
                if (utenteSelezionato.getCognome() != null) {
                    json.addProperty("cognome", utenteSelezionato.getCognome());
                }
                
                if (utenteSelezionato.getEmail() != null) {
                    json.addProperty("email", utenteSelezionato.getEmail());
                }
                
                if (utenteSelezionato.getUsername() != null) {
                    json.addProperty("username", utenteSelezionato.getUsername());
                }
                
                if (utenteSelezionato.getStato_utente() != null) {
                    json.addProperty("stato", utenteSelezionato.getStato_utente().toString().toLowerCase());
                }
                
                if (utenteSelezionato.getEtà() != 0) {
                    json.addProperty("età", utenteSelezionato.getEtà());
                }
                
                if (utenteSelezionato.getIndirizzo() != null) {
                    json.addProperty("indirizzo", utenteSelezionato.getIndirizzo());
                }
                if (utenteSelezionato.getRuolo().getNome() != null) {
                    json.addProperty("ruolo", utenteSelezionato.getRuolo().getNome());
                }
                
                InfoTrack infoTrack = new InfoTrack("READ",
                        "Utente controller - API - (/findById)",
                        200,
                        "Utenza con id " + utenteSelezionato.getId() + " trovata.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.ok(json.toString()).build();
            } else {
                InfoTrack infoTrack = new InfoTrack("READ",
                        "Utente controller - API - (/findById)",
                        401,
                        "Ruolo con autorizzato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
            }
            
        } catch (Exception e) {
            LOGGER.error("Errore nella ricerca dell'utenza con id " + selectedUserId, e);
            InfoTrack infoTrack = new InfoTrack("READ",
                    "Utente controller - API - (/findById)",
                    500,
                    "Errore - Utenza con id " + selectedUserId + " non trovato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    Utils.estraiEccezione(e),
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
            return Response.serverError().entity("{\"error\": \"Errore interno nella ricerca dell'utenza\"}").build();
        }
    }
    
    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Secured
    public Response creaUtente(
            @FormParam("userId") Long userId,
            @FormParam("nome") String nome,
            @FormParam("cognome") String cognome,
            @FormParam("email") String email,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("età") int età,
            @FormParam("indirizzo") String indirizzo,
            @FormParam("ruolo") int ruolo_id,
            @HeaderParam("Authorization") String authorizationHeader
    ) {
        
        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {
            try {
                jpaUtil.creaUtente(nome, cognome, email, username, password, età, indirizzo, ruolo_id, LOGGER);
                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Utente controller - API - (/create)",
                        200,
                        "Utenza creata con successo.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                
                return Response.ok().entity("{\"status\":\"Utenza creata con successo.\"}").build();
            } catch (Exception e) {
                LOGGER.error("Errore nella creazione dell'utenza. " + e);
                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Utente controller - API - (/create)",
                        500,
                        "Errore - Utenza non creata.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.serverError().entity("{\"error\": \"Errore interno durante la creazione dell'utenza\"}").build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("CREATE",
                    "Utente controller - API - (/create)",
                    401,
                    "Ruolo con autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }
    
    @PATCH
    @Path("/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response aggiornaUtente(
            @FormParam("userId") Long userId,
            @FormParam("selectedUserId") Long selectedUserId,
            @FormParam("nome") String nome,
            @FormParam("cognome") String cognome,
            @FormParam("email") String email,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("statoUtente") String stato_utente,
            @FormParam("età") int età,
            @FormParam("indirizzo") String indirizzo,
            @FormParam("ruolo") int ruolo_id,
            @HeaderParam("Authorization") String authorizationHeader
    ) {
        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {
            try {
                jpaUtil.modificaUtente(selectedUserId, nome, cognome, email, username, password, stato_utente, età, indirizzo, ruolo_id, LOGGER);
                
                InfoTrack infoTrack = new InfoTrack("UPDATE",
                        "Utente controller - API - (/update)",
                        200,
                        "Utenza con id " + selectedUserId + " aggiornata con successo.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                
                return Response.ok().entity("{\"status\":\"Utenza modificata con successo.\"}").build();
            } catch (Exception e) {
                LOGGER.error("Errore nell'aggiornamento dell'utenza. " + e);
                InfoTrack infoTrack = new InfoTrack("UPDATE",
                        "Utente controller - API - (/update)",
                        500,
                        "Errore - Utenza con id  " + selectedUserId + " non aggiornata.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.serverError().entity("{\"error\": \"Errore interno durante l'aggiornamento dell'utenza\"}").build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("UPDATE",
                    "Utente controller - API - (/update)",
                    401,
                    "Ruolo con autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }
    
    @DELETE
    @Path("/delete")
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@FormParam("userId") Long userId, @FormParam("selectedUserId") Long selectedUserId, @HeaderParam("Authorization") String authorizationHeader
    ) {
        
        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {
            try {
                Utente utenteSelezionato = jpaUtil.findUserByUserId(selectedUserId.toString());
                if (utenteSelezionato == null) {
                    InfoTrack infoTrack = new InfoTrack("DELETE",
                            "Utente controller - API - (/delete)",
                            404,
                            "Errore - Utente con id " + selectedUserId + "non trovato.",
                            "API chiamata dall'utente con id " + userId + ".",
                            "Errore 404 - NOT_FOUND",
                            Utils.formatLocalDateTime(LocalDateTime.now()));
                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\":\"Utenza non trovato\"}")
                            .build();
                }
                boolean deleted = jpaUtil.deleteUtenteById(selectedUserId);
                if (deleted) {
                    InfoTrack infoTrack = new InfoTrack("DELETE",
                            "Utente controller - API - (/delete)",
                            200,
                            "Utenza con id " + selectedUserId + " eliminata con successo.",
                            "API chiamata dall'utente con id " + utente.getId() + ".",
                            null,
                            Utils.formatLocalDateTime(LocalDateTime.now()));
                    
                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    return Response.ok("{\"status\":\"Utenza eliminata con successo\"}").build();
                } else {
                    InfoTrack infoTrack = new InfoTrack("DELETE",
                            "Utente controller - API - (/delete)",
                            500,
                            "Errore - Utenza con id  " + selectedUserId + " non eliminata.",
                            "API chiamata dall'utente con id " + userId + ".",
                            "Errore - 500 - errore interno ",
                            Utils.formatLocalDateTime(LocalDateTime.now()));
                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    
                    return Response.serverError()
                            .entity("{\"error\":\"Errore durante l'eliminazione dell'utenza \"}")
                            .build();
                }
                
            } catch (Exception e) {
                LOGGER.error("Errore interno durante l'eliminazione dell'utenza ", e.getMessage());
                InfoTrack infoTrack = new InfoTrack("DELETE",
                        "Utente controller - API - (/delete)",
                        500,
                        "Errore - Utenza con id  " + selectedUserId + " non eliminata.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                
                return Response.serverError()
                        .entity("{\"error\":\"Errore interno durante l'eliminazione dell'utenza\"}")
                        .build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("DELETE",
                    "Utente controller - API - (/delete)",
                    401,
                    "Ruolo con autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }
    
}
