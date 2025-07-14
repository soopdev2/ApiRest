/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Enbas.Controllers;

import Entity.Utente;
import Enbas.Services.QuestionarioService;
import Entity.InfoTrack;
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
import java.time.LocalDateTime;
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
    JPAUtil jpaUtil = new JPAUtil();

    @POST
    @Path("/assegna")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response assegnaQuestionario(
            @FormParam("userId") Long userId,
            @FormParam("id_utenti") List<String> assegnatiUtenti,
            @HeaderParam("Authorization") String authorizationHeader) {

        Utente utente_ = jpaUtil.findUserByUserId(userId.toString());
        if (utente_.getRuolo().getId() == 1) {
            try {
                for (String utente_selezionato_id : assegnatiUtenti) {
                    Utente utente_selezionato = jpaUtil.findUserByUserId(utente_selezionato_id);
                    if (utente_selezionato.getRuolo().getId() == 1) {
                        InfoTrack infoTrack = new InfoTrack("CREATE",
                                "Questionario controller - API - (/assegna)",
                                400,
                                "Errore - assegnazione questionario non effettuata.",
                                "API chiamata dall'utente con id " + userId + ".",
                                "Errore 400 - BAD_REQUEST - L'utente ha provate ad assegnare un questionario ad un admin.",
                                Utils.formatLocalDateTime(LocalDateTime.now()));
                        jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\": \"Non puoi assegnare un questionario ad un admin.\"}").build();
                    }
                    Questionario questionario = jpaUtil.findUtenteQuestionarioIdByUserId(utente_selezionato.getId());
                    if (!(questionario != null && questionario.getDescrizione().equals(Stato_questionario.COMPLETATO)
                            && questionario.getStatus() == 3 || questionario == null)) {
                        InfoTrack infoTrack = new InfoTrack("CREATE",
                                "Questionario controller - API - (/assegna)",
                                400,
                                "Errore - assegnazione questionario non effettuata.",
                                "API chiamata dall'utente con id " + userId + ".",
                                "Errore 400 - BAD_REQUEST - Uno o più utenti selezionati hanno un questionario non ancora completato.",
                                Utils.formatLocalDateTime(LocalDateTime.now()));
                        jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                        return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Uno o più utenti selezionati hanno un questionario non ancora completato.\"}").build();
                    }
                }
                questionarioService.assegnaQuestionarioDigicomp(assegnatiUtenti.toArray(String[]::new), LOGGER);
                LOGGER.info("Questionario assegnato con successo.");
                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Questionario controller - API - (/assegna)",
                        200,
                        "Questionario assegnato con successo per il/i seguente/i utente/i con id : " + assegnatiUtenti.toArray(String[]::new),
                        "API chiamata dall'utente con id " + utente_.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.ok("{\"status\":\"Questionario assegnato con successo.\"}").build();

            } catch (Exception e) {
                LOGGER.error("Errore durante l'assegnazione del questionario: " + e.getMessage());
                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Questionario controller - API - (/assegna)",
                        500,
                        "Errore - Questionario/i non assegnato/i. ",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Errore durante l'assegnazione\"}")
                        .build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("CREATE",
                    "Questionario controller - API - (/assegna)",
                    401,
                    "Ruolo con autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
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
        Utente utente_ = jpaUtil.findUserByUserId(userId.toString());
        if (utente_.getRuolo().getId() == 2) {
            if (utente_.getId().equals(selectedUserId)) {
                try {
                    questionarioService.SalvaStatoQuestionarioPresoInCarico(selectedUserId, LOGGER);
                    InfoTrack infoTrack = new InfoTrack("CREATE",
                            "Questionario controller - API - (/inizia)",
                            200,
                            "Questionario iniziato con successo per il seguente utente con id : " + selectedUserId,
                            "API chiamata dall'utente con id " + utente_.getId() + ".",
                            null,
                            Utils.formatLocalDateTime(LocalDateTime.now()));

                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    return Response.ok("{\"status\":\"Questionario iniziato con successo.\"}").build();
                } catch (IOException e) {
                    LOGGER.error("Errore durante l'inizio del questionario: " + e.getMessage());
                    InfoTrack infoTrack = new InfoTrack("CREATE",
                            "Questionario controller - API - (/inizia)",
                            500,
                            "Errore - Questionario non iniziato dall'utente con id " + selectedUserId + ".",
                            "API chiamata dall'utente con id " + userId + ".",
                            Utils.estraiEccezione(e),
                            Utils.formatLocalDateTime(LocalDateTime.now()));

                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\":\"Errore durante l'inizio del questionario\"}")
                            .build();
                }
            } else {
                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Questionario controller - API - (/inizia)",
                        401,
                        "Ruolo con autorizzato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per iniziare il questionario di un altro utente.",
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Non puoi iniziare il questionario di un altro utente\"}").build();

            }
        } else {
            InfoTrack infoTrack = new InfoTrack("CREATE",
                    "Questionario controller - API - (/inizia)",
                    401,
                    "Ruolo con autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
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

        Utente utente_ = jpaUtil.findUserByUserId(userId.toString());
        if (utente_.getRuolo().getId() == 2) {
            if (utente_.getId().equals(selectedUserId)) {
                try {
                    questionarioService.salvaQuestionario(selectedUserId, questionarioId, jsonInput, LOGGER);
                    InfoTrack infoTrack = new InfoTrack("CREATE",
                            "Questionario controller - API - (/salvaQuestionario)",
                            200,
                            "Questionario con id " + questionarioId + " salvato con successo per il seguente utente con id : " + selectedUserId,
                            "API chiamata dall'utente con id " + utente_.getId() + ".",
                            null,
                            Utils.formatLocalDateTime(LocalDateTime.now()));

                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    LOGGER.info("Questionario salvato con successo.");
                    return Response.ok("{\"status\":\"Questionario salvato con successo.\"}").build();
                } catch (IOException e) {
                    LOGGER.error("Errore durante il salvataggio del questionario: " + e.getMessage());
                    InfoTrack infoTrack = new InfoTrack("CREATE",
                            "Questionario controller - API - (/salvaQuestionario)",
                            500,
                            "Errore - Questionario con id " + questionarioId + " non salvato dall'utente con id " + selectedUserId + ".",
                            "API chiamata dall'utente con id " + userId + ".",
                            Utils.estraiEccezione(e),
                            Utils.formatLocalDateTime(LocalDateTime.now()));

                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\":\"Errore durante il salvataggio del questionario\"}")
                            .build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Non puoi salvare il questionario di un altro utente\"}").build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("CREATE",
                    "Questionario controller - API - (/salvaQuestionario)",
                    401,
                    "Ruolo con autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
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
                            InfoTrack infoTrack = new InfoTrack("CREATE",
                                    "Questionario controller - API - (/salvaProgressi)",
                                    200,
                                    "Progressi questionario salvati con successo per il seguente utente con id : " + selectedUserId,
                                    "API chiamata dall'utente con id " + userId + ".",
                                    null,
                                    Utils.formatLocalDateTime(LocalDateTime.now()));

                            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                            return Response.ok("{\"status\":\"Progressi del questionario salvati con successo.\"}").build();

                        } catch (Exception e) {
                            if (em.getTransaction().isActive()) {
                                em.getTransaction().rollback();
                            }
                            LOGGER.error("Errore durante il salvataggio: " + Utils.estraiEccezione(e));

                            InfoTrack infoTrack = new InfoTrack("CREATE",
                                    "Questionario controller - API - (/salvaProgressi)",
                                    500,
                                    "Errore - Progressi questionario non salvati dall'utente con id " + selectedUserId + ".",
                                    "API chiamata dall'utente con id " + userId + ".",
                                    Utils.estraiEccezione(e),
                                    Utils.formatLocalDateTime(LocalDateTime.now()));

                            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                    .entity("{\"error\":\"Errore durante il salvataggio dei progressi.\"}")
                                    .build();
                        } finally {
                            if (em.isOpen()) {
                                em.close();
                            }
                        }
                    } else {
                        InfoTrack infoTrack = new InfoTrack("CREATE",
                                "Questionario controller - API - (/salvaProgressi)",
                                400,
                                "Errore - Progressi questionario non salvati dall'utente con id " + selectedUserId + ".",
                                "API chiamata dall'utente con id " + userId + ".",
                                "Error - 400 - BAD_REQUEST. userId mancante o non valido.",
                                Utils.formatLocalDateTime(LocalDateTime.now()));

                        jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\":\"userId mancante o non valido.\"}")
                                .build();
                    }

                } catch (IOException e) {
                    InfoTrack infoTrack = new InfoTrack("CREATE",
                            "Questionario controller - API - (/salvaProgressi)",
                            400,
                            "Errore - Progressi questionario non salvati dall'utente con id " + selectedUserId + ".",
                            "API chiamata dall'utente con id " + userId + ".",
                            "Error - 400 - BAD_REQUEST. Formato json non valido.",
                            Utils.formatLocalDateTime(LocalDateTime.now()));

                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    LOGGER.error("Errore parsing JSON: " + e.getMessage());
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\":\"Formato JSON non valido.\"}")
                            .build();
                }
            } else {
                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Questionario controller - API - (/salvaProgressi)",
                        401,
                        "Ruolo con autorizzato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per salvare i progressi di un questionario per un altro utente.",
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Non puoi salvare i progressi di un questionario per un altro utente.\"}")
                        .build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("CREATE",
                    "Questionario controller - API - (/salvaProgressi)",
                    401,
                    "Ruolo con autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
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
        Utente utente_ = jpaUtil.findUserByUserId(userId.toString());
        if (utente_.getRuolo().getId() == 2 && utente_.getId().equals(selectedUserId) || utente_.getRuolo().getId() == 1) {
            try {
                byte[] pdfBytes = questionarioService.generaPdfQuestionario(selectedUserId, id_questionario, LOGGER);
                Utente selectedUser = jpaUtil.findUserByUserId(selectedUserId.toString());
                InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                        "Questionario controller - API - (/visualizzaPdf)",
                        200,
                        "Il questionario con id " + id_questionario + " per il seguente utente con id : " + selectedUserId + " in formato PDF è stato generato con successo.",
                        "API chiamata dall'utente con id " + userId + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.ok(pdfBytes, MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", "attachment; filename=\"questionario_" + Utils.sanitize(selectedUser.getNome().toUpperCase()) + "_" + Utils.sanitize(selectedUser.getCognome().toUpperCase()) + ".pdf\"")
                        .build();
            } catch (IllegalArgumentException e) {
                LOGGER.error("Questionario non trovato: " + e.getMessage());
                InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                        "Questionario controller - API - (/visualizzaPdf)",
                        404,
                        "Errore - il questionario con id " + id_questionario + " dell'utente " + selectedUserId + " non è stato generato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        "Error - 404 - NOT_FOUND. Questionario non trovato ",
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
            } catch (Exception e) {
                LOGGER.error("Errore durante la generazione del PDF:: " + e.getMessage());
                InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                        "Questionario controller - API - (/visualizzaPdf)",
                        500,
                        "Errore - Il questionario con id " + id_questionario + " dell'utente " + selectedUserId + " non è stato generato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Errore durante la generazione del PDF: " + e.getMessage()).build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                    "Questionario controller - API - (/visualizzaPdf)",
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
