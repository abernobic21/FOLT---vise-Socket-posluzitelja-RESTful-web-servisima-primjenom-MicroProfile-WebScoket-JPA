/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3;

import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author NWTiS
 */
@Controller
@Path("tvrtka")
@RequestScoped
public class Kontroler {

  @Inject
  private Models model;

  @Inject
  private BindingResult bindingResult;

  @Inject
  @RestClient
  ServisTvrtkaKlijent servisTvrtka;

  @GET
  @Path("pocetak")
  @View("index.jsp")
  public void pocetak() {}

  @GET
  @Path("privatno")
  @View("privatno/index.jsp")
  public void pocetakPriatno() {}

  @GET
  @Path("admin")
  @View("admin/index.jsp")
  public void pocetakAdmin() {}


  @GET
  @Path("status")
  @View("status.jsp")
  public void status() {
    dohvatiStatuse();
  }

  @GET
  @Path("/admin/kraj")
  public Response kraj() {
    try {
      var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
      this.model.put("statusOperacije", status);
      if (status == 200) {
        return Response.status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NO_CONTENT).build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @GET
  @Path("start/{id}")
  @View("status.jsp")
  public void startId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  @GET
  @Path("pauza/{id}")
  @View("status.jsp")
  public void pauzatId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljPauza(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  @GET
  @Path("partner")
  @View("partneri.jsp")
  public void partneri() {
    var odgovor = this.servisTvrtka.getPartneri();
    var status = odgovor.getStatus();
    if (status == 200) {
      var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {});
      this.model.put("status", status);
      this.model.put("partneri", partneri);
    }
  }

  @GET
  @Path("partner/{id}")
  @View("partnerDetalji.jsp")
  public void partnerDetalji(@PathParam("id") int id) {
    var odgovor = this.servisTvrtka.getPartner(id);
    var status = odgovor.getStatus();
    if (status == 200) {
      var partner = odgovor.readEntity(new GenericType<Partner>() {});
      this.model.put("status", status);
      this.model.put("partner", partner);
    }
  }

  @GET
  @Path("privatno/obracuni")
  @View("privatno/obracuni.jsp")
  public void pregledObracuni(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {

    var odgovor = this.servisTvrtka.getObracuni(vrijemeOd, vrijemeDo);
    var status = odgovor.getStatus();

    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracuni", obracuni);
    } else {
      this.model.put("status", status);
      this.model.put("obracuni", List.of());
    }
  }

  @GET
  @Path("privatno/obracuni/sve")
  @View("privatno/tablicaObracuni.jsp")
  public void obracuni(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {

    var odgovor = this.servisTvrtka.getObracuni(vrijemeOd, vrijemeDo);
    var status = odgovor.getStatus();

    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracuni", obracuni);
    } else {
      this.model.put("status", status);
      this.model.put("obracuni", List.of());
    }
  }

  @GET
  @Path("privatno/obracuni/jelo")
  @View("privatno/tablicaObracuni.jsp")
  public void obracuniJelo(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {

    var odgovor = this.servisTvrtka.getObracuniJelo(vrijemeOd, vrijemeDo);
    var status = odgovor.getStatus();

    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracuni", obracuni);
    } else {
      this.model.put("status", status);
      this.model.put("obracuni", List.of());
    }
  }

  @GET
  @Path("privatno/obracuni/pice")
  @View("privatno/tablicaObracuni.jsp")
  public void obracuniPice(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {

    var odgovor = this.servisTvrtka.getObracuniPice(vrijemeOd, vrijemeDo);
    var status = odgovor.getStatus();

    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracuni", obracuni);
    } else {
      this.model.put("status", status);
      this.model.put("obracuni", List.of());
    }
  }

  @GET
  @Path("privatno/obracuni/partner")
  @View("privatno/obracuniPartner.jsp")
  public void pregledObracuniPartner() {

    var odgovor = this.servisTvrtka.getPartneri();
    var status = odgovor.getStatus();
    if (status == 200) {
      var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {});
      this.model.put("status", status);
      this.model.put("partneri", partneri);
    }
  }

  @GET
  @Path("privatno/obracuni/partner/{id}")
  @View("privatno/tablicaObracuni.jsp")
  public void obracuniPartner(@PathParam("id") int id, @QueryParam("od") Long vrijemeOd,
      @QueryParam("do") Long vrijemeDo) {

    var odgovor = this.servisTvrtka.getObracuniPartner(id, vrijemeOd, vrijemeDo);
    var status = odgovor.getStatus();

    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracuni", obracuni);
    } else {
      this.model.put("status", status);
      this.model.put("obracuni", List.of());
    }
  }

  @GET
  @Path("admin/partner/")
  @View("admin/dodavanjePartnera.jsp")
  public void dodavanjePartnera() {}

  @Path("admin/partner")
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response dodajPartnera(@FormParam("id") int id, @FormParam("naziv") String naziv,
      @FormParam("vrstaKuhinje") String vrstaKuhinje, @FormParam("adresa") String adresa,
      @FormParam("mreznaVrata") int mreznaVrata, @FormParam("mreznaVrataKraj") int mreznaVrataKraj,
      @FormParam("gpsSirina") float gpsSirina, @FormParam("gpsDuzina") float gpsDuzina,
      @FormParam("sigurnosniKod") String sigurnosniKod, @FormParam("adminKod") String adminKod) {

    Partner novi = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj,
        gpsSirina, gpsDuzina, sigurnosniKod, adminKod);

    var status = servisTvrtka.postPartner(novi).getStatus();

    System.out.println(status);
    if (status == 201) {
      return Response.status(Response.Status.CREATED).build();
    } else if (status == 409) {
      return Response.status(Response.Status.CONFLICT).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @GET
  @Path("admin/spavanjeTvrtka")
  @View("admin/spava.jsp")
  public void spavanjePrikaz() {}

  @GET
  @Path("admin/spava")
  public Response spavanje(@QueryParam("vrijeme") Long brojSekundi) {

    var odgovor = this.servisTvrtka.getSpava(brojSekundi);
    var status = odgovor.getStatus();

    if (status == 200) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @GET
  @Path("admin/nadzornaKonzolaTvrtka")
  @View("admin/nadzornaKonzolaTvrtka.jsp")
  public void nadzornaKonzolaTvrtka() {
    dohvatiStatuse();
  }

  @GET
  @Path("admin/status/{id}")
  public Response statusPosluzitelja(@PathParam("id") int id) {
    var status = servisTvrtka.headPosluziteljStatus(id).getStatus();

    if (status == 200) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @GET
  @Path("admin/pauza/{id}")
  public Response pauzaPoslzitelja(@PathParam("id") int id) {
    var status = servisTvrtka.headPosluziteljPauza(id).getStatus();

    if (status == 200) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @GET
  @Path("admin/start/{id}")
  public Response startPoslzitelja(@PathParam("id") int id) {
    var status = servisTvrtka.headPosluziteljStart(id).getStatus();

    if (status == 200) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }


  private void dohvatiStatuse() {
    try {
      this.model.put("samoOperacija", false);
      var statusT = this.servisTvrtka.headPosluzitelj().getStatus();
      this.model.put("statusT", statusT);
      var statusT1 = this.servisTvrtka.headPosluziteljStatus(1).getStatus();
      this.model.put("statusT1", statusT1);
      var statusT2 = this.servisTvrtka.headPosluziteljStatus(2).getStatus();
      this.model.put("statusT2", statusT2);
    } catch (Exception e) {
      this.model.put("statusT", 204);
      this.model.put("statusT1", 204);
      this.model.put("statusT2", 204);
    }
  }

}
