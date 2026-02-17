package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.podaci.Obracun;

public class PosluziteljPartner {

  /** Konfiguracijski podaci */
  private Konfiguracija konfig;

  /** Predložak za kraj */
  private Pattern predlozakKraj = Pattern.compile("^KRAJ$");
  /** Predložak za kraj */
  private Pattern predlozakPartner = Pattern.compile("^PARTNER$");

  private Thread dretvaZaKraj;
  private Thread dretvaZaRad;

  String adresa = null;

  int mreznaVrata = 0;

  /** Id partnera */
  private int idPartnera = 0;
  /** Sigurnosni kod partnera */
  private String sigKod = null;
  /** Naziv partnera */
  private String naziv;

  /** Kolekcija podataka jelovnika */
  private Map<String, Jelovnik> jelovnik = new HashMap<>();
  /** Kolekcija podataka karte pica */
  private Map<String, KartaPica> kartaPica = new HashMap<>();

  /** Kolekcija podataka otvorenih narudzbi */
  private Map<String, List<Narudzba>> otvoreneNarudzbe = new ConcurrentHashMap<>();
  /** Kolekcija podataka placenih narudzbi */
  private Queue<Narudzba> placeneNarudzbe = new ConcurrentLinkedQueue<>();
  /** Broj naplaceni narudzbi */
  private AtomicInteger brojNaplacenihNarudzbi = new AtomicInteger(0);
  /** Izvrsitelj dretvi */
  private ExecutorService executorRadSKlijentima = null;

  /** Objekt za zaključavanje rada s narudžbama */
  private ReentrantLock zakljucavanjeNarudzbi = new ReentrantLock();

  /** Kolekcija aktivnih dretvi */
  private Queue<Future<?>> aktivneDretve = new ConcurrentLinkedQueue<>();

  /** Brojač zatvorenih mrežnih veza poslužitelja. */
  private AtomicInteger zatvoreneUticnice = new AtomicInteger(0);

  /** Mrežna vrata poslužitelja za rad. */
  private ServerSocket mreznaUticnicaPosluzitelj = null;

  /** Mrežna vrata poslužitelja za kraj. */
  private ServerSocket mreznaUticnicaKraj = null;

  /** Zastavica za kraj rada. */
  private AtomicBoolean kraj = new AtomicBoolean(false);

  /** Zastavica za pauzu poslužitelja za rad s klijentima */
  private AtomicBoolean pauzaRad = new AtomicBoolean(false);

  // Logger
  private static final java.util.logging.Logger logger =
      java.util.logging.Logger.getLogger(PosluziteljPartner.class.getName());


  public static void main(String[] args) {


    if (args.length > 2) {


      System.out.println("Broj argumenata je veći od 2.");
      return;
    }

    var program = new PosluziteljPartner();
    var nazivDatoteke = args[0];


    if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }

    if (args.length == 1) {
      program.registrirajPartnera();
      return;
    }

    var linija = args[1];

    var poklapanjeKraj = program.predlozakKraj.matcher(linija);
    if (poklapanjeKraj.matches()) {
      program.posaljiKraj();
      return;
    }

    var poklapanjePartner = program.predlozakPartner.matcher(linija);
    if (poklapanjePartner.matches()) {
      program.pokreniPosluziteljeZaRad();
      return;
    }

  }

  /**
   * Registriraj partnera<br>
   * Šalje komandu za registraciju partnera i sprema sigurnosni kod u konfiguracijsku datoteku.
   */
  private void registrirajPartnera() {
    try {
      var odgovor = posaljiZahtjevaZaRegistraciju();

      if (odgovor == null || !odgovor[0].equals("OK")) {
        return;
      }
      sigKod = odgovor[1];

      if (!konfig.spremiPostavku("sigKod", sigKod)) {
        return;
      }
      konfig.spremiKonfiguraciju();

    } catch (Exception e) {
    }
  }

  /**
   * Slanje zahtjeva za registraciju.<br>
   * Šalje komandu : <br>
   * <code>"PARTNER id "Naziv partnera" vrstaKuhinje adresa mreznaVrata gpsSirina gpsDuzina"</code><br>
   * na adresu "adresa" i mrezna vrata "mreznaVrataRegistacija".<br>
   * Podaci se učitvaju iz konfiguracijske datoteke.
   *
   * @return String[] odgovor
   */
  private String[] posaljiZahtjevaZaRegistraciju() {
    Socket mreznaUticnica = null;
    try {
      var adresaTvrtka = this.konfig.dajPostavku("adresa");
      var mreznaVrataRegistracija =
          Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
      mreznaUticnica = new Socket(adresaTvrtka, mreznaVrataRegistracija);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      int idPartnera = Integer.parseInt(konfig.dajPostavku("id"));
      String naziv = konfig.dajPostavku("naziv");
      String vrstaKuhinje = konfig.dajPostavku("kuhinja");
      String adresa = InetAddress.getLocalHost().getHostAddress();
      int mreznaVrata = Integer.parseInt(konfig.dajPostavku("mreznaVrataRegistracija"));
      float gpsSirina = Float.parseFloat(konfig.dajPostavku("gpsSirina"));
      float gpsDuzina = Float.parseFloat(konfig.dajPostavku("gpsDuzina"));
      int mreznaVrataKraj = Integer.parseInt(konfig.dajPostavku("mreznaVrataKrajPartner"));
      String adminKod = konfig.dajPostavku("kodZaAdmin");

      out.write(String.format("PARTNER %s \"%s\" %s %s %s %s %s %s %s\n", idPartnera, naziv,
          vrstaKuhinje, adresa, mreznaVrata, gpsSirina, gpsDuzina, mreznaVrataKraj, adminKod));
      out.flush();
      mreznaUticnica.shutdownOutput();

      var linija = in.readLine();
      var dijelovi = linija.split(" ");

      return dijelovi;
    } catch (Exception e) {
      return null;
    } finally {
      try {
        mreznaUticnica.shutdownInput();
        mreznaUticnica.close();
      } catch (Exception ex) {
      }
    }
  }

  /**
   * Pošalji kraj.<br>
   * Šalje zahtjeva za kraj rada.
   */
  private void posaljiKraj() {
    var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));

    Socket mreznaUticnica = null;
    try {
      mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "UTF-8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "UTF-8"));
      out.write("KRAJ " + kodZaKraj + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();

      mreznaUticnica.shutdownInput();
      if (linija.equals("OK")) {
        System.out.println("Uspješan kraj poslužitelja.");
      }
    } catch (IOException e) {
    } finally {
      try {
        mreznaUticnica.close();
      } catch (Exception e) {
      }
    }
  }

  /**
   * Pokreni prijem zahtjeve kupaca.<br>
   * Dohvaća jelovnik i kartu pića te poziva pokretanje poslužitelja za rad s kupcima.
   */
  private void pokreniPosluziteljeZaRad() {
    dretvaZaRad = Thread.currentThread();
    try {
      adresa = this.konfig.dajPostavku("adresa");
      mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
      idPartnera = Integer.parseInt(this.konfig.dajPostavku("id"));
      sigKod = this.konfig.dajPostavku("sigKod");

      if (adresa == null || sigKod == null) {
        return;
      }

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.print("\nPrisilno gašenje poslužitelja\n");
        AtomicInteger prisilnoUgaseneDretve = new AtomicInteger(0);
        aktivneDretve.forEach(d -> {
          if (!d.isDone()) {
            d.cancel(true);
            prisilnoUgaseneDretve.incrementAndGet();
          }
        });

        if (dretvaZaKraj != null && dretvaZaKraj.isAlive()) {
          dretvaZaKraj.interrupt();
          prisilnoUgaseneDretve.incrementAndGet();
        }

        try {
          if (mreznaUticnicaPosluzitelj != null && !mreznaUticnicaPosluzitelj.isClosed()) {
            mreznaUticnicaPosluzitelj.close();
          }
          if (mreznaUticnicaKraj != null && !mreznaUticnicaKraj.isClosed()) {
            mreznaUticnicaKraj.close();
          }
        } catch (IOException e) {
          System.out.println("Greška pri zatvaranju server socketa.");
        }


        try {
          Thread.sleep(1000);
        } catch (Exception e) {
        }

        System.out.println("");
        System.out.println("Broj zatvorenih mrežnih utičnica : " + zatvoreneUticnice);
        System.out.println("Broj prisilno ugašenih dretvi: " + prisilnoUgaseneDretve);

      }));

      jelovnik = dohvatiJelovnik(adresa, mreznaVrata, idPartnera, sigKod);
      kartaPica = dohvatiKartuPica(adresa, mreznaVrata, idPartnera, sigKod);


      if (jelovnik == null || kartaPica == null) {

        System.out.println("Kraj rada");
        return;
      }

      dretvaZaKraj = Thread.startVirtualThread(() -> pokreniPosluziteljZaKraj());

      pokreniPosluziteljZaRadSKupcima();

    } catch (Exception e) {
      return;
    }
  }

  /**
   * Pokreni poslužitelj za rad s kupcima.<br>
   * Kreira izvršitelj dreti, otvara mrežna vrata na mrežnim vratima <code>mreznaVrata</code> i
   * osluškuje.<br>
   * Za svaki zahtjev izvršitelj kreira novu virtualnu dretvu te ona obrađuje zahtjev.
   */
  private void pokreniPosluziteljZaRadSKupcima() {
    var builderRadSKlijentima = Thread.ofVirtual().name("Dretva rad s klijentima", 0);
    var factoryRadSKlijentima = builderRadSKlijentima.factory();
    this.executorRadSKlijentima = Executors.newThreadPerTaskExecutor(factoryRadSKlijentima);

    int mreznaVrata = 0, pauzaDretve = 0, brojCekaca = 0;
    try {
      mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
      pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
      brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
    } catch (NumberFormatException nfe) {
      return;
    }

    try {
      this.mreznaUticnicaPosluzitelj = new ServerSocket(mreznaVrata, brojCekaca);

      while (!this.kraj.get()) {
        try {
          Socket klijent = mreznaUticnicaPosluzitelj.accept();
          aktivneDretve.add(executorRadSKlijentima.submit(() -> obradiKlijenta(klijent)));
        } catch (IOException e) {
          break;
        }

        try {
          Thread.sleep(pauzaDretve);
        } catch (InterruptedException e) {
          break;
        }
      }

    } catch (IOException e) {
    } catch (Exception e) {

    } finally {
      if (mreznaUticnicaPosluzitelj != null && !mreznaUticnicaPosluzitelj.isClosed()) {
        try {
          mreznaUticnicaPosluzitelj.close();
        } catch (IOException e) {
        }
      }
    }

  }

  /**
   * Obradi klijenta.<br>
   * Čita komandu s mrežne utičnice te poziva odgovarajuću funkciju obrade.
   *
   * @param mreznaUticnica the mrezna uticnica
   */
  private void obradiKlijenta(Socket mreznaUticnica) {

    try (
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "UTF-8"));
        PrintWriter out =
            new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "UTF-8"));) {

      if (pauzaRad.get()) {
        out.print("ERROR 48 - Poslužitelj za prijem zahtjeva kupaca u pauzi\n");
        out.flush();
        return;
      }

      String unos = in.readLine();

      String[] dijelovi = unos.split(" ");
      String komanda = dijelovi[0];

      switch (komanda) {
        case "JELOVNIK":
          dohvatiJelovnikKorisnik(unos, out);
          break;
        case "KARTAPIĆA":
          dohvatiKartaPicaKorisnik(unos, out);
          break;
        case "NARUDŽBA":
          kreirajNarudžbu(unos, out);
          break;
        case "JELO":
          dodajJelo(unos, out);
          break;
        case "PIĆE":
          dodajPiće(unos, out);
          break;
        case "RAČUN":
          kreirajRacun(unos, out);
          break;
        case "STANJE":
          dajStanje(unos, out);
          break;
        default:
          out.print("ERROR 40 - Format komande nije ispravan\n");
      }

      out.flush();
      mreznaUticnica.shutdownOutput();

    } catch (SocketException e) {

    } catch (IOException e) {
    } finally {
      try {
        mreznaUticnica.close();
        if (Thread.currentThread().isInterrupted())
          zatvoreneUticnice.incrementAndGet();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Dohvati jelovnik.<br>
   * Dohvaća jelovnik za vrstu kuhinje partnera.
   *
   * @param adresa adresa poslužitelja tvrtka
   * @param mreznaVrata mrežna vrata poslužitelja tvrtka
   * @param id id partnera
   * @param sigKod sigurnosi kod partnera
   * @return jelovik formata {@code Map<idJelovnika, jelovnik>}
   */
  private Map<String, Jelovnik> dohvatiJelovnik(String adresa, int mreznaVrata, int id,
      String sigKod) {
    try (var mreznaUticnica = new Socket(adresa, mreznaVrata)) {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "UTF-8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "UTF-8"));

      jelovnik.clear();

      out.write(String.format("JELOVNIK %s %s\n", id, sigKod));
      out.flush();
      mreznaUticnica.shutdownOutput();

      var poruka = in.readLine();
      mreznaUticnica.shutdownInput();

      if (poruka.equals("OK")) {

        StringBuilder jsonOdgovor = new StringBuilder();
        String linija;

        while ((linija = in.readLine()) != null) {

          jsonOdgovor.append(linija).append("\n");
          if (linija.trim().contains("]"))
            break;
        }
        mreznaUticnica.close();

        String stringificiraniJelovnik = jsonOdgovor.toString();

        Gson gson = new Gson();
        Jelovnik[] jelovnikPolje = gson.fromJson(stringificiraniJelovnik, Jelovnik[].class);
        Map<String, Jelovnik> jelovnik = new HashMap<>();

        var jelovnikTok = Stream.of(jelovnikPolje);
        jelovnikTok.forEach(j -> jelovnik.put(j.id(), j));

        return jelovnik;
      }
    } catch (IOException e) {

    } catch (Exception e) {

    }

    return null;
  }

  /**
   * Dohvati kartu pića.<br>
   * Dohvaća kartu pića za vrstu kuhinje partnera.
   *
   * @param adresa adresa poslužitelja tvrtka
   * @param mreznaVrata mrežna vrata poslužitelja tvrtka
   * @param id id partnera
   * @param sigKod sigurnosi kod partnera
   * @return karta pića formata {@code Map<idKartePica, kartaPica>}
   */
  private Map<String, KartaPica> dohvatiKartuPica(String adresa, int mreznaVrata, int id,
      String sigKod) {
    try (var mreznaUticnica = new Socket(adresa, mreznaVrata)) {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "UTF-8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "UTF-8"));

      kartaPica.clear();

      out.write(String.format("KARTAPIĆA %s %s\n", idPartnera, sigKod));
      out.flush();
      mreznaUticnica.shutdownOutput();

      var poruka = in.readLine();
      mreznaUticnica.shutdownInput();

      if (poruka.equals("OK")) {
        StringBuilder jsonOdgovor = new StringBuilder();
        String linija;

        while ((linija = in.readLine()) != null) {
          jsonOdgovor.append(linija).append("\n");
          if (linija.trim().contains("]"))
            break;
        }

        String stringificiranaKartaPica = jsonOdgovor.toString();

        Gson gson = new GsonBuilder().create();
        KartaPica[] kartaPicaPolje = gson.fromJson(stringificiranaKartaPica, KartaPica[].class);

        Map<String, KartaPica> kartaPica = new HashMap<>();

        var kartaPicaTok = Stream.of(kartaPicaPolje);
        kartaPicaTok.forEach(kp -> kartaPica.put(kp.id(), kp));

        return kartaPica;
      }
    } catch (IOException e) {

    } catch (Exception e) {
    }

    return null;
  }



  /**
   * Obrada komande za dohvat jelovnika.<br>
   * Provjerava komandu. Zatim stringificira jelonik te ga šalje tu mrežnu utičnicu.<br>
   * Format odgovora: <br>
   * <code>OK</code><br>
   * <code>{jsonJelovnik}</code>
   *
   * @param unos komanda zahtjeva (format "JELOVNIK {korisnik}")
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void dohvatiJelovnikKorisnik(String unos, PrintWriter out) {
    try {
      if (!provjeriKomandu(unos, "JELOVNIK")) {
        out.print("ERROR 40 - Format komande nije ispravan\n");
        return;
      }

      Gson gson = new Gson();
      String json = gson.toJson(jelovnik);
      out.print("OK\n" + json + "\n");
    } catch (Exception e) {
    }
  }



  /**
   * Obrada komande za dohvat karte pića.<br>
   * Provjerava komandu. Zatim stringificira kartu pića te ju šalje tu mrežnu utičnicu.<br>
   * Format odgovora: <br>
   * <code>OK</code><br>
   * <code>{jsonKartaPica}</code>
   *
   * @param unos komanda zahtjeva (format "KARTAPIĆA {korisnik}")
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void dohvatiKartaPicaKorisnik(String unos, PrintWriter out) {
    try {
      if (!provjeriKomandu(unos, "KARTAPIĆA")) {
        out.print("ERROR 40 - Format komande nije ispravan\n");
      } else {
        Gson gson = new Gson();
        String json = gson.toJson(kartaPica);
        out.print("OK\n" + json + "\n");
      }
    } catch (Exception e) {
    }
  }

  /**
   * Obrada komande za kreiranje nove narudžbe korisnika.<br>
   * Provjerava komandu. Pokušava otvoriti novu narudžbu za korisnika. <br>
   * <br>
   * Ako narudžba za korisnika već postoji, vraća grešku:<br>
   * <code>ERROR 44 - Već postoji otvorena narudžba za korisnika/kupca</code><br>
   * Ako je komanda neispravna:<br>
   * <code>ERROR 40 - Format komande nije ispravan</code><br>
   * U slučaju uspjeha:<br>
   * <code>OK</code>
   *
   * @param unos komanda zahtjeva (format NARUDŽBA {korisnik})
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void kreirajNarudžbu(String unos, PrintWriter out) {
    if (!provjeriKomandu(unos, "NARUDŽBA")) {
      out.print("ERROR 40 - Format komande nije ispravan\n");
      return;
    }
    String korisnik = unos.split(" ")[1];
    try {
      zakljucavanjeNarudzbi.lockInterruptibly();

      if (otvoreneNarudzbe.containsKey(korisnik)) {
        out.print("ERROR 44 - Već postoji otvorena narudžba za korisnika/kupca\n");
        return;
      }
      otvoreneNarudzbe.put(korisnik, new ArrayList<Narudzba>());
      out.print("OK\n");

    } catch (InterruptedException ie) {

    } catch (Exception e) {
      out.print("ERROR 49 - Nešto drugo nije u redu\n");
    } finally {
      zakljucavanjeNarudzbi.unlock();
    }

  }


  /**
   * Obrada komande za dodavanje jela u narudžbu korisnika.<br>
   * Provjerava komandu. Pokušava otvoriti novu narudžbu za korisnika. Dodaje jelo u otvorenu
   * narudžbu. <br>
   * <br>
   * Ako je komanda neispravna:<br>
   * <code>ERROR 40 - Format komande nije ispravan</code><br>
   * Ako je količina manja od 1, vraća grešku:<br>
   * <code>ERROR 40 - Format komande nije ispravan</code><br>
   * Ako narudžba za korisnika ne postoji, vraća grešku:<br>
   * <code>ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca </code><br>
   * Ako ne postoji jelo u jelovniku s tim idJela, vraća grešku:<br>
   * <code>ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera</code><br>
   * Ako dođe do neke druge greške, vraća:<br>
   * <code>ERROR 49 - Nešto drugo nije u redu.</code><br>
   * U slučaju uspjeha:<br>
   * <code>OK</code>
   *
   * @param unos komanda zahtjeva (format JELO {korisnik} {idJela} {kolicina})
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void dodajJelo(String unos, PrintWriter out) {
    if (!provjeriKomandu(unos, "JELO")) {
      out.print("ERROR 40 - Format komande nije ispravan\n");
      return;
    }
    String[] dijelovi = unos.split(" ");
    String korisnik = dijelovi[1];
    String idJela = dijelovi[2];
    Float kolicina = Float.parseFloat(dijelovi[3]);

    if (kolicina < 1) {
      out.print("ERROR 49 - Neispravna količina!\n");
      return;
    }

    try {
      zakljucavanjeNarudzbi.lockInterruptibly();

      if (!otvoreneNarudzbe.containsKey(korisnik)) {
        out.print("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca \n");
        return;
      }
      if (!jelovnik.containsKey(idJela)) {
        out.print("ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera\n");
        return;
      }
      Float cijena = jelovnik.get(idJela).cijena();
      Narudzba novaNarudzba =
          new Narudzba(korisnik, idJela, true, kolicina, cijena, System.currentTimeMillis());
      otvoreneNarudzbe.get(korisnik).add(novaNarudzba);
      out.print("OK\n");
      return;

    } catch (InterruptedException ie) {

    } catch (Exception e) {
      out.print("ERROR 49 - Nešto drugo nije u redu.\n");

    } finally {
      zakljucavanjeNarudzbi.unlock();
    }
  }

  /**
   * Obrada komande za dodavanje pića u narudžbu korisnika.<br>
   * Provjerava komandu. Pokušava otvoriti novu narudžbu za korisnika. Dodaje piće u otvorenu
   * narudžbu. <br>
   * <br>
   * Ako je komanda neispravna:<br>
   * <code>ERROR 40 - Format komande nije ispravan</code><br>
   * Ako je količina manja od 0, vraća grešku:<br>
   * <code>ERROR 40 - Format komande nije ispravan</code><br>
   * Ako narudžba za korisnika ne postoji, vraća grešku:<br>
   * <code>ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca </code><br>
   * Ako ne postoji piće u karti pića s tim idPica, vraća grešku:<br>
   * <code>ERROR 42 - Ne postoji piće s id u kolekciji karte pića kod partnera</code><br>
   * Ako dođe do neke druge greške, vraća:<br>
   * <code>ERROR 49 - Nešto drugo nije u redu.</code><br>
   * U slučaju uspjeha:<br>
   * <code>OK</code>
   *
   * @param unos komanda zahtjeva (format PIĆE {korisnik} {idPica} {kolicina})
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void dodajPiće(String unos, PrintWriter out) {
    if (!provjeriKomandu(unos, "PIĆE")) {
      out.print("ERROR 40 - Format komande nije ispravan\n");
      return;
    }
    String[] dijelovi = unos.split(" ");
    String korisnik = dijelovi[1];
    String idPica = dijelovi[2];
    Float kolicina = Float.parseFloat(dijelovi[3]);

    if (kolicina < 0) {
      out.print("ERROR 49 - Neispravna količina!\n");
      return;
    }

    try {
      zakljucavanjeNarudzbi.lockInterruptibly();

      if (!otvoreneNarudzbe.containsKey(korisnik)) {
        out.print("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca \n");
        return;
      }
      if (!kartaPica.containsKey(idPica)) {
        out.print("ERROR 42 - Ne postoji piće s id u kolekciji karte pića kod partnera\n");
        return;
      }
      Float cijena = kartaPica.get(idPica).cijena();
      Narudzba novaNarudzba =
          new Narudzba(korisnik, idPica, false, kolicina, cijena, System.currentTimeMillis());

      otvoreneNarudzbe.get(korisnik).add(novaNarudzba);

      out.print("OK\n");

    } catch (InterruptedException ie) {

    } catch (Exception e) {
      out.print("ERROR 49 - Nešto drugo nije u redu.\n");

    } finally {
      zakljucavanjeNarudzbi.unlock();
    }
  }

  /**
   * Obrada komande za kreiranje računa.<br>
   * Provjera komandu. Provjerava postojanje otvorene narudžbe. Poziva funkciju za obradu računa.
   * <br>
   * Ako je komanda neispravna:<br>
   * <code>ERROR 40 - Format komande nije ispravan</code><br>
   * Ako narudžba za korisnika ne postoji, vraća grešku:<br>
   * <code>ERROR 49 - Nešto drugo nije u redu.</code><br>
   * U slučaju uspjeha:<br>
   * <code>OK</code>
   *
   * @param unos komanda zahtjeva (format RAČUN {korisnik})
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void kreirajRacun(String unos, PrintWriter out) {
    String korisnik = unos.split(" ")[1];

    if (!provjeriKomandu(unos, "RAČUN")) {
      out.print("ERROR 40 - Format komande nije ispravan\n");
      return;
    }

    try {
      zakljucavanjeNarudzbi.lockInterruptibly();

      if (!otvoreneNarudzbe.containsKey(korisnik)) {
        out.print("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca \n");
        return;
      }

      if (obradiRacun(korisnik))
        out.print("OK\n");

    } catch (InterruptedException ie) {

    } catch (NumberFormatException nfe) {
      out.print("ERROR 49 - Neispravni konfiguracijski pdoaci.\n");

    } catch (Exception e) {
      out.print("ERROR 49 - Nešto drugo nije u redu.\n");

    } finally {
      zakljucavanjeNarudzbi.unlock();
    }
  }


  /**
   * Obrada računa.<br>
   * Inkrementira broj plaćenih narudžbi. Ako je broj plaćenih narudžbi višekratnik broja kvota
   * narudžbi poziva pripremu obračuna te slanje obračuna.
   *
   * @param korisnik korisnik
   * @return true, ako je operacija uspješna
   */
  private boolean obradiRacun(String korisnik) {

    placeneNarudzbe.addAll(otvoreneNarudzbe.remove(korisnik));

    if (brojNaplacenihNarudzbi.incrementAndGet()
        % Integer.parseInt(konfig.dajPostavku("kvotaNarudzbi")) == 0) {
      var obracuni = pripremiObracun();
      placeneNarudzbe.clear();
      return posaljiObracun(obracuni);
    } else {
      return true;
    }
  }


  /**
   * Priprema obrauna.<br>
   * Zbraja količinu artikla prema id-u. Kreira obračun.
   *
   * @param korisnik korisnik
   * @return lista obraćuna
   */
  private List<Obracun> pripremiObracun() {
    List<Obracun> obracuni = new ArrayList<>();

    Map<String, List<Narudzba>> grupiraneNarudzbe =
        placeneNarudzbe.stream().collect(Collectors.groupingBy(Narudzba::id));

    for (Map.Entry<String, List<Narudzba>> grupiraniId : grupiraneNarudzbe.entrySet()) {
      String id = grupiraniId.getKey();
      List<Narudzba> narudzbePoId = grupiraniId.getValue();

      float kolicina = 0;
      boolean jelo = narudzbePoId.get(0).jelo();
      float cijena = narudzbePoId.get(0).cijena();
      long vrijeme = System.currentTimeMillis();

      for (Narudzba narudzba : narudzbePoId) {
        kolicina += narudzba.kolicina();
      }

      obracuni.add(new Obracun(idPartnera, id, jelo, kolicina, cijena, vrijeme));

    }
    return obracuni;
  }

  /**
   * Slanje obračun poslužitelju tvrtka.<br>
   * Čita podatke o mrežnoj utičnici iz konfiguracije te šalje poslužitelju tvrtci komandu
   * formata:<br>
   * <code>OBRAČUN id sigurnosniKod<br>
  jsonPodaciObračuna
  </code>
   * 
   * @param obracuni lista obračuna
   * @return true, ako poslužitelj tvrtka vrati odgovor <code>OK</code>
   */
  private boolean posaljiObracun(List<Obracun> obracuni) {
    var adresaTvrtka = this.konfig.dajPostavku("adresa");
    var mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
    try (var mreznaUticnica = new Socket(adresaTvrtka, mreznaVrataRad)) {

      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "UTF-8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "UTF-8"));

      Gson gson = new Gson();
      String stringificiraniObracuni = gson.toJson(obracuni);

      out.write(String.format("OBRAČUN %s %s\n%s\n", idPartnera, sigKod, stringificiraniObracuni));
      out.flush();
      mreznaUticnica.shutdownOutput();

      var poruka = in.readLine();
      mreznaUticnica.shutdownInput();

      if (poruka.equals("OK")) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Daj stanje. <br>
   * Vraća stanje orvorene narudzbe.
   * 
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  private void dajStanje(String unos, PrintWriter out) {
    String korisnik = unos.split(" ")[1];

    if (!provjeriKomandu(unos, "STANJE")) {
      out.print("ERROR 40 - Format komande nije ispravan\n");
      return;
    }

    var narudzba = otvoreneNarudzbe.get(korisnik);

    if (narudzba == null) {
      out.print("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca \n");
      return;
    }
    Gson gson = new Gson();
    var stringificiranaNarudzba = gson.toJson(narudzba);

    out.print("OK\n" + stringificiranaNarudzba + "\n");
  }

  /**
   * Provjera komande.<br>
   * Ovisno o određenoj komandi, provjerava poklapanje s odabranim predloškom.
   *
   * @param unos tekst zahtjeva
   * @param komanda komanda koju se provjerava
   * @return true, ako je uspješni format komande zahtjeva
   */
  private boolean provjeriKomandu(String unos, String komanda) {
    String regex = "";
    switch (komanda) {
      case "JELOVNIK":
        regex = "^JELOVNIK\\s([A-Za-z0-9]+)$";
        break;
      case "KARTAPIĆA":
        regex = "^KARTAPIĆA\\s([A-Za-z0-9]+)$";
        break;
      case "NARUDŽBA":
        regex = "^NARUDŽBA\\s([A-Za-z0-9]+)$";
        break;
      case "JELO":
        regex = "^JELO\\s([A-Za-z0-9]+)\\s([A-Za-z0-9]+)\\s([0-9]+\\.[0-9]+)$";
        break;
      case "PIĆE":
        regex = "^PIĆE\\s([A-Za-z0-9]+)\\s([A-Za-z0-9]+)\\s([0-9]+\\.[0-9]+)$";
        break;
      case "RAČUN":
        regex = "^RAČUN\\s([A-Za-z0-9]+)$";
        break;
      case "STANJE":
        regex = "^STANJE\\s([A-Za-z0-9]+)$";
        break;
      case "KRAJ":
        regex = "^KRAJ\\s+([A-Za-z0-9]+)$";
        break;
      case "STATUS":
        regex = "^STATUS\\s+([A-Za-z0-9]+)\\s+1$";
        break;
      case "PAUZA":
        regex = "^PAUZA\\s+([A-Za-z0-9]+)\\s+1$";
        break;
      case "START":
        regex = "^START\\s+([A-Za-z0-9]+)\\s+1$";
        break;
      case "SPAVA":
        regex = "^SPAVA\\s+([A-Za-z0-9]+)\\s+(\\d+)$";
        break;
      case "OSVJEŽI":
        regex = "^OSVJEŽI\\s+([A-Za-z0-9]+)$";
        break;
    }
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(unos);

    return matcher.matches();
  }

  private void pokreniPosluziteljZaKraj() {

    int mreznaVrata = 0, pauzaDretve = 0, brojCekaca = 0;
    try {
      mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKrajPartner"));
      brojCekaca = 0;
    } catch (NumberFormatException nfe) {
      return;
    }

    try {
      this.mreznaUticnicaKraj = new ServerSocket(mreznaVrata, brojCekaca);

      while (!this.kraj.get()) {
        var mreznaUticnica = mreznaUticnicaKraj.accept();
        this.obradiKraj(mreznaUticnica);
      }

      ugasiDretve();

    } catch (IOException e) {
    } catch (Exception e) {

    } finally {
      if (mreznaUticnicaKraj != null && !mreznaUticnicaKraj.isClosed()) {
        try {
          mreznaUticnicaKraj.close();
        } catch (IOException e) {
        }
      }
    }

  }

  private void ugasiDretve() {
    aktivneDretve.forEach(d -> {
      if (!d.isDone()) {
        d.cancel(true);
      }
    });

    if (dretvaZaRad.isAlive()) {
      dretvaZaRad.interrupt();;
    }


    try {
      if (mreznaUticnicaKraj != null && !mreznaUticnicaKraj.isClosed()) {
        mreznaUticnicaKraj.close();
      }
      if (mreznaUticnicaPosluzitelj != null && !mreznaUticnicaPosluzitelj.isClosed()) {
        mreznaUticnicaPosluzitelj.close();
      }
    } catch (IOException e) {
    }
  }

  /**
   * Obrada kraja.<br>
   * Provejerava komandu i kod za kraj. Postavlja zastavicu kraja rada na true.
   *
   * @param mreznaUticnica mrežna uticnica
   */
  private void obradiKraj(Socket mreznaUticnica) {
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
        case "OSVJEŽI":
          osvjezi(unos, out);
          break;
        default:
          out.print("ERROR 60 - Format komande nije ispravan\n");
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
   * Provjerava komandu i kod za kraj. Ako su uspješne podize zastavicu za kraj rada.
   *
   * @param komanda komanda koju se provjerava
   * @param out izlazni tok za pisanje odgovora u mrežnu utičnicu
   */
  public void krajRada(String komanda, PrintWriter out) {
    if (!provjeriKomandu(komanda, "KRAJ")) {
      out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
      return;
    }
    var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");

    if (!komanda.trim().equals("KRAJ " + kodZaKraj)) {
      out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
      return;
    }

    this.kraj.set(true);

    out.write("OK\n");
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
        out.print("ERROR 60 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];
      int dioPosluzitelja = Integer.parseInt(komanda.split(" ")[2]);

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdmin"))) {
        out.print("ERROR 61 - Pogrešan kodZaAdminPartnera \n");
        return;
      }

      int status = 0;
      switch (dioPosluzitelja) {
        case 1:
          status = pauzaRad.get() ? 0 : 1;
          break;
      }

      out.printf("OK %d\n", status);

    } catch (Exception e) {
      out.print("ERROR 69 - Nešto drugo nije u redu\n");
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
        out.print("ERROR 60 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];
      int dioPosluzitelja = Integer.parseInt(komanda.split(" ")[2]);

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdmin"))) {
        out.print("ERROR 61 - Pogrešan kodZaAdminPartnera \n");
        return;
      }

      switch (dioPosluzitelja) {
        case 1:
          if (pauzaRad.get()) {
            out.print("ERROR 62 - Pogrešna promjena pauze ili starta\n");
          } else {
            pauzaRad.set(true);
            out.print("OK\n");
          }
          break;
      }

    } catch (Exception e) {
      out.print("ERROR 69 - Nešto drugo nije u redu\n");
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
        out.print("ERROR 60 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];
      int dioPosluzitelja = Integer.parseInt(komanda.split(" ")[2]);

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdmin"))) {
        out.print("ERROR 61 - Pogrešan kodZaAdminPartnera \n");
        return;
      }

      switch (dioPosluzitelja) {
        case 1:
          if (pauzaRad.get()) {
            pauzaRad.set(false);
            out.print("OK\n");
          } else {
            out.print("ERROR 62 - Pogrešna promjena pauze ili starta\n");
          }
          break;
      }

    } catch (Exception e) {
      out.print("ERROR 69 - Nešto drugo nije u redu\n");
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
        out.print("ERROR 60 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];
      int n = Integer.parseInt(komanda.split(" ")[2]);

      if (n < 0) {
        out.print("ERROR 60 - Format komande nije ispravan\n");
        return;
      }

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdmin"))) {
        out.print("ERROR 61 - Pogrešan kodZaAdminPartnera \n");
        return;
      }

      try {
        Thread.sleep(n);
        out.print("OK\n");
      } catch (InterruptedException e) {
        out.print("ERROR 63 – Prekid spavanja dretve\n");
      }

    } catch (Exception e) {
      out.print("ERROR 69 - Nešto drugo nije u redu\n");
    }
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
        out.print("ERROR 60 - Format komande nije ispravan\n");
        return;
      }
      String kodZaAdmina = komanda.split(" ")[1];

      if (!kodZaAdmina.equals(this.konfig.dajPostavku("kodZaAdmin"))) {
        out.print("ERROR 61 - Pogrešan kodZaAdminPartnera \n");
        return;
      }

      if (pauzaRad.get()) {
        out.print("ERROR 69 - Poslužitelj za rad klijenata u pauzi\n");
        return;
      }

      try {

        jelovnik = dohvatiJelovnik(adresa, mreznaVrata, idPartnera, sigKod);
        kartaPica = dohvatiKartuPica(adresa, mreznaVrata, idPartnera, sigKod);

        out.print("OK\n");
      } catch (Exception e) {
        out.print("ERROR 19 - Nemoguće učitavanje karte piće ili jelovnika\n");
        return;
      }

    } catch (Exception e) {
      out.print("ERROR 19 - Nešto drugo nije u redu\n");
    }
  }

  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke naziv datoteke
   * @return true, ako je uspješno učitavanje konfiguracije
   */
  private boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      return false;
    }
  }
}
