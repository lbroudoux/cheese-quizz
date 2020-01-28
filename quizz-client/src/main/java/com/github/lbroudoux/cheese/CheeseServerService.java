package com.github.lbroudoux.cheese;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/api/cheese")
@RegisterRestClient
@RegisterClientHeaders
public interface CheeseServerService {

    @GET
    @Path("/fetch")
    @Produces("application/json")
    Cheese getNewCheese();
}