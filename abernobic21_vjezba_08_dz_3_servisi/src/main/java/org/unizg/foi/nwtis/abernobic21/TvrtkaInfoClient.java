package org.unizg.foi.nwtis.abernobic21;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "klijentTvrtkaInfo")
@Path("/api/tvrtka")
public interface TvrtkaInfoClient {
  @GET
  @Path("/kraj/info")
  Response getKrajInfo();

  @GET
  @Path("/obracun/ws")
  Response getObracunWs();
}
