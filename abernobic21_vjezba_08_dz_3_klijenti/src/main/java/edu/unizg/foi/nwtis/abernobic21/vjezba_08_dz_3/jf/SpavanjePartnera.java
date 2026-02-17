package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

@Named("spavanjePartnera")
@RequestScoped
public class SpavanjePartnera implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartnerKlijent;

  private Long vrijeme;

  private String statusPoruka;

  public void spavaj() {
    try {

      Response odgovor = servisPartnerKlijent.getSpava(vrijeme);

      System.out.println(odgovor.getStatus());
      if (odgovor.getStatus() == 200) {
        statusPoruka = "Spavanje uspješno odrađeno";

      } else {
        statusPoruka = "Greška pri pokušaju spavanja.";
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      statusPoruka = "Greška pri pokušaju spavanja.";
    }
  }

  public Long getVrijeme() {
    return vrijeme;
  }

  public void setVrijeme(Long vrijeme) {
    this.vrijeme = vrijeme;
  }

  public String getStatusPoruka() {
    return statusPoruka;
  }

  public void setStatusPoruka(String statusPoruka) {
    this.statusPoruka = statusPoruka;
  }


}
