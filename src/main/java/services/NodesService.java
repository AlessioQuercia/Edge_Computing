package services;


import beans.City;
import beans.Node;
import beans.Stat;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

@Path("nodesServices")
public class NodesService
{
    //restituisce l'istanza della città
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getNodesList(){

//        City.getInstance().toString();
//        System.out.println(City.getInstance().getNodesList().toString());
        return Response.ok(City.getInstance()).build();

    }


    //It allows to add a new Node to the city grid (and to the list of nodes)
    @Path("add/{id}/{ipAddress}/{sensorsPort}/{nodesPort}/{x}/{y}")
    @POST
    @Produces({"application/json", "application/xml"})
    public Response addNode(@PathParam("id") int id, @PathParam("ipAddress") String ipAddress,
                            @PathParam("sensorsPort") int sensorsPort, @PathParam("nodesPort") int nodesPort,
                            @PathParam("x") int x, @PathParam("y") int y)
    {
        if (!City.getInstance().isAddable(id, x, y))
            return Response.status(Response.Status.BAD_REQUEST).build();

        Node node = new Node(id, ipAddress, sensorsPort, nodesPort, x, y);

        List<Node> nodes = City.getInstance().add(node);

        GenericEntity<List<Node>> entity
                = new GenericEntity<List<Node>>(nodes) {};

        return Response.ok(entity).build();
    }

    //It allows to add a new Node to the city grid (and to the list of nodes)
    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addNode(Node node)
    {
        if (!City.getInstance().isAddable(node))
            return Response.status(Response.Status.BAD_REQUEST).build();

        Node n = new Node(node.getId(), node.getIpAddress(), node.getSensorsPort(), node.getNodesPort(), node.getX(), node.getY());

        List<Node> nodes = City.getInstance().add(n);

        GenericEntity<List<Node>> entity
                = new GenericEntity<List<Node>>(nodes) {};

        return Response.ok(entity).build();
    }

    //It allows to remove an existing Node from the city grid (and from the list of nodes) by specifying the ID
    @Path("remove/{id}")
    @DELETE
    @Produces({"application/json", "application/xml"})
    public Response removeNode(@PathParam("id") int id){
        Node node = City.getInstance().getByID(id);
        if(node!=null)
        {
            City.getInstance().remove(node);
            return Response.ok().build();
        }
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    //It allows the Nodes Coordinator to send global stats to the server
    @Path("sendGlobalStat")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response sendGlobalStat(Stat stat)
    {
        System.out.println(stat);
        City.getInstance().addGlobalStat(stat);
        return Response.ok().build();
    }

    //It allows the Nodes Coordinator to send local stats to the server
    @Path("sendLocalStats")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response sendLocalStats(Set<Stat> stats)
    {
        City.getInstance().addLocalStats(stats);
        return Response.ok().build();
    }
}
