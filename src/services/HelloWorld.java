package services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/helloworld")
public class HelloWorld {

    @GET
    @Produces("text/plain")
    public String helloWorld(){

        return "Hello world!";

    }

    @GET
    @Produces("application/json")
    public String helloWorld2(){
        return "{\"message\": \"helloWorld\"}";
    }

    @Path("/inner")
    @GET
    @Produces("text/plain")
    public String innerHello(){
        return "Inner Hello!";
    }

}
