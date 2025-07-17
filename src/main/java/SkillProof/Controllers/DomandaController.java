/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SkillProof.Controllers;

import Entity.Categoria;
import Entity.Competenza;
import Utils.JPAUtil;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import Entity.Domanda;
import Entity.InfoTrack;
import Entity.Utente;
import Enum.Tipo_inserimento;
import Services.Filter.Secured;
import Utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Salvatore
 */
@Path("/domanda")
public class DomandaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomandaController.class.getName());
    JPAUtil jpaUtil = new JPAUtil();

    @POST
    @Path("/findById")
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@FormParam("userId") Long userId, @FormParam("domanda_id") Long id, @HeaderParam("Authorization") String authorizationHeader) {
        try {
            Utente utente = jpaUtil.findUserByUserId(userId.toString());
            if (utente.getRuolo().getId() == 1) {
                Domanda domanda = jpaUtil.findDomandaById(id);
                if (domanda == null) {
                    InfoTrack infoTrack = new InfoTrack("READ",
                            "Domanda controller - API - (/findById)",
                            404,
                            "Errore - Domanda con id " + id + "non trovata.",
                            "API chiamata dall'utente con id " + userId + ".",
                            "Errore 404 - NOT_FOUND",
                            Utils.formatLocalDateTime(LocalDateTime.now()));
                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\":\"Domanda non trovata\"}")
                            .build();
                }
                JsonObject json = new JsonObject();
                json.addProperty("id", domanda.getId());
                if (domanda.getTitolo() != null) {
                    json.addProperty("titolo", domanda.getTitolo());
                }
                if (domanda.getNome_domanda() != null) {
                    json.addProperty("nome", domanda.getNome_domanda());
                }

                if (domanda.getTipo_inserimento() != null) {
                    json.addProperty("tipo_inserimento", domanda.getTipo_inserimento().name());
                }
                if (domanda.getDescrizione() != null) {
                    json.addProperty("descrizione", domanda.getDescrizione());
                }
                if (domanda.getVisibilità_domanda() != null) {
                    json.addProperty("descrizione", domanda.getVisibilità_domanda().toString().toLowerCase());
                }
                if (domanda.getOpzioni() == null && domanda.getTipo_inserimento().equals(Tipo_inserimento.AUTOMATICO)) {
                    String risposteJson = domanda.getRisposte();

                    JSONObject jsonRisposte = new JSONObject(risposteJson);

                    JSONArray risposteArray = jsonRisposte.getJSONArray("risposte");

                    JsonArray risposteEstratte = new JsonArray();

                    for (int i = 0; i < risposteArray.length(); i++) {
                        JSONObject risposta = risposteArray.getJSONObject(i);

                        JsonObject rispostaEstratta = new JsonObject();
                        rispostaEstratta.addProperty("id", risposta.getInt("id"));
                        rispostaEstratta.addProperty("corretta", risposta.getBoolean("corretta"));
                        rispostaEstratta.addProperty("testo", risposta.getString("testo"));

                        risposteEstratte.add(rispostaEstratta);
                    }

                    json.add("opzioni", risposteEstratte);
                } else {
                    json.addProperty("opzioni", domanda.getOpzioni());
                }
                if (domanda.getCategoria() != null && domanda.getCategoria().getNome() != null) {
                    json.addProperty("area", domanda.getCategoria().getNome());
                }
                if (domanda.getCompetenza() != null
                        && domanda.getCompetenza().getAreeCompetenze() != null
                        && domanda.getCompetenza().getAreeCompetenze().getNome() != null) {
                    json.addProperty("competenza", "area competenza " + domanda.getCompetenza().getAreeCompetenze().getNome() + " - "
                            + "descrizione competenza " + domanda.getCompetenza().getDescrizione()
                            + "\n - livello - "
                            + domanda.getCompetenza().getLivello());
                }

                InfoTrack infoTrack = new InfoTrack("READ",
                        "Domanda controller - API - (/findById)",
                        200,
                        "Domanda con id " + domanda.getId() + " trovata.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.ok(json.toString()).build();
            } else {
                InfoTrack infoTrack = new InfoTrack("READ",
                        "Domanda controller - API - (/findById)",
                        401,
                        "Ruolo con autorizzato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
            }

        } catch (Exception e) {
            LOGGER.error("Errore nella ricerca della domanda con id " + id, e);
            InfoTrack infoTrack = new InfoTrack("READ",
                    "Domanda controller - API - (/findById)",
                    500,
                    "Errore - Domanda con id " + id + " non trovata.",
                    "API chiamata dall'utente con id " + userId + ".",
                    Utils.estraiEccezione(e),
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

            return Response.serverError().entity("{\"error\": \"Errore interno\"}").build();
        }
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Secured
    public Response createDomanda(
            @FormParam("userId") Long userId,
            @FormParam("area") String area_id_param,
            @FormParam("abilità_competenza") String abilità_competenza_param,
            @FormParam("stato") String stato,
            @FormParam("titolo") String titolo,
            @FormParam("nome_domanda") String nome_domanda,
            @FormParam("risposta_text") List<String> risposta_text,
            @FormParam("corretta") List<String> si_no_select,
            @HeaderParam("Authorization") String authorizationHeader
    ) {

        try {
            Utente utente = jpaUtil.findUserByUserId(userId.toString());
            if (utente.getRuolo().getId() == 1) {
                Long areaId = Utils.tryParseLong(area_id_param);
                Categoria categoria = jpaUtil.findCategoriaById(areaId);

                Long abilità_competenza_id = Utils.tryParseLong(abilità_competenza_param);
                Competenza competenza = jpaUtil.findCompetenzaById(abilità_competenza_id);

                jpaUtil.creaDomanda(categoria, competenza, stato, titolo, nome_domanda,
                        risposta_text.toArray(new String[0]),
                        si_no_select.toArray(new String[0]),
                        LOGGER);

                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Domanda controller - API - (/create)",
                        200,
                        "Nuova domanda creata con successo.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.ok().entity("{\"status\":\"domanda creata con successo.\"}").build();
            } else {
                InfoTrack infoTrack = new InfoTrack("CREATE",
                        "Domanda controller - API - (/create)",
                        401,
                        "Ruolo con autorizzato.",
                        "API chiamata dall'utente con id " + userId + ".",
                        "L'utente che ha effettuato la chiamata non dispone dell'autorizzazione necessaria per effettuarla.",
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
            }
        } catch (Exception e) {
            LOGGER.error("Errore nella creazione della domanda " + e);
            InfoTrack infoTrack = new InfoTrack("CREATE",
                    "Domanda controller - API - (/create)",
                    500,
                    "Errore - Nuova domanda " + "non creata.",
                    "API chiamata dall'utente con id " + userId + ".",
                    Utils.estraiEccezione(e),
                    Utils.formatLocalDateTime(LocalDateTime.now()));
            jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

            return Response.serverError().entity("{\"error\": \"Errore interno\"}").build();
        }
    }

    @PATCH
    @Path("/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response updateDomanda(
            @FormParam("userId") Long userId,
            @FormParam("domanda_id") String domanda_id_param,
            @FormParam("area") String area_id_param,
            @FormParam("abilità_competenza") String competenza_param,
            @FormParam("stato") String stato,
            @FormParam("titolo") String titolo,
            @FormParam("nome_domanda") String nome_domanda,
            @FormParam("risposta_text") List<String> risposta_text,
            @FormParam("corretta") List<String> si_no_select,
            @FormParam("id_risposta") List<String> idRisposte,
            @HeaderParam("Authorization") String authorizationHeader
    ) {

        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {

            try {
                Long domanda_id = Utils.tryParseLong(domanda_id_param);
                Long areaId = Utils.tryParseLong(area_id_param);
                Long competenza_id = Utils.tryParseLong(competenza_param);

                Categoria categoria = jpaUtil.findCategoriaById(areaId);
                Competenza competenza = jpaUtil.findCompetenzaById(competenza_id);

                jpaUtil.modificaDomanda(
                        domanda_id,
                        categoria,
                        competenza,
                        stato,
                        titolo,
                        nome_domanda,
                        risposta_text.toArray(new String[0]),
                        idRisposte.toArray(new String[0]),
                        si_no_select.toArray(new String[0]),
                        LOGGER
                );

                InfoTrack infoTrack = new InfoTrack("UPDATE",
                        "Domanda controller - API - (/update)",
                        200,
                        "Domanda con id " + domanda_id + " aggiornata con successo.",
                        "API chiamata dall'utente con id " + utente.getId() + ".",
                        null,
                        Utils.formatLocalDateTime(LocalDateTime.now()));

                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.ok("{\"status\":\"domanda aggiornata con successo.\"}").build();

            } catch (Exception e) {
                LOGGER.error("Errore durante l'aggiornamento della domanda", e);

                InfoTrack infoTrack = new InfoTrack("UPDATE",
                        "Domanda controller - API - (/update)",
                        500,
                        "Errore - Domanda con id " + domanda_id_param + "non aggiornata.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Errore interno\"}")
                        .build();
            }

        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }

    @DELETE
    @Path("/delete")
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@FormParam("userId") Long userId, @FormParam("domanda_id") Long domanda_id, @HeaderParam("Authorization") String authorizationHeader
    ) {
        Utente utente = jpaUtil.findUserByUserId(userId.toString());
        if (utente.getRuolo().getId() == 1) {
            try {

                Domanda domanda = jpaUtil.findDomandaById(domanda_id);
                if (domanda == null) {
                    InfoTrack infoTrack = new InfoTrack("DELETE",
                            "Domanda controller - API - (/delete)",
                            404,
                            "Errore - Domanda con id " + domanda_id + "non trovata.",
                            "API chiamata dall'utente con id " + userId + ".",
                            "Errore 404 - NOT_FOUND",
                            Utils.formatLocalDateTime(LocalDateTime.now()));
                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\":\"Domanda non trovata\"}")
                            .build();
                }

                boolean deleted = jpaUtil.deleteDomandaById(domanda_id);
                if (deleted) {
                    InfoTrack infoTrack = new InfoTrack("DELETE",
                            "Domanda controller - API - (/delete)",
                            200,
                            "Domanda con id " + domanda_id + " eliminata con successo.",
                            "API chiamata dall'utente con id " + utente.getId() + ".",
                            null,
                            Utils.formatLocalDateTime(LocalDateTime.now()));

                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);
                    return Response.ok("{\"status\":\"Domanda eliminata con successo\"}").build();
                } else {
                    InfoTrack infoTrack = new InfoTrack("DELETE",
                            "Domanda controller - API - (/delete)",
                            500,
                            "Errore - Domanda con id " + domanda_id + "non eliminata.",
                            "API chiamata dall'utente con id " + userId + ".",
                            "Errore durante l'eliminazione della domanda",
                            Utils.formatLocalDateTime(LocalDateTime.now()));
                    jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                    return Response.serverError()
                            .entity("{\"error\":\"Errore durante l'eliminazione della domanda\"}")
                            .build();
                }

            } catch (Exception e) {
                LOGGER.error("Errore interno durante l'eliminazione della domanda", e.getMessage());
                InfoTrack infoTrack = new InfoTrack("DELETE",
                        "Domanda controller - API - (/delete)",
                        500,
                        "Errore - Domanda con id " + domanda_id + "non eliminata.",
                        "API chiamata dall'utente con id " + userId + ".",
                        Utils.estraiEccezione(e),
                        Utils.formatLocalDateTime(LocalDateTime.now()));
                jpaUtil.SalvaInfoTrack(infoTrack, LOGGER);

                return Response.serverError()
                        .entity("{\"error\":\"Errore interno durante l'eliminazione della domanda\"}")
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Ruolo non autorizzato.\"}").build();
        }
    }

}
