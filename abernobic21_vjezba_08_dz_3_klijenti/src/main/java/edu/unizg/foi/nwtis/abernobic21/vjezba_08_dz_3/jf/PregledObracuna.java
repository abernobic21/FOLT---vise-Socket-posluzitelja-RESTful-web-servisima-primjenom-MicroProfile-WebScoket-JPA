package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.ObracuniFacade;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("pregledObracuna")
@RequestScoped
public class PregledObracuna implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  ObracuniFacade obracuniFacade;

  @Inject
  PartneriFacade partneriFacade;

  private List<Obracun> obracuni = new ArrayList<>();

  private int odabraniPartner;

  private List<Partner> partneri = new ArrayList<>();

  private Date vrijemeOd = null;
  private Date vrijemeDo = null;

  public List<Obracun> getObracuni() {
    return obracuni;
  }

  public List<Partner> getPartneri() {
    return partneri;
  }

  public int getOdabraniPartner() {
    return odabraniPartner;
  }

  public void setOdabraniPartner(int partner) {
    this.odabraniPartner = partner;
  }

  public Date getVrijemeOd() {
    return vrijemeOd;
  }

  public void setVrijemeOd(Date vrijemeOd) {
    this.vrijemeOd = vrijemeOd;
  }

  public Date getVrijemeDo() {
    return vrijemeDo;
  }

  public void setVrijemeDo(Date vrijemeDo) {
    this.vrijemeDo = vrijemeDo;
  }

  @PostConstruct
  public void ucitajPartnere() {
    this.partneri = partneriFacade.findAll(true);
  }

  public void dohvatiObracune() {

    Timestamp tsVrijemeOd = vrijemeOd == null ? null : new Timestamp(vrijemeOd.getTime());
    Timestamp tsVrijemeDo = vrijemeDo == null ? null : new Timestamp(vrijemeDo.getTime());

    this.obracuni =
        obracuniFacade.pretvori(obracuniFacade.findAll(odabraniPartner, tsVrijemeOd, tsVrijemeDo));
  }

}
