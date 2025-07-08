/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Enbas.Controllers;

import Entity.Utente;
import Enbas.Services.QuestionarioService;
import Entity.Questionario;
import Enum.Stato_questionario;
import Services.Filter.Secured;
import Utils.JPAUtil;
import Utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Salvatore
 */
@Path("/questionario")
public class QuestionarioController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomandaController.class.getName());
    QuestionarioService questionarioService = new QuestionarioService();

    @POST
    @Path("/assegna")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response assegnaQuestionario(
            @FormParam("userId") Long userId,
            @FormParam("id_utenti") List<String> assegnatiUtenti,
            @HeaderParam("Authorization") String authorizationHeader) {

        JPAUtil jpaUtil = new JPAUtil();
        Utente utente_ = jpaUtil.findUserByUserId(userId.toString());
        if (utente_.getRuolo().getId() == 1) {
            try {
                for (String utente_selezionato_id : assegnatiUtenti) {
                    Utente utente_selezionato = jpaUtil.findUserByUserId(utente_selezionato_id);
                    if (utente_selezionato.getRuolo().getId() == 1) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\": \"Non puoi assegnare un questionario ad un admin.\"}").build();
                    }
                    Questionario questionario = jpaUtil.findUtenteQuestionarioIdByUserId(utente_selezionato.getId());
                    if (!(questionario != null && questionario.getDescrizione().equals(Stato_questionario.COMPLETATO)
                            && questionario.getStatus() == 3 || questionario == null)) {
                        return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Uno o più utenti selezionati hanno un questionario non ancora completato\"}").build();
                    }
                }
                questionarioService.assegnaQuestionarioDigicomp(assegnatiUtenti.toArray(String[]::new), LOGGER);
                LOGGER.info("Questionario assegnato con successo.");
                return Response.ok("{\"status\":\"Questionario assegnato con successo.\"}").build();

            } catch (Exception e) {
                LOGGER.error("Errore durante l'assegnazione del questionario: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Errore durante l'assegnazione\"}")
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }

    @POST
    @Path("/inizia")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response salvaStatoQuestionarioPresoInCarico(
            @FormParam("userId") Long userId, @FormParam("selectedUserId") Long selectedUserId, @HeaderParam("Authorization") String authorizationHeader
    ) {
        JPAUtil jpaUtil = new JPAUtil();
        Utente utente_ = jpaUtil.findUserByUserId(userId.toString());
        if (utente_.getRuolo().getId() == 2) {
            if (utente_.getId().equals(selectedUserId)) {
                try {
                    questionarioService.SalvaStatoQuestionarioPresoInCarico(selectedUserId, LOGGER);
                    return Response.ok("{\"status\":\"Questionario iniziato con successo.\"}").build();
                } catch (IOException e) {
                    LOGGER.error("Errore durante l'inizio del questionario: " + e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\":\"Errore durante l'inizio del questionario\"}")
                            .build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Non puoi iniziare il questionario di un altro utente\"}").build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }

    @POST
    @Path("/salvaQuestionario")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response salvaQuestionario(
            @QueryParam("userId") Long userId,
            @QueryParam("selectedUserId") Long selectedUserId,
            @QueryParam("questionarioId") Long questionarioId,
            String jsonInput,
            @HeaderParam("Authorization") String authorizationHeader
    ) {

        JPAUtil jpaUtil = new JPAUtil();
        Utente utente_ = jpaUtil.findUserByUserId(userId.toString());
        if (utente_.getRuolo().getId() == 2) {
            if (utente_.getId().equals(selectedUserId)) {
                try {
                    questionarioService.salvaQuestionario(selectedUserId, questionarioId, jsonInput, LOGGER);
                    LOGGER.info("Questionario salvato con successo.");
                    return Response.ok("{\"status\":\"Questionario salvato con successo.\"}").build();
                } catch (IOException e) {
                    LOGGER.error("Errore durante il salvataggio del questionario: " + e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\":\"Errore durante il salvataggio del questionario\"}")
                            .build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Non puoi salvare il questionario di un altro utente\"}").build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }

    @POST
    @Path("/salvaProgressi")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response salvaProgressi(
            @HeaderParam("Authorization") String authorizationHeader,
            @QueryParam("userId") Long userId,
            @QueryParam("selectedUserId") Long selectedUserId,
            String jsonInput
    ) {
        JPAUtil jpaUtil = new JPAUtil();

        Utente utente = jpaUtil.findUserByUserId(userId.toString());

        if (utente != null && utente.getRuolo().getId() == 2) {
            if (utente.getId().equals(selectedUserId)) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> formData = objectMapper.readValue(jsonInput, Map.class);

                    Long userIdParsed = Utils.tryParseLong((String) formData.get("userId"));

                    if (userIdParsed != null) {
                        Map<String, Object> progressData = new HashMap<>();
                        progressData.put("userId", userIdParsed);

                        formData.forEach((key, value) -> {
                            if (!key.equals("userId")) {
                                progressData.put(key, value);
                            }
                        });

                        progressData.put("data_salvataggio", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

                        String progressJson = objectMapper.writeValueAsString(progressData);

                        EntityManager em = jpaUtil.getEm();
                        try {
                            em.getTransaction().begin();

                            Utente u = em.find(Utente.class, userIdParsed);
                            if (u != null) {
                                Questionario questionario = jpaUtil.findUtenteQuestionarioIdByUserId(u.getId());
                                questionario.setProgressi(progressJson);
                                jpaUtil.salvaStatoQuestionario(questionario);
                                em.merge(questionario);
                            }

                            em.getTransaction().commit();
                            LOGGER.info("Progressi questionario salvati con successo dall'utente con id " + userIdParsed);
                            return Response.ok("{\"status\":\"Progressi del questionario salvati con successo.\"}").build();

                        } catch (Exception e) {
                            if (em.getTransaction().isActive()) {
                                em.getTransaction().rollback();
                            }
                            LOGGER.error("Errore durante il salvataggio: " + Utils.estraiEccezione(e));
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                    .entity("{\"error\":\"Errore durante il salvataggio dei progressi.\"}")
                                    .build();
                        } finally {
                            if (em.isOpen()) {
                                em.close();
                            }
                        }
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\":\"userId mancante o non valido.\"}")
                                .build();
                    }

                } catch (IOException e) {
                    LOGGER.error("Errore parsing JSON: " + e.getMessage());
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\":\"Formato JSON non valido.\"}")
                            .build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Non puoi salvare i progressi di un questionario per un altro utente.\"}")
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Ruolo non autorizzato.\"}")
                    .build();
        }
    }

    @POST
    @Path("/visualizzaPdf")
    @Produces("application/pdf")
    @Secured
    public Response visualizzaQuestionarioAdmin(@FormParam("userId") Long userId,
            @FormParam("selectedUserId") Long selectedUserId,
            @FormParam("id_questionario") Long id_questionario,
            @HeaderParam("Authorization") String authorizationHeader
    ) {
        JPAUtil jpaUtil = new JPAUtil();
        Utente utente_ = jpaUtil.findUserByUserId(userId.toString());
        if (utente_.getRuolo().getId() == 2 && utente_.getId().equals(selectedUserId) || utente_.getRuolo().getId() == 1) {
            try {
                byte[] pdfBytes = questionarioService.generaPdfQuestionario(selectedUserId, id_questionario, LOGGER);
                Utente selectedUser = jpaUtil.findUserByUserId(selectedUserId.toString());
                return Response.ok(pdfBytes, MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", "attachment; filename=\"questionario_" + Utils.sanitize(selectedUser.getNome().toUpperCase()) + "_" + Utils.sanitize(selectedUser.getCognome().toUpperCase()) + ".pdf\"")
                        .build();
            } catch (IllegalArgumentException e) {
                LOGGER.error("Questionario non trovato: " + e.getMessage());
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
            } catch (Exception e) {
                LOGGER.error("Errore durante la generazione del PDF:: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Errore durante la generazione del PDF: " + e.getMessage()).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }
}
