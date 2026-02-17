package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalniPodaci {

  private int brojObracuna = 0;

  private final Map<Integer, Integer> brojOtvorenihNarudzbi = new ConcurrentHashMap<>();
  private final Map<Integer, Integer> brojRacuna = new ConcurrentHashMap<>();

  public int getBrojObracuna() {
    return brojObracuna;
  }

  public void setBrojObracuna(int noviBrojObracuna) {
    brojObracuna = noviBrojObracuna;
  }

  public void povecajBrojObracuna() {
    brojObracuna++;
  }

  public void smanjiBrojObracuna() {
    brojObracuna--;
  }

  public Map<Integer, Integer> getBrojOtvorenihNarudzbi() {
    return brojOtvorenihNarudzbi;
  }

  public int getBrojOtvorenihNarudzbi(int partnerId) {
    return brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
  }

  public void setBrojOtvorenihNarudzbi(int partnerId, int broj) {
    brojOtvorenihNarudzbi.put(partnerId, broj);
  }

  public void povecajBrojOtvorenihNarudzbi(int partnerId) {
    int trenutni = brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
    brojOtvorenihNarudzbi.put(partnerId, trenutni + 1);
  }

  public void smanjiBrojOtvorenihNarudzbi(int partnerId) {
    int trenutni = brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
    if (trenutni > 1) {
      brojOtvorenihNarudzbi.put(partnerId, trenutni - 1);
    } else {
      brojOtvorenihNarudzbi.remove(partnerId);
    }
  }

  public Map<Integer, Integer> getBrojRacuna() {
    return brojRacuna;
  }

  public int getBrojRacuna(int partnerId) {
    return brojRacuna.getOrDefault(partnerId, 0);
  }

  public void setBrojRacuna(int partnerId, int broj) {
    brojRacuna.put(partnerId, broj);
  }

  public void povecajBrojRacuna(int partnerId) {
    int trenutni = brojRacuna.getOrDefault(partnerId, 0);
    brojRacuna.put(partnerId, trenutni + 1);
  }

  public void smanjiBrojRacuna(int partnerId) {
    int trenutni = brojRacuna.getOrDefault(partnerId, 0);
    if (trenutni > 1) {
      brojRacuna.put(partnerId, trenutni - 1);
    } else {
      brojRacuna.remove(partnerId);
    }
  }
}
