package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Antonio Bernobić
 */
public class UlogaDAO {
  private Connection vezaBP;

  public UlogaDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  public boolean dodaj(String korisnik) {
    return dodaj(korisnik, "nwtis");
  }

  public boolean dodaj(String korisnik, String grupa) {
    String upit = "INSERT INTO uloge (korisnik, grupa) " + "VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, korisnik);
      s.setString(2, grupa);

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (Exception ex) {
      Logger.getLogger(UlogaDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
