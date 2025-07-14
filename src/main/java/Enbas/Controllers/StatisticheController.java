/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Enbas.Controllers;

import Enbas.Services.StatisticheService;
import Entity.InfoTrack;
import Entity.Utente;
import Services.Filter.Secured;
import Utils.JPAUtil;
import Utils.Utils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
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
@Path("/statistiche")
public class StatisticheController {

    private final Logger LOGGER = LoggerFactory.getLogger(StatisticheController.class);
    private final StatisticheService statisticheService = new StatisticheService();
    JPAUtil jpaUtil = new JPAUtil();

    @POST
    @Path("/utente")
    @Secured
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response estraiExcelPerUtente(@FormParam("userId") Long userId, @FormParam("selectedUserId") Long selectedUserId, @HeaderParam("Authorization") String authorizationHeader) {

        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 2 && utente.getId().equals(selectedUserId) || utente.getRuolo().getId() == 1) {
            try {
                byte[] excelData = statisticheService.estraiExcelPerUtente(selectedUserId, LOGGER);
                Utente selectedUser = jpaUtil.findUserByUserId(selectedUserId.toString());

                InfoTrack infoTrack = new InfoTrack("READ",
                        "Statistiche controller - API - (/utente)",
                        200,
                        "Excel dell'utenza con id " + selectedUser.getId() + " generato.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.ok(excelData)
                        .header("Content-Disposition", "attachment; filename=\"statistiche_utente_" + Utils.sanitize(selectedUser.getNome().toUpperCase()) + "_" + Utils.sanitize(selectedUser.getCognome().toUpperCase()) + ".xlsx\"")
                        .build();
            } catch (Exception e) {
                LOGGER.error("Errore nell'estrazione dell'Excel per l'utente " + selectedUserId, e);
                InfoTrack infoTrack = new InfoTrack("READ",
                        "Statistiche controller - API - (/utente)",
                        500,
                        "Errore - Excel dell'utenza con id " + selectedUserId + " non generato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"\"Errore durante l'estrazione dell'Excel.")
                        .build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("READ",
                    "Statistiche controller - API - (/utente)",
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
    @Path("/digicomp/controlla")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response controllaDigicomp(@FormParam("userId") Long userId, @HeaderParam("Authorization") String authorizationHeader
    ) {

        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {
            try {
                statisticheService.controllaDigicompPerUtenti(LOGGER);

                InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                        "Statistiche controller - API - (/digicomp/controlla)",
                        200,
                        "Controllo effettuato con successo.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.status(Response.Status.OK)
                        .entity("{\"status\": \"Controllo completato con successo\"}")
                        .build();
            } catch (Exception e) {
                LOGGER.error("Errore durante il controllo dei questionari Digicomp.", e);
                InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                        "Statistiche controller - API - (/digicomp/controlla)",
                        500,
                        "Errore - Controllo non effettuato con successo.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Errore durante il controllo dei questionari Digicomp.\"}")
                        .build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                    "Statistiche controller - API - (/digicomp/controlla)",
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
