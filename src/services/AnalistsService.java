package services;

import beans.City;
import beans.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("analistsServices")
public class AnalistsService
{
    //Returns the city grid
    @Path("getGrid")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getNodesGrid()
    {
        return Response.ok(City.getInstance().getNodesGrid()).build();
    }

    //Returns last n stats (with timestamp) produced by a specific Edge Node
    @Path("getNodeStats/{id}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getNodeStats(@PathParam("id") int id, @PathParam("n") int n)
    {

        for (Node node : City.getInstance().getNodesList()) {
            if (node.getID() == id)
                return Response.ok(City.getInstance().getNodeStats(id, n)).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    //Returns last n global stats (with timestamp)
    @Path("getGlobalStats/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getGlobalStats(@PathParam("n") int n)
    {
        return Response.ok(City.getInstance().getGlobalStats(n)).build();
    }

    //Returns last n local stats (with timestamp)
    @Path("getLocalStats/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getLocalStats(@PathParam("n") int n)
    {
        return Response.ok(City.getInstance().getLocalStats(n)).build();
    }

    //Returns the standard deviation of last n stats produced by a specific Edge Node
    @Path("getNodeStatsStdDev/{id}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getNodeStatsStandardDeviation(@PathParam("id") int id, @PathParam("n") int n)
    {
        return Response.ok(City.getInstance().getNodeStatsStandardDeviation(id, n)).build();
    }

    //Returns the mean of last n stats produced by a specific Edge Node
    @Path("getNodeStatsMean/{id}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getNodeStatsMean(@PathParam("id") int id, @PathParam("n") int n)
    {
        return Response.ok(City.getInstance().getNodeStatsMean(id, n)).build();
    }

    //Returns the standard deviation of last n global stats
    @Path("getGlobalStatsStdDev/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getGlobalStatsStandardDeviation(@PathParam("n") int n)
    {
        return Response.ok(City.getInstance().getGlobalStatsStandardDeviation(n)).build();
    }

    //Returns the mean of last n global stats
    @Path("getGlobalStatsMean/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getGlobalStatsMean(@PathParam("n")int n)
    {
        return Response.ok(City.getInstance().getGlobalStatsMean(n)).build();
    }

}
