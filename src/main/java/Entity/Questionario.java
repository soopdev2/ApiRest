/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

import Enum.Stato_questionario;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Salvatore
 */
@Entity
@Table(name = "questionario")
public class Questionario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String progressi;

    @Lob
    private String risposte;

    private LocalDateTime dataCompletamento;

    private String dataDiAssegnazione;

    private int status;

    @Enumerated(EnumType.STRING)
    @Column(name = "descrizione")
    private Stato_questionario descrizione;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "questionario_utenti",
            joinColumns = @JoinColumn(name = "questionario_id"),
            inverseJoinColumns = @JoinColumn(name = "utente_id")
    )
    private List<Utente> utenti;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "questionario_modelliPredefiniti",
            joinColumns = @JoinColumn(name = "questionario_id"),
            inverseJoinColumns = @JoinColumn(name = "modelliPredefiniti_id")
    )
    private List<ModelloPredefinito> modelliPredefiniti;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "questionario_categoria",
            joinColumns = @JoinColumn(name = "questionario_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categoria;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "questionario_domande",
            joinColumns = @JoinColumn(name = "questionario_id"),
            inverseJoinColumns = @JoinColumn(name = "domanda_id")
    )
    private List<Domanda> domande;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "questionario_digicomp",
            joinColumns = @JoinColumn(name = "questionario_id"),
            inverseJoinColumns = @JoinColumn(name = "digicomp_questionario")
    )
    private List<Digicomp> digicomp_questionario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProgressi() {
        return progressi;
    }

    public void setProgressi(String progressi) {
        this.progressi = progressi;
    }

    public String getRisposte() {
        return risposte;
    }

    public void setRisposte(String risposte) {
        this.risposte = risposte;
    }

    public LocalDateTime getDataCompletamento() {
        return dataCompletamento;
    }

    public void setDataCompletamento(LocalDateTime dataCompletamento) {
        this.dataCompletamento = dataCompletamento;
    }

    public String getDataDiAssegnazione() {
        return dataDiAssegnazione;
    }

    public void setDataDiAssegnazione(String dataDiAssegnazione) {
        this.dataDiAssegnazione = dataDiAssegnazione;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Stato_questionario getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(Stato_questionario descrizione) {
        this.descrizione = descrizione;
    }

    public List<Utente> getUtenti() {
        return utenti;
    }

    public void setUtenti(List<Utente> utenti) {
        this.utenti = utenti;
    }

    public List<ModelloPredefinito> getQuestionari() {
        return modelliPredefiniti;
    }

    public void setQuestionari(List<ModelloPredefinito> modelliPredefiniti) {
        this.modelliPredefiniti = modelliPredefiniti;
    }

    public List<Categoria> getCategoria() {
        return categoria;
    }

    public void setCategoria(List<Categoria> categoria) {
        this.categoria = categoria;
    }

    public List<Domanda> getDomande() {
        return domande;
    }

    public void setDomande(List<Domanda> domande) {
        this.domande = domande;
    }

    public List<ModelloPredefinito> getModelliPredefiniti() {
        return modelliPredefiniti;
    }

    public void setModelliPredefiniti(List<ModelloPredefinito> modelliPredefiniti) {
        this.modelliPredefiniti = modelliPredefiniti;
    }

    public List<Digicomp> getDigicomp_questionario() {
        return digicomp_questionario;
    }

    public void setDigicomp_questionario(List<Digicomp> digicomp_questionario) {
        this.digicomp_questionario = digicomp_questionario;
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
        if (!(object instanceof Questionario)) {
            return false;
        }
        Questionario other = (Questionario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entity.Utente_questionario[ id=" + id + " ]";
    }

}
