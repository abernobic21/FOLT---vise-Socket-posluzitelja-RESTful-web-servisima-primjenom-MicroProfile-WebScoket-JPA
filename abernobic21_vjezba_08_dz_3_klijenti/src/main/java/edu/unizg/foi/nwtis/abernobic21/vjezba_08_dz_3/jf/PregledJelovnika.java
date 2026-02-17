package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

@Named("pregledJelovnika")
@RequestScoped
public class PregledJelovnika implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartnerKlijent;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

  private String statusPoruka;

  public String getStatusPoruka() {
    return statusPoruka;
  }

  private List<Jelovnik> jelovnik;

  public List<Jelovnik> getJelovnik() {
    return jelovnik;
  }

  public String getNaziv(String id) {
    return jelovnik.stream().filter(j -> id.equals(j.id())).findFirst().orElse(null).naziv();
  }

  @PostConstruct
  public void ucitajJelovik() {
    try {
      Response odgovor = servisPartnerKlijent.getJelovnik(prijavaKorisnika.getKorisnickoIme(),
          prijavaKorisnika.getLozinka());

      if (odgovor.getStatus() == 200) {
        Map<String, Jelovnik> jelovnikMapa =
            odgovor.readEntity(new GenericType<Map<String, Jelovnik>>() {});

        jelovnik = new ArrayList<>(jelovnikMapa.values());
        jelovnik.sort(Comparator.comparing(Jelovnik::id));

      } else {
        statusPoruka = "Greška pri dohvaćanju jelovnika.";
        jelovnik = List.of();
      }
    } catch (Exception e) {
      statusPoruka = "Greška pri dohvaćanju jelovnika.";
      jelovnik = List.of();
    }
  }
}
