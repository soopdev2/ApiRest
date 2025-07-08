/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entity;

import Enum.Assegnabile_enum;
import jakarta.persistence.CascadeType;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

/**
 *
 * @author Salvatore
 */
@Entity
@Table(name = "digicomp")
public class Digicomp implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descrizione;

    @Enumerated(EnumType.STRING)
    private Assegnabile_enum assegnabile_enum;

    @OneToMany(mappedBy = "digicomp", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DigicompDomanda> domande_digicomp;
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public List<DigicompDomanda> getDomande_digicomp() {
        return domande_digicomp;
    }

    public void setDomande_digicomp(List<DigicompDomanda> domande_digicomp) {
        this.domande_digicomp = domande_digicomp;
    }


    public Assegnabile_enum getAssegnabile_enum() {
        return assegnabile_enum;
    }

    public void setAssegnabile_enum(Assegnabile_enum assegnabile_enum) {
        this.assegnabile_enum = assegnabile_enum;
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
        if (!(object instanceof Digicomp)) {
            return false;
        }
        Digicomp other = (Digicomp) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entity.Digicomp[ id=" + id + " ]";
    }

}
