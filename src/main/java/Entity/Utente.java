/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

import Enum.Disponibilità_utente;
import Enum.Stato_utente;
import jakarta.persistence.Table;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import java.util.List;

/**
 *
 * @author Salvatore
 */
@Entity
@NamedQuery(name = "getUtenteUSPASS", query = "SELECT u FROM Utente u WHERE u.username = :username")
@Table(name = "utente")
public class Utente implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String cognome;

    private String email;

    private int età;

    private String indirizzo;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Stato_utente stato_utente;

    @Enumerated(EnumType.STRING)
    private Disponibilità_utente disponibilità_utente;

    @ManyToOne
    private Ruolo ruolo;

    @ManyToMany(mappedBy = "utenti")
    private List<Questionario> questionari;

    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getEtà() {
        return età;
    }

    public void setEtà(int età) {
        this.età = età;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public List<Questionario> getQuestionari() {
        return questionari;
    }

    public void setQuestionari(List<Questionario> questionari) {
        this.questionari = questionari;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Stato_utente getStato_utente() {
        return stato_utente;
    }

    public void setStato_utente(Stato_utente stato_utente) {
        this.stato_utente = stato_utente;
    }

    public Disponibilità_utente getDisponibilità_utente() {
        return disponibilità_utente;
    }

    public void setDisponibilità_utente(Disponibilità_utente disponibilità_utente) {
        this.disponibilità_utente = disponibilità_utente;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Utente)) {
            return false;
        }
        Utente other = (Utente) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entity.Utente[ id=" + id + " ]";
    }

}
