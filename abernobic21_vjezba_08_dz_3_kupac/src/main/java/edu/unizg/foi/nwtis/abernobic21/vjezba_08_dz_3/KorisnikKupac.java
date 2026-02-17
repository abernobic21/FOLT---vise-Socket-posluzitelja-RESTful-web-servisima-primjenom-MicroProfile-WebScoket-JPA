package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

public class KorisnikKupac {

  /** Konfiguracijski podaci */
  private Konfiguracija konfig;

  /** Dozvoljene komande */
  private List<String> dozvoljeneKomande = new ArrayList<String>(
      Arrays.asList("JELOVNIK", "KARTAPIĆA", "NARUDŽBA", "JELO", "PIĆE", "RAČUN"));

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Broj argumenata nije 2.");
      return;
    }

    var program = new KorisnikKupac();
    var nazivKonfiguracije = args[0];
    var nazivDatotekePodataka = args[1];

    program.izvrsiKomande(nazivKonfiguracije, nazivDatotekePodataka);
  }

  /**
   * Učitava konfiguraciju i izvršava komande iz datoteke s podacima liniju po liniju.<br>
   * Datoteka s podacima treba biti formata Korisnik;Adresa;Mrežna vrata;Spavanje;Komanda.
   * 
   * @param nazivKonfiguracije
   * @param nazivDatotekePodataka
   */
  public void izvrsiKomande(String nazivKonfiguracije, String nazivDatotekePodataka) {
    if (!this.ucitajKonfiguraciju(nazivKonfiguracije)) {
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(nazivDatotekePodataka))) {
      String linija;
      while ((linija = reader.readLine()) != null) {
        String[] dijelovi = linija.split(";");
        if (dijelovi.length != 5) {
          continue;
        }

        String korisnik = dijelovi[0];
        String adresa = dijelovi[1];
        int mreznaVrata = Integer.parseInt(dijelovi[2]);
        int spavanje = Integer.parseInt(dijelovi[3]);
        String komanda = dijelovi[4];

        String[] dijeloviKomande = komanda.split(" ");
        if (!dozvoljeneKomande.contains(dijeloviKomande[0]))
          continue;

        if (dijeloviKomande.length > 1) {
          String korisnikKomanda = dijeloviKomande[1].trim();
          if (!korisnikKomanda.equals(korisnik))
            continue;
        }

        try {
          var mreznaUticnica = new Socket(adresa, mreznaVrata);
          BufferedReader in =
              new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
          PrintWriter out =
              new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

          Thread.sleep(spavanje);

          out.write(komanda + "\n");
          out.flush();
          mreznaUticnica.shutdownOutput();
          mreznaUticnica.close();

        } catch (IOException e) {
        }
      }
    } catch (InterruptedException e) {
    } catch (IOException e) {
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
    }
    return false;
  }
}
