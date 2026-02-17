package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("pregledPartnera")
@RequestScoped
public class PregledPartnera implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  PartneriFacade partneriFacade;

  private List<Partner> partneri = new ArrayList<>();

  private int partner;

  public int getPartner() {
    return partner;
  }

  public void setPartner(int partner) {
    this.partner = partner;
  }

  public List<Partner> getPartneri() {
    return partneri;
  }

  @PostConstruct
  public void ucitajPartnere() {
    this.partneri = partneriFacade.findAll(true);
  }

  public String detaljiPartnera() {
    if (this.partner > 0) {
      return "detaljiPartnera.xhtml?faces-redirect=true&partnerId=" + this.partner;
    } else {
      return "index.html?faces-redirect=true";
    }
  }

}
