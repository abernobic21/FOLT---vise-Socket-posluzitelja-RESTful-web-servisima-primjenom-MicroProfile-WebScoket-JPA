package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("pregledRada")
@RequestScoped
public class PregledRada implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  ZapisiFacade zapisiFacade;

  @Inject
  KorisniciFacade korisniciFacade;

  private List<Korisnik> korisnici = new ArrayList<>();

  private String odabraniKorisnik;

  private List<Zapisi> zapisi = new ArrayList<>();

  private Date vrijemeOd = null;
  private Date vrijemeDo = null;

  public List<Korisnik> getKorisnici() {
    return korisnici;
  }

  public void setKorisnici(List<Korisnik> korisnici) {
    this.korisnici = korisnici;
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

  public List<Zapisi> getZapisi() {
    return zapisi;
  }

  public void setZapisi(List<Zapisi> zapisi) {
    this.zapisi = zapisi;
  }

  public String getOdabraniKorisnik() {
    return odabraniKorisnik;
  }

  public void setOdabraniKorisnik(String odabraniKorisnik) {
    this.odabraniKorisnik = odabraniKorisnik;
  }


  @PostConstruct
  public void ucitajKorisnike() {
    this.korisnici = korisniciFacade.pretvori(korisniciFacade.findAll());
  }

  public void dohvatiZapise() {

    Timestamp tsVrijemeOd = vrijemeOd == null ? null : new Timestamp(vrijemeOd.getTime());
    Timestamp tsVrijemeDo = vrijemeDo == null ? null : new Timestamp(vrijemeDo.getTime());

    this.zapisi = zapisiFacade.findAll(odabraniKorisnik, tsVrijemeOd, tsVrijemeDo);
    System.out.println(zapisi);

  }

}
