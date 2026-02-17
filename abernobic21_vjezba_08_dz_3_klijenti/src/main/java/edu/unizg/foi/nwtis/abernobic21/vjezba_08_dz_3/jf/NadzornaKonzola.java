package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

@Named("nadzornaKonzola")
@ViewScoped
public class NadzornaKonzola implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartnerKlijent;

  private String statusKlijent = "";


  private String statusPoruka = "";

  public String getStatusPoruka() {
    return statusPoruka;
  }

  public String getStatusKlijent() {
    return statusKlijent;
  }

  @PostConstruct
  public void provjeriStatusKlijent() {
    try {
      Response odgovor = servisPartnerKlijent.headPosluziteljStatus(1);

      if (odgovor.getStatus() == 200) {
        statusKlijent = "200";

      } else if (odgovor.getStatus() == 204) {
        statusKlijent = "204";
      } else {
        statusPoruka = "Greška pri dohvaćanju statusa klijenta.";
      }
    } catch (Exception e) {
      statusPoruka = "Greška pri dohvaćanju statusa klijenta.";
    }
  }

  public void pauziraj() {
    try {

      var status = servisPartnerKlijent.headPosluziteljPauza(1).getStatus();

      if (status == 200) {
        statusPoruka = "";
      } else if (status == 204) {
        statusPoruka = "Poslužitelj klijent je već pauziran.";
      } else {
        statusPoruka = "Greška pri pauziranju poslužitelja klijent.";
      }
      provjeriStatusKlijent();
    } catch (Exception e) {
      statusPoruka = "Greška pri pauziranju poslužitelja klijent.";
    }
  }

  public void startaj() {
    try {

      var status = servisPartnerKlijent.headPosluziteljStart(1).getStatus();

      if (status == 200) {
        statusPoruka = "";
      } else if (status == 204) {
        statusPoruka = "Poslužitelj klijent je već pokrenut.";
      } else {
        statusPoruka = "Greška pri startanju poslužitelja klijent.";
      }
      provjeriStatusKlijent();
    } catch (Exception e) {
      statusPoruka = "Greška pri startanju poslužitelja klijent.";
    }
  }
}

