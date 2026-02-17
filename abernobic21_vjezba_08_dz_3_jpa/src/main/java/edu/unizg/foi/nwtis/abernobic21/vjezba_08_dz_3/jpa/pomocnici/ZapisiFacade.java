package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici;


import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 *
 * @author Antonio Bernobić
 */
@Stateless
public class ZapisiFacade extends EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = 1L;

  public void create(Zapisi zapis) {
    getEntityManager().persist(zapis);
  }

  public void edit(Zapisi zapis) {
    getEntityManager().merge(zapis);
  }

  public void remove(Zapisi zapis) {
    getEntityManager().remove(getEntityManager().merge(zapis));
  }

  public Zapisi find(int id) {
    return getEntityManager().find(Zapisi.class, id);
  }

  public List<Zapisi> findAll() {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
    cq.select(cq.from(Zapisi.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

  public List<Zapisi> findAll(String korisnik, Timestamp vrijemeOd, Timestamp vrijemeDo) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
    Root<Zapisi> zapisi = cq.from(Zapisi.class);

    List<Predicate> uvjeti = new ArrayList<>();

    if (korisnik != null && !korisnik.isEmpty()) {
      uvjeti.add(cb.equal(zapisi.get("korisnickoime"), korisnik));
    }

    if (vrijemeOd != null) {
      uvjeti.add(cb.greaterThanOrEqualTo(zapisi.get("vrijeme"), vrijemeOd));
    }

    if (vrijemeDo != null) {
      uvjeti.add(cb.lessThanOrEqualTo(zapisi.get("vrijeme"), vrijemeDo));
    }

    if (!uvjeti.isEmpty()) {
      cq.where(cb.and(uvjeti.toArray(new Predicate[0])));
    }

    return getEntityManager().createQuery(cq).getResultList();
  }


}
