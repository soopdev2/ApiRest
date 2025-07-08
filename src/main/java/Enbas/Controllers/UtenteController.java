/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Enbas.Controllers;

import Entity.Utente;
import Services.Filter.Secured;
import Utils.JPAUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Salvatore
 */
@Path("/utente")
public class UtenteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtenteController.class.getName());

    @POST
    @Path("/findById")
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@FormParam("userId") Long userId, @FormParam("selectedUserId") Long selectedUserId, @HeaderParam("Authorization") String authorizationHeader) {
        try {
            JPAUtil jpaUtil = new JPAUtil();
            Utente utente = jpaUtil.findUserByUserId(userId.toString());
            if (utente.getRuolo().getId() == 1) {
                Utente utenteSelezionato = jpaUtil.findUserByUserId(selectedUserId.toString());
                if (utenteSelezionato == null) {
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

                return Response.ok(json.toString()).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
            }

        } catch (Exception e) {
            LOGGER.error("Errore nella ricerca dell'utenza con id " + selectedUserId, e);
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
        JPAUtil jpaUtil = new JPAUtil();
        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {
            jpaUtil.creaUtente(nome, cognome, email, username, password, età, indirizzo, ruolo_id, LOGGER);
            return Response.ok().entity("{\"status\":\"Utenza creata con successo.\"}").build();
        } else {
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
        JPAUtil jpaUtil = new JPAUtil();
        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {
            jpaUtil.modificaUtente(selectedUserId, nome, cognome, email, username, password, stato_utente, età, indirizzo, ruolo_id, LOGGER);
            return Response.ok().entity("{\"status\":\"Utenza modificata con successo.\"}").build();
        } else {
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
        JPAUtil jpaUtil = new JPAUtil();
        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {
            try {
                Utente utenteSelezionato = jpaUtil.findUserByUserId(selectedUserId.toString());
                if (utenteSelezionato == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\":\"Utenza non trovato\"}")
                            .build();
                }
                boolean deleted = jpaUtil.deleteUtenteById(selectedUserId);
                if (deleted) {
                    return Response.ok("{\"status\":\"Utenza eliminata con successo\"}").build();
                } else {
                    return Response.serverError()
                            .entity("{\"error\":\"Errore durante l'eliminazione dell'utenza \"}")
                            .build();
                }

            } catch (Exception e) {
                LOGGER.error("Errore interno durante l'eliminazione dell'utenza ", e.getMessage());
                return Response.serverError()
                        .entity("{\"error\":\"Errore interno durante l'eliminazione dell'utenza\"}")
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }

}
