/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import Entity.AreeCompetenze;
import Entity.Categoria;
import Entity.Competenza;
import Entity.Digicomp;
import Entity.DigicompDomanda;
import Entity.Domanda;
import Entity.ModelloPredefinito;
import Entity.Pagina;
import Entity.Questionario;
import Entity.Ruolo;
import Entity.SottoCategoria;
import Entity.Utente;
import Enum.Assegnabile_enum;
import Enum.Si_no;
import Enum.Stato_questionario;
import Enum.Stato_utente;
import Enum.Tipo_domanda;
import Enum.Tipo_inserimento;
import Enum.Visibilità_domanda;
import static Utils.Utils.estraiEccezione;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Salvatore
 */
public class JPAUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JPAUtil.class.getName());
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("gestionale_questionario");

    public EntityManager getEm() {
        return emf.createEntityManager();
    }

    //LOGIN
    public int authenticate(String username, String enteredPassword) {
        EntityManager em2 = this.getEm();
        try {
            TypedQuery<Utente> query = em2.createNamedQuery("getUtenteUSPASS", Utente.class);
            query.setParameter("username", username);
            query.setMaxResults(1);
            List<Utente> resultList = query.getResultList();

            if (!resultList.isEmpty()) {
                Utente user = resultList.get(0);
                String hashedPasswordFromDatabase = user.getPassword();
                boolean isPasswordValid = BCrypt.checkpw(enteredPassword, hashedPasswordFromDatabase);
                if (isPasswordValid) {
                    return user.getRuolo().getId();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare l'autenticazione dell'utente" + " " + username + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return -1;
    }

    public boolean isPasswordValid(String username, String enteredPassword) {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Utente> query = em2.createNamedQuery("getUtenteUSPASS", Utente.class);
            query.setParameter("username", username);
            query.setMaxResults(1);
            List<Utente> resultList = query.getResultList();

            if (!resultList.isEmpty()) {
                Utente user = resultList.get(0);
                String hashedPasswordFromDatabase = user.getPassword();

                return BCrypt.checkpw(enteredPassword, hashedPasswordFromDatabase);
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la validazione della password dell'utente" + " " + username + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return false;
    }

    public Utente getUserByUsername(String username) {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Utente> query = em2.createNamedQuery("getUtenteUSPASS", Utente.class
            );
            query.setParameter("username", username);
            query.setMaxResults(1);
            List<Utente> resultList = query.getResultList();

            if (!resultList.isEmpty()) {
                return resultList.get(0);

            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca dell'utente" + " " + username + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return null;
    }

    //ALTRO
    public Utente findUserByUserId(String userId) {
        EntityManager em2 = this.getEm();
        try {
            Utente utente = em2.find(Utente.class, Utils.tryParseLong((userId)));
            if (utente != null) {
                return utente;
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca dell'utente con id " + userId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Ruolo findRuoloById(int ruolo_int) {
        EntityManager em2 = this.getEm();
        try {
            Ruolo ruolo = em2.find(Ruolo.class, (ruolo_int));
            if (ruolo != null) {
                return ruolo;
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca del ruolo con id " + ruolo_int + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Utente findUserByClientId(String clientId) {
        EntityManager em2 = this.getEm();
        try {
            TypedQuery<Utente> query = em2.createQuery(
                    "SELECT u FROM Utente u WHERE u.username = :username",
                    Utente.class
            ).setParameter("username", clientId);

            if (query.getResultList() != null) {
                return query.getResultList().get(0);
            }

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca dell'utente con username " + clientId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Competenza findCompetenzaById(Long abilità_competenza_id) {
        EntityManager em2 = this.getEm();
        try {
            Competenza competenza = em2.find(Competenza.class, abilità_competenza_id);
            if (competenza != null) {
                return competenza;
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca della competenza con id " + abilità_competenza_id + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Questionario findUltimoQuestionarioCompletatoPerUtente(Utente utente) {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Questionario> query = em2.createQuery(
                    "SELECT q FROM Questionario q "
                    + "JOIN q.utenti u "
                    + "WHERE u.id = :utenteId AND q.descrizione = :completato "
                    + "ORDER BY q.dataCompletamento DESC",
                    Questionario.class
            );
            query.setParameter("utenteId", utente.getId());
            query.setParameter("completato", Stato_questionario.COMPLETATO);
            query.setMaxResults(1);
            return query.getResultList().stream().findFirst().orElse(null);
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca dell'ultimo questionario associato all'utente con id " + utente.getId() + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Questionario findUtenteQuestionarioByUtenteQuestionarioId(Long utente_questionario) {
        EntityManager em2 = this.getEm();
        try {

            Questionario utente_questionario2 = em2.find(Questionario.class, utente_questionario);
            if (utente_questionario2 != null) {
                return utente_questionario2;
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca del questionario con id " + utente_questionario + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Categoria findCategoriaById(Long categoria_id) {
        EntityManager em2 = this.getEm();
        try {

            Categoria categoria = em2.find(Categoria.class, categoria_id);
            if (categoria != null) {
                return categoria;
            }
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca della categoria con id " + categoria_id + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Questionario findUtenteQuestionarioIdByUserId(Long userId) {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Questionario> query = em2.createQuery(
                    "SELECT q FROM Questionario q JOIN q.utenti utente WHERE utente.id = :userId ORDER BY q.id DESC",
                    Questionario.class
            );

            query.setParameter("userId", userId);
            query.setMaxResults(1);

            List<Questionario> results = query.getResultList();

            if (results.isEmpty()) {
                return null;
            }

            return results.get(0);
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca del questionario con l'id dell'utente " + userId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Utente findUserByUtenteQuestionario(Long utenteQuestionarioId) {
        EntityManager em2 = this.getEm();
        try {

            Questionario questionario = em2.find(Questionario.class, utenteQuestionarioId);

            if (questionario != null && !questionario.getUtenti().isEmpty()) {
                return questionario.getUtenti().get(0);
            }

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca dell'utente con l'id questionario" + utenteQuestionarioId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public Domanda findDomandaById(Long domandaId) {
        EntityManager em2 = this.getEm();
        try {

            Domanda domanda = em2.find(Domanda.class, domandaId);

            if (domanda != null) {
                return domanda;
            }

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca della domanda con id " + domandaId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public boolean deleteDomandaById(Long domandaId) {
        EntityManager em2 = this.getEm();

        try {

            Domanda domanda = em2.find(Domanda.class, domandaId);
            em2.getTransaction().begin();

            if (domanda != null) {
                domanda.setVisibilità_domanda(Visibilità_domanda.NON_VISIBILE);
                em2.merge(domanda);
                em2.getTransaction().commit();
                return true;
            }

        } catch (Exception e) {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            LOGGER.error("Non è stato possibile effettuare l'eliminazione della domanda con id " + domandaId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return false;
    }

    public boolean deleteUtenteById(Long utente_id) {
        EntityManager em2 = this.getEm();

        try {

            Utente utente = em2.find(Utente.class, utente_id);
            em2.getTransaction().begin();

            if (utente != null) {
                utente.setStato_utente(Stato_utente.NON_ATTIVO);
                em2.merge(utente);
                em2.getTransaction().commit();
                return true;
            }

        } catch (Exception e) {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            LOGGER.error("Non è stato possibile effettuare l'eliminazione dell'utenza con id " + utente_id + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return false;
    }

    public Questionario salvaStatoQuestionario(Questionario questionario) {
        EntityManager em2 = this.getEm();
        try {
            if (questionario.getStatus() == 1 && questionario.getDescrizione().equals(Stato_questionario.PRESO_IN_CARICO)) {
                try {
                    em2.getTransaction().begin();
                    questionario.setStatus(2);
                    questionario.setDescrizione(Stato_questionario.DA_COMPLETARE);
                    em2.merge(questionario);
                    em2.getTransaction().commit();
                } catch (Exception e) {
                    if (em2.getTransaction().isActive()) {
                        em2.getTransaction().rollback();
                        LOGGER.error("Non è stato possibile effettuare il merge dello stato (DA_COMPLETARE) del questionario con id " + questionario.getId() + "\n" + Utils.estraiEccezione(e));
                    }
                }
            }
            return questionario;

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare il salvataggio dello stato del questionario  (DA_COMPLETARE) con id " + questionario.getId() + "\n" + Utils.estraiEccezione(e));
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    public List<AreeCompetenze> findAreeCompetenzeByCategoria(Long categoriaId) {
        EntityManager em = getEm();
        List<AreeCompetenze> resultList = new ArrayList<>();

        try {
            TypedQuery<AreeCompetenze> query = em.createQuery(
                    "SELECT ac FROM AreeCompetenze ac WHERE ac.categoria.id = :categoriaId",
                    AreeCompetenze.class
            );
            query.setParameter("categoriaId", categoriaId);
            resultList = query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare il recupero delle aree di competenza per la categoria ID: " + categoriaId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return resultList;
    }

    public AreeCompetenze findAreeCompetenzeById(Long area_competenza_id) {
        EntityManager em = getEm();

        try {
            TypedQuery<AreeCompetenze> query = em.createQuery(
                    "SELECT ac FROM AreeCompetenze ac WHERE ac.id = :area_competenza_id",
                    AreeCompetenze.class
            );
            query.setParameter("area_competenza_id", area_competenza_id);
            if (!query.getResultList().isEmpty()) {
                return query.getResultList().get(0);
            }

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare il recupero dell' area di competenza con id: " + area_competenza_id + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return null;
    }

    //CONTROLLA DIGICOMP
    public List<Domanda> findQuestions(Long questionarioId) {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Domanda> query = em2.createQuery(
                    "SELECT d FROM Domanda d WHERE d.modelloPredefinito.id = :questionarioId AND d.visibilità_domanda = :visibilità",
                    Domanda.class
            );
            query.setParameter("questionarioId", questionarioId);
            query.setParameter("visibilità", Visibilità_domanda.VISIBILE);

            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca delle domande del questionario con id " + questionarioId + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public List<Utente> findAllUtenti() {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Utente> query = em2.createQuery(
                    "SELECT u FROM Utente u WHERE u.ruolo.id = 2",
                    Utente.class
            );

            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutti gli utenti " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public List<Digicomp> findAllDigicomp() {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Digicomp> query = em2.createQuery(
                    "SELECT d FROM Digicomp d WHERE d.assegnabile_enum = :stato",
                    Digicomp.class
            );
            query.setParameter("stato", Assegnabile_enum.ASSEGNABILE);
            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutti i Digicomp  " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public List<ModelloPredefinito> findAllModelliPredefiniti() {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<ModelloPredefinito> query = em2.createQuery(
                    "SELECT mp FROM ModelloPredefinito mp",
                    ModelloPredefinito.class
            );

            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutti i Modelli Predefiniti  " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public List<Questionario> findAllQuestionariCompletati() {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Questionario> query = em2.createQuery(
                    "SELECT q FROM Questionario q WHERE q.status = 3 AND q.descrizione = :stato",
                    Questionario.class
            );
            query.setParameter("stato", Stato_questionario.COMPLETATO);

            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutti i questionari completati  " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public List<Categoria> findAllCategorie() {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Categoria> query = em2.createQuery(
                    "SELECT c FROM Categoria c",
                    Categoria.class
            );

            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutte le categorie  " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public List<AreeCompetenze> findAllAreeCompetenze() {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<AreeCompetenze> query = em2.createQuery(
                    "SELECT ac FROM AreeCompetenze ac",
                    AreeCompetenze.class
            );

            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutte le competenze  " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public List<Competenza> getCompetenzaListByAreaCompetenza(AreeCompetenze ac) {
        EntityManager em2 = this.getEm();
        try {

            TypedQuery<Competenza> query = em2.createQuery(
                    "SELECT c FROM Competenza c WHERE c.areeCompetenze = :ac",
                    Competenza.class
            );
            query.setParameter("ac", ac);
            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutte le competenze  " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();

    }

    public List<SottoCategoria> findAllSottoCategorieByCategoriaId(Long categoriaId) {
        EntityManager em2 = this.getEm();

        try {

            TypedQuery<SottoCategoria> query = em2.createQuery(
                    "SELECT s FROM SottoCategoria s WHERE s.categoria.id = :categoria_id",
                    SottoCategoria.class
            );
            query.setParameter("categoria_id", categoriaId);

            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutte le categorie  " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public List<Competenza> findAllCompetenzeByCategoriaId(Long categoria) {
        EntityManager em2 = this.getEm();

        try {

            TypedQuery<Competenza> query = em2.createQuery(
                    "SELECT c FROM Competenza c WHERE c.areeCompetenze.categoria.id = :categoria",
                    Competenza.class
            );
            query.setParameter("categoria", categoria);

            return query.getResultList();

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca di tutte le categorie  " + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }

        return Collections.emptyList();
    }

    public Boolean isVisible(String ruolo, String page) {
        if (ruolo == null || ruolo.isEmpty() || page == null || page.isEmpty()) {
            return false;
        }
        EntityManager em2 = this.getEm();

        try {
            String jpql = "SELECT p FROM Pagina p WHERE p.nome = :page";
            TypedQuery<Pagina> query = em2.createQuery(jpql, Pagina.class
            );
            query.setParameter("page", page);

            List<Pagina> paginaList = query.getResultList();
            if (!paginaList.isEmpty()) {
                Pagina pagina = paginaList.get(0);
                String[] permessi = pagina.getPermessi().split("-");

                for (String permesso : permessi) {
                    if (permesso.equals(ruolo)) {
                        return true;
                    }
                }

            } else {
                return null;
            }

        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la ricerca del permesso per gli utenti con ruolo " + ruolo + " della pagina " + page + "\n" + Utils.estraiEccezione(e));
            return false;
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return false;
    }

    public void assegnaNuovoQuestionario(Questionario vecchioQuestionario, int nuovoLivello) {

        EntityManager em2 = this.getEm();
        try {

            em2.getTransaction().begin();
            List<Domanda> nuoveDomande = new ArrayList<>();

            List<Questionario> questionariCompletato2 = getQuestionariCompletatiByUtente(vecchioQuestionario.getUtenti(), Stato_questionario.COMPLETATO2);

            questionariCompletato2.add(vecchioQuestionario);

            if (!questionariCompletato2.isEmpty()) {
                nuoveDomande = selezionaDomande2(vecchioQuestionario, nuovoLivello + 1);
            }
            //} else {
            //    nuoveDomande = selezionaDomande(vecchioQuestionario, nuovoLivello + 1);

            //}
            vecchioQuestionario.setStatus(4);
            vecchioQuestionario.setDescrizione(Stato_questionario.COMPLETATO2);
            em2.merge(vecchioQuestionario);

            int nuovoLivelloSomma = nuovoLivello + 1;
            String nuovoLivelloString = Utils.tryParseString(nuovoLivelloSomma);
            Digicomp nuovoDigicomp = em2.find(Digicomp.class,
                    Utils.tryParseLong(nuovoLivelloString));

            if (nuovoDigicomp == null) {
                LOGGER.warn("Errore: Digicomp ID " + nuovoLivelloString + " non trovato.");
                em2.getTransaction().rollback();
                return;
            }

            Questionario nuovoQuestionario = new Questionario();
            nuovoQuestionario.setUtenti(new ArrayList<>(vecchioQuestionario.getUtenti()));
            nuovoQuestionario.setStatus(0);
            nuovoQuestionario.setDigicomp_questionario(List.of(nuovoDigicomp));

            List<DigicompDomanda> digicompDomande = new ArrayList<>();
            for (Domanda domanda : nuoveDomande) {
                DigicompDomanda digicompDomanda = new DigicompDomanda();
                digicompDomanda.setDigicomp(nuovoDigicomp);
                digicompDomanda.setDomanda(domanda);
                digicompDomanda.setLivello(Utils.tryParseInt(nuovoLivelloString));
                digicompDomanda.setCategoria(domanda.getCategoria());
                digicompDomande.add(digicompDomanda);
                em2.persist(digicompDomanda);
            }

            nuovoDigicomp.setDomande_digicomp(digicompDomande);

            nuovoQuestionario.setDescrizione(Stato_questionario.ASSEGNATO);
            nuovoQuestionario.setDomande(nuoveDomande);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String formattedDate = sdf.format(new Date());
            nuovoQuestionario.setDataDiAssegnazione(formattedDate);

            em2.persist(nuovoQuestionario);
            em2.getTransaction().commit();
            LOGGER.info("Nuovo questionario assegnato a " + vecchioQuestionario.getUtenti() + " di livello " + nuovoLivelloString);
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile assegnare un nuovo questionario a " + vecchioQuestionario.getUtenti() + "\n" + estraiEccezione(e));
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
    }

    public Long getCategoriaIdByDomandaId(Long domandaId) {
        EntityManager em2 = this.getEm();

        try {
            Domanda domanda = em2.find(Domanda.class,
                    domandaId);
            return (domanda != null && domanda.getCategoria() != null) ? domanda.getCategoria().getId() : null;
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile estrarre la categoria dalla domanda" + "\n" + estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return null;
    }

    private List<Domanda> selezionaDomande2(Questionario questionarioVecchio, int nuovoLivello) {
        try {
            List<Questionario> questionari = getQuestionariCompletatiByUtente(
                    questionarioVecchio.getUtenti(), Stato_questionario.COMPLETATO2);
            questionari.add(questionarioVecchio);

            Set<Long> domandeUsateIds = questionari.stream()
                    .flatMap(q -> q.getDomande().stream())
                    .map(Domanda::getId)
                    .collect(Collectors.toSet());

            List<Domanda> tutteDomandeVisibili = getDomandeVisibili();

            Map<Long, Map<Long, List<Domanda>>> domandePerCategoriaEArea = new HashMap<>();
            for (Domanda d : tutteDomandeVisibili) {
                if (d.getCategoria() == null) {
                    continue;
                }

                Long catId = d.getCategoria().getId();
                Long areaId = (d.getCompetenza() != null && d.getCompetenza().getAreeCompetenze() != null)
                        ? d.getCompetenza().getAreeCompetenze().getId()
                        : null;

                domandePerCategoriaEArea
                        .computeIfAbsent(catId, k -> new HashMap<>())
                        .computeIfAbsent(areaId, k -> new ArrayList<>())
                        .add(d);
            }

            Map<Long, Integer> distribuzione = getDistribuzioneDomandePerAreaCompetenza();
            List<Domanda> domandeSelezionate = new ArrayList<>();

            for (Map.Entry<Long, Integer> entry : distribuzione.entrySet()) {
                Long categoriaId = entry.getKey();
                int numRichiesto = entry.getValue();

                Map<Long, List<Domanda>> aree = domandePerCategoriaEArea.getOrDefault(categoriaId, Collections.emptyMap());
                List<Domanda> domandeCategoria = new ArrayList<>();

                for (Map.Entry<Long, List<Domanda>> areaEntry : aree.entrySet()) {
                    Long areaId = areaEntry.getKey();
                    List<Domanda> domandeArea = areaEntry.getValue();

                    List<Domanda> candidateLivelloCorretto = domandeArea.stream()
                            .filter(d -> d.getCompetenza() != null)
                            .filter(d -> String.valueOf(nuovoLivello).equals(d.getCompetenza().getLivello()))
                            .filter(d -> !domandeUsateIds.contains(d.getId()))
                            .collect(Collectors.toList());

                    Collections.shuffle(candidateLivelloCorretto);
                    if (!candidateLivelloCorretto.isEmpty()) {
                        Domanda scelta = candidateLivelloCorretto.get(0);
                        domandeCategoria.add(scelta);
                        domandeUsateIds.add(scelta.getId());
                        LOGGER.info("Aggiunta domanda livello corretto ID " + scelta.getId() + " categoria " + categoriaId + " area_competenza_id " + areaId);
                    }
                }

                if (domandeCategoria.size() < numRichiesto) {
                    List<Domanda> fallbackLivelloDiverso = tutteDomandeVisibili.stream()
                            .filter(d -> d.getCategoria() != null && d.getCategoria().getId().equals(categoriaId))
                            .filter(d -> d.getCompetenza() != null)
                            .filter(d -> !String.valueOf(nuovoLivello).equals(d.getCompetenza().getLivello()))
                            .filter(d -> !domandeUsateIds.contains(d.getId()))
                            .collect(Collectors.toList());

                    Collections.shuffle(fallbackLivelloDiverso);
                    for (Domanda d : fallbackLivelloDiverso) {
                        if (d.getCategoria() == null) {
                            continue;
                        }
                        if (domandeCategoria.size() >= numRichiesto) {
                            break;
                        }
                        domandeCategoria.add(d);
                        domandeUsateIds.add(d.getId());
                        LOGGER.info("Aggiunta domanda fallback livello diverso ID " + d.getId() + " categoria " + categoriaId);
                    }
                }

                if (domandeCategoria.size() < numRichiesto) {
                    List<Domanda> fallbackSenzaCompetenza = tutteDomandeVisibili.stream()
                            .filter(d -> d.getCategoria() != null && d.getCategoria().getId().equals(categoriaId))
                            .filter(d -> d.getCompetenza() == null)
                            .filter(d -> !domandeUsateIds.contains(d.getId()))
                            .collect(Collectors.toList());

                    Collections.shuffle(fallbackSenzaCompetenza);
                    for (Domanda d : fallbackSenzaCompetenza) {
                        if (domandeCategoria.size() >= numRichiesto) {
                            break;
                        }
                        domandeCategoria.add(d);
                        domandeUsateIds.add(d.getId());
                        LOGGER.info("Aggiunta domanda fallback senza competenza ID " + d.getId() + " categoria " + categoriaId);
                    }
                }

                domandeSelezionate.addAll(domandeCategoria);
            }

            if (domandeSelezionate.size() < 21) {
                List<Domanda> extra = tutteDomandeVisibili.stream()
                        .filter(d -> !domandeUsateIds.contains(d.getId()))
                        .collect(Collectors.toList());

                Collections.shuffle(extra);
                for (Domanda d : extra) {
                    if (domandeSelezionate.size() >= 21) {
                        break;
                    }
                    domandeSelezionate.add(d);
                    domandeUsateIds.add(d.getId());
                    LOGGER.info("Aggiunta domanda extra per raggiungere 21 ID " + d.getId());
                }
            }

            LOGGER.info("Selezione domande completata, totale domande selezionate: " + domandeSelezionate.size());
            return domandeSelezionate;

        } catch (Exception e) {
            LOGGER.error("Errore nella selezione domande: " + estraiEccezione(e));
            return Collections.emptyList();
        }
    }

    private Map<Long, Integer> getDistribuzioneDomandePerAreaCompetenza() {
        return Map.of(
                1L, 3,
                2L, 6,
                3L, 4,
                4L, 4,
                5L, 4
        );
    }

    private List<Questionario> getQuestionariCompletatiByUtente(List<Utente> utenti, Stato_questionario stato) {
        EntityManager em2 = this.getEm();

        try {
            TypedQuery<Questionario> query = em2.createQuery(
                    "SELECT q FROM Questionario q JOIN q.utenti u JOIN q.digicomp_questionario dq WHERE u.id IN :utentiIds AND q.descrizione = :stato ORDER BY dq.id",
                    Questionario.class
            );
            query.setParameter("utentiIds", utenti.stream().map(Utente::getId).collect(Collectors.toList()));
            query.setParameter("stato", stato);

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la selezione dei questionari completati degli utenti " + utenti + "\n" + estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return Collections.emptyList();
    }

    private List<Domanda> getDomandeVisibili() {
        EntityManager em2 = this.getEm();

        try {
            TypedQuery<Domanda> query = em2.createQuery(
                    "SELECT d FROM Domanda d WHERE d.categoria IS NOT NULL AND d.visibilità_domanda = :visibilità", Domanda.class
            );
            query.setParameter("visibilità", Visibilità_domanda.VISIBILE);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error("Non è stato possibile effettuare la selezione delle domande visibili" + "\n" + estraiEccezione(e));
        } finally {
            if (em2 != null) {
                em2.close();
            }
        }
        return Collections.emptyList();
    }

    private Map<Integer, Integer> getDistribuzioneDomande() {
        return Map.of(
                1, 3,
                2, 6,
                3, 4,
                4, 4,
                5, 4
        );
    }

    public void createExcel(Questionario ultimoQuestionario, HttpServletResponse response) {
        EntityManager em = this.getEm();
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

            Utils utils = new Utils();
            utils.generaExcel(categoriaSottocategoriaStats, response);

        } catch (Exception e) {
            LOGGER.error("Errore nella generazione del file Excel: " + estraiEccezione(e));
        } finally {
            if (em != null) {
                em.close();

            }
        }
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
        EntityManager em = this.getEm();

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
        EntityManager em = this.getEm();

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
        EntityManager em2 = this.getEm();

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

    public long countDomande(String area, String area_competenza, String competenza, String stato, Logger logger) {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();
        long totalRecords = 0;

        try {

            StringBuilder query = new StringBuilder(
                    "SELECT COUNT(d) FROM Domanda d WHERE 1=1");

            if (area != null && !area.equals("Tutti")) {
                query.append(" AND d.categoria.id = :area");
            }

            if (area_competenza != null && !area_competenza.equals("Tutti")) {
                query.append(" AND d.competenza.areeCompetenze.id = :area_competenza");
            }

            if (competenza != null && !competenza.equals("Tutti")) {
                query.append(" AND d.competenza.id = :competenza");
            }

            if (stato != null && !stato.equals("Tutti")) {
                query.append(" AND d.visibilità_domanda = :stato");
            }

            Query countQuery = em.createQuery(query.toString());

            if (area != null && !area.equals("Tutti")) {
                Long areaId = Utils.tryParseLong(area);
                countQuery.setParameter("area", areaId);
            }

            if (area_competenza != null && !area_competenza.equals("Tutti")) {
                Long areaCompetenzaId = Utils.tryParseLong(area_competenza);
                countQuery.setParameter("area_competenza", areaCompetenzaId);
            }

            if (competenza != null && !competenza.equals("Tutti")) {
                Long competenzaId = Utils.tryParseLong(competenza);
                countQuery.setParameter("competenza", competenzaId);
            }

            if (stato != null && !stato.equals("Tutti")) {
                if (stato.equals(Visibilità_domanda.VISIBILE.name())) {
                    countQuery.setParameter("stato", Visibilità_domanda.VISIBILE);
                } else {
                    countQuery.setParameter("stato", Visibilità_domanda.NON_VISIBILE);
                }
            }

            List<Long> countList = countQuery.getResultList();
            if (!countList.isEmpty()) {
                totalRecords = countList.get(0);
            } else {
                return 0;
            }

        } catch (Exception e) {
            logger.error("Non è stato possibile effettuare il conteggio dei questionari (ADMIN)" + "\n" + Utils.estraiEccezione(e));
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return totalRecords;
    }

    public List<Domanda> RicercaDomande(int start, int pageSize, String area, String area_competenza, String competenza, String stato, Logger logger) {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();
        List<Domanda> resultList = new ArrayList<>();

        try {
            em.getTransaction().begin();

            StringBuilder query = new StringBuilder("SELECT d FROM Domanda d WHERE 1=1");

            if (area != null && !area.equals("Tutti")) {
                query.append(" AND d.categoria.id = :area");
            }

            if (area_competenza != null && !area_competenza.equals("Tutti")) {
                query.append(" AND d.competenza.areeCompetenze.id = :area_competenza");
            }

            if (competenza != null && !competenza.equals("Tutti")) {
                query.append(" AND d.competenza.id = :competenza");
            }

            if (stato != null && !stato.equals("Tutti")) {
                query.append(" AND d.visibilità_domanda = :stato");
            }

            Query jpqlQuery = em.createQuery(query.toString(), Domanda.class)
                    .setFirstResult(start)
                    .setMaxResults(pageSize);

            if (area != null && !area.equals("Tutti")) {
                Long areaId = Utils.tryParseLong(area);
                jpqlQuery.setParameter("area", areaId);
            }

            if (area_competenza != null && !area_competenza.equals("Tutti")) {
                Long areaCompetenzaId = Utils.tryParseLong(area_competenza);
                jpqlQuery.setParameter("area_competenza", areaCompetenzaId);
            }

            if (competenza != null && !competenza.equals("Tutti")) {
                Long competenzaId = Utils.tryParseLong(competenza);
                jpqlQuery.setParameter("competenza", competenzaId);
            }

            if (stato != null && !stato.equals("Tutti")) {
                if (stato.equals(Visibilità_domanda.VISIBILE.name())) {
                    jpqlQuery.setParameter("stato", Visibilità_domanda.VISIBILE);
                } else {
                    jpqlQuery.setParameter("stato", Visibilità_domanda.NON_VISIBILE);
                }
            }

            resultList = jpqlQuery.getResultList();

            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Non è stato possibile effettuare la ricerca delle domande (GESTIONE DOMANDE)" + "\n" + Utils.estraiEccezione(e));
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return resultList;
    }

    public void creaDomanda(Categoria categoria, Competenza competenza, String stato, String titolo, String nome_domanda, String[] risposta_text, String[] si_no_select, Logger logger) {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();
        JSONObject jSONObject = new JSONObject();
        try {
            em.getTransaction().begin();
            Domanda domanda = new Domanda();

            if (categoria != null) {
                domanda.setCategoria(categoria);
            }
            if (competenza != null) {
                domanda.setCompetenza(competenza);
            }
            if (stato != null) {
                if (stato.equals(Visibilità_domanda.VISIBILE.name())) {
                    domanda.setVisibilità_domanda(Visibilità_domanda.VISIBILE);
                    jSONObject.put("visibilità_domanda", Visibilità_domanda.VISIBILE.name().toLowerCase());
                } else {
                    domanda.setVisibilità_domanda(Visibilità_domanda.NON_VISIBILE);
                    jSONObject.put("visibilità_domanda", Visibilità_domanda.NON_VISIBILE.name().toLowerCase());
                }
            }

            if (titolo != null) {
                domanda.setTitolo(titolo);
            }
            if (nome_domanda != null) {
                domanda.setNome_domanda(nome_domanda);
            }

            if (risposta_text != null && si_no_select != null) {
                JSONArray risposteArray = new JSONArray();
                List<String> corrette = new ArrayList<>();

                int minLength = Math.min(risposta_text.length, si_no_select.length);

                for (int i = 0; i < minLength; i++) {
                    String risposta = risposta_text[i];
                    String corretta = si_no_select[i];

                    if (risposta != null && !risposta.trim().isEmpty()) {
                        risposta = risposta.trim();

                        JSONObject rispostaJSON = new JSONObject();
                        rispostaJSON.put("id", i + 1);
                        rispostaJSON.put("testo", risposta);
                        rispostaJSON.put("corretta", corretta != null && corretta.equals(Si_no.SI.name()));

                        risposteArray.put(rispostaJSON);

                        if (rispostaJSON.getBoolean("corretta")) {
                            corrette.add(String.valueOf(i + 1));
                        }
                    }
                }

                jSONObject.put("risposte", risposteArray);
                jSONObject.put("risposte_corrette", corrette);
            }

            jSONObject.put("descrizione", "domanda_scelta_multipla");
            jSONObject.put("tipo_domanda", Tipo_domanda.DOMANDA_SCELTA_MULTIPLA.name().toLowerCase());
            jSONObject.put("tipo_inserimento", Tipo_inserimento.AUTOMATICO.name().toLowerCase());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String stringDateFormatted = sdf.format(date);
            jSONObject.put("data_creazione", stringDateFormatted);

            domanda.setDescrizione("domanda_scelta_multipla");
            domanda.setTipo_domanda(Tipo_domanda.DOMANDA_SCELTA_MULTIPLA);
            domanda.setData_creazione(new Date());
            domanda.setTipo_inserimento(Tipo_inserimento.AUTOMATICO);
            domanda.setRisposte(jSONObject.toString());

            em.persist(domanda);
            em.getTransaction().commit();

            logger.info("Domanda creata con successo! " + sdf.format(new Date()));

        } catch (Exception e) {
            logger.error("Non è stato possibile creare la nuova domanda" + "\n" + Utils.estraiEccezione(e));
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void creaUtente(String nome, String cognome, String email, String username, String password, int età, String indirizzo, int ruolo_int, Logger logger) {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();

        try {
            em.getTransaction().begin();
            Utente utente = new Utente();

            if (nome != null) {
                utente.setNome(nome);
            }
            if (cognome != null) {
                utente.setCognome(cognome);
            }
            if (email != null) {
                utente.setEmail(email);
            }

            if (username != null) {
                utente.setUsername(username);
            }
            if (età != 0) {
                utente.setEtà(età);
            }

            if (indirizzo != null) {
                utente.setIndirizzo(indirizzo);
            }

            if (ruolo_int != 0) {
                Ruolo ruolo = findRuoloById(ruolo_int);
                utente.setRuolo(ruolo);
            }
            if (password != null) {
                utente.setPassword(password);
            }
            utente.setStato_utente(Stato_utente.ATTIVO);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            em.persist(utente);
            em.getTransaction().commit();

            logger.info("Utenza creata con successo! " + sdf.format(new Date()));

        } catch (Exception e) {
            logger.error("Non è stato possibile creare una nuova utenza" + "\n" + Utils.estraiEccezione(e));
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void modificaUtente(Long userId, String nome, String cognome, String email, String username, String password, String stato_utente, int età, String indirizzo, int ruolo_int, Logger logger) {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();

        try {
            Utente vecchioUtente = em.find(Utente.class, userId);
            if (vecchioUtente == null) {
                logger.error("Utenza con id " + userId + " non trovata.");
                return;
            }

            em.getTransaction().begin();

            if (nome != null) {
                vecchioUtente.setNome(nome);
            }
            if (cognome != null) {
                vecchioUtente.setCognome(cognome);
            }

            if (email != null) {
                vecchioUtente.setEmail(email);
            }

            if (username != null) {
                vecchioUtente.setUsername(username);
            }
            if (età != 0) {
                vecchioUtente.setEtà(età);
            }

            if (indirizzo != null) {
                vecchioUtente.setIndirizzo(indirizzo);
            }

            if (ruolo_int != 0) {
                Ruolo ruolo = findRuoloById(ruolo_int);
                vecchioUtente.setRuolo(ruolo);
            }

            if (stato_utente != null) {
                if (stato_utente.equalsIgnoreCase(Stato_utente.ATTIVO.toString())) {
                    vecchioUtente.setStato_utente(Stato_utente.ATTIVO);
                } else {
                    vecchioUtente.setStato_utente(Stato_utente.NON_ATTIVO);
                }
            }

            if (password != null) {
                vecchioUtente.setPassword(password);
            }

            em.merge(vecchioUtente);
            em.getTransaction().commit();

            logger.info("Utenza aggiornata con successo! id: " + userId);
        } catch (Exception e) {
            logger.error("Errore nell'aggiornamento dell'utenza con id " + userId + "\n" + Utils.estraiEccezione(e));
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void modificaDomanda(Long domanda_id, Categoria categoria, Competenza competenza, String stato,
            String titolo, String nome_domanda, String[] risposta_text, String[] idRisposte,
            String[] si_no_select, Logger logger) {
        JPAUtil jPAUtil = new JPAUtil();
        EntityManager em = jPAUtil.getEm();

        try {
            Domanda vecchiaDomanda = em.find(Domanda.class, domanda_id);
            if (vecchiaDomanda == null) {
                logger.error("Domanda con ID " + domanda_id + " non trovata.");
                return;
            }

            em.getTransaction().begin();

            if (categoria != null) {
                vecchiaDomanda.setCategoria(categoria);
            }
            if (competenza != null) {
                vecchiaDomanda.setCompetenza(competenza);
            }

            if (stato != null) {
                if (stato.equals(Visibilità_domanda.VISIBILE.name())) {
                    vecchiaDomanda.setVisibilità_domanda(Visibilità_domanda.VISIBILE);
                } else {
                    vecchiaDomanda.setVisibilità_domanda(Visibilità_domanda.NON_VISIBILE);
                }
            }

            if (titolo != null) {
                vecchiaDomanda.setTitolo(titolo);
            }
            if (nome_domanda != null) {
                vecchiaDomanda.setNome_domanda(nome_domanda);
            }

            if (risposta_text != null && idRisposte != null && si_no_select != null) {

                JSONArray nuoveRisposteArray = new JSONArray();
                List<String> corrette = new ArrayList<>();

                if (vecchiaDomanda.getOpzioni() != null && vecchiaDomanda.getRisposte() == null) {
                    int nextId = 1;

                    for (int i = 0; i < risposta_text.length; i++) {
                        String testoRisposta = risposta_text[i];
                        String selezione = si_no_select[i];

                        if (testoRisposta != null && !testoRisposta.trim().isEmpty()) {
                            JSONObject rispostaJSON = new JSONObject();
                            rispostaJSON.put("id", nextId++);
                            rispostaJSON.put("testo", testoRisposta.trim());
                            rispostaJSON.put("corretta", "SI".equalsIgnoreCase(selezione));

                            nuoveRisposteArray.put(rispostaJSON);

                            if (rispostaJSON.getBoolean("corretta")) {
                                corrette.add(String.valueOf(rispostaJSON.getInt("id")));
                            }
                        }
                    }

                } else if (vecchiaDomanda.getRisposte() != null) {
                    JSONObject risposteJsonPrecedente = new JSONObject(vecchiaDomanda.getRisposte());
                    JSONArray rispostePrecedenti = risposteJsonPrecedente.optJSONArray("risposte");
                    Map<Integer, JSONObject> risposteEsistenti = new HashMap<>();

                    if (rispostePrecedenti != null) {
                        for (int i = 0; i < rispostePrecedenti.length(); i++) {
                            JSONObject r = rispostePrecedenti.getJSONObject(i);
                            risposteEsistenti.put(r.getInt("id"), r);
                        }
                    }

                    int nextId = risposteEsistenti.keySet().stream().max(Integer::compareTo).orElse(0) + 1;

                    for (int i = 0; i < risposta_text.length; i++) {
                        String testoRisposta = risposta_text[i];
                        String selezione = si_no_select[i];
                        int idRisposta;

                        try {
                            idRisposta = Integer.parseInt(idRisposte[i]);
                        } catch (NumberFormatException e) {
                            idRisposta = 0;
                        }

                        if (testoRisposta != null && !testoRisposta.trim().isEmpty()) {
                            JSONObject rispostaJSON = (idRisposta > 0 && risposteEsistenti.containsKey(idRisposta))
                                    ? risposteEsistenti.get(idRisposta)
                                    : new JSONObject();

                            if (idRisposta <= 0) {
                                idRisposta = nextId++;
                            }

                            rispostaJSON.put("id", idRisposta);
                            rispostaJSON.put("testo", testoRisposta.trim());
                            rispostaJSON.put("corretta", "SI".equalsIgnoreCase(selezione));

                            nuoveRisposteArray.put(rispostaJSON);

                            if (rispostaJSON.getBoolean("corretta")) {
                                corrette.add(String.valueOf(idRisposta));
                            }
                        }
                    }
                }

                JSONObject nuovoJson = new JSONObject();
                nuovoJson.put("descrizione", "domanda_scelta_multipla");
                nuovoJson.put("tipo_domanda", Tipo_domanda.DOMANDA_SCELTA_MULTIPLA.name().toLowerCase());
                nuovoJson.put("tipo_inserimento", Tipo_inserimento.AUTOMATICO.name().toLowerCase());
                nuovoJson.put("risposte_corrette", corrette);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String dataFormattata = sdf.format(new Date());
                nuovoJson.put("data_modifica", dataFormattata);
                nuovoJson.put("risposte", nuoveRisposteArray);

                vecchiaDomanda.setDescrizione("domanda_scelta_multipla");
                vecchiaDomanda.setTipo_domanda(Tipo_domanda.DOMANDA_SCELTA_MULTIPLA);
                vecchiaDomanda.setData_modifica(new Date());
                vecchiaDomanda.setTipo_inserimento(Tipo_inserimento.AUTOMATICO);
                vecchiaDomanda.setRisposte(nuovoJson.toString());
            }

            em.merge(vecchiaDomanda);
            em.getTransaction().commit();

            logger.info("Domanda aggiornata con successo! ID: " + domanda_id);
        } catch (Exception e) {
            logger.error("Errore nell'aggiornamento della domanda con ID " + domanda_id + "\n" + Utils.estraiEccezione(e));
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

}
