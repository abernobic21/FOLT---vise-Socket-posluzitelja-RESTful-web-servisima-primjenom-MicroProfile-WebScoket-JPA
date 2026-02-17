package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;

public class PosluziteljTvrtka {

  /** Konfiguracijski podaci. */
  private Konfiguracija konfig;

  /** Izvršitelj glavnih dretvi. */
  private ExecutorService executorGlavni = null;

  /** Pauza dretve. */
  private int pauzaDretve = 1000;

  /** Kod za kraj rada. */
  private String kodZaKraj = "";

  /** Zastavica za kraj rada. */
  private AtomicBoolean kraj = new AtomicBoolean(false);

  /** Zastavica za pauzu poslužitelja za registraciju */
  private AtomicBoolean pauzaRegistracija = new AtomicBoolean(false);

  /** Zastavica za pauzu poslužitelja za rad s parnerima */
  private AtomicBoolean pauzaRad = new AtomicBoolean(false);

  /** Kolekcija podataka kuhinja. */
  private Map<Integer, String> kuhinje = new ConcurrentHashMap<>();

  /** Kolekcija podataka jelovnika. */
  private Map<String, Map<String, Jelovnik>> jelovnici = new ConcurrentHashMap<>();

  /** Kolekcija podataka karte pića. */
  private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

  /** Kolekcija podataka partnera. */
  private Map<Integer, Partner> partneri = new ConcurrentHashMap<>();

  /** Izvršitelj dretvi registracije. */
  private ExecutorService executorRegistracija = null;

  /** Izvršitelj dretvi rada s klijentima. */
  private ExecutorService executorRad = null;

  /** Dretva za rad kraj. */
  private Future<?> dretvaZaKraj;
  /** Dretva za registraciju partnera. */
  private Future<?> dretvaRegistracijaPartnera;
  /** Dretva za rad s partnerima. */
  private Future<?> dretvaRadSPartnerima;
  /** Kolekcija aktivnih dretvi. */
  private Queue<Future<?>> aktivneDretve = new ConcurrentLinkedQueue<>();

  /** Brojač zatvorenih mrežnih veza poslužitelja registracija. */
  private AtomicInteger zatvoreneUticniceRegistracija = new AtomicInteger(0);

  /** Brojač zatvorenih mrežnih veza poslužitelja za rad s klijentima. */
  private AtomicInteger zatvoreneUticniceRad = new AtomicInteger(0);

  /** Mrežna vrata poslužitelja za kraj. */
  private ServerSocket mreznaUticnicaKraj = null;

  /** Mrežna vrata poslužitelja za registraciju. */
  private ServerSocket mreznaUticnicaRegistracija = null;

  /** Mrežna vrata poslužitelja za rad. */
  private ServerSocket mreznaUticnicaRad = null;

  public Konfiguracija getKonfig() {
    return konfig;
  }

  public boolean getKraj() {
    return kraj.get();
  }

  public void setKraj(boolean vrijednost) {
    kraj.set(vrijednost);
  }

  public void setKodZaKraj(String kod) {
    kodZaKraj = kod;
  }

  public void dodajPartnera(Partner partner) {
    partneri.put(partner.id(), partner);
  }

  public void dodajJelovnik(String kuhinja, Map<String, Jelovnik> jelovnik) {
    jelovnici.put(kuhinja, jelovnik);
  }

  public void dodajKartuPica(KartaPica kartaPica) {
    this.kartaPica.put(kartaPica.id(), kartaPica);
  }

  public void dodajKuhinju(int id, String kuhinja) {
    kuhinje.put(id, kuhinja);
  }

  // Logger
  private static final java.util.logging.Logger logger =
      java.util.logging.Logger.getLogger(PosluziteljTvrtka.class.getName());


  /**
   * Main metoda
   *
   * @param args argumenti (format: nazivDatotekeKonfiguracije)
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Broj argumenata nije 1.");
      return;
    }

    var program = new PosluziteljTvrtka();
    var nazivDatoteke = args[0];

    program.pripremiKreni(nazivDatoteke);
  }


  /**
   * Priprema za početak rada.<br>
   * Učitava konfiguraciju. Inicijalizira izvršitelje i pokreće poslužitelje za registraciju, kraj i
   * rad s klijentima. Nakon toga čeka kraj njihovog rada.
   *
   * @param nazivDatoteke naziv datoteke kondifuracije
   */
  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke) || !ucitajPartnere() || !ucitajKuhinje()
        || !ucitajJelovnike() || !ucitajKartuPica()) {
      return;
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.print("Prisilno gašenje poslužitelja\n");
      this.kraj.set(true);
      ugasiDretve();
      executorGlavni.shutdownNow();
      executorRegistracija.shutdownNow();
      executorRad.shutdownNow();
    }));

    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

    var builder = Thread.ofVirtual().name("Glavna dretva", 0);
    var factory = builder.factory();
    this.executorGlavni = Executors.newThreadPerTaskExecutor(factory);

    var builderRegistracija = Thread.ofVirtual().name("Dretva registracija", 0);
    var factoryRegistracija = builderRegistracija.factory();
    this.executorRegistracija = Executors.newThreadPerTaskExecutor(factoryRegistracija);

    var builderRad = Thread.ofVirtual().name("Dretva rad", 0);
    var factoryRad = builderRad.factory();
    this.executorRad = Executors.newThreadPerTaskExecutor(factoryRad);

    dretvaZaKraj = this.executorGlavni.submit(() -> this.pokreniPosluziteljKraj());
    dretvaRegistracijaPartnera =
        this.executorGlavni.submit(() -> this.pokreniPosluziteljZaRegistracijuPartnera());
    dretvaRadSPartnerima =
        this.executorGlavni.submit(() -> this.pokreniPosluziteljZaRadSPartnerima());


    while (!dretvaZaKraj.isDone() && !dretvaRegistracijaPartnera.isDone()
        && !dretvaRadSPartnerima.isDone()) {
      if (this.kraj.get()) {
        ugasiDretve();
      } else {
        try {
          Thread.sleep(this.pauzaDretve);
        } catch (InterruptedException e) {
        }
      }
    }
  }


  private void ugasiDretve() {

    AtomicInteger prisilnoUgaseneDretve = new AtomicInteger(0);
    aktivneDretve.forEach(d -> {
      if (!d.isDone()) {
        d.cancel(true);
        prisilnoUgaseneDretve.incrementAndGet();
      }
    });

    if (!dretvaRegistracijaPartnera.isDone()) {
      dretvaRegistracijaPartnera.cancel(true);
    }
    if (!dretvaRadSPartnerima.isDone()) {
      dretvaRadSPartnerima.cancel(true);
    }

    if (!dretvaZaKraj.isDone()) {
      dretvaZaKraj.cancel(true);
    }

    try {
      if (mreznaUticnicaKraj != null && !mreznaUticnicaKraj.isClosed()) {
        mreznaUticnicaKraj.close();
      }
      if (mreznaUticnicaRegistracija != null && !mreznaUticnicaRegistracija.isClosed()) {
        mreznaUticnicaRegistracija.close();
      }
      if (mreznaUticnicaRad != null && !mreznaUticnicaRad.isClosed()) {
        mreznaUticnicaRad.close();
      }
    } catch (IOException e) {
    }

    try {
      Thread.sleep(1000);
    } catch (Exception e) {
    }

    System.out.println("");
    System.out.println("Broj zatvorenih mrežnih utičnica poslužitelja registracija: "
        + zatvoreneUticniceRegistracija);
    System.out.println("Broj zatvorenih mrežnih utičnica poslužitelja za rad s partnerima: "
        + zatvoreneUticniceRad);
    System.out.println("Broj prisilno ugašenih dretvi: " + prisilnoUgaseneDretve);
  }


  /**
   * Pokretanje poslužitelja kraj.<br>
   * Osluškuje mrežna vrate i pokreće obradu kraja.
   */
  public void pokreniPosluziteljKraj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
    var brojCekaca = 0;
    try {
      mreznaUticnicaKraj = new ServerSocket(mreznaVrata, brojCekaca);
      while (!this.kraj.get()) {
        var mreznaUticnica = mreznaUticnicaKraj.accept();
        this.obradiKraj(mreznaUticnica);
      }
    } catch (IOException e) {
    } finally {
      if (mreznaUticnicaKraj != null && !mreznaUticnicaKraj.isClosed()) {
        try {
          mreznaUticnicaKraj.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Obrada kraja.<br>
   * Provejerava komandu i kod za kraj. Postavlja zastavicu kraja rada na true.
   *
   * @param mreznaUticnica mrežna uticnica
   */
  public void obradiKraj(Socket mreznaUticnica) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      String unos = in.readLine();

      String[] dijelovi = unos.split(" ");
      String komanda = dijelovi[0];

      switch (komanda) {
        case "KRAJ":
          krajRada(unos, out);
          break;
        case "STATUS":
          dajStatus(unos, out);
          break;
        case "PAUZA":
          pauzirajPosluzitelja(unos, out);
          break;
        case "START":
          startajPosluzitelja(unos, out);
          break;
        case "SPAVA":
          spavaj(unos, out);
          break;
        case "KRAJWS":
          krajRadaWS(unos, out);
          break;
        case "OSVJEŽI":
          osvjezi(unos, out);
          break;
        default:
          out.print("ERROR 10 - Format komande nije ispravan\n");
      }

      out.flush();

    } catch (Exception e) {
    } finally {
      try {
        mreznaUticnica.shutdownOutput();
        mreznaUticnica.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Kraj rada.<br>
   * Provjerava komandu i kod za kraj. Ako su uspješne šalje KRAJ svim partnerima, ako svi vrate OK,
   * šalje RESTful servisu KRAJ.
   *
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void krajRada(String komanda, PrintWriter out) {
    if (!provjeriKomandu(komanda, "KRAJ")) {
      out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
      return;
    }
    if (!komanda.trim().equals("KRAJ " + this.kodZaKraj)) {
      out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
      return;
    }

    boolean krajRadaPartnera = posaljiKrajPartnerima();
    if (!krajRadaPartnera) {
      out.write("ERROR 14 - Barem jedan partner nije završio rad\n");
      return;
    }

    if (!posaljiKrajServisu()) {
      out.write("ERROR 17 - RESTful zahtjev nije uspješan\n");
      return;
    }

    out.write("OK\n");

    this.kraj.set(true);
  }

  /**
   * Pošalji kraj pratnerima.<br>
   * Šalje kraj svim partnerima, ako svi vrate OK vraća true.
   */
  private boolean posaljiKrajPartnerima() {
    try {

      for (Partner partner : partneri.values()) {
        if (posaljiZahtjevZaKraj(partner.adresa(), partner.mreznaVrataKraj()) == false) {
          return false;
        }
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * Pošalji zahtjev kraj.<br>
   * Šalje KRAJ na adresu i mrezni port.
   *
   * @param adresa adresa za slanje
   * @param mreznaVrta mrezna vrata za slanje
   */
  private boolean posaljiZahtjevZaKraj(String adresa, int mreznaVrataKraj) {
    Socket mreznaUticnica = null;
    try {
      mreznaUticnica = new Socket(adresa, mreznaVrataKraj);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      out.write(String.format("KRAJ %s\n", this.kodZaKraj));
      out.flush();
      mreznaUticnica.shutdownOutput();

      var linija = in.readLine();

      return linija.equals("OK");

    } catch (Exception e) {
      return true;
    } finally {
      try {
        mreznaUticnica.shutdownInput();
        mreznaUticnica.close();
      } catch (Exception ex) {
      }
    }
  }

  /**
   * Pošalji kraj servisu.<br>
   * Šalje RESTful servisu KRAJ.
   * 
   */
  private boolean posaljiKrajServisu() {
    try {
      String restAdresa = konfig.dajPostavku("restAdresa") + "/kraj/info";

      HttpClient client = HttpClient.newHttpClient();

      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(restAdresa))
          .method("HEAD", HttpRequest.BodyPublishers.noBody()).build();

      HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

      return response.statusCode() == 200;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Daj status. <br>
   * Vraća 0 odnosno 1, ovisno je li posluzitelj u pauzi ili ne.
   *
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void dajStatus(String komanda, PrintWriter out) {
    try {
      if (!provjeriKomandu(komanda, "STATUS")) {
        out.print("ERROR 10 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];
      int dioPosluzitelja = Integer.parseInt(komanda.split(" ")[2]);

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdminTvrtke"))) {
        out.print("ERROR 12 - Pogrešan kodZaAdminTvrtke \n");
        return;
      }

      int status = 0;
      switch (dioPosluzitelja) {
        case 1:
          status = pauzaRegistracija.get() ? 0 : 1;
          break;
        case 2:
          status = pauzaRad.get() ? 0 : 1;
          break;
      }

      out.printf("OK %d\n", status);

    } catch (Exception e) {
      out.print("ERROR 19 - Nešto drugo nije u redu\n");
    }

  }

  /**
   * Pauziraj posluzitelja<br>
   * Postavlja zastavicu pauze na 1.
   * 
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void pauzirajPosluzitelja(String komanda, PrintWriter out) {
    try {
      if (!provjeriKomandu(komanda, "PAUZA")) {
        out.print("ERROR 10 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];
      int dioPosluzitelja = Integer.parseInt(komanda.split(" ")[2]);

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdminTvrtke"))) {
        out.print("ERROR 12 - Pogrešan kodZaAdminTvrtke \n");
        return;
      }

      switch (dioPosluzitelja) {
        case 1:
          if (pauzaRegistracija.get()) {
            out.print("ERROR 13 - Pogrešna promjena pauze ili starta\n");
          } else {
            pauzaRegistracija.set(true);
            out.print("OK\n");
          }
          break;
        case 2:
          if (pauzaRad.get()) {
            out.print("ERROR 13 - Pogrešna promjena pauze ili starta\n");
          } else {
            pauzaRad.set(true);
            out.print("OK\n");
          }
          break;
      }

    } catch (Exception e) {
      out.print("ERROR 19 - Nešto drugo nije u redu\n");
    }

  }

  /**
   * Startaj posluzitelja.<br>
   * Postavlja zastavu spavanja na false;
   *
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void startajPosluzitelja(String komanda, PrintWriter out) {
    try {
      if (!provjeriKomandu(komanda, "START")) {
        out.print("ERROR 10 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];
      int dioPosluzitelja = Integer.parseInt(komanda.split(" ")[2]);

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdminTvrtke"))) {
        out.print("ERROR 12 - Pogrešan kodZaAdminTvrtke \n");
        return;
      }

      switch (dioPosluzitelja) {
        case 1:
          if (pauzaRegistracija.get()) {
            pauzaRegistracija.set(false);
            out.print("OK\n");
          } else {
            out.print("ERROR 13 - Pogrešna promjena pauze ili starta\n");
          }
          break;
        case 2:
          if (pauzaRad.get()) {
            pauzaRad.set(false);
            out.print("OK\n");
          } else {
            out.print("ERROR 13 - Pogrešna promjena pauze ili starta\n");
          }
          break;
      }

    } catch (Exception e) {
      out.print("ERROR 19 - Nešto drugo nije u redu\n");
    }

  }

  /**
   * Spavaj.<br>
   * Spava posluzitelj n sekundi
   *
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void spavaj(String komanda, PrintWriter out) {
    try {
      if (!provjeriKomandu(komanda, "SPAVA")) {
        out.print("ERROR 10 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];
      int n = Integer.parseInt(komanda.split(" ")[2]);

      if (n < 0) {
        out.print("ERROR 10 - Format komande nije ispravan\n");
        return;
      }

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdminTvrtke"))) {
        out.print("ERROR 12 - Pogrešan kodZaAdminTvrtke \n");
        return;
      }

      try {
        Thread.sleep(n);
        out.print("OK\n");
      } catch (InterruptedException e) {
        out.print("ERROR 16 – Prekid spavanja dretve\n");
      }

    } catch (Exception e) {
      out.print("ERROR 19 - Nešto drugo nije u redu\n");
    }
  }

  /**
   * Kraj rada s web servise.<br>
   * Provjerava komandu i kod za kraj. Ako su uspješne šalje KRAJ svim partnerima, ako svi vrate OK,
   * završava s radom.
   *
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void krajRadaWS(String komanda, PrintWriter out) {
    if (!provjeriKomandu(komanda, "KRAJ")) {
      out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
      return;
    }
    if (!komanda.trim().equals("KRAJWS " + this.kodZaKraj)) {
      out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
    }

    boolean krajRadaPartnera = posaljiKrajPartnerima();
    if (!krajRadaPartnera) {
      out.write("ERROR 14 - Barem jedan partner nije završio rad\n");
      return;
    }

    out.write("OK\n");

    this.kraj.set(true);
  }

  /**
   * Osvjezi<br>
   * Ponovno cita jelovnike i karte pica.
   *
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void osvjezi(String komanda, PrintWriter out) {
    try {
      if (!provjeriKomandu(komanda, "OSVJEŽI")) {
        out.print("ERROR 10 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdminTvrtke"))) {
        out.print("ERROR 12 - Pogrešan kodZaAdminTvrtke \n");
        return;
      }

      if (pauzaRad.get()) {
        out.print("ERROR 15 - Poslužitelj za partnere u pauzi\n");
        return;
      }


      if (ucitajKuhinje() && ucitajJelovnike() && ucitajKartuPica()) {
        out.print("OK\n");
      } else {
        out.print("ERROR 19 - Nemoguće učitavanje karte piće ili jelovnika\n");
        return;
      }

    } catch (Exception e) {
      out.print("ERROR 19 - Nešto drugo nije u redu\n");
    }
  }


  /**
   * Pokretanje posluzitelja za registraciju partnera.<br>
   * Pokreće mrežnu utičnicu i osluškuje. Svaki zahtjev proslijeđuje novoj virtualnoj dretvi.
   */
  public void pokreniPosluziteljZaRegistracijuPartnera() {
    int mreznaVrata = 0;
    int brojCekaca = 0;
    try {
      mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
      brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
    } catch (Exception e) {
      return;
    }

    try {
      mreznaUticnicaRegistracija = new ServerSocket(mreznaVrata, brojCekaca);
      while (!this.kraj.get()) {
        var mreznaUticnica = mreznaUticnicaRegistracija.accept();
        aktivneDretve.add(executorRegistracija.submit(() -> obradiRegistraciju(mreznaUticnica)));
      }
    } catch (IOException e) {
    } finally {
      if (mreznaUticnicaRegistracija != null && !mreznaUticnicaRegistracija.isClosed()) {
        try {
          mreznaUticnicaRegistracija.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Obrada registracije.<br>
   * Čita komandu iz mrežne utičnice i poziva odgovarajuću funkciju.
   *
   * @param mreznaUticnica mrežna utičnica
   */
  public void obradiRegistraciju(Socket mreznaUticnica) {

    try (
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "UTF-8"));
        PrintWriter out =
            new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "UTF-8"));) {

      if (Thread.currentThread().isInterrupted()) {
        return;
      }


      if (pauzaRegistracija.get()) {
        out.print("ERROR 24 - Poslužitelj za registraciju partnera u pauzi\n");
        out.flush();
        mreznaUticnica.shutdownOutput();
        return;
      }

      String unos = in.readLine();

      String[] dijelovi = unos.split(" ");
      String komanda = dijelovi[0];

      switch (komanda) {
        case "PARTNER":
          registrirajPartnera(unos, out);
          break;
        case "OBRIŠI":
          obrisiPartnera(unos, out);
          break;
        case "POPIS":
          dohvatiPopisPartnera(unos, out);
          break;
        default:
          out.print("ERROR 20 - Format komande nije ispravan\n");
      }

      out.flush();
      mreznaUticnica.shutdownOutput();

    } catch (SocketException e) {

    } catch (IOException e) {
    } finally {
      try {
        mreznaUticnica.close();
        if (Thread.currentThread().isInterrupted())
          zatvoreneUticniceRegistracija.incrementAndGet();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Registracija partnera.<br>
   * Provjerava format komande. Kreira sigurnosni kod. Te šalje ga kao odgovor ako partner nije
   * prisutan u kolekciji partneri.<br>
   * Ako je neispravna komanda, vraća grešku:<br>
   * <code>ERROR 20 - Format komande nije ispravan</code><br>
   * Ako je partner s tim id-om prisutan u kolekciji partnera, vraća grešku:<br>
   * <code>ERROR 21 - Već postoji partner s id u kolekciji partnera</code><br>
   * Ako partner ima nepostojeću vrstu kuhinje, vraća grešku:<br>
   * <code>ERROR 29 - Registracija za nepostojeću kuhinju</code><br>
   * Ako nešto drugo nije u redu, vraća grešku:<br>
   * <code>ERROR 29 - Nešto drugo nije u redu</code><br>
   * 
   * @param komanda komanda zahtjeva
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void registrirajPartnera(String komanda, PrintWriter out) {

    try {

      Pattern pattern = Pattern.compile(
          "^PARTNER\\s+(\\d+)\\s+\"([^\"]+)\"\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+([-+]?\\d+(?:\\.\\d+)?)\\s+([-+]?\\d+(?:\\.\\d+)?)\\s+(\\d+)\\s+([A-Za-z0-9]+)$");
      Matcher matcher = pattern.matcher(komanda);

      if (!matcher.matches()) {
        out.print("ERROR 20 - Format komande nije ispravan\n");
        return;
      }
      int id = Integer.parseInt(matcher.group(1));
      String naziv = matcher.group(2);
      String vrstaKuhinje = matcher.group(3);
      String adresa = matcher.group(4);
      int mreznaVrata = Integer.parseInt(matcher.group(5));
      float gpsSirina = Float.parseFloat(matcher.group(6));
      float gpsDuzina = Float.parseFloat(matcher.group(7));
      int mreznaVrataKraj = Integer.parseInt(matcher.group(8));
      String adminKod = matcher.group(9);



      if (partneri.containsKey(id)) {
        out.print("ERROR 21 - Već postoji partner s id u kolekciji partnera\n");
        return;
      }
      if (this.jelovnici.get(vrstaKuhinje) == null) {
        out.print("ERROR 29 - Registracija za nepostojeću kuhinju\n");
        return;
      }

      String sigurnosniKod = Integer.toHexString((naziv + adresa).hashCode());
      Partner partner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj,
          gpsSirina, gpsDuzina, sigurnosniKod, adminKod);

      partneri.put(id, partner);
      spremiPartnere();
      out.print("OK " + sigurnosniKod + "\n");

    } catch (Exception e) {
      try {
        out.print("ERROR 29 - Nešto drugo nije u redu.\n");
      } catch (Exception ex) {
      }
    }
  }

  /**
   * Brisanje partnera.<br>
   * Provjerava format komade. Provjerava sigurnosni kod partnera. Briše partnera ako postoji.<br>
   * Ako je neispravna komanda, vraća grešku:<br>
   * <code>ERROR 20 - Format komande nije ispravan</code><br>
   * Ako je partner s tim id-om nije prisutan u kolekciji partnera, vraća grešku:<br>
   * <code>ERROR 23 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera</code><br>
   * Ako je neispravan sigurnosni kod, vraća grešku:<br>
   * <code>ERROR 22 - Neispravan sigurnosni kod partnera</code><br>
   * Ako nešto drugo nije u redu, vraća grešku:<br>
   * <code>ERROR 29 - Nešto drugo nije u redu</code><br>
   *
   * @param komanda komanda zahtjeva
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void obrisiPartnera(String komanda, PrintWriter out) {
    if (!provjeriKomandu(komanda, "OBRIŠI")) {
      out.print("ERROR 20 - Format komande nije ispravan\n");
      return;
    }
    try {
      int partnerId = Integer.parseInt(komanda.split(" ")[1]);
      String sigurnosniKod = komanda.split(" ")[2];
      Partner partner = partneri.get(partnerId);
      if (partner == null) {
        out.print(
            "ERROR 23 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
        return;
      }
      if (!partner.sigurnosniKod().equals(sigurnosniKod)) {
        out.print("ERROR 22 - Neispravan sigurnosni kod partnera\n");
        return;
      }
      partneri.remove(partnerId);
      spremiPartnere();
      out.print("OK\n");

    } catch (Exception e) {
      out.print("ERROR 29 - Nešto drugo nije u redu\n");
    }
  }

  /**
   * Dohvat popisa partnera.<br>
   * Dohvaća popis partnera, parsira ih u kolekciju oblika PartnerPopis te ih vraća u odgovoru. <br>
   * Ako je neispravna komanda, vraća grešku:<br>
   * <code>ERROR 20 - Format komande nije ispravan</code><br>
   * Ako nešto drugo nije u redu vraća grešku:<br>
   * <code>ERROR 29 - Nešto drugo nije u redu</code><br>
   * 
   * @param komanda komanda zahtjeva
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void dohvatiPopisPartnera(String komanda, PrintWriter out) {
    try {
      if (!provjeriKomandu(komanda, "POPIS")) {
        out.print("ERROR 20 - Format komande nije ispravan\n");
        return;
      }
      Gson gson = new Gson();

      List<PartnerPopis> popisPartnera =
          partneri.values().stream().map(p -> new PartnerPopis(p.id(), p.naziv(), p.vrstaKuhinje(),
              p.adresa(), p.mreznaVrata(), p.gpsSirina(), p.gpsDuzina())).toList();
      String json = gson.toJson(popisPartnera);
      out.print("OK\n" + json + "\n");

    } catch (Exception e) {
      try {
        out.print("ERROR 29 - Nešto drugo nije u redu\n");
      } catch (Exception ex) {
      }
    }
  }

  /**
   * Sinkronizirano spremanje partnera.<br>
   * Sprema parnere u datoteku partnera.
   */
  private synchronized void spremiPartnere() {
    try (FileWriter writer = new FileWriter(konfig.dajPostavku("datotekaPartnera"))) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(partneri.values(), writer);
    } catch (IOException e) {
    }
  }

  /**
   * Provjera komande.<br>
   * Ovisno o određenoj komandi, provjerava poklapanje s odabranim predloškom.
   *
   * @param unos tekst zahtjeva
   * @param komanda komanda koju se provjerava
   * @return true, ako je uspješni format komande zahtjeva
   */
  public boolean provjeriKomandu(String unos, String komanda) {
    String regex = "";
    switch (komanda) {
      case "KRAJ":
        regex = "^KRAJ\\s+([A-Za-z0-9]+)$";
        break;
      case "STATUS":
        regex = "^STATUS\\s+([A-Za-z0-9]+)\\s+(1|2)$";
        break;
      case "PAUZA":
        regex = "^PAUZA\\s+([A-Za-z0-9]+)\\s+(1|2)$";
        break;
      case "START":
        regex = "^START\\s+([A-Za-z0-9]+)\\s+(1|2)$";
        break;
      case "SPAVA":
        regex = "^SPAVA\\s+([A-Za-z0-9]+)\\s+(\\d+)$";
        break;
      case "KRAJWS":
        regex = "^KRAJWS\\s+([A-Za-z0-9]+)$";
        break;
      case "OSVJEŽI":
        regex = "^OSVJEŽI\\s+([A-Za-z0-9]+)$";
        break;
      case "OBRIŠI":
        regex = "^OBRIŠI\\s+(\\d+)\\s+(\\w+)$";
        break;
      case "POPIS":
        regex = "^POPIS$";
        break;
      case "JELOVNIK":
        regex = "^JELOVNIK\\s+(\\d+)\\s+(\\w+)$";
        break;
      case "KARTAPIĆA":
        regex = "^KARTAPIĆA\\s+(\\d+)\\s+(\\w+)$";
        break;
      case "OBRAČUN":
        regex = "^OBRAČUN\\s+(\\d+)\\s+(\\w+)$";
        break;
      case "OBRAČUNWS":
        regex = "^OBRAČUNWS\\s+(\\d+)\\s+(\\w+)$";
        break;
    }
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(unos);

    return matcher.matches();
  }

  /**
   * Pokretanje poslužitelja za rad s partnerima.<br>
   * Otvara mrežna vrata za rad. Svaki zahtjev proslijeđuje novoj virtualnoj dretvi.
   */
  public void pokreniPosluziteljZaRadSPartnerima() {
    try {

      var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
      var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));

      mreznaUticnicaRad = new ServerSocket(mreznaVrata, brojCekaca);
      while (!this.kraj.get()) {
        var mreznaUticnica = mreznaUticnicaRad.accept();
        aktivneDretve.add(executorRad.submit(() -> obradiRad(mreznaUticnica)));
      }
    } catch (IOException e) {
    } finally {
      if (mreznaUticnicaRad != null && !mreznaUticnicaRad.isClosed()) {
        try {
          mreznaUticnicaRad.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Obrada rada.<br>
   * Ovisno o komandi zahtjeva poziva odgovarajuću funkciju.
   *
   * @param mreznaUticnica mrežna utičnica
   */
  public void obradiRad(Socket mreznaUticnica) {

    try (
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
        PrintWriter out =
            new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));) {

      if (pauzaRad.get()) {
        out.print("ERROR 36 – Poslužitelj za partnere u pauzi\n");
        out.flush();
        mreznaUticnica.shutdownOutput();
        return;
      }
      String unos = in.readLine();


      String[] dijelovi = unos.split(" ");
      String komanda = dijelovi[0];

      switch (komanda) {
        case "JELOVNIK":
          dohvatiJelovnik(unos, out);
          break;
        case "KARTAPIĆA":
          dohvatiKartuPica(unos, out);
          break;
        case "OBRAČUN":
          obradiObracun(unos, in, out);
          break;
        case "OBRAČUNWS":
          obradiObracunWS(unos, in, out);
          break;
        default:
          out.print("ERROR 30 - Format komande nije ispravan\n");
      }

      out.flush();
      mreznaUticnica.shutdownOutput();

    } catch (SocketException e) {

    } catch (IOException e) {
    } finally {
      try {
        mreznaUticnica.close();
        if (Thread.currentThread().isInterrupted())
          zatvoreneUticniceRad.incrementAndGet();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Dohvaćanje jelovnika<br>
   * Provjerava komandu, postojanje partnera i sigurnosni kod. Vraća jelovnik ako takav postoji.<br>
   * Ako je neispravna komanda, vraća grešku:<br>
   * <code>ERROR 30 - Format komande nije ispravan</code><br>
   * Ako ne posotji partner ili je neisptavan sigurnosni kod, vraća grešku:<br>
   * <code>ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera</code><br>
   * Ako ne postoji jelovnik s vrstom kuhinje partnera, vraća grešku:<br>
   * <code>ERROR 32 - Ne postoji jelovnik s vrstom kuhinje koju partner ima ugovorenu</code><br>
   * Ako nešto drugo nije u redu, vraća grešku:<br>
   * <code>ERROR 39 - Nešto drugo nije u redu</code><br>
   * 
   * @param komanda komanda zahtjeva
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void dohvatiJelovnik(String komanda, PrintWriter out) {
    if (!provjeriKomandu(komanda, "JELOVNIK")) {


      out.print("ERROR 30 - Format komande nije ispravan\n");
      return;
    }
    try {
      int partnerId = Integer.parseInt(komanda.split(" ")[1]);
      String sigurnosniKod = komanda.split(" ")[2];
      Partner partner = partneri.get(partnerId);
      if (partner == null) {

        out.print(
            "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
        return;
      }
      if (!partner.sigurnosniKod().equals(sigurnosniKod)) {

        out.print(
            "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
        return;
      }
      var vrstaKuhinje = partner.vrstaKuhinje();
      var jelovnik = this.jelovnici.get(vrstaKuhinje);
      if (jelovnik == null) {

        out.print("ERROR 33 - Neispravan jelovnik\n");
        return;
      }
      Gson gson = new Gson();
      String json = gson.toJson(jelovnik.values());

      out.print("OK\n" + json + "\n");

    } catch (Exception e) {
      try {

        out.print("ERROR 39 - Nešto drugo nije u redu\n");
      } catch (Exception ex) {
      }
    }

  }

  /**
   * Dohvaćanje karte pića<br>
   * Provjerava komandu, postojanje partnera i sigurnosni kod. Vraća kartu puća ako takav
   * postoji.<br>
   * Ako je neispravna komanda, vraća grešku:<br>
   * <code>ERROR 30 - Format komande nije ispravan</code><br>
   * Ako ne posotji partner ili je neisptavan sigurnosni kod, vraća grešku:<br>
   * <code>ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera</code><br>
   * Ako je neispravna karta pića, vraća grešku:<br>
   * <code>ERROR 34 - Neispravna karta pića</code><br>
   * Ako nešto drugo nije u redu, vraća grešku:<br>
   * <code>ERROR 39 - Nešto drugo nije u redu</code><br>
   * 
   * @param komanda komanda zahtjeva
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void dohvatiKartuPica(String komanda, PrintWriter out) {
    if (!provjeriKomandu(komanda, "KARTAPIĆA")) {
      out.print("ERROR 30 - Format komande nije ispravan\n");
    } else {
      try {
        int partnerId = Integer.parseInt(komanda.split(" ")[1]);
        String sigurnosniKod = komanda.split(" ")[2];
        Partner partner = partneri.get(partnerId);
        if (partner == null) {
          out.print(
              "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
          return;
        }
        if (!partner.sigurnosniKod().equals(sigurnosniKod)) {
          out.print(
              "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
          return;
        }
        if (kartaPica.isEmpty()) {
          out.print("ERROR 34 - Neispravna karta pića\n");

          return;
        }
        Gson gson = new Gson();
        String json = gson.toJson(kartaPica.values());

        out.print("OK\n" + json + "\n");

      } catch (Exception e) {
        try {
          out.print("ERROR 39 - Nešto drugo nije u redu\n");
        } catch (Exception ex) {
        }
      }
    }
  }


  /**
   * Obrada komande obračun.<br>
   * Provjerava komandu, postojanje partnera i sigurnosni kod. Provjerava obračune i ako su u redu
   * dodaje ih u kolekciju obračuna i sprema ju.
   * 
   * Ako je neispravna komanda, vraća grešku:<br>
   * <code>ERROR 30 - Format komande nije ispravan</code><br>
   * Ako ne posotji partner ili je neisptavan sigurnosni kod, vraća grešku:<br>
   * <code>ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera</code><br>
   * Ako je neispravan obračun, vraća grešku:<br>
   * <code>ERROR 35 - Neispravan obračun</code><br>
   * Ako nešto drugo nije u redu, vraća grešku:<br>
   * <code>ERROR 39 - Nešto drugo nije u redu</code><br>
   *
   * @param komanda komanda zahtjeva
   * @param in ulazni tok za čitanje zahtjeva iz mrežne utičnice
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void obradiObracun(String komanda, BufferedReader in, PrintWriter out) {
    try {
      if (!provjeriKomandu(komanda, "OBRAČUN")) {
        out.print("ERROR 30 - Format komande nije ispravan\n");
        return;
      }
      int partnerId = Integer.parseInt(komanda.split(" ")[1]);
      String sigurnosniKod = komanda.split(" ")[2];
      Partner partner = partneri.get(partnerId);
      if (partner == null) {
        out.print(
            "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
        return;
      }
      if (!partner.sigurnosniKod().equals(sigurnosniKod)) {
        out.print(
            "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
        return;
      }

      List<Obracun> noviObracuni = procitajObracun(in);
      if (!provjeriObracun(partner, noviObracuni)) {
        out.print("ERROR 35 - Neispravan obračun\n");
        return;
      }

      dodajObracune(noviObracuni);

      if (!saljiObracune(noviObracuni)) {
        out.print("ERROR 37 - RESTful zahtjev nije uspješan\n");
        return;
      }

      out.print("OK\n");

    } catch (JsonSyntaxException e) {
      out.print("ERROR 35 - Neispravan obračun\n");

    } catch (Exception e) {
      out.print("ERROR 39 - Nešto drugo nije u redu\n");
    }
  }

  public void obradiObracunWS(String komanda, BufferedReader in, PrintWriter out) {
    try {
      if (!provjeriKomandu(komanda, "OBRAČUN")) {
        out.print("ERROR 30 - Format komande nije ispravan\n");
        return;
      }
      int partnerId = Integer.parseInt(komanda.split(" ")[1]);
      String sigurnosniKod = komanda.split(" ")[2];
      Partner partner = partneri.get(partnerId);
      if (partner == null) {
        out.print(
            "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
        return;
      }
      if (!partner.sigurnosniKod().equals(sigurnosniKod)) {
        out.print(
            "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
        return;
      }

      List<Obracun> noviObracuni = procitajObracun(in);
      if (!provjeriObracun(partner, noviObracuni)) {
        out.print("ERROR 35 - Neispravan obračun\n");
        return;
      }

      dodajObracune(noviObracuni);

      out.print("OK\n");

    } catch (JsonSyntaxException e) {
      out.print("ERROR 35 - Neispravan obračun\n");

    } catch (Exception e) {
      out.print("ERROR 39 - Nešto drugo nije u redu\n");
    }
  }

  /**
   * Provjera obračuna.<br>
   * Provjerava vrstu kuhinje, postojanje jela i pića te količinu.
   *
   * @param partner partner
   * @param obracuni obračuni
   * @return true, ako su ispravni
   */
  public boolean provjeriObracun(Partner partner, List<Obracun> obracuni) {
    String vrstaKuhinje = partner.vrstaKuhinje();
    var jelovnik = jelovnici.get(vrstaKuhinje);
    if (jelovnik == null)
      return false;

    for (Obracun obracun : obracuni) {
      if (obracun.partner() != partner.id()) {
        return false;
      }

      if (obracun.jelo()) {

        if (jelovnik.get(obracun.id()) == null
            || jelovnik.get(obracun.id()).cijena() != obracun.cijena()) {
          return false;

        }
        if (obracun.kolicina() < 1) {
          return false;
        }
      } else {
        if (kartaPica == null || kartaPica.get(obracun.id()).cijena() != obracun.cijena()) {
          return false;
        }
        if (obracun.kolicina() < 1) {
          return false;
        }
      }
    }
    return true;
  }


  /**
   * Čitanje obračuna.<br>
   * Čita obračun iz zahtjeva.
   *
   * @param in ulazni tok za čitanje zahtjeva iz mrežne utičnice
   * @return polje obračuna
   * @throws IOException greška pri radu sa mrežnpm utičnicom
   */
  public List<Obracun> procitajObracun(BufferedReader in) throws IOException {
    StringBuilder jsonBuilder = new StringBuilder();
    String linija;

    while ((linija = in.readLine()) != null) {
      jsonBuilder.append(linija);
      if (linija.contains("]"))
        break;
    }

    String stringificiraniObracuni = jsonBuilder.toString();

    Gson gson = new GsonBuilder().create();
    List<Obracun> noviObracuni =
        gson.fromJson(stringificiraniObracuni, new TypeToken<List<Obracun>>() {}.getType());
    return noviObracuni;
  }

  /**
   * Sinkrono dodavanje obračuna.<br>
   * Otvara datoteku, dohvaća postojeće obračune te im dodaje nove obračune. Zatim pohranjuje
   * ažurirani popis obračuna.
   *
   * @param noviObracuni novi obračuni
   * @return true, je uspješno spremanje
   */
  private synchronized boolean dodajObracune(List<Obracun> noviObracuni) {
    var nazivDatoteke = this.konfig.dajPostavku("datotekaObracuna");
    var datoteka = Path.of(nazivDatoteke);
    if (!Files.exists(datoteka)) {
      try {
        Files.createFile(datoteka);
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }

    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      Obracun[] stariObracuni = gson.fromJson(br, Obracun[].class);

      List<Obracun> sviObracuni = new ArrayList<>();

      if (stariObracuni != null) {
        sviObracuni.addAll((List.of(stariObracuni)));
      }
      sviObracuni.addAll(noviObracuni);

      try (FileWriter writer = new FileWriter(nazivDatoteke)) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(sviObracuni, writer);
      }

    } catch (IOException ex) {
      return false;
    }
    return true;
  }

  private boolean saljiObracune(List<Obracun> noviObracuni) {
    try {
      String restAdresa = konfig.dajPostavku("restAdresa") + "/obracun";

      Gson gson = new Gson();
      String stringificiraniObracuni = gson.toJson(noviObracuni);

      HttpClient client = HttpClient.newHttpClient();

      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(restAdresa))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(stringificiraniObracuni)).build();

      HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

      return response.statusCode() == 201;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke naziv datoteke
   * @return true, ako je uspješno učitavanje konfiguracije
   */
  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);

      return true;
    } catch (NeispravnaKonfiguracija ex) {
      return false;
    }
  }

  /**
   * Učitavanje partnera.<br>
   * Otvara datoteku partnera. Dohvaća ih i sprema u kolekciju parneri.
   *
   * @return true, ako je uspješno
   */
  private boolean ucitajPartnere() {
    var nazivDatoteke = this.konfig.dajPostavku("datotekaPartnera");
    var datoteka = Path.of(nazivDatoteke);
    if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
      return false;
    }
    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      var partnerNiz = gson.fromJson(br, Partner[].class);
      var partnerTok = Stream.of(partnerNiz);
      partnerTok.forEach(p -> this.partneri.put(p.id(), p));
    } catch (IOException ex) {
      return false;
    }
    return true;
  }

  /**
   * Učitavanje kuhinja.<br>
   * Otvara datoteku konfigurcije. Dohvaća kuhinje i sprema ih u kolekciju kuhinje.
   *
   * @return true, ako je uspješno
   */
  private boolean ucitajKuhinje() {
    boolean postojiKuhinja = false;

    kuhinje.clear();
    for (int i = 1; i < 10; i++) {
      var datotekaKuhinja = "kuhinja_" + i;
      var podaciKuhinja = this.konfig.dajPostavku(datotekaKuhinja);

      Path putanja = Paths.get(datotekaKuhinja + ".json");

      if (Files.exists(putanja) && Files.isRegularFile(putanja) && Files.isReadable(putanja)) {
        this.kuhinje.put(i, podaciKuhinja);
        postojiKuhinja = true;
      }
    }
    return postojiKuhinja;
  }

  /**
   * Učitavanje jelovnika.<br>
   * Iterira kroz koleciju kuhinja. Otvara datoteku jelovnika za svaku kuhinju. Dohvaća ih i sprema
   * u kolekciju jelovnika.
   *
   * @return true, ako je uspješno
   */
  private boolean ucitajJelovnike() {

    jelovnici.clear();

    for (var zapis : kuhinje.entrySet()) {
      int kuhinjaId = zapis.getKey();
      var kuhinjaNaziv = zapis.getValue().split(";");
      if (kuhinjaNaziv.length < 2)
        continue;

      var datotekaKuhinja = Path.of("kuhinja_" + kuhinjaId + ".json");
      if (!Files.isReadable(datotekaKuhinja)) {
        continue;
      }

      try (var br = Files.newBufferedReader(datotekaKuhinja)) {
        Gson gson = new Gson();

        var jelovnikNiz = gson.fromJson(br, Jelovnik[].class);
        var mapa = jelovnici.computeIfAbsent(kuhinjaNaziv[0], k -> new ConcurrentHashMap<>());
        for (var jelovnik : jelovnikNiz) {
          mapa.put(jelovnik.id(), jelovnik);
        }

      } catch (IOException e) {
        return false;
      }
    }
    return true;
  }

  /**
   * Učitavanje karte pića.<br>
   * Otvara datoteku karte pića. Dohvaća je i sprema u kolekciju karte pića.
   *
   * @return true, ako je uspješno
   */
  private boolean ucitajKartuPica() {
    var nazivDatoteke = this.konfig.dajPostavku("datotekaKartaPica");
    var datoteka = Path.of(nazivDatoteke);
    if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
      return false;
    }
    kartaPica.clear();
    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      var kartaPicaNiz = gson.fromJson(br, KartaPica[].class);
      var kartaPicaTok = Stream.of(kartaPicaNiz);
      kartaPicaTok.forEach(kp -> this.kartaPica.put(kp.id(), kp));
    } catch (IOException ex) {
      return false;
    }
    return true;
  }
}
