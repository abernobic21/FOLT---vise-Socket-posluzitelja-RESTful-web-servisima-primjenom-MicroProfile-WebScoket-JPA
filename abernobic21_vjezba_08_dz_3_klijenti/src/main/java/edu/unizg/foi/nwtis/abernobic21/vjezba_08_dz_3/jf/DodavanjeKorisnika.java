import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Named("dodavanjeKorisnika")
@RequestScoped
public class DodavanjeKorisnika {

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartnerKlijent;

  private String statusPoruka;

  private Korisnik korisnik;

  @PostConstruct
  public void init() {
    korisnik = new Korisnik("", "", "", "", "");
  }

  public String getStatusPoruka() {
    return statusPoruka;
  }

  public String getIme() {
    return korisnik.ime();
  }

  public void setIme(String ime) {
    korisnik = new Korisnik(korisnik.korisnik(), korisnik.lozinka(), korisnik.prezime(), ime,
        korisnik.email());
  }

  public String getPrezime() {
    return korisnik.prezime();
  }

  public void setPrezime(String prezime) {
    korisnik = new Korisnik(korisnik.korisnik(), korisnik.lozinka(), prezime, korisnik.ime(),
        korisnik.email());
  }

  public String getEmail() {
    return korisnik.email();
  }

  public void setEmail(String email) {
    korisnik = new Korisnik(korisnik.korisnik(), korisnik.lozinka(), korisnik.prezime(),
        korisnik.ime(), email);
  }

  public String getKorisnickoIme() {
    return korisnik.korisnik();
  }

  public void setKorisnickoIme(String korisnickoIme) {
    korisnik = new Korisnik(korisnickoIme, korisnik.lozinka(), korisnik.prezime(), korisnik.ime(),
        korisnik.email());
  }

  public String getLozinka() {
    return korisnik.lozinka();
  }

  public void setLozinka(String lozinka) {
    korisnik = new Korisnik(korisnik.korisnik(), lozinka, korisnik.prezime(), korisnik.ime(),
        korisnik.email());
  }

  public void dodajKorisnika() {
    System.out.println("tusmo");

    try {
      Response odgovor = servisPartnerKlijent.postKorisnik(korisnik);

      System.out.println(odgovor);

      if (odgovor.getStatus() == 201) {
        statusPoruka = "Korisnik uspješno dodan.";
        korisnik = new Korisnik("", "", "", "", "");
      } else {
        statusPoruka = "Greška pri dodavanju korisnika.";
      }
    } catch (WebApplicationException ex) {
      int status = ex.getResponse().getStatus();
      if (status == 409) {
        statusPoruka = "Korisnik već postoji.";
      } else if (status == 500) {
        statusPoruka = "Greška na poslužitelju.";
      } else {
        statusPoruka = "Greška (status " + status + ").";
      }
    } catch (Exception e) {
      statusPoruka = "Neočekivana greška: " + e.getMessage();
    }
  }
}
