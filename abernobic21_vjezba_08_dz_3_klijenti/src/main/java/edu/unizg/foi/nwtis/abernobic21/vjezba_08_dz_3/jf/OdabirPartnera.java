package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("odabirPartnera")
public class OdabirPartnera implements Serializable {

  private static final long serialVersionUID = -524581462819739622L;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

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

  public String odaberiPartnera() {
    if (this.partner > 0) {
      Optional<Partner> partnerO =
          this.partneri.stream().filter((p) -> p.id() == this.partner).findFirst();
      if (partnerO.isPresent()) {
        this.prijavaKorisnika.setOdabraniPartner(partnerO.get());
        this.prijavaKorisnika.setPartnerOdabran(true);
      } else {
        this.prijavaKorisnika.setPartnerOdabran(false);
      }
    } else {
      this.prijavaKorisnika.setPartnerOdabran(false);
    }
    return "index.html?faces-redirect=true";
  }

}
