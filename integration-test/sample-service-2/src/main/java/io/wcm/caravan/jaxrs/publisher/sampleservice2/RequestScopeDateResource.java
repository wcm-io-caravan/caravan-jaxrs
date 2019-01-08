package io.wcm.caravan.jaxrs.publisher.sampleservice2;

import java.text.DateFormat;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("/date")
public class RequestScopeDateResource {

  private final String date = DateFormat.getDateTimeInstance().format(new Date());

  @Context
  private JaxRsService service;

  /**
   * @return the current date
   */
  @GET
  @Produces("text/plain")
  public String getDate() {
    return service.getServiceId() + " knows it's " + date;
  }
}
