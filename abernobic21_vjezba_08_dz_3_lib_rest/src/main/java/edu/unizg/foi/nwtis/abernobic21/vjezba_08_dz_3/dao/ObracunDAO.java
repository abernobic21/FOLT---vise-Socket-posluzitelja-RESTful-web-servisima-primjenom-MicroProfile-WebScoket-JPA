package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.podaci.Obracun;



/**
 *
 * @author Antonio Bernobić
 */
public class ObracunDAO {
  private Connection vezaBP;

  public ObracunDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }


  public List<Obracun> dohvatiSve(boolean obracunJelo, Long vrijemeOd, Long vrijemeDo) {
    List<Obracun> obracuni = new ArrayList<>();

    StringBuilder sql = new StringBuilder(
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE 1=1 ");

    sql.append("AND jelo = ? ");

    if (vrijemeOd != null) {
      sql.append("AND vrijeme >= ? ");
    }

    if (vrijemeDo != null) {
      sql.append("AND vrijeme <= ? ");
    }

    sql.append("ORDER BY vrijeme");

    try (PreparedStatement ps = vezaBP.prepareStatement(sql.toString())) {
      int paramIndex = 1;

      ps.setBoolean(paramIndex++, obracunJelo);

      if (vrijemeOd != null) {
        ps.setTimestamp(paramIndex++, new Timestamp(vrijemeOd));
      }
      if (vrijemeDo != null) {
        ps.setTimestamp(paramIndex++, new Timestamp(vrijemeDo));
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          int partner = rs.getInt("partner");
          String id = rs.getString("id");
          boolean jelo = rs.getBoolean("jelo");
          float kolicina = rs.getFloat("kolicina");
          float cijena = rs.getFloat("cijena");
          long vrijeme = rs.getTimestamp("vrijeme").getTime();

          Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(obracun);
        }
      }
    } catch (SQLException e) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, e);
    }

    return obracuni;
  }


  public List<Obracun> dohvatiSve(Long vrijemeOd, Long vrijemeDo) {
    List<Obracun> obracuni = new ArrayList<>();

    StringBuilder sql = new StringBuilder(
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE 1=1  ");

    if (vrijemeOd != null) {
      sql.append("AND vrijeme >= ? ");
    }

    if (vrijemeDo != null) {
      sql.append("AND vrijeme <= ? ");
    }

    sql.append("ORDER BY vrijeme");

    try (PreparedStatement ps = vezaBP.prepareStatement(sql.toString())) {
      int paramIndex = 1;
      if (vrijemeOd != null) {
        ps.setTimestamp(paramIndex++, new Timestamp(vrijemeOd));
      }
      if (vrijemeDo != null) {
        ps.setTimestamp(paramIndex++, new Timestamp(vrijemeDo));
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          int partner = rs.getInt("partner");
          String id = rs.getString("id");
          boolean jelo = rs.getBoolean("jelo");
          float kolicina = rs.getFloat("kolicina");
          float cijena = rs.getFloat("cijena");
          long vrijeme = rs.getTimestamp("vrijeme").getTime();

          Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(obracun);
        }
      }
    } catch (SQLException e) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, e);
    }

    return obracuni;
  }

  public List<Obracun> dohvatiSve(int idPartnera, Long vrijemeOd, Long vrijemeDo) {
    List<Obracun> obracuni = new ArrayList<>();

    StringBuilder sql = new StringBuilder(
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE 1=1  ");

    sql.append("AND partner = ? ");

    if (vrijemeOd != null) {
      sql.append("AND vrijeme >= ? ");
    }

    if (vrijemeDo != null) {
      sql.append("AND vrijeme <= ? ");
    }

    sql.append("ORDER BY vrijeme");

    try (PreparedStatement ps = vezaBP.prepareStatement(sql.toString())) {
      int paramIndex = 1;

      ps.setInt(paramIndex++, idPartnera);

      if (vrijemeOd != null) {
        ps.setTimestamp(paramIndex++, new Timestamp(vrijemeOd));
      }
      if (vrijemeDo != null) {
        ps.setTimestamp(paramIndex++, new Timestamp(vrijemeDo));
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          int partner = rs.getInt("partner");
          String id = rs.getString("id");
          boolean jelo = rs.getBoolean("jelo");
          float kolicina = rs.getFloat("kolicina");
          float cijena = rs.getFloat("cijena");
          long vrijeme = rs.getTimestamp("vrijeme").getTime();

          Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(obracun);
        }
      }
    } catch (SQLException e) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, e);
    }

    return obracuni;
  }


  public boolean dodaj(Obracun o) {
    String upit =
        "INSERT INTO obracuni (partner, id, jelo, kolicina, cijena, vrijeme) VALUES (?, ?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setInt(1, o.partner());
      s.setString(2, o.id());
      s.setBoolean(3, o.jelo());
      s.setFloat(4, o.kolicina());
      s.setFloat(5, o.cijena());
      s.setTimestamp(6, new Timestamp(o.vrijeme()));

      int brojAzuriranja = s.executeUpdate();
      return brojAzuriranja == 1;

    } catch (Exception ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
