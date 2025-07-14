/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Enbas.Services;

import Entity.Categoria;
import Entity.Competenza;
import Entity.Digicomp;
import Entity.Domanda;
import Entity.Questionario;
import Entity.SottoCategoria;
import Entity.Utente;
import Enum.Stato_questionario;
import Utils.JPAUtil;
import Utils.Utils;
import static Utils.Utils.estraiEccezione;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Salvatore
 */
public class StatisticheService {

    private final JPAUtil jpaUtil = new JPAUtil();
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticheService.class.getName());

    public byte[] estraiExcelPerUtente(Long utenteId, Logger logger) throws Exception {
        try {
            Utente utente = jpaUtil.findUserByUserId(String.valueOf(utenteId));
            if (utente == null) {
                logger.warn("Utente non trovato per ID: " + utenteId);
                throw new IllegalArgumentException("Utente non trovato.");
            }

            Questionario ultimoQuestionario = jpaUtil.findUltimoQuestionarioCompletatoPerUtente(utente);
            if (ultimoQuestionario == null) {
                logger.warn("Nessun questionario completato trovato per l'utente con ID: " + utenteId);
                throw new IllegalArgumentException("Nessun questionario completato trovato.");
            }

            if (ultimoQuestionario.getDigicomp_questionario() == null) {
                logger.warn("Nessun questionario DIGICOMP 2.2 trovato per l'utente con ID: " + utenteId);
                throw new IllegalArgumentException("Nessun questionario DIGICOMP 2.2 trovato.");
            }

            return createExcel(ultimoQuestionario);
        } catch (IllegalArgumentException e) {
            logger.error("Errore durante l'estrazione dell'Excel per l'utente " + utenteId + ":\n" + e.getMessage());
            throw new Exception("Errore durante l'estrazione dell'Excel.", e);
        }
    }

    public void controllaDigicompPerUtenti(Logger logger) throws Exception {
        try {
            List<Utente> utenti = jpaUtil.findAllUtenti();
            for (Utente utente : utenti) {
                Questionario ultimoQuestionario = jpaUtil.findUltimoQuestionarioCompletatoPerUtente(utente);

                if (ultimoQuestionario == null) {
                    logger.info("Nessun questionario completato trovato per l'utente " + utente.getId());
                    continue;
                }

                if (ultimoQuestionario.getDigicomp_questionario() == null || ultimoQuestionario.getDigicomp_questionario().isEmpty()) {
                    logger.info("Il questionario con ID " + ultimoQuestionario.getId() + " non ha un Digicomp associato per l'utente " + utente.getId());
                    continue;
                }

                Digicomp digicompAttuale = ultimoQuestionario.getDigicomp_questionario().get(0);
                int livelloCorrente = Utils.tryParseInt(digicompAttuale.getId().toString());

                if (livelloCorrente >= 5) {
                    logger.info("L'utente con ID " + utente.getId() + " ha già completato il livello massimo.");
                    jpaUtil.resettaDisponibilitàUtente(utente.getId(),LOGGER);
                    continue;
                }

                String jsonRisposte = ultimoQuestionario.getRisposte();
                if (jsonRisposte == null || jsonRisposte.isEmpty()) {
                    logger.info("Nessuna risposta trovata per l'utente " + utente.getId() + ".");
                    continue;
                }

                JsonNode rootNode = objectMapper.readTree(jsonRisposte);
                JsonNode risposteNode = rootNode.path("risposte");

                if (risposteNode.isMissingNode()) {
                    logger.info("Formato JSON non valido per l'utente " + utente.getId() + ".");
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

                logger.info("Totale risposte corrette per l'utente " + utente.getId() + ": " + risposteCorretteTotali + " su " + risposteNode.size() + " risposte.");
                if (!domandeSbagliate.isEmpty()) {
                    logger.info("Domande sbagliate per l'utente " + utente.getId() + ": " + domandeSbagliate);
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
                    logger.info("L'utente con ID " + utente.getId() + " ha completato il questionario con successo.");
                } else {
                    jpaUtil.resettaDisponibilitàUtente(utente.getId(),LOGGER);
                    logger.info("Il questionario con ID " + ultimoQuestionario.getId() + " per l'utente " + utente.getId() + " non ha superato il livello.");
                    for (String errore : domandeSbagliate) {
                        logger.info(errore);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Errore durante il controllo dei questionari Digicomp.\n" + e.getMessage());
            throw new Exception("Errore durante il controllo dei questionari.", e);
        }
    }

    public byte[] createExcel(Questionario ultimoQuestionario) throws IOException {
        EntityManager em = jpaUtil.getEm();
        try {
            List<Long> utentiIds = ultimoQuestionario.getUtenti()
                    .stream()
                    .map(Utente::getId)
                    .collect(Collectors.toList());

            List<Questionario> questionari = getQuestionariCompletati(em, utentiIds);
            questionari.add(ultimoQuestionario);

            Map<Long, Map<Long, int[]>> categoriaSottocategoriaStats = new HashMap<>();

            for (Questionario questionario : questionari) {
                processaRisposte(questionario, categoriaSottocategoriaStats);
            }

            return generaExcel(categoriaSottocategoriaStats);

        } catch (Exception e) {
            LOGGER.error("Errore nella generazione del file Excel: " + estraiEccezione(e));
            throw new IOException("Errore nella generazione del file Excel", e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    private void processaRisposte(Questionario questionario, Map<Long, Map<Long, int[]>> categoriaSottocategoriaStats) {
        try {
            List<Domanda> domande = questionario.getDomande();
            if (domande == null || domande.isEmpty()) {
                LOGGER.warn("Nessuna domanda trovata per il questionario ID " + questionario.getId());
                return;
            }

            for (Domanda domanda : domande) {
                Long domandaId = domanda.getId();

                if (domandaId == null) {
                    LOGGER.warn("Domanda ID nullo per una domanda nel questionario ID " + questionario.getId());
                    continue;
                }

                boolean corretta = isRispostaCorretta(questionario, domanda);

                Long categoriaId = findCategoriaByDomanda(domanda);
                Long competenzaId = findCompetenzaByDomanda(domanda);

                if (categoriaId != null && competenzaId != null) {
                    categoriaSottocategoriaStats.putIfAbsent(categoriaId, new HashMap<>());
                    Map<Long, int[]> competenzaStats = categoriaSottocategoriaStats.get(categoriaId);
                    competenzaStats.putIfAbsent(competenzaId, new int[]{0, 0});

                    int[] stats = competenzaStats.get(competenzaId);
                    stats[1]++;
                    if (corretta) {
                        stats[0]++;
                    }
                } else {
                    LOGGER.warn("Categoria o competenza non trovata per domanda ID " + domandaId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Errore nel processo di gestione delle risposte per il questionario ID " + questionario.getId() + ": " + estraiEccezione(e));
        }
    }

    private boolean isRispostaCorretta(Questionario questionario, Domanda domanda) {
        try {
            Map<String, Map<String, String>> risposte = getRisposteFromJson(questionario.getRisposte());
            if (risposte == null) {
                return false;
            }

            String idDomanda = String.valueOf(domanda.getId());
            if (!risposte.containsKey(idDomanda)) {
                return false;
            }

            Map<String, String> dettagliRisposta = risposte.get(idDomanda);

            if (dettagliRisposta.containsKey("risposta") && dettagliRisposta.containsKey("risposta corretta")) {
                String rispostaUtente = dettagliRisposta.get("risposta").trim();
                String rispostaCorretta = Utils.escapeHtmlAttribute(dettagliRisposta.get("risposta corretta").trim());

                rispostaUtente = Utils.removeHtmlTags(rispostaUtente);
                rispostaCorretta = Utils.removeHtmlTags(rispostaCorretta);

                LOGGER.info("Controllando la risposta corretta per domanda ID " + idDomanda + ": Risposta dell'utente = " + rispostaUtente + ", Risposta corretta = " + rispostaCorretta);

                return rispostaUtente.equalsIgnoreCase(rispostaCorretta);
            }

            if (dettagliRisposta.containsKey("risposta_testuale")) {
                String[] rispostaUtenteArray = dettagliRisposta.get("risposta_testuale").split(",");
                String[] rispostaCorrettaArray = Utils.escapeHtmlAttribute(dettagliRisposta.get("testi_risposte_corrette")).split(",");

                List<String> rispostaUtenteList = Arrays.stream(rispostaUtenteArray)
                        .map(r -> Utils.removeHtmlTags(r.trim()))
                        .sorted()
                        .collect(Collectors.toList());

                List<String> risposteCorretteList = Arrays.stream(rispostaCorrettaArray)
                        .map(r -> Utils.removeHtmlTags(r.trim()))
                        .sorted()
                        .collect(Collectors.toList());

                LOGGER.info("Controllando la risposta corretta per domanda ID " + idDomanda + ": Risposta dell'utente = " + rispostaUtenteList + ", Risposta corretta = " + risposteCorretteList);

                return rispostaUtenteList.equals(risposteCorretteList);
            }

            if (dettagliRisposta.containsKey("risposta_id") && dettagliRisposta.containsKey("risposte_corrette")) {
                Set<String> rispostaUtenteSet = new HashSet<>(Arrays.asList(dettagliRisposta.get("risposta_id").split(",")));
                Set<String> risposteCorretteSet = new HashSet<>(Arrays.asList(dettagliRisposta.get("risposte_corrette").split(",")));

                LOGGER.info("Controllando la/e risposta/a corretta/e per domanda ID " + idDomanda + ": Risposta/i dell'utente = " + rispostaUtenteSet + ", Risposta/i corretta/e = " + risposteCorretteSet);

                return rispostaUtenteSet.equals(risposteCorretteSet);
            }

            return false;

        } catch (Exception e) {
            LOGGER.error("Errore nell'estrazione della risposta corretta dalla domanda: " + estraiEccezione(e));
            return false;
        }
    }

    private Map<String, Map<String, String>> getRisposteFromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> jsonMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            Object risposteNode = jsonMap.get("risposte");
            if (risposteNode == null) {
                return null;
            }

            if (risposteNode instanceof Map) {
                Map<String, Object> risposteMap = (Map<String, Object>) risposteNode;

                for (Map.Entry<String, Object> entry : risposteMap.entrySet()) {
                    Map<String, Object> dettaglio = (Map<String, Object>) entry.getValue();

                    if (dettaglio.containsKey("risposta_testuale")) {
                        Object rispostaTestuale = dettaglio.get("risposta_testuale");

                        if (rispostaTestuale instanceof List) {
                            List<String> rispostaList = (List<String>) rispostaTestuale;
                            dettaglio.put("risposta_testuale", String.join(", ", rispostaList));
                        } else if (rispostaTestuale instanceof String) {
                        } else {
                            LOGGER.warn("Unexpected type for risposta_testuale: " + rispostaTestuale.getClass());
                        }
                    }

                    if (dettaglio.containsKey("risposta_id")) {
                        Object rispostaId = dettaglio.get("risposta_id");

                        if (rispostaId instanceof List) {
                            List<String> rispostaIdList = (List<String>) rispostaId;
                            dettaglio.put("risposta_id", String.join(", ", rispostaIdList));
                        } else if (rispostaId instanceof String) {
                        } else {
                            LOGGER.warn("Unexpected type for risposta_id: " + rispostaId.getClass());
                        }
                    }

                    if (dettaglio.containsKey("risposte_corrette")) {
                        Object risposteCorrette = dettaglio.get("risposte_corrette");

                        if (risposteCorrette instanceof List) {
                            List<String> risposteCorretteList = (List<String>) risposteCorrette;
                            dettaglio.put("risposte_corrette", String.join(", ", risposteCorretteList));
                        } else if (risposteCorrette instanceof String) {
                        } else {
                            LOGGER.warn("Unexpected type for risposte_corrette: " + risposteCorrette.getClass());
                        }
                    }

                    if (dettaglio.containsKey("testi_risposte_corrette")) {
                        Object testiRisposteCorrette = dettaglio.get("testi_risposte_corrette");

                        if (testiRisposteCorrette instanceof List) {
                            List<String> testiRisposteCorretteList = (List<String>) testiRisposteCorrette;
                            dettaglio.put("testi_risposte_corrette", String.join(", ", testiRisposteCorretteList));
                        } else if (testiRisposteCorrette instanceof String) {
                        } else {
                            LOGGER.warn("Unexpected type for testi_risposte_corrette: " + testiRisposteCorrette.getClass());
                        }
                    }
                }
            }

            Map<String, Map<String, String>> risposte = objectMapper.convertValue(risposteNode, new TypeReference<Map<String, Map<String, String>>>() {
            });

            return risposte;

        } catch (JsonProcessingException | IllegalArgumentException e) {
            LOGGER.error("Errore nell'estrazione delle risposte dal json: " + estraiEccezione(e));
            return null;
        }
    }

    private Long findCategoriaByDomanda(Domanda domanda) {
        EntityManager em = jpaUtil.getEm();

        try {
            Domanda d = em.find(Domanda.class,
                    domanda.getId());
            return (d != null && d.getCategoria() != null) ? d.getCategoria().getId() : null;
        } catch (Exception e) {
            LOGGER.error("Errore nell'estrazione della categoria dalla domanda: " + estraiEccezione(e));
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return null;
    }

    private Long findCompetenzaByDomanda(Domanda domanda) {
        EntityManager em = jpaUtil.getEm();

        try {
            Domanda d = em.find(Domanda.class,
                    domanda.getId());
            return (d != null && d.getCompetenza() != null) ? d.getCompetenza().getId() : null;
        } catch (Exception e) {
            LOGGER.error("Errore nell'estrazione della categoria dalla domanda: " + estraiEccezione(e));
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return null;
    }

    public SottoCategoria findSottocategoriaById(Long sottoCategoriaId) {
        EntityManager em2 = jpaUtil.getEm();

        try {
            SottoCategoria sottoCategoria = em2.find(SottoCategoria.class,
                    sottoCategoriaId);
            if (sottoCategoria != null) {
                return sottoCategoria;
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca della sottocategoria con id " + sottoCategoriaId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    private List<Questionario> getQuestionariCompletati(EntityManager em, List<Long> utentiIds) {
        TypedQuery<Questionario> query = em.createQuery(
                "SELECT q FROM Questionario q JOIN q.utenti u JOIN q.digicomp_questionario dq "
                + "WHERE u.id IN :utentiIds AND q.descrizione = :stato ORDER BY dq.id",
                Questionario.class
        );
        query.setParameter("utentiIds", utentiIds);
        query.setParameter("stato", Stato_questionario.COMPLETATO2);
        return query.getResultList();
    }

    public byte[] generaExcel(Map<Long, Map<Long, int[]>> categoriaSottocategoriaStats) throws IOException {
        int maxLength = 40;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Statistiche Questionari");

            String[] colonne = {"Categoria", "Competenza", "Abilità/Conoscenze", "Livello", "Domande totali", "Risposte corrette", "Risposte errate", "Percentuale"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < colonne.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colonne[i]);
                cell.setCellStyle(creaStileIntestazione(workbook));
            }

            CellStyle centraleStyle = workbook.createCellStyle();
            centraleStyle.setAlignment(HorizontalAlignment.CENTER);
            centraleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            int rowNum = 1;
            int totaleDomande = 0;
            int totaleCorrette = 0;

            JPAUtil jPAUtil = new JPAUtil();

            for (Map.Entry<Long, Map<Long, int[]>> entry : categoriaSottocategoriaStats.entrySet()) {
                Long categoriaId = entry.getKey();
                Categoria categoria = jPAUtil.findCategoriaById(categoriaId);

                Map<Long, int[]> statsPerCompetenza = entry.getValue();
                List<Map.Entry<Long, int[]>> competenzeList = new ArrayList<>(statsPerCompetenza.entrySet());

                competenzeList.sort((e1, e2) -> {
                    Competenza c1 = jPAUtil.findCompetenzaById(e1.getKey());
                    Competenza c2 = jPAUtil.findCompetenzaById(e2.getKey());
                    return c1.getDescrizione().compareTo(c2.getDescrizione());
                });

                int startRowCategoria = rowNum;
                int categoriaTotale = 0;
                int categoriaCorrette = 0;
                String lastAreaCompetenza = "";
                for (int i = 0; i < competenzeList.size(); i++) {
                    Map.Entry<Long, int[]> competenzaEntry = competenzeList.get(i);
                    Competenza competenza = jPAUtil.findCompetenzaById(competenzaEntry.getKey());

                    String currentAreaNome = truncateString(competenza.getAreeCompetenze().getNome(), maxLength);

                    Row row = sheet.createRow(rowNum++);

                    row.createCell(1).setCellValue(currentAreaNome.equals(lastAreaCompetenza) ? "" : currentAreaNome);

                    row.createCell(2).setCellValue(truncateString(competenza.getDescrizione(), maxLength));
                    row.createCell(3).setCellValue(competenza.getLivello());

                    int risposteCorrette = competenzaEntry.getValue()[0];
                    int domandeTotali = competenzaEntry.getValue()[1];
                    int risposteSbagliate = domandeTotali - risposteCorrette;
                    double percentuale = domandeTotali > 0 ? ((double) risposteCorrette / domandeTotali) * 100 : 0;

                    row.createCell(4).setCellValue(domandeTotali);
                    row.createCell(5).setCellValue(risposteCorrette);
                    row.createCell(6).setCellValue(risposteSbagliate);
                    row.createCell(7).setCellValue(String.format("%.2f%%", percentuale));

                    categoriaTotale += domandeTotali;
                    categoriaCorrette += risposteCorrette;

                    boolean isLast = (i == competenzeList.size() - 1);
                    if (!isLast) {
                        Long nextCompetenzaId = competenzeList.get(i + 1).getKey();
                        Competenza nextCompetenza = jPAUtil.findCompetenzaById(nextCompetenzaId);
                        String nextArea = nextCompetenza.getAreeCompetenze().getNome();

                        if (!currentAreaNome.equals(truncateString(nextArea, maxLength))) {
                            sheet.createRow(rowNum++);
                        }
                    }

                    lastAreaCompetenza = currentAreaNome;
                }

                if (startRowCategoria < rowNum) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowCategoria, rowNum - 1, 0, 0));
                    Row categoriaRow = sheet.getRow(startRowCategoria);
                    Cell cell = categoriaRow.getCell(0);
                    if (cell == null) {
                        cell = categoriaRow.createCell(0);
                    }
                    cell.setCellValue(categoria.getNome());
                    cell.setCellStyle(centraleStyle);

                    categoriaRow.getCell(0).setCellStyle(centraleStyle);
                }

                Row categoriaTotalRow = sheet.createRow(rowNum++);
                int risposteSbagliateCategoria = categoriaTotale - categoriaCorrette;
                double percentualeCategoria = categoriaTotale > 0 ? ((double) categoriaCorrette / categoriaTotale) * 100 : 0;

                categoriaTotalRow.createCell(0).setCellValue("Totale " + categoria.getNome());
                categoriaTotalRow.createCell(4).setCellValue(categoriaTotale);
                categoriaTotalRow.createCell(5).setCellValue(categoriaCorrette);
                categoriaTotalRow.createCell(6).setCellValue(risposteSbagliateCategoria);
                categoriaTotalRow.createCell(7).setCellValue(String.format("%.2f%%", percentualeCategoria));

                totaleDomande += categoriaTotale;
                totaleCorrette += categoriaCorrette;
            }

            Row totalRow = sheet.createRow(rowNum++);
            int risposteErrateTotali = totaleDomande - totaleCorrette;
            double percentualeTotale = totaleDomande > 0 ? ((double) totaleCorrette / totaleDomande) * 100 : 0;

            totalRow.createCell(0).setCellValue("Totale Generale");
            totalRow.createCell(4).setCellValue(totaleDomande);
            totalRow.createCell(5).setCellValue(totaleCorrette);
            totalRow.createCell(6).setCellValue(risposteErrateTotali);
            totalRow.createCell(7).setCellValue(String.format("%.2f%%", percentualeTotale));

            for (int i = 0; i < colonne.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        } catch (IOException e) {
            throw new IOException("Errore nella generazione del file Excel: " + estraiEccezione(e));
        }
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }

    private CellStyle creaStileIntestazione(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public static String estraiNumeriIniziali(String input) {
        Pattern pattern = Pattern.compile("^([\\d.]+)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "-";
    }

    public static String estraiMNumero(String input) {
        Pattern pattern = Pattern.compile("\\b(M\\d+)\\b");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "-";
    }

    public static String escapeJsonString(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        StringBuilder escapedString = new StringBuilder();
        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);

            switch (c) {
                case '\\':
                    escapedString.append("\\\\");
                    break;
                case '"':
                    escapedString.append("\\\"");
                    break;
                case '\n':
                    escapedString.append("\\n");
                    break;
                case '\r':
                    escapedString.append("\\r");
                    break;
                case '\t':
                    escapedString.append("\\t");
                    break;
                case '\b':
                    escapedString.append("\\b");
                    break;
                case '\f':
                    escapedString.append("\\f");
                    break;
                default:
                    escapedString.append(c);
                    break;
            }
        }
        return escapedString.toString();
    }

    public static String removeHtmlTags(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("<[^>]*>", "").trim();
    }

}
