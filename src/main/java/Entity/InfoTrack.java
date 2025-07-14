/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 *
 * @author Salvatore
 */
@Entity
@Table(name = "info_track")
public class InfoTrack {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "azione")
    private String azione;

    @Column(name = "entità")
    private String entita;

    @Column(name = "response_status")
    private Integer response_status;

    @Column(name = "descrizione", columnDefinition = "longtext")
    @Lob
    private String descrizione;

    @Column(name = "utente")
    private String dettagli_utente;

    @Column(name = "dettagli_errore", columnDefinition = "longtext")
    @Lob
    private String dettagli_errore;

    @Column(name = "data_azione")
    private String timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAzione() {
        return azione;
    }

    public void setAzione(String azione) {
        this.azione = azione;
    }

    public String getEntita() {
        return entita;
    }

    public void setEntita(String entita) {
        this.entita = entita;
    }

    public Integer getResponse_status() {
        return response_status;
    }

    public void setResponse_status(Integer response_status) {
        this.response_status = response_status;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDettagli_utente() {
        return dettagli_utente;
    }

    public void setDettagli_utente(String dettagli_utente) {
        this.dettagli_utente = dettagli_utente;
    }

    public String getDettagli_errore() {
        return dettagli_errore;
    }

    public void setDettagli_errore(String dettagli_errore) {
        this.dettagli_errore = dettagli_errore;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public InfoTrack(String azione, String entita, Integer response_status, String descrizione, String dettagli_utente, String dettagli_errore, String timestamp) {
        this.azione = azione;
        this.entita = entita;
        this.response_status = response_status;
        this.descrizione = descrizione;
        this.dettagli_utente = dettagli_utente;
        this.dettagli_errore = dettagli_errore;
        this.timestamp = timestamp;
    }

}
