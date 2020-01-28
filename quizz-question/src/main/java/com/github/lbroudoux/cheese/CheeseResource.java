package com.github.lbroudoux.cheese;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Path("/api/cheese")
public class CheeseResource {

    private final Logger logger = Logger.getLogger(getClass());

    /** Counter to help us see the lifecycle */
    private int count = 0;
    /** Flag for waiting when enabled */
    private boolean timeout = false;
    /** Flag for throwing a 503 when enabled */
    private boolean misbehave = false;

    private static String HOSTNAME = null;

    private static String parseContainerIdFromHostname(String hostname) {
        System.err.println("hostname is " + hostname);
        System.err.println("  after subst: " + hostname.replaceAll("cheese-quizz-question-v\\d+-", ""));
        return hostname.replaceAll("cheese-quizz-question-v\\d+-", "");
    }

    @ConfigProperty(name = "cheese.name", defaultValue = "cheddar")
    String cheeseName;

    @ConfigProperty(name = "cheese.description", defaultValue = "cheddar description")
    String cheeseDescription;

    @ConfigProperty(name = "cheese.image", defaultValue = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAb1npUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHjarZtnkhs5toX")
    String cheeseImage;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/fetch")
    public Response getCheese() {
        logger.info("getCheese() invoked");
        try {
            if (HOSTNAME == null) {
                HOSTNAME = parseContainerIdFromHostname(System.getenv().getOrDefault("HOSTNAME", "unknown"));
            }
            if (timeout) {
                timeout();
            }
            if (misbehave) {
                return doMisbehavior();
            }

            // Build a new cheese.
            Cheese cheese = new Cheese(cheeseName, cheeseDescription, cheeseImage);
            return Response.ok(cheese).build();
        } catch (Throwable t) {
            logger.error(t);
        }
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/flag/timeout")
    public Response flagTimeout() {
        logger.info("flagTimeout() invoked");
        this.timeout = true;
        return Response.ok("Following requests to / will wait 3s\n").build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/flag/timein")
    public Response flagTimein() {
        logger.info("flagTimein() invoked");
        this.timeout = false;
        return Response.ok("Following requests to / will not wait\n").build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/flag/misbehave")
    public Response flagMisbehave() {
        logger.info("flagMisbehave() invoked");
        this.misbehave = true;
        return Response.ok("Following requests to / will return a 503\n").build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/flag/behave")
    public Response flagBehave() {
        logger.info("flagBehave() invoked");
        this.misbehave = false;
        return Response.ok("Following requests to / will return 200\n").build();
    }

    private void timeout() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.info("Thread interrupted");
        }
    }

    private Response doMisbehavior() {
        logger.debug(String.format("Misbehaving %d", count));
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(String.format("cheese-quizz-question misbehavior from '%s'\n", HOSTNAME)).build();
    }
}