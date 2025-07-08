/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Enbas.Services;

import Entity.AreeCompetenze;
import Entity.Domanda;
import Enum.Stato_questionario;
import Enum.Tipo_inserimento;
import Enum.Visibilità_domanda;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import Entity.Categoria;
import Entity.Digicomp;
import Entity.ModelloPredefinito;
import Entity.Questionario;
import Entity.Utente;
import Utils.JPAUtil;
import Utils.Utils;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

/**
 *
 * @author Salvatore
 */
public class QuestionarioService {

    public final static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public void assegnaQuestionarioDigicomp(String[] assegna_questionario_select_utente, Logger logger) {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();

        try {
            em.getTransaction().begin();

            Long digicomp_id = Utils.tryParseLong("1");
            Digicomp digicomp = em.find(Digicomp.class, digicomp_id);

            TypedQuery<Domanda> query = em.createQuery(
                    "SELECT d FROM Domanda d WHERE d.visibilità_domanda = :visibilità AND d.competenza.livello = :livello", Domanda.class)
                    .setParameter("visibilità", Visibilità_domanda.VISIBILE)
                    .setParameter("livello", "1");

            List<Domanda> domande = query.getResultList();

            Map<Long, Map<Long, List<Domanda>>> domandePerCategoriaEArea = new HashMap<>();
            for (Domanda domanda : domande) {
                if (domanda.getCategoria() != null && domanda.getCompetenza() != null) {
                    Long categoriaId = domanda.getCategoria().getId();

                    AreeCompetenze area = domanda.getCompetenza().getAreeCompetenze();
                    Long areaId = area.getId();

                    domandePerCategoriaEArea
                            .computeIfAbsent(categoriaId, k -> new HashMap<>())
                            .computeIfAbsent(areaId, k -> new ArrayList<>())
                            .add(domanda);
                }
            }

            Map<Long, Integer> distribuzione = Map.of(
                    1L, 3,
                    2L, 6,
                    3L, 4,
                    4L, 4,
                    5L, 4
            );

            List<Domanda> domandeSelezionate = new ArrayList<>();

            for (Map.Entry<Long, Map<Long, List<Domanda>>> categoriaEntry : domandePerCategoriaEArea.entrySet()) {
                Long categoriaId = categoriaEntry.getKey();
                Map<Long, List<Domanda>> domandePerArea = categoriaEntry.getValue();

                if (distribuzione.containsKey(categoriaId)) {
                    for (Map.Entry<Long, List<Domanda>> areaEntry : domandePerArea.entrySet()) {
                        List<Domanda> domandeArea = areaEntry.getValue();

                        if (!domandeArea.isEmpty()) {
                            Collections.shuffle(domandeArea);
                            domandeSelezionate.add(domandeArea.get(0));
                        }
                    }
                }
            }

            for (String userIdStr : assegna_questionario_select_utente) {
                Long userId = Utils.tryParseLong(userIdStr);
                Utente utente = em.find(Utente.class, userId);

                if (utente != null) {
                    Questionario ultimo_questionario = jPAUtil.findUtenteQuestionarioIdByUserId(userId);
                    if (ultimo_questionario != null && ultimo_questionario.getDescrizione().equals(Stato_questionario.COMPLETATO)
                            && ultimo_questionario.getStatus() == 3 || ultimo_questionario == null) {

                        Questionario questionario = new Questionario();
                        questionario.setUtenti(List.of(utente));
                        questionario.setDigicomp_questionario(List.of(digicomp));
                        questionario.setStatus(0);
                        questionario.setDescrizione(Stato_questionario.ASSEGNATO);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date date = new Date();
                        String formattedDate = sdf.format(date);
                        questionario.setDataDiAssegnazione(formattedDate);
                        questionario.setDomande(domandeSelezionate);

                        em.persist(questionario);

                    }
                }

            }

            em.getTransaction().commit();

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Non è stato possibile assegnare un nuovo questionario di tipo Digicomp" + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public void SalvaStatoQuestionarioPresoInCarico(Long userId, Logger logger) throws IOException {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();

        try {
            em.getTransaction().begin();
            Questionario questionario = jPAUtil.findUtenteQuestionarioIdByUserId(userId);

            if (questionario.getStatus() == 0 && questionario.getDescrizione().equals(Stato_questionario.ASSEGNATO)) {
                questionario.setStatus(1);
                questionario.setDescrizione(Stato_questionario.PRESO_IN_CARICO);
                em.merge(questionario);
                em.getTransaction().commit();
                logger.info("Questionario con id " + questionario.getId() + "  è stato preso in carico con successo dall'utente con id " + userId + " in data " + sdf.format(new Date()));
            }
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Non è stato possibile cambiare lo stato (PRESO_IN CARICO)" + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public void salvaQuestionario(Long userId, Long questionarioId, String jsonInput, Logger logger) throws IOException {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();
        Utente utente = jPAUtil.findUserByUserId(String.valueOf(userId));

        try {
            em.getTransaction().begin();

            logger.info("Inizio salvataggio questionario - userId: " + userId + ", questionarioId: " + questionarioId);
            Questionario questionario = jPAUtil.findUtenteQuestionarioIdByUserId(utente.getId());

            Map<String, Object> informazioniGenerali = new HashMap<>();
            informazioniGenerali.put("nome", utente.getNome());
            informazioniGenerali.put("cognome", utente.getCognome());
            informazioniGenerali.put("età", utente.getEtà());
            informazioniGenerali.put("indirizzo", utente.getIndirizzo());
            informazioniGenerali.put("data_completamento", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            informazioniGenerali.put("data_assegnazione", questionario.getDataDiAssegnazione());

            List<Domanda> domande = questionario.getDomande();
            logger.info("Numero domande caricate: " + domande.size());

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> surveyMap = objectMapper.readValue(jsonInput, new TypeReference<Map<String, Object>>() {
            });

            Map<String, Object> risposteCompletate = new HashMap<>();

            for (Domanda domanda : domande) {
                String parametroRisposta = "risposta_" + domanda.getId();
                Map<String, Object> rispostaDetails = new LinkedHashMap<>();
                rispostaDetails.put("domanda", domanda.getTitolo());

                logger.info("Elaboro domanda ID: " + domanda.getId() + " - Tipo inserimento: " + domanda.getTipo_inserimento());

                if (domanda.getTipo_inserimento() == Tipo_inserimento.AUTOMATICO) {
                    try {
                        ObjectNode jsonDomanda = (ObjectNode) objectMapper.readTree(domanda.getRisposte());
                        String tipoDomanda = jsonDomanda.get("tipo_domanda").asText();

                        ArrayNode risposteCorretteArray = (ArrayNode) jsonDomanda.get("risposte_corrette");
                        ArrayNode risposteArray = (ArrayNode) jsonDomanda.get("risposte");

                        List<String> risposteCorrette = new ArrayList<>();
                        risposteCorretteArray.forEach(rc -> risposteCorrette.add(rc.asText()));

                        if (tipoDomanda.equalsIgnoreCase("domanda_scelta_multipla")) {
                            Object rispostaObj = surveyMap.get(parametroRisposta);
                            List<String> risposteUtente = new ArrayList<>();

                            if (rispostaObj instanceof List<?>) {
                                for (Object item : (List<?>) rispostaObj) {
                                    risposteUtente.add(item.toString());
                                }
                            } else if (rispostaObj instanceof String) {
                                risposteUtente.add((String) rispostaObj);
                            }

                            Map<String, String> testoToIdMap = new HashMap<>();
                            Map<String, String> idToTestoMap = new HashMap<>();
                            for (JsonNode r : risposteArray) {
                                String id = r.get("id").asText();
                                String testo = r.get("testo").asText().replaceAll("<[^>]*>", "").trim();
                                testoToIdMap.put(testo, id);
                                idToTestoMap.put(id, testo);
                            }

                            List<String> idRisposteUtente = new ArrayList<>();
                            for (String testoRisposta : risposteUtente) {
                                String testoPulito = testoRisposta.replaceAll("<[^>]*>", "").trim();
                                String idRisposta = testoToIdMap.get(testoPulito);
                                if (idRisposta != null) {
                                    idRisposteUtente.add(idRisposta);
                                }
                            }

                            List<String> testiRisposteCorrette = new ArrayList<>();
                            for (String idCorretto : risposteCorrette) {
                                String testo = idToTestoMap.get(idCorretto);
                                if (testo != null) {
                                    testiRisposteCorrette.add(testo);
                                }
                            }

                            rispostaDetails.put("risposta_testuale", risposteUtente);
                            rispostaDetails.put("risposta_id", idRisposteUtente);
                            rispostaDetails.put("risposte_corrette", risposteCorrette);
                            rispostaDetails.put("testi_risposte_corrette", testiRisposteCorrette);

                            risposteCompletate.put(domanda.getId().toString(), rispostaDetails);
                        }

                    } catch (JsonProcessingException e) {
                        logger.error("Errore parsing json DOMANDA_AUTOMATICA id " + domanda.getId(), e);
                    }

                } else {
                    String tipo = domanda.getTipo_domanda().toString();
                    switch (tipo) {
                        case "DOMANDA_APERTA":
                            String rispostaAperta = (String) surveyMap.get(parametroRisposta);
                            if (rispostaAperta != null && !rispostaAperta.isEmpty()) {
                                rispostaDetails.put("risposta", rispostaAperta);
                                risposteCompletate.put(domanda.getId().toString(), rispostaDetails);
                            }
                            break;
                        case "DOMANDA_SCELTA_MULTIPLA":
                            String rispostaMultipla = (String) surveyMap.get(parametroRisposta);
                            if (rispostaMultipla != null && !rispostaMultipla.isEmpty()) {
                                rispostaDetails.put("risposta", rispostaMultipla);
                                String opzioni = domanda.getOpzioni();
                                String[] opzioniArray = opzioni.split(",");
                                rispostaDetails.put("risposta corretta", opzioniArray[0]);
                                risposteCompletate.put(domanda.getId().toString(), rispostaDetails);
                            }
                            break;
                        case "DOMANDA_SCALA_VALUTAZIONE":
                            Integer scalaRisposta = (Integer) surveyMap.get(parametroRisposta);
                            if (scalaRisposta != null) {
                                rispostaDetails.put("risposta", scalaRisposta.toString());
                                risposteCompletate.put(domanda.getId().toString(), rispostaDetails);
                            }
                            break;
                        case "DOMANDA_SELECT":
                            String rispostaSelect = (String) surveyMap.get(parametroRisposta);
                            if (rispostaSelect != null && !rispostaSelect.isEmpty()) {
                                rispostaDetails.put("risposta", rispostaSelect);
                                risposteCompletate.put(domanda.getId().toString(), rispostaDetails);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

            String jsonRisposte = objectMapper.writeValueAsString(risposteCompletate);
            Map<String, Object> risposteFinali = new HashMap<>(informazioniGenerali);
            risposteFinali.put("risposte", objectMapper.readTree(jsonRisposte));

            questionario.setRisposte(objectMapper.writeValueAsString(risposteFinali));
            salvaStatoQuestionarioCompletato(questionario, logger);

            em.merge(questionario);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Errore nel salvataggio del questionario ID: " + questionarioId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public static Questionario salvaStatoQuestionarioCompletato(Questionario questionario, Logger logger) {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();

        try {
            em.getTransaction().begin();

            if ((questionario.getStatus() == 1 && questionario.getDescrizione().equals(Stato_questionario.PRESO_IN_CARICO))
                    || (questionario.getStatus() == 2 && questionario.getDescrizione().equals(Stato_questionario.DA_COMPLETARE))) {

                questionario.setStatus(3);
                questionario.setDescrizione(Stato_questionario.COMPLETATO);
                questionario.setDataCompletamento(LocalDateTime.now());

                em.merge(questionario);
                em.getTransaction().commit();
            }
            logger.info("Questionario con id " + questionario.getId() + " salvato con successo " + " in data " + sdf.format(new Date()));

            return questionario;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Non è stato possibile salvare il questionario (Stato Completato)" + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return null;
    }

    public void salvaProgressi(String jsonInput, Logger logger) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> formData = objectMapper.readValue(jsonInput, new TypeReference<Map<String, Object>>() {
        });

        Long userId = Utils.tryParseLong((String) formData.get("userId"));
        if (userId == null) {
            return;
        }

        formData.put("data_salvataggio", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        String progressJson = objectMapper.writeValueAsString(formData);

        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();

        try {
            em.getTransaction().begin();
            Utente utente = em.find(Utente.class, userId);
            if (utente != null) {
                Questionario questionario = jPAUtil.findUtenteQuestionarioIdByUserId(utente.getId());
                questionario.setProgressi(progressJson);
                jPAUtil.salvaStatoQuestionario(questionario);
                em.merge(questionario);
            }
            em.getTransaction().commit();
            logger.info("Progressi del questionario salvati con successo per l'utente con id " + userId);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Errore salvataggio progressi: " + Utils.estraiEccezione(e));
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public byte[] generaPdfQuestionario(Long idUtente, Long idQuestionario, Logger logger) throws Exception {
        try {
            JPAUtil jpaUtil = new JPAUtil();
            Questionario questionario = jpaUtil.findUtenteQuestionarioByUtenteQuestionarioId(idQuestionario);
            Utente utente = jpaUtil.findUserByUtenteQuestionario(idQuestionario);

            if (questionario == null || utente == null) {
                throw new IllegalArgumentException("Questionario o utente non trovato");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseColor green = new BaseColor(0, 128, 85);
            Font fontBoldGreen = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, green);
            BaseColor red = new BaseColor(204, 51, 77);
            Font fontBoldRed = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, red);
            BaseColor blue = new BaseColor(0, 102, 204);
            Font fontBoldBlue = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, blue);

            JSONObject jsonRisposte = new JSONObject(questionario.getRisposte());
            JSONObject risposte2 = jsonRisposte.getJSONObject("risposte");

            int risposteDate = 0;
            int risposteCorrette = 0;
            int numeroDomanda = 0;

            for (String key : risposte2.keySet()) {
                JSONObject risposta = risposte2.getJSONObject(key);

                numeroDomanda++;
                String domanda = risposta.getString("domanda");
                boolean corretta = false;

                List<String> risposteUtente = new ArrayList<>();
                List<String> risposteGiuste = new ArrayList<>();

                if (risposta.has("risposta")) {
                    String rispostaTesto = risposta.getString("risposta");
                    String rispostaCorretta = risposta.getString("risposta corretta");
                    risposteUtente.add(rispostaTesto);
                    risposteGiuste.add(rispostaCorretta);
                    corretta = rispostaTesto.equals(rispostaCorretta);
                } else if (risposta.has("risposta_id") && risposta.has("risposte_corrette")) {
                    JSONArray idDate = risposta.getJSONArray("risposta_id");
                    JSONArray idCorrette = risposta.getJSONArray("risposte_corrette");

                    JSONArray testoDate = risposta.optJSONArray("risposta_testuale");
                    JSONArray testiCorrette = risposta.optJSONArray("testi_risposte_corrette");

                    Set<String> idDateSet = new HashSet<>();
                    for (int i = 0; i < idDate.length(); i++) {
                        idDateSet.add(idDate.getString(i));
                        if (testoDate != null && testoDate.length() > i) {
                            risposteUtente.add(testoDate.getString(i));
                        }
                    }

                    for (int i = 0; testiCorrette != null && i < testiCorrette.length(); i++) {
                        risposteGiuste.add(testiCorrette.getString(i));
                    }

                    Set<String> idCorretteSet = new HashSet<>();
                    for (int i = 0; i < idCorrette.length(); i++) {
                        idCorretteSet.add(idCorrette.getString(i));
                    }

                    corretta = idDateSet.equals(idCorretteSet);
                }

                risposteDate++;
                if (corretta) {
                    risposteCorrette++;
                }

                document.add(new Paragraph("\n"));
                PdfPTable table = new PdfPTable(2);

                try {
                    PdfPCell titleCell = new PdfPCell(new Phrase("Domanda - " + numeroDomanda + "\n" + extractTextAfterBase64(domanda.trim())));
                    titleCell.setColspan(2);
                    titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(titleCell);

                    Image img = extractImageAfterBase64(domanda, logger);
                    if (img != null) {
                        PdfPCell imageCell = new PdfPCell(img, true);
                        imageCell.setColspan(2);
                        table.addCell(imageCell);
                    }
                } catch (Exception e) {
                    logger.error("Errore nel parsing della domanda o immagine base64: \n" + Utils.estraiEccezione(e));
                }

                if (!corretta) {
                    List<String> risposteUtentePulite = cleanHtmlFromList(risposteUtente);
                    List<String> risposteGiustePulite = cleanHtmlFromList(risposteGiuste);

                    table.addCell(new PdfPCell(new Phrase("Risposta data:")));
                    table.addCell(new PdfPCell(new Phrase(String.join(", ", risposteUtentePulite), fontBoldRed)));

                    table.addCell(new PdfPCell(new Phrase("Risposta corretta:")));
                    table.addCell(new PdfPCell(new Phrase(String.join(", ", risposteGiustePulite), fontBoldGreen)));
                } else {
                    List<String> risposteUtentePulite = cleanHtmlFromList(risposteUtente);
                    List<String> risposteGiustePulite = cleanHtmlFromList(risposteGiuste);

                    if (risposteGiustePulite.size() > 1) {
                        table.addCell(new PdfPCell(new Phrase("Risposte date (corrette):")));
                        table.addCell(new PdfPCell(new Phrase(String.join(", ", risposteUtentePulite), fontBoldGreen)));

                        table.addCell(new PdfPCell(new Phrase("Risposte corrette :")));
                        table.addCell(new PdfPCell(new Phrase(String.join(", ", risposteGiustePulite), fontBoldGreen)));
                    } else {
                        table.addCell(new PdfPCell(new Phrase("Risposta corretta:")));
                        table.addCell(new PdfPCell(new Phrase(String.join(", ", risposteGiustePulite), fontBoldGreen)));
                    }
                }
                document.add(table);
            }

            int risposteErrate = risposteDate - risposteCorrette;
            int percentuale = risposteDate > 0 ? (risposteCorrette * 100 / risposteDate) : 0;

            String livello = "Generico";
            if (questionario.getDigicomp_questionario().isEmpty() && questionario.getCategoria().isEmpty()) {
                for (ModelloPredefinito md : questionario.getModelliPredefiniti()) {
                    livello = "Modello Predefinito (" + md.getDescrizione() + ")";
                }
            } else if (!questionario.getCategoria().isEmpty()) {
                for (Categoria c : questionario.getCategoria()) {
                    livello = "Categoria (" + c.getNome() + ")";
                }
            } else {
                for (Digicomp dg : questionario.getDigicomp_questionario()) {
                    livello = dg.getDescrizione();
                }
            }

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Questionario Completato (" + percentuale + "%)"));
            document.add(new Paragraph("Tipo di questionario - " + livello));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Dati personali", fontBoldBlue));
            document.add(new Paragraph("Nome completo: " + utente.getNome() + " " + utente.getCognome()));
            document.add(new Paragraph("Età: " + jsonRisposte.optInt("età", utente.getEtà())));
            document.add(new Paragraph("Indirizzo: " + jsonRisposte.optString("indirizzo", utente.getIndirizzo())));
            document.add(new Paragraph("Assegnato in data: " + jsonRisposte.optString("data_assegnazione", questionario.getDataDiAssegnazione().toString())));
            document.add(new Paragraph("Completato in data: " + jsonRisposte.optString("data_completamento", questionario.getDataCompletamento().toString())));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Totale domande: " + risposteDate, fontBoldBlue));
            document.add(new Paragraph("Risposte corrette: " + risposteCorrette, fontBoldGreen));
            document.add(new Paragraph("Risposte errate: " + risposteErrate, fontBoldRed));

            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Errore nella generazione del PDF: " + Utils.estraiEccezione(e));
            throw new Exception("Errore durante la generazione del PDF.", e);
        }
    }

    private static String extractTextAfterBase64(String input) {
        Pattern pattern = Pattern.compile("<br/><img src='data:image/[^;]+;base64,[^']*'[^>]* />");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String textAfterBase64 = input.substring(matcher.end()).trim();
            return textAfterBase64;
        }
        return input;
    }

    private static Image extractImageAfterBase64(String input, Logger logger) {
        Pattern pattern = Pattern.compile("<br/><img src='data:image/[^;]+;base64,([^']+)'[^>]* />");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String base64String = matcher.group(1);

            base64String = base64String.replaceAll("[^A-Za-z0-9+/=]", "");

            try {
                byte[] decodedBytes = Base64.decodeBase64(base64String);
                Image img = Image.getInstance(decodedBytes);
                return img;
            } catch (BadElementException | IOException e) {
                logger.error("Non è stato possibile estrarre l'immagine dal base64String" + "\n" + Utils.estraiEccezione(e));
            }
        }
        return null;
    }

    private static List<String> cleanHtmlFromList(List<String> lista) {
        List<String> pulita = new ArrayList<>();
        for (String s : lista) {
            pulita.add(s.replaceAll("<[^>]*>", "").trim());
        }
        return pulita;
    }

}
