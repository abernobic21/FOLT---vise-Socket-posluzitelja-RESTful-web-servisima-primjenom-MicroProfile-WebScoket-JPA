package org.unizg.foi.nwtis.abernobic21.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.dao.KorisnikDAO;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("api/partner")
public class PartnerResource {

  @Inject
  @ConfigProperty(name = "adresaPartner")
  private String adresaPartner;
  @Inject
  @ConfigProperty(name = "mreznaVrataKrajPartner")
  private String mreznaVrataKrajPartner;
  @Inject
  @ConfigProperty(name = "mreznaVrataRadPartner")
  private String mreznaVrataRadPartner;
  @Inject
  @ConfigProperty(name = "kodZaAdminPartnera")
  private String kodZaAdminPartnera;
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;
  @ConfigProperty(name = "idPartner")
  private String idPartner;

  @Inject
  RestConfiguration restConfiguration;

  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response headPosluzitelj() {
    var status = posaljiKomandu("KRAJ xxx", 0);
    if (status != null) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa dijela poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljStatus",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStatus", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStatus(@PathParam("id") int id) {
    var status = posaljiKomandu("STATUS " + this.kodZaAdminPartnera + " " + id, 0);

    if (status.contains("OK 1\n")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja partner u pauzu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljPauza",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPauza(@PathParam("id") int id) {
    var status = posaljiKomandu("PAUZA " + this.kodZaAdminPartnera + " " + id, 0);
    if (status.equals("OK\n")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja partner u rad")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljStart",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStart(@PathParam("id") int id) {
    var status = posaljiKomandu("START " + this.kodZaAdminPartnera + " " + id, 0);
    if (status.equals("OK\n")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @Path("kraj")
  @HEAD
  @Operation(summary = "Zaustavljanje poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljKraj",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKraj", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKraj() {
    var status = posaljiKomandu("KRAJ " + this.kodZaKraj, 0);
    if (status.equals("OK\n")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jelovnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Nije auzotizirano"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
  public Response getJelovnik(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {

    if (!autenticirajKorisnika(korisnickoIme, lozinka)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    var odgovor = posaljiKomandu("JELOVNIK " + korisnickoIme, 1);
    String status = odgovor.split("\n")[0];

    if (status.equals("OK")) {
      String stringificiraniJelovnik = odgovor.split("\n")[1];

      return Response.ok(stringificiraniJelovnik).build();

    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("kartapica")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat karte pica")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Nije autorizirano"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKartapica",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKartapica", description = "Vrijeme trajanja metode")
  public Response getKartapica(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {

    if (!autenticirajKorisnika(korisnickoIme, lozinka)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    var odgovor = posaljiKomandu("KARTAPIĆA " + korisnickoIme, 1);
    String status = odgovor.split("\n")[0];

    if (status.equals("OK")) {
      String stringificiranaKartaPica = odgovor.split("\n")[1];

      return Response.ok(stringificiranaKartaPica).build();

    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("narudzba")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat otvorene narudzbe")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Nije autorizirano"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getNarudzba",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getNarudzba", description = "Vrijeme trajanja metode")
  public Response getNarudzba(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {

    if (!autenticirajKorisnika(korisnickoIme, lozinka)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    var odgovor = posaljiKomandu("STANJE " + korisnickoIme, 1);
    String status = odgovor.split("\n")[0];

    if (status.equals("OK")) {
      String stringificiranaNarudzba = odgovor.split("\n")[1];

      return Response.ok(stringificiranaNarudzba).build();

    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("narudzba")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje narudzbe")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "401", description = "Nije autorizirano"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postNarudzba",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postNarudzba", description = "Vrijeme trajanja metode")
  public Response postNarudzba(Narudzba jelo, @HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {

    if (!autenticirajKorisnika(korisnickoIme, lozinka)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    var odgovor = posaljiKomandu("NARUDŽBA " + korisnickoIme, 1);
    String status = odgovor.split("\n")[0];

    if (status.equals("OK")) {
      return Response.ok().build();
    } else if (status.contains("ERROR")) {
      return Response.status(Response.Status.CONFLICT).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("jelo")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje jela u narudzbu")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "401", description = "Nije autorizirano"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postJelo", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postJelo", description = "Vrijeme trajanja metode")
  public Response postJelo(Narudzba jelo, @HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {
    try {
      if (!autenticirajKorisnika(korisnickoIme, lozinka)) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      if (jelo.jelo() == false || !korisnickoIme.equals(jelo.korisnik())) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      var odgovor =
          posaljiKomandu("JELO " + korisnickoIme + " " + jelo.id() + " " + jelo.kolicina(), 1);
      String status = odgovor.split("\n")[0];

      if (status.equals("OK")) {
        return Response.ok().build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("pice")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje pica u narudzbu")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "401", description = "Nije autorizirano"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPice", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPice", description = "Vrijeme trajanja metode")
  public Response postPice(Narudzba pice, @HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {
    try {
      if (!autenticirajKorisnika(korisnickoIme, lozinka)) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      if (pice.jelo() == true || !korisnickoIme.equals(pice.korisnik())) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      var odgovor =
          posaljiKomandu("PIĆE " + korisnickoIme + " " + pice.id() + " " + pice.kolicina(), 1);
      String status = odgovor.split("\n")[0];

      if (status.equals("OK")) {
        return Response.ok().build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("racun")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(summary = "Zahtjevanje racuna")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "401", description = "Nije autorizirano"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postRacun",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postRacun", description = "Vrijeme trajanja metode")
  public Response postRacun(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {

    if (!autenticirajKorisnika(korisnickoIme, lozinka)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    var odgovor = posaljiKomandu("RAČUN " + korisnickoIme, 1);
    String status = odgovor.split("\n")[0];

    if (status.equals("OK")) {
      return Response.ok().build();
    } else if (status.contains("ERROR")) {
      return Response.status(Response.Status.CONFLICT).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("korisnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKorisnici",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKorisnici", description = "Vrijeme trajanja metode")
  public Response getKorisnici() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korsnici = korisnikDAO.dohvatiSve();
      return Response.ok(korsnici).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("korisnik/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKorisnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKorisnik", description = "Vrijeme trajanja metode")
  public Response getKorisnik(@PathParam("id") String id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korsinik = korisnikDAO.dohvati(id, "", false);
      if (korsinik != null) {
        return Response.ok(korsinik).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("korisnik")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje jednog korisnika")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postKorisnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postKorisnik", description = "Vrijeme trajanja metode")
  public Response postKorisnik(Korisnik korisnik) {

    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var odgovor = korisnikDAO.dodaj(korisnik);
      if (odgovor) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  private boolean autenticirajKorisnika(String korisnickoIme, String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korisnik = korisnikDAO.dohvati(korisnickoIme, lozinka, true);
      return korisnik != null;
    } catch (Exception e) {
      return false;
    }
  }

  @Path("spava")
  @GET
  @Operation(summary = "Šalje zahtjev za spavanje")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getSpava", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getSpava", description = "Vrijeme trajanja metode")
  public Response getSpava(@QueryParam("vrijeme") Long brojSekundi) {

    if (brojSekundi == null || brojSekundi < 0) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    var odgovor = posaljiKomandu("SPAVA " + this.kodZaAdminPartnera + " " + brojSekundi, 0);
    String status = odgovor.split("\n")[0];

    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  private String posaljiKomandu(String komanda, int vrstaPorta) {
    String mreznaVrata = "";
    switch (vrstaPorta) {
      case 0:
        mreznaVrata = this.mreznaVrataKrajPartner;
        break;
      case 1:
        mreznaVrata = this.mreznaVrataRadPartner;
        break;
      default:
        return null;
    }

    try {
      var mreznaUticnica = new Socket(this.adresaPartner, Integer.parseInt(mreznaVrata));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      StringBuilder odgovor = new StringBuilder();
      String linija = in.readLine();
      odgovor.append(linija).append("\n");

      while ((linija = in.readLine()) != null) {
        odgovor.append(linija);
      }
      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
      return odgovor.toString();
    } catch (IOException e) {
    }
    return null;
  }
}
