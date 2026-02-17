package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

/**
 *
 * @author Antonio Bernobić
 */
@Stateless
public class PartneriFacade extends EntityManagerProducer implements Serializable {


  private static final long serialVersionUID = 1L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public void create(Partneri partner) {
    getEntityManager().persist(partner);
  }

  public void edit(Partneri partner) {
    getEntityManager().merge(partner);
  }

  public void remove(Partneri partner) {
    getEntityManager().remove(getEntityManager().merge(partner));
  }

  public Partneri find(int id) {
    return getEntityManager().find(Partneri.class, id);
  }

  public List<Partneri> findAll() {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Partneri> cq = cb.createQuery(Partneri.class);
    cq.select(cq.from(Partneri.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

  public List<Partner> findAll(boolean skriveniPodaci) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Partneri> cq = cb.createQuery(Partneri.class);
    cq.select(cq.from(Partneri.class));
    return pretvori(getEntityManager().createQuery(cq).getResultList(), skriveniPodaci);
  }

  public Partner pretvori(Partneri p) {
    if (p == null) {
      return null;
    }
    var pObjekt = new Partner(p.getId(), p.getNaziv(), p.getVrstakuhinje(), p.getAdresa(),
        p.getMreznavrata(), p.getMreznavratakraj(), (float) p.getGpssirina(),
        (float) p.getGpsduzina(), p.getSigurnosnikod(), p.getAdminkod());

    return pObjekt;
  }

  public Partneri pretvori(Partner p) {
    if (p == null) {
      return null;
    }
    var pEntitet = new Partneri();
    pEntitet.setId(p.id());
    pEntitet.setAdminkod(p.adminKod());
    pEntitet.setAdresa(p.adresa());
    pEntitet.setGpsduzina(p.gpsDuzina());
    pEntitet.setGpssirina(p.gpsSirina());
    pEntitet.setMreznavrata(p.mreznaVrata());
    pEntitet.setMreznavratakraj(p.mreznaVrataKraj());
    pEntitet.setNaziv(p.naziv());
    pEntitet.setSigurnosnikod(p.sigurnosniKod());
    pEntitet.setVrstakuhinje(p.vrstaKuhinje());

    return pEntitet;
  }


  public Partner pretvori(Partneri p, boolean skriveniPodaci) {
    if (p == null) {
      return null;
    }

    return new Partner(p.getId(), p.getNaziv(), p.getVrstakuhinje(), p.getAdresa(),
        p.getMreznavrata(), p.getMreznavratakraj(), (float) p.getGpsduzina(),
        (float) p.getGpssirina(), skriveniPodaci ? "********" : p.getSigurnosnikod(),
        skriveniPodaci ? "********" : p.getAdminkod());
  }

  public List<Partner> pretvori(List<Partneri> partneriE, boolean skriveniPodaci) {
    List<Partner> partneri = new ArrayList<>();
    for (Partneri pEntitet : partneriE) {
      var kObjekt = pretvori(pEntitet, skriveniPodaci);

      partneri.add(kObjekt);
    }

    return partneri;
  }
}
