import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

@Named("provjeraPosluzitelja")
@RequestScoped
public class ProvjeraPosluzitelja {

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartnerKlijent;

  private String statusPoruka;

  public String getStatusPoruka() {
    return statusPoruka;
  }

  public void provjeri() {
    try {
      Response odgovor = servisPartnerKlijent.headPosluzitelj();
      if (odgovor.getStatus() == 200) {
        statusPoruka = "Poslužitelj je dostupan (" + odgovor.getStatus() + ")";
      } else {
        statusPoruka = "Poslužitelj nije dostupan (" + odgovor.getStatus() + ")";
      }
    } catch (Exception e) {
      statusPoruka = "Poslužitelj nije dostupan (204)";
    }
  }
}
