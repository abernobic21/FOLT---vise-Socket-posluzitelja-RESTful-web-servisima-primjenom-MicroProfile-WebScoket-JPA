package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisTvrtkaKlijent;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ws.WebSocketPartneri;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

@SessionScoped
@Named("pracenjeNarudzbi")
public class PracenjeNarudzbi implements Serializable {

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartnerKlijent;

  @Inject
  @RestClient
  ServisTvrtkaKlijent servisTvrtkaKlijent;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

  @Inject
  private PregledJelovnika pregledJelovnika;

  @Inject
  private PregledKartePica pregledKartePica;

  @Inject
  private ZapisiFacade zapisiFacade;

  @Inject
  private GlobalniPodaci globalniPodaci;

  private static final long serialVersionUID = 1L;

  private String statusPoruka;

  private boolean aktivnaNarudzba = false;

  private List<Narudzba> stavkeNarudzbe = new ArrayList<>();

  private List<Narudzba> narucenaJela = new ArrayList<>();

  private List<Narudzba> narucenaPica = new ArrayList<>();

  private String odabranoJelo = "";

  private String odabranoPice = "";

  private int kolicinaJelo = 0;

  private int kolicinaPice = 0;


  public String getStatusPoruka() {
    return statusPoruka;
  }

  public boolean isAktivnaNarudzba() {
    return aktivnaNarudzba;
  }

  public void setAktivnaNarudzba(boolean aktivnaNarudzba) {
    this.aktivnaNarudzba = aktivnaNarudzba;
  }

  public List<Narudzba> getStavkeNarudzbe() {
    return stavkeNarudzbe;
  }

  public void setStavkeNarudzbe(List<Narudzba> stavkeNarudzbe) {
    this.stavkeNarudzbe = stavkeNarudzbe;
  }

  public List<Narudzba> getNarucenaJela() {
    return narucenaJela;
  }

  public void setNarucenaJela(List<Narudzba> narucenaJela) {
    this.narucenaJela = narucenaJela;
  }

  public List<Narudzba> getNarucenaPica() {
    return narucenaPica;
  }

  public void setNarucenaPica(List<Narudzba> narucenaPica) {
    this.narucenaPica = narucenaPica;
  }

  public int getKolicinaPice() {
    return kolicinaPice;
  }

  public void setKolicinaPice(int kolicinaPice) {
    this.kolicinaPice = kolicinaPice;
  }

  public int getKolicinaJelo() {
    return kolicinaJelo;
  }

  public void setKolicinaJelo(int kolicinaJelo) {
    this.kolicinaJelo = kolicinaJelo;
  }

  public String getOdabranoPice() {
    return odabranoPice;
  }

  public void setOdabranoPice(String odabranoPice) {
    this.odabranoPice = odabranoPice;
  }

  public String getOdabranoJelo() {
    return odabranoJelo;
  }

  public void setOdabranoJelo(String odabranoJelo) {
    this.odabranoJelo = odabranoJelo;
  }


  @PostConstruct
  public void dohvatiNarudzbu() {
    try {
      Response odgovor = servisPartnerKlijent.getNarudzba(prijavaKorisnika.getKorisnickoIme(),
          prijavaKorisnika.getLozinka());

      if (odgovor.getStatus() == 200) {
        stavkeNarudzbe = odgovor.readEntity(new GenericType<List<Narudzba>>() {});
        stavkeNarudzbe.sort(Comparator.comparing(Narudzba::id));

        aktivnaNarudzba = true;

        narucenaJela = stavkeNarudzbe.stream().filter(Narudzba::jelo).collect(Collectors.toList());

        narucenaPica = stavkeNarudzbe.stream().filter(s -> !s.jelo()).collect(Collectors.toList());

      } else {
        statusPoruka = "Greška pri dohvaćanju jelovnika.";
        aktivnaNarudzba = false;
        stavkeNarudzbe = List.of();
      }
    } catch (Exception e) {
      statusPoruka = "Greška pri dohvaćanju jelovnika.";
      aktivnaNarudzba = false;
      stavkeNarudzbe = List.of();
    }
  }

  public void otvoriNarudzbu() {
    try {
      Response odgovor = servisPartnerKlijent.postNarudzba(prijavaKorisnika.getKorisnickoIme(),
          prijavaKorisnika.getLozinka());

      if (odgovor.getStatus() == 200) {
        aktivnaNarudzba = true;
        statusPoruka = "";
        stavkeNarudzbe = List.of();

        dodajZapis("Otvorena nova narudžba.");
        globalniPodaci.povecajBrojOtvorenihNarudzbi(1);
        posaljiSocketu();
      }
    } catch (Exception e) {
    }
  }

  public void platiNarudzbu() {
    try {
      Response odgovor = servisPartnerKlijent.postRacun(prijavaKorisnika.getKorisnickoIme(),
          prijavaKorisnika.getLozinka());

      if (odgovor.getStatus() == 200) {
        aktivnaNarudzba = false;
        stavkeNarudzbe = List.of();
        narucenaJela = List.of();
        narucenaPica = List.of();
        odabranoJelo = "";
        odabranoPice = "";
        kolicinaJelo = 0;
        kolicinaPice = 0;

        statusPoruka = "Uspješno plaćanje narudžbe";
        dodajZapis("Plaćena narudžba.");
        globalniPodaci.smanjiBrojOtvorenihNarudzbi(1);
        globalniPodaci.povecajBrojRacuna(1);

        posaljiSocketu();

        FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
      }
    } catch (Exception e) {
      statusPoruka = "Greška pri plaćanju narudžbe.";
    }
  }

  public void naruciJelo() {
    var cijena = pregledJelovnika.getJelovnik().stream().filter(j -> j.id().equals(odabranoJelo))
        .findFirst().orElse(null).cijena();

    var jelo = new Narudzba(prijavaKorisnika.getKorisnickoIme(), odabranoJelo, true, kolicinaJelo,
        cijena, System.currentTimeMillis());
    try {
      Response odgovor = servisPartnerKlijent.postJelo(jelo, prijavaKorisnika.getKorisnickoIme(),
          prijavaKorisnika.getLozinka());

      if (odgovor.getStatus() == 200) {
        statusPoruka = "";
        dohvatiNarudzbu();
      }
    } catch (Exception e) {
      statusPoruka = "Greška pri dodavanju jela,";
    }
  }

  public void naruciPice() {
    var cijena = pregledKartePica.getKartaPica().stream().filter(j -> j.id().equals(odabranoPice))
        .findFirst().orElse(null).cijena();

    var pice = new Narudzba(prijavaKorisnika.getKorisnickoIme(), odabranoPice, false, kolicinaPice,
        cijena, System.currentTimeMillis());
    try {
      Response odgovor = servisPartnerKlijent.postPice(pice, prijavaKorisnika.getKorisnickoIme(),
          prijavaKorisnika.getLozinka());

      if (odgovor.getStatus() == 200) {
        statusPoruka = "";
        dohvatiNarudzbu();
      }
    } catch (Exception e) {
      statusPoruka = "Greška pri dodavanju pića,";
    }
  }

  private void dodajZapis(String opisRada) {


    var ipAdresa = dohvatiIpAdresu();
    Zapisi zapis = new Zapisi();
    zapis.setKorisnickoime(prijavaKorisnika.getKorisnickoIme());
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

  private void posaljiSocketu() {
    WebSocketPartneri.send(formirajStatusPorukuPartner());
  }

  public String formirajStatusPorukuPartner() {
    String status = serverPartnerRadi() ? "RADI" : "NE RADI";
    int brojOtvorenihNarudzbi = globalniPodaci.getBrojOtvorenihNarudzbi(1);
    int brojPlacenihRacuna = globalniPodaci.getBrojRacuna(1);

    String poruka = status + ";" + brojOtvorenihNarudzbi + ";" + brojPlacenihRacuna;

    return poruka;
  }

  private boolean serverPartnerRadi() {
    try {
      Response odgovor = servisPartnerKlijent.headPosluzitelj();
      return odgovor.getStatus() == 200;
    } catch (Exception e) {
      return false;
    }
  }
}
