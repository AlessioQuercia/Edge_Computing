package services;

import beans.City;
import beans.Node;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("sensorsServices")
public class SensorsService
{
    //Returns to the asking sensor the nearest Node
    @Path("/getNearestNode/{x}/{y}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getNearestNode(@PathParam("x") int x, @PathParam("y") int y)
    {
        Node nearestNode = City.getInstance().getNearestNode(x, y);
        return Response.ok(nearestNode).build();
    }
}
