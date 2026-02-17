package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

@Named("detaljiKorisnika")
@RequestScoped
public class DetaljiKorisnika implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartnerKlijent;

  private String statusPoruka;

  public String getStatusPoruka() {
    return statusPoruka;
  }

  private Korisnik odabraniKorisnik;

  public Korisnik getOdabraniKorisnik() {
    return odabraniKorisnik;
  }

  public void setOdabraniKorisnik(Korisnik odabraniKorisnik) {
    this.odabraniKorisnik = odabraniKorisnik;
  }

  @PostConstruct
  public void init() {
    Map<String, String> parametri =
        FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    String korisnikIdParam = parametri.get("korisnikId");
    if (korisnikIdParam != null) {

      try {
        Response odgovor = servisPartnerKlijent.getKorisnik(korisnikIdParam);

        System.out.println(odgovor.getStatus());
        if (odgovor.getStatus() == 200) {
          odabraniKorisnik = odgovor.readEntity(new GenericType<Korisnik>() {});

          System.out.println(odabraniKorisnik);
        } else {
          statusPoruka = "Greška pri dohvaćanju korisnika.";
          odabraniKorisnik = new Korisnik("", "", "", "", "");
        }
      } catch (Exception e) {
        statusPoruka = "Greška pri dohvaćanju korisnika.";
        odabraniKorisnik = new Korisnik("", "", "", "", "");
      }
    }
  }
}
