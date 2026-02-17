package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

@Named("pregledKorisnikaRest")
@RequestScoped
public class PregledKorisnikaRest implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartnerKlijent;

  private String statusPoruka;

  public String getStatusPoruka() {
    return statusPoruka;
  }

  private List<Korisnik> korisnici;


  public List<Korisnik> getKorisnici() {
    return korisnici;
  }

  public void setKorisnici(List<Korisnik> korisnici) {
    this.korisnici = korisnici;
  }

  @PostConstruct
  public void ucitajKorisnike() {
    try {
      Response odgovor = servisPartnerKlijent.getKorisnik();

      if (odgovor.getStatus() == 200) {
        korisnici = odgovor.readEntity(new GenericType<List<Korisnik>>() {});

      } else {
        statusPoruka = "Greška pri dohvaćanju korisnika.";
        korisnici = List.of();
      }
    } catch (Exception e) {
      statusPoruka = "Greška pri dohvaćanju korisnika.";
      korisnici = List.of();
    }
  }

  public String detalji(String korisnik) {


    return "detaljiKorisnika.xhtml?faces-redirect=true&korisnikId=" + korisnik;
  }
}
