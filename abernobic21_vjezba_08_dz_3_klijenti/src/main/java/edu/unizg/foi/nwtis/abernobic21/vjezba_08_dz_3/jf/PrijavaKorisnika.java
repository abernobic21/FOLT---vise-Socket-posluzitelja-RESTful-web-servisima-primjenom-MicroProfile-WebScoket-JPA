package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.net.InetAddress;
import java.sql.Timestamp;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;

@SessionScoped
@Named("prijavaKorisnika")
public class PrijavaKorisnika implements Serializable {
  private static final long serialVersionUID = -1826447622277477398L;
  private String korisnickoIme;
  private String lozinka;
  private Korisnik korisnik;
  private boolean prijavljen = false;
  private String poruka = "";
  private Partner odabraniPartner;
  private boolean partnerOdabran = false;

  @Inject
  KorisniciFacade korisniciFacade;

  @Inject
  ZapisiFacade zapisiFacade;

  @Inject
  private SecurityContext securityContext;

  public String getKorisnickoIme() {
    return korisnickoIme;
  }

  public void setKorisnickoIme(String korisnickoIme) {
    this.korisnickoIme = korisnickoIme;
  }

  public String getLozinka() {
    return lozinka;
  }

  public void setLozinka(String lozinka) {
    this.lozinka = lozinka;
  }

  public String getIme() {
    return this.korisnik.ime();
  }

  public String getPrezime() {
    return this.korisnik.prezime();
  }

  public String getEmail() {
    return this.korisnik.email();
  }

  public boolean isPrijavljen() {
    if (!this.prijavljen) {
      provjeriPrijavuKorisnika();
    }
    return this.prijavljen;
  }

  public String getPoruka() {
    return poruka;
  }

  public Partner getOdabraniPartner() {
    return odabraniPartner;
  }

  public void setOdabraniPartner(Partner odabraniPartner) {
    this.odabraniPartner = odabraniPartner;
  }

  public boolean isPartnerOdabran() {
    return partnerOdabran;
  }

  public void setPartnerOdabran(boolean partnerOdabran) {
    this.partnerOdabran = partnerOdabran;
  }

  @PostConstruct
  private void provjeriPrijavuKorisnika() {
    if (this.securityContext.getCallerPrincipal() != null) {
      var korIme = this.securityContext.getCallerPrincipal().getName();
      this.korisnik = this.korisniciFacade.pretvori(this.korisniciFacade.find(korIme));
      if (this.korisnik != null) {
        this.prijavljen = true;
        this.korisnickoIme = korIme;
        this.lozinka = this.korisnik.lozinka();
        dodajZapis("Prijava korisnika");
      }
    }
  }

  public String odjavaKorisnika() {
    if (this.prijavljen) {
      this.prijavljen = false;

      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.getExternalContext().invalidateSession();
      dodajZapis("Odjava korisnika.");
      return "/index.xhtml?faces-redirect=true";
    }
    return "";
  }

  private void dodajZapis(String opisRada) {


    var ipAdresa = dohvatiIpAdresu();
    Zapisi zapis = new Zapisi();
    zapis.setKorisnickoime(this.korisnickoIme);
    zapis.setIpadresaracunala(ipAdresa);
    zapis.setAdresaracunala(dohvatiAdresu(ipAdresa));
    zapis.setOpisrada(opisRada);
    zapis.setVrijeme(new Timestamp(System.currentTimeMillis()));

    zapisiFacade.create(zapis);
  }

  private String dohvatiIpAdresu() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    if (facesContext == null) {
      return "unknown";
    }
    HttpServletRequest request =
        (HttpServletRequest) facesContext.getExternalContext().getRequest();
    String ipAddress = request.getHeader("X-FORWARDED-FOR");
    if (ipAddress == null || ipAddress.isEmpty()) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }

  public String dohvatiAdresu(String ipAddress) {
    try {
      InetAddress inetAddr = InetAddress.getByName(ipAddress);
      return inetAddr.getHostName();
    } catch (Exception e) {
      return "unknown";
    }
  }
}
