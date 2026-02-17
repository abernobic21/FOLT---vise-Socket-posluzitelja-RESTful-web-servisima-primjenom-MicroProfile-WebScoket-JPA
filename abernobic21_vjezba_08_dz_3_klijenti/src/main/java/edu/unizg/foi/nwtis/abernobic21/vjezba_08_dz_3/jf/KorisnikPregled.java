package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class KorisnikPregled implements Serializable {

  private static final long serialVersionUID = 1L;

  private String prezime = "";
  private String ime = "";

  private List<Korisnik> korisnici;

  @Inject
  private KorisniciFacade korisniciFacade;

  public void pretrazi() {
    if (prezime == null || prezime.isEmpty())
      prezime = "";
    if (ime == null || ime.isEmpty())
      ime = "";
    korisnici =
        korisniciFacade.pretvori(korisniciFacade.findAll("%" + prezime + "%", "%" + ime + "%"));
  }


  public String getPrezime() {
    return prezime;
  }

  public void setPrezime(String prezime) {
    this.prezime = prezime;
  }

  public String getIme() {
    return ime;
  }

  public void setIme(String ime) {
    this.ime = ime;
  }

  public List<Korisnik> getKorisnici() {
    return korisnici;
  }

  public void setKorisnici(List<Korisnik> korisnici) {
    this.korisnici = korisnici;
  }

}
