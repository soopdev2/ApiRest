/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

import jakarta.persistence.Column;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 *
 * @author Salvatore
 */
@Entity
@Table(name = "competenza")
public class Competenza implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private SottoCategoria sottoCategoria;

    @Column(name = "descrizione", columnDefinition = "longtext")
    @Lob
    private String descrizione;

    private String livello;

    @ManyToOne
    private AreeCompetenze areeCompetenze;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SottoCategoria getSottoCategoria() {
        return sottoCategoria;
    }

    public void setSottoCategoria(SottoCategoria sottoCategoria) {
        this.sottoCategoria = sottoCategoria;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getLivello() {
        return livello;
    }

    public void setLivello(String livello) {
        this.livello = livello;
    }

    public AreeCompetenze getAreeCompetenze() {
        return areeCompetenze;
    }

    public void setAreeCompetenze(AreeCompetenze areeCompetenze) {
        this.areeCompetenze = areeCompetenze;
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
        if (!(object instanceof Competenza)) {
            return false;
        }
        Competenza other = (Competenza) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entity.Competenza[ id=" + id + " ]";
    }

}
