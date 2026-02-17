package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

@Named("pregledKartePica")
@RequestScoped
public class PregledKartePica implements Serializable {

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

  private List<KartaPica> kartaPica;

  public List<KartaPica> getKartaPica() {
    return kartaPica;
  }

  public String getNaziv(String id) {
    return kartaPica.stream().filter(kp -> id.equals(kp.id())).findFirst().orElse(null).naziv();
  }

  @PostConstruct
  public void ucitajKartuPica() {
    try {
      Response odgovor = servisPartnerKlijent.getKartaPica(prijavaKorisnika.getKorisnickoIme(),
          prijavaKorisnika.getLozinka());

      if (odgovor.getStatus() == 200) {
        Map<String, KartaPica> kartaPicaMapa =
            odgovor.readEntity(new GenericType<Map<String, KartaPica>>() {});

        kartaPica = new ArrayList<>(kartaPicaMapa.values());
        kartaPica.sort(Comparator.comparing(KartaPica::id));

      } else {
        statusPoruka = "Greška pri dohvaćanju karte pića.";
        kartaPica = List.of();
      }
    } catch (Exception e) {
      statusPoruka = "Greška pri dohvaćanju karte pića.";
      kartaPica = List.of();
    }
  }
}
