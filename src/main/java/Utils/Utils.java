/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import Entity.Categoria;
import Entity.Competenza;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;
import static org.apache.commons.lang.exception.ExceptionUtils.getStackTrace;
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
public class Utils {

    private final static Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());

    public static Integer tryParseInt(String param) {
        try {
            return Integer.valueOf(param);
        } catch (NumberFormatException e) {
            LOGGER.error("Non è stato possibile effettuare il parsing (da String a int) del parametro" + " " + param + "\n" + estraiEccezione(e));
        }
        return 0;
    }

    public static String tryParseString(int param) {
        try {
            return String.valueOf(param);
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare il parsing (da int a String) del parametro" + " " + param + "\n" + estraiEccezione(e));
        }
        return null;
    }

    public static Long tryParseLong(String param) {
        try {
            if (param != null) {
                return Long.valueOf(param.trim());
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Non è stato possibile effettuare il parsing (da String a Long) del parametro" + " " + param + "\n" + estraiEccezione(e));
        }
        return 0L;
    }

    public static String checkAttribute(HttpSession session, String attribute) {
        try {
            if (session.getAttribute(attribute) != null) {
                return String.valueOf(session.getAttribute(attribute));
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare il check del parametro" + " " + attribute + "\n" + estraiEccezione(e));
        }
        return "";
    }

    public static String escapeHtmlAttribute(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static String getparsedDate(String date) throws Exception {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ITALIAN);
        String s1 = date;
        String s2 = null;
        Date d;
        try {
            d = sdf.parse(s1);
            s2 = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(d);

        } catch (ParseException e) {
            LOGGER.error("Non è stato possibile effettuare il parsing della data" + " " + date + "\n" + estraiEccezione(e));
        }

        return s2;

    }

    public static String estraiEccezione(Exception ec1) {
        try {
            return ec1.getStackTrace()[0].getMethodName() + " - " + getStackTrace(ec1);
        } catch (Exception e) {
            LOGGER.error("ERRORE GENERICO", e);
        }
        return ec1.getMessage();
    }

    public static final ResourceBundle config = ResourceBundle.getBundle("conf.config");

    public void generaExcel(Map<Long, Map<Long, int[]>> categoriaSottocategoriaStats, HttpServletResponse response) {
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

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"statistiche.xlsx\"");

            try (ServletOutputStream out = response.getOutputStream()) {
                workbook.write(out);
                out.flush();
            }

        } catch (IOException e) {
            LOGGER.error("Errore nella generazione del file Excel: " + estraiEccezione(e));
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

    public static LocalDateTime calcolaScadenza(String scadenza) {
        scadenza = scadenza.trim().toLowerCase();

        String valoreNumerico = scadenza.replaceAll("[^0-9]", "");
        String tipo = scadenza.replaceAll("[0-9]", "");

        int valore = Integer.parseInt(valoreNumerico);
        LocalDateTime now = LocalDateTime.now();

        switch (tipo) {
            case "m" -> {
                return now.plusMinutes(valore);
            }
            case "mo" -> {
                return now.plusMonths(valore);
            }
            case "y" -> {
                return now.plusYears(valore);
            }
            default ->
                throw new IllegalArgumentException("Formato scadenza non valido: " + scadenza);
        }
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        input = input.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        input = input.replaceAll("[\r\n]", "");
        return StringEscapeUtils.escapeHtml4(input);
    }

    public static List<String> cleanHtmlFromList(List<String> lista) {
        List<String> pulita = new ArrayList<>();
        for (String s : lista) {
            String cleaned = s.replaceAll("<[^>]*>", "").trim();
            cleaned = StringEscapeUtils.unescapeHtml4(cleaned);
            pulita.add(cleaned);
        }
        return pulita;

    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return dtf.format(localDateTime);
    }
}
