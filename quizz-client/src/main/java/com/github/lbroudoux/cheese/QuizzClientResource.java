package com.github.lbroudoux.cheese;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import org.jboss.logging.Logger;

import io.netty.util.Timeout;

@Path("/api/quizz")
public class QuizzClientResource {

    private final Logger logger = Logger.getLogger(getClass());

    @ConfigProperty(name = "failure.image")
    String failureImage;

    @ConfigProperty(name = "timeout.image")
    String timeoutImage;

    @ConfigProperty(name = "quizz-like-function.url")
    String quizzLikeFunctionURL;

    @Inject
    @RestClient
    CheeseServerService cheeseService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/likeFunctionURL")
    public String getLikeFunctionURL() {
        return quizzLikeFunctionURL;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cheese")
    public Response getCheeseQuestion() {
        try {
            Cheese cheese = cheeseService.getNewCheese();
            CheeseQuestion question = new CheeseQuestion(cheese);
            return Response.ok(question).build();
        } catch (WebApplicationException wae) {
            Response response = wae.getResponse();
            logger.warn("Non HTTP 20x trying to get the response from Cheese service: " + response.getStatus());
            CheeseQuestion question = new CheeseQuestion("Cheese service returns " + response.getStatus(), failureImage);
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(question)
                    .build();
        } catch (ProcessingException pe) {
            logger.warn("Exception trying to get the response from Cheese service.", pe);
            CheeseQuestion question = new CheeseQuestion("Cheese service timeout", timeoutImage);
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(question)
                    .build();

        }
    }
}