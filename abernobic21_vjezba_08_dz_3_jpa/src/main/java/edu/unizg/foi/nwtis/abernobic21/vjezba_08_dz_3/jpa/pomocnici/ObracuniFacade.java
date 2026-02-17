package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici;


import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.entiteti.Obracuni;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.entiteti.Obracuni_;
import edu.unizg.foi.nwtis.podaci.Obracun;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 *
 * @author Antonio Bernobić
 */
@Stateless
public class ObracuniFacade extends EntityManagerProducer implements Serializable {
  private static final long serialVersionUID = 3595041786540495885L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public List<Obracuni> findAll() {
    CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
    cq.select(cq.from(Obracuni.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

  public List<Obracuni> findAll(int partner, Timestamp vrijemeOd, Timestamp vrijemeDo) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
    Root<Obracuni> obracuni = cq.from(Obracuni.class);

    List<Predicate> uvjeti = new ArrayList<>();

    uvjeti.add(cb.equal(obracuni.get(Obracuni_.partneri).get("id"), partner));

    if (vrijemeOd != null) {
      uvjeti.add(cb.greaterThanOrEqualTo(obracuni.get(Obracuni_.vrijeme), vrijemeOd));
    }

    if (vrijemeDo != null) {
      uvjeti.add(cb.lessThanOrEqualTo(obracuni.get(Obracuni_.vrijeme), vrijemeDo));
    }

    cq.where(cb.and(uvjeti.toArray(new Predicate[0])));

    TypedQuery<Obracuni> q = getEntityManager().createQuery(cq);
    return q.getResultList();
  }


  public Obracun pretvori(Obracuni o) {
    if (o == null) {
      return null;
    }


    var oObjekt = new Obracun(o.getPartneri().getId(), o.getId(), o.getJelo(),
        (float) o.getKolicina(), (float) o.getCijena(), o.getVrijeme().getTime());

    return oObjekt;
  }

  public List<Obracun> pretvori(List<Obracuni> obracuniE) {
    List<Obracun> obracuni = new ArrayList<>();
    for (Obracuni oEntitet : obracuniE) {
      var oObjekt = pretvori(oEntitet);

      obracuni.add(oObjekt);
    }

    return obracuni;
  }
}
