/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SkillProof.Controllers;

import SkillProof.Services.StatisticheService;
import Entity.Digicomp;
import Entity.InfoTrack;
import Entity.Questionario;
import Entity.Utente;
import Services.Filter.Secured;
import Utils.JPAUtil;
import Utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

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
    public Response estraiExcelPerUtente(@FormParam("userId") Long userId,
            @FormParam("selectedUserId") Long selectedUserId,
            @HeaderParam("Authorization") String authorizationHeader) {
        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        byte[] excelData = null;

        if ((utente.getRuolo().getId() == 2 && utente.getId().equals(selectedUserId)) || utente.getRuolo().getId() == 1) {
            try {
                Utente selectedUser = jpaUtil.findUserByUserId(selectedUserId.toString());
                try {
                    Utente utente_ = jpaUtil.findUserByUserId(String.valueOf(selectedUser.getId()));
                    if (utente_ == null) {
                        LOGGER.warn("Utente non trovato per ID: " + selectedUserId);

                        InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                                "Statistiche controller - API - (/utente)",
                                404,
                                "Errore - Utente con id " + selectedUserId + " non trovato.",
                                "API chiamata dall'utente con id " + userId + ".",
                                "Errore 404 - NOT_FOUND",
                                Utils.formatLocalDateTime(LocalDateTime.now()));
                        jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\":\"Utente non trovato\"}")
                                .build();
                    }

                    Questionario ultimoQuestionario = jpaUtil.findUltimoQuestionarioCompletatoPerUtente(utente);
                    if (ultimoQuestionario == null) {
                        LOGGER.warn("Nessun questionario completato trovato per l'utente con ID: " + selectedUserId);

                        InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                                "Statistiche controller - API - (/utente)",
                                404,
                                "Errore - Nessun questionario completato trovato per l'utente con id " + selectedUserId,
                                "API chiamata dall'utente con id " + userId + ".",
                                "Errore 404 - NOT_FOUND",
                                Utils.formatLocalDateTime(LocalDateTime.now()));
                        jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\":\"Nessun questionario completato trovato per l'utente con ID: " + selectedUserId + "\"}")
                                .build();
                    }

                    if (ultimoQuestionario.getDigicomp_questionario() == null) {
                        LOGGER.warn("Nessun questionario DIGICOMP 2.2 trovato per l'utente con ID: " + selectedUserId);

                        InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                                "Statistiche controller - API - (/utente)",
                                404,
                                "Errore - Nessun questionario DIGICOMP 2.2 trovato per l'utente con id " + selectedUserId,
                                "API chiamata dall'utente con id " + userId + ".",
                                "Errore 404 - NOT_FOUND",
                                Utils.formatLocalDateTime(LocalDateTime.now()));
                        jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\":\"Nessun questionario DIGICOMP 2.2 trovato per l'utente con ID: " + selectedUserId + "\"}")
                                .build();
                    }

                    excelData = statisticheService.createExcel(ultimoQuestionario);

                } catch (IllegalArgumentException e) {
                    LOGGER.error("Errore durante l'estrazione dell'Excel per l'utente " + selectedUserId + ":\n" + e.getMessage());
                }

                InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                        "Statistiche controller - API - (/utente)",
                        200,
                        "Excel dell'utenza con id " + selectedUser.getId() + " generato.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.ok(excelData)
                        .header("Content-Disposition", "attachment; filename=\"statistiche_utente_"
                                + Utils.sanitize(selectedUser.getNome().toUpperCase()) + "_"
                                + Utils.sanitize(selectedUser.getCognome().toUpperCase()) + ".xlsx\"")
                        .build();

            } catch (Exception e) {
                LOGGER.error("Errore nell'estrazione dell'Excel per l'utente " + selectedUserId, e);

                InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                        "Statistiche controller - API - (/utente)",
                        500,
                        "Errore - Excel dell'utenza con id " + selectedUserId + " non generato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Errore durante l'estrazione dell'Excel.\"}")
                        .build();
            }
        } else {
            InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                    "Statistiche controller - API - (/utente)",
                    401,
                    "Ruolo non autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Ruolo non autorizzato.\"}")
                    .build();
        }
    }

    @POST
    @Path("/digicomp/controlla")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response controllaDigicomp(@FormParam("userId") Long userId, @HeaderParam("Authorization") String authorizationHeader) {
        Utente utente_sessione = jpaUtil.findUserByUserId(userId.toString());

        if (utente_sessione.getRuolo().getId() == 1) {
            try {
                List<String> messaggi = new ArrayList<>();
                ObjectMapper objectMapper = new ObjectMapper();
                List<Utente> utenti = jpaUtil.findAllUtenti();

                for (Utente utente : utenti) {
                    Questionario ultimoQuestionario = jpaUtil.findUltimoQuestionarioCompletatoPerUtente(utente);

                    JsonObject jsonRisposta = new JsonObject();
                    jsonRisposta.addProperty("id_utente", utente.getId());

                    if (ultimoQuestionario == null) {
                        LOGGER.warn("Nessun questionario completato trovato per l'utente con ID: " + utente.getId());
                        messaggi.add("Utente con ID " + utente.getId() + ": Nessun questionario completato trovato.");
                        continue;
                    }

                    if (ultimoQuestionario.getDigicomp_questionario() == null || ultimoQuestionario.getDigicomp_questionario().isEmpty()) {
                        LOGGER.warn("Nessun questionario DIGICOMP 2.2 trovato per l'utente con ID: " + utente.getId());
                        messaggi.add("Utente con ID " + utente.getId() + ": Nessun questionario DIGICOMP 2.2 trovato.");
                        continue;
                    }

                    Digicomp digicompAttuale = ultimoQuestionario.getDigicomp_questionario().get(0);
                    int livelloCorrente = Utils.tryParseInt(digicompAttuale.getId().toString());

                    if (livelloCorrente >= 5) {
                        LOGGER.info("L'utente con ID " + utente.getId() + " ha già completato il livello massimo.");
                        jpaUtil.resettaDisponibilitàUtente(utente.getId(), LOGGER);
                        messaggi.add("Utente con ID " + utente.getId() + ": Ha già completato il livello massimo.");
                        continue;
                    }

                    String jsonRisposte = ultimoQuestionario.getRisposte();
                    if (jsonRisposte == null || jsonRisposte.isEmpty()) {
                        LOGGER.warn("Nessuna risposta trovata per l'utente " + utente.getId() + ".");
                        messaggi.add("Utente con ID " + utente.getId() + ": Nessuna risposta trovata.");
                        continue;
                    }

                    JsonNode rootNode = objectMapper.readTree(jsonRisposte);
                    JsonNode risposteNode = rootNode.path("risposte");

                    if (risposteNode.isMissingNode()) {
                        LOGGER.warn("Formato JSON non valido per l'utente " + utente.getId() + ".");
                        messaggi.add("Utente con ID " + utente.getId() + ": Formato JSON non valido.");
                        continue;
                    }

                    Map<Long, Integer> risposteCorrettePerCategoria = new HashMap<>();
                    List<String> domandeSbagliate = new ArrayList<>();
                    int risposteCorretteTotali = 0;

                    for (Iterator<Map.Entry<String, JsonNode>> it = risposteNode.fields(); it.hasNext();) {
                        Map.Entry<String, JsonNode> entry = it.next();
                        JsonNode rispostaUtente = entry.getValue();
                        Long domandaId = Utils.tryParseLong(entry.getKey());

                        // DOMANDE MANUALI
                        if (rispostaUtente.has("risposta") && rispostaUtente.has("risposta corretta")) {
                            String rispostaData = rispostaUtente.path("risposta").asText();
                            String rispostaCorretta = rispostaUtente.path("risposta corretta").asText();

                            if (rispostaData.equalsIgnoreCase(rispostaCorretta)) {
                                Long categoriaId = jpaUtil.getCategoriaIdByDomandaId(domandaId);
                                risposteCorrettePerCategoria.put(categoriaId, risposteCorrettePerCategoria.getOrDefault(categoriaId, 0) + 1);
                                risposteCorretteTotali++;
                            } else {
                                domandeSbagliate.add("Domanda ID: " + domandaId + " - Risposta sbagliata.");
                            }
                        } // DOMANDE AUTOMATICHE
                        else if (rispostaUtente.has("risposta_id") && rispostaUtente.has("risposte_corrette")) {
                            Set<String> risposteDate = new HashSet<>();
                            for (JsonNode id : rispostaUtente.withArray("risposta_id")) {
                                risposteDate.add(id.asText());
                            }

                            Set<String> risposteCorrette = new HashSet<>();
                            for (JsonNode id : rispostaUtente.withArray("risposte_corrette")) {
                                risposteCorrette.add(id.asText());
                            }

                            if (risposteDate.equals(risposteCorrette)) {
                                Long categoriaId = jpaUtil.getCategoriaIdByDomandaId(domandaId);
                                risposteCorrettePerCategoria.put(categoriaId, risposteCorrettePerCategoria.getOrDefault(categoriaId, 0) + 1);
                                risposteCorretteTotali++;
                            } else {
                                domandeSbagliate.add("Domanda ID: " + domandaId + " - Risposte multiple sbagliate.");
                            }
                        } else {
                            domandeSbagliate.add("Domanda ID: " + domandaId + " - Formato risposta non riconosciuto.");
                        }
                    }

                    LOGGER.info("Totale risposte corrette per l'utente " + utente.getId() + ": " + risposteCorretteTotali + " su " + risposteNode.size() + " risposte.");
                    if (!domandeSbagliate.isEmpty()) {
                        LOGGER.info("Domande sbagliate per l'utente " + utente.getId() + ": " + domandeSbagliate);
                    }

                    Map<Integer, Integer> sogliaMinima = Map.of(
                            1, 2,
                            2, 4,
                            3, 3,
                            4, 3,
                            5, 3
                    );

                    boolean avanzare = true;
                    for (Map.Entry<Integer, Integer> entry : sogliaMinima.entrySet()) {
                        int categoriaId = entry.getKey();
                        int minimoRichiesto = entry.getValue();
                        int corrette = risposteCorrettePerCategoria.getOrDefault((long) categoriaId, 0);

                        if (corrette < minimoRichiesto) {
                            avanzare = false;
                            domandeSbagliate.add("Categoria ID: " + categoriaId + " - Numero di risposte corrette insufficienti.");
                            break;
                        }
                    }

                    if (avanzare) {
                        jpaUtil.assegnaNuovoQuestionario(ultimoQuestionario, livelloCorrente);
                        LOGGER.info("L'utente con ID " + utente.getId() + " ha completato il questionario con successo.");
                        messaggi.add("Utente con ID " + utente.getId() + ": Questionario completato con successo.");
                    } else {
                        jpaUtil.resettaDisponibilitàUtente(utente.getId(), LOGGER);
                        LOGGER.info("Il questionario con ID " + ultimoQuestionario.getId() + " per l'utente " + utente.getId() + " non ha superato il livello.");
                        for (String errore : domandeSbagliate) {
                            LOGGER.info(errore);
                        }
                    }
                }

                InfoTrack infoTrack = new InfoTrack("READ,CREATE",
                        "Statistiche controller - API - (/digicomp/controlla)",
                        200,
                        "Controllo effettuato con successo.",
                        "API chiamata dall'utente con id " + utente_sessione.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("status", "Controllo completato con successo");
                responseMap.put("details", messaggi);

                String jsonResponse = objectMapper.writeValueAsString(responseMap);

                return Response.ok(jsonResponse)
                        .type(MediaType.APPLICATION_JSON)
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
            InfoTrack infoTrack = new InfoTrack(
                    "READ,CREATE",
                    "Statistiche controller - API - (/digicomp/controlla)",
                    401,
                    "Ruolo non autorizzato.",
                    "API chiamata dall'utente con id " + userId + ".",
                    "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                    Utils.formatLocalDateTime(LocalDateTime.now())
            );
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Ruolo non autorizzato.\"}")
                    .build();
        }
    }

}
