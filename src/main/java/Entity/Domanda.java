/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

import Enum.Tipo_domanda;
import Enum.Tipo_inserimento;
import Enum.Visibilità_domanda;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Salvatore
 */
@Entity
@Table(name = "domanda")
public class Domanda implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "modelloPredefinito")
    private ModelloPredefinito modelloPredefinito;

    @Column(name = "titolo", columnDefinition = "longtext")
    @Lob
    private String titolo;

    @Column(name = "nome_domanda", columnDefinition = "longtext")
    @Lob
    private String nome_domanda;

    @Column(name = "descrizione", columnDefinition = "longtext")
    @Lob
    private String descrizione;

    @Enumerated(EnumType.STRING)
    private Tipo_domanda tipo_domanda;

    @Column(name = "opzioni", columnDefinition = "longtext")
    @Lob
    private String opzioni;

    @Enumerated(EnumType.STRING)
    private Visibilità_domanda visibilità_domanda;

    @ManyToOne
    @JoinColumn(name = "categoria_id", referencedColumnName = "id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "competenza_id", referencedColumnName = "id")
    private Competenza competenza;

    @ManyToMany(mappedBy = "domande")
    private List<Questionario> questionari;

    @Column(name = "tipo_inserimento")
    @Enumerated(EnumType.STRING)
    private Tipo_inserimento tipo_inserimento;

    @Column(name = "risposte_json", columnDefinition = "longtext")
    @Lob
    private String risposte;

    @Column(name = "timestamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data_creazione;

    @Column(name = "timestamp_modifica", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data_modifica;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Tipo_domanda getTipo_domanda() {
        return tipo_domanda;
    }

    public void setTipo_domanda(Tipo_domanda tipo_domanda) {
        this.tipo_domanda = tipo_domanda;
    }

    public ModelloPredefinito getQuestionario() {
        return modelloPredefinito;
    }

    public void setQuestionario(ModelloPredefinito modelloPredefinito) {
        this.modelloPredefinito = modelloPredefinito;
    }

    public String getOpzioni() {
        return opzioni;
    }

    public void setOpzioni(String opzioni) {
        this.opzioni = opzioni;
    }

    public Visibilità_domanda getVisibilità_domanda() {
        return visibilità_domanda;
    }

    public void setVisibilità_domanda(Visibilità_domanda visibilità_domanda) {
        this.visibilità_domanda = visibilità_domanda;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Competenza getCompetenza() {
        return competenza;
    }

    public void setCompetenza(Competenza competenza) {
        this.competenza = competenza;
    }

    public ModelloPredefinito getModelloPredefinito() {
        return modelloPredefinito;
    }

    public void setModelloPredefinito(ModelloPredefinito modelloPredefinito) {
        this.modelloPredefinito = modelloPredefinito;
    }

    public List<Questionario> getQuestionari() {
        return questionari;
    }

    public void setQuestionari(List<Questionario> questionari) {
        this.questionari = questionari;
    }

    public String getNome_domanda() {
        return nome_domanda;
    }

    public void setNome_domanda(String nome_domanda) {
        this.nome_domanda = nome_domanda;
    }

    public Date getData_creazione() {
        return data_creazione;
    }

    public void setData_creazione(Date data_creazione) {
        this.data_creazione = data_creazione;
    }

    public Date getData_modifica() {
        return data_modifica;
    }

    public void setData_modifica(Date data_modifica) {
        this.data_modifica = data_modifica;
    }

    public Tipo_inserimento getTipo_inserimento() {
        return tipo_inserimento;
    }

    public void setTipo_inserimento(Tipo_inserimento tipo_inserimento) {
        this.tipo_inserimento = tipo_inserimento;
    }

    public String getRisposte() {
        return risposte;
    }

    public void setRisposte(String risposte) {
        this.risposte = risposte;
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
        if (!(object instanceof Domanda)) {
            return false;
        }
        Domanda other = (Domanda) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entity.Domanda[ id=" + id + " ]";
    }

}
