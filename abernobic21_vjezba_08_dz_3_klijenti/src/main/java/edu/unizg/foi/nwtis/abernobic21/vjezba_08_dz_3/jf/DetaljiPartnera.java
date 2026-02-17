package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.Map;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("detaljiPartnera")
@RequestScoped
public class DetaljiPartnera implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  PartneriFacade partneriFacade;

  private Partner partner;

  public Partner getPartner() {
    return partner;
  }

  @PostConstruct
  public void init() {
    Map<String, String> parametri =
        FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    String partnerIdParam = parametri.get("partnerId");
    if (partnerIdParam != null) {
      int id = Integer.parseInt(partnerIdParam);

      this.partner = partneriFacade.pretvori(partneriFacade.find(id), true);
    }
  }
}
