package org.unizg.foi.nwtis.abernobic21.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.unizg.foi.nwtis.abernobic21.TvrtkaInfoClient;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.dao.ObracunDAO;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.dao.PartnerDAO;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/tvrtka")
public class TvrtkaResource {

  @Inject
  @ConfigProperty(name = "adresa")
  private String tvrtkaAdresa;
  @Inject
  @ConfigProperty(name = "mreznaVrataKraj")
  private String mreznaVrataKraj;
  @Inject
  @ConfigProperty(name = "mreznaVrataRegistracija")
  private String mreznaVrataRegistracija;
  @Inject
  @ConfigProperty(name = "mreznaVrataRad")
  private String mreznaVrataRad;
  @Inject
  @ConfigProperty(name = "kodZaAdminTvrtke")
  private String kodZaAdminTvrtke;
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;

  @Inject
  RestConfiguration restConfiguration;

  @Inject
  @RestClient
  TvrtkaInfoClient tvrtkaInfoClient;


  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response headPosluzitelj() {
    var status = posaljiKomandu("KRAJWS xxx", 0);
    if (status != null) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa dijela poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_eadPosluziteljStatus",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_eadPosluziteljStatus", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStatus(@PathParam("id") int id) {
    var status = posaljiKomandu("STATUS " + this.kodZaAdminTvrtke + " " + id, 0);

    if (status.contains("OK 1\n")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u pauzu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljPauza",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPauza(@PathParam("id") int id) {
    var status = posaljiKomandu("PAUZA " + this.kodZaAdminTvrtke + " " + id, 0);
    if (status.equals("OK\n")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u rad")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljStart",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStart(@PathParam("id") int id) {
    var status = posaljiKomandu("START " + this.kodZaAdminTvrtke + " " + id, 0);
    if (status.equals("OK\n")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @Path("kraj")
  @HEAD
  @Operation(summary = "Zaustavljanje poslužitelja tvrtka")
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

  @Path("kraj/info")
  @HEAD
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKrajInfo() {
    try {
      Response getResponse = tvrtkaInfoClient.getKrajInfo();

      if (getResponse.getStatus() == 200) {
        return Response.status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NO_CONTENT).build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }


  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih jelovnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnici",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnici", description = "Vrijeme trajanja metode")
  public Response getJelovnici() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);

      Set<Jelovnik> jelovnici = new HashSet<>();
      for (Partner partner : partneri) {
        var odgovor = posaljiKomandu("JELOVNIK " + partner.id() + " " + partner.sigurnosniKod(), 2);
        String status = odgovor.split("\n")[0];
        if (status.equals("OK")) {
          String stringificiraniJelovnik = odgovor.split("\n")[1];

          Gson gson = new Gson();
          Jelovnik[] jelovnik = gson.fromJson(stringificiraniJelovnik, Jelovnik[].class);

          jelovnici.addAll(Arrays.asList(jelovnik));
        }
      }

      if (jelovnici.isEmpty()) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
      return Response.ok(jelovnici).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("jelovnik/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jelovnika partner s id")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Nije pronađeno"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
  public Response getJelovnik(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partner = partnerDAO.dohvati(id, false);

      if (partner == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      var odgovor = posaljiKomandu("JELOVNIK " + partner.id() + " " + partner.sigurnosniKod(), 2);
      String status = odgovor.split("\n")[0];

      if (status.equals("OK")) {
        String stringificiraniJelovnik = odgovor.split("\n")[1];

        Gson gson = new Gson();
        Jelovnik[] jelovnik = gson.fromJson(stringificiraniJelovnik, Jelovnik[].class);
        return Response.ok(jelovnik).build();

      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("kartapica")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih karta pica")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKartapica",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKartapica", description = "Vrijeme trajanja metode")
  public Response getKartapica() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);

      Gson gson = new Gson();

      for (Partner partner : partneri) {
        var odgovor =
            posaljiKomandu("KARTAPIĆA " + partner.id() + " " + partner.sigurnosniKod(), 2);
        String status = odgovor.split("\n")[0];
        if (status.equals("OK")) {
          String stringificiranaKartaPica = odgovor.split("\n")[1];

          KartaPica[] kartaPica = gson.fromJson(stringificiranaKartaPica, KartaPica[].class);
          return Response.ok(kartaPica).build();
        }
      }
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartneri",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartneri", description = "Vrijeme trajanja metode")
  public Response getPartneri() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(true);
      return Response.ok(partneri).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner/provjera")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat i provjera svih partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartneriProvjera",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartneriProvjera", description = "Vrijeme trajanja metode")
  public Response getPartneriProvjera() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneriBaza = partnerDAO.dohvatiSve(true);

      var odgovor = posaljiKomandu("POPIS", 1);
      String status = odgovor.split("\n")[0];

      if (status.equals("OK")) {
        String stringificiraniPartnerPopis = odgovor.split("\n")[1];

        Gson gson = new Gson();
        PartnerPopis[] partnerPopis =
            gson.fromJson(stringificiraniPartnerPopis, PartnerPopis[].class);

        Set<Integer> partneriBazaIdSet =
            partneriBaza.stream().map(p -> p.id()).collect(Collectors.toSet());

        List<PartnerPopis> zajednickiPartneri = Arrays.stream(partnerPopis)
            .filter(p -> partneriBazaIdSet.contains(p.id())).collect(Collectors.toList());

        return Response.ok(zajednickiPartneri).build();

      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartner", description = "Vrijeme trajanja metode")
  public Response getPartner(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partner = partnerDAO.dohvati(id, true);
      if (partner != null) {
        return Response.ok(partner).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje jednog partnera")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPartner", description = "Vrijeme trajanja metode")
  public Response postPartner(Partner partner) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var odgovor = partnerDAO.dodaj(partner);
      if (odgovor) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("obracun")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih obračuna ili filtriranih po vremenu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracuni",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracuni", description = "Vrijeme trajanja metode")
  public Response getObracuni(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni;

      obracuni = obracunDAO.dohvatiSve(vrijemeOd, vrijemeDo);

      return Response.ok(obracuni).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("obracun/jelo")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obracuna s jelima")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracunJelo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracunJelo", description = "Vrijeme trajanja metode")
  public Response getObracunJelo(@QueryParam("od") Long vrijemeOd,
      @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni;

      obracuni = obracunDAO.dohvatiSve(true, vrijemeOd, vrijemeDo);

      return Response.ok(obracuni).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("obracun/pice")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obracuna s picima")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracunPice",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracunPice", description = "Vrijeme trajanja metode")
  public Response getObracunPice(@QueryParam("od") Long vrijemeOd,
      @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni;

      obracuni = obracunDAO.dohvatiSve(false, vrijemeOd, vrijemeDo);

      return Response.ok(obracuni).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("obracun/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih obračuna partnera s id ili filtriranih po vremenu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracuniPartnerId",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracuniPartnerId", description = "Vrijeme trajanja metode")
  public Response getObracuniPartnerId(@PathParam("id") int id, @QueryParam("od") Long vrijemeOd,
      @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni;

      obracuni = obracunDAO.dohvatiSve(id, vrijemeOd, vrijemeDo);

      return Response.ok(obracuni).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("obracun")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje obracuna")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postObracun",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postObracun", description = "Vrijeme trajanja metode")
  public Response postObracun(List<Obracun> obracuni) {
    if (obracuni == null || obracuni.isEmpty()) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    int partnerId = obracuni.get(0).partner();
    boolean sviIstiPartneri = obracuni.stream().allMatch(o -> o.partner() == partnerId);

    if (!sviIstiPartneri) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);

      for (Obracun obracun : obracuni) {
        var odgovor = obracunDAO.dodaj(obracun);
        if (!odgovor) {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
      }

      Response wsResponse = tvrtkaInfoClient.getObracunWs();

      if (wsResponse.getStatus() == 200) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("obracun/ws")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje obracuna")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postObracunWS",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postObracunWS", description = "Vrijeme trajanja metode")
  public Response postObracunWS(List<Obracun> obracuni) {
    if (obracuni == null || obracuni.isEmpty()) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    int partnerId = obracuni.get(0).partner();
    boolean sviIstiPartneri = obracuni.stream().allMatch(o -> o.partner() == partnerId);

    if (!sviIstiPartneri) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);

      for (Obracun obracun : obracuni) {
        var odgovor = obracunDAO.dodaj(obracun);
        if (!odgovor) {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
      }

      Gson gson = new Gson();
      String stringificiraniObracuni = gson.toJson(obracuni);

      var partnerDAO = new PartnerDAO(vezaBP);
      String sigurnosniKod = partnerDAO.dohvati(partnerId, false).sigurnosniKod();

      var odgovorTvrtka = posaljiKomandu(
          "OBRAČUN " + partnerId + " " + sigurnosniKod + "\n" + stringificiraniObracuni, 2);
      String status = odgovorTvrtka.split("\n")[0];
      if (status.equals("OK")) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
    var odgovor = posaljiKomandu("SPAVA " + this.kodZaAdminTvrtke + " " + brojSekundi, 0);
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
        mreznaVrata = this.mreznaVrataKraj;
        break;
      case 1:
        mreznaVrata = this.mreznaVrataRegistracija;
        break;
      case 2:
        mreznaVrata = this.mreznaVrataRad;
        break;
      default:
        return null;
    }

    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(mreznaVrata));
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
