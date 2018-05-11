package services;

import beans.Dictionary;
import beans.Word;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("services")
public class WordsService
{
    //restituisce il dizionario
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getWordsList(){

        return Response.ok(Dictionary.getInstance()).build();

    }

    //permette di inserire una parola (parola e definizione)
    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addWord(Word w){
        Dictionary.getInstance().add(w);
        return Response.ok().build();
    }

    //permette di visualizzare la definizione di una parola data
    @Path("get/{word}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getByWord(@PathParam("word") String word){
        Word w = Dictionary.getInstance().getByWord(word);
        if(w!=null)
            return Response.ok(w.getDefinition()).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    //permette di aggiornare la definizione di una parola data
    @Path("update/{word}")
    @PUT
    @Produces({"application/json", "application/xml"})
    public Response updateWordDefinition(@PathParam("word") String word, String newDefinition){
        Word w = Dictionary.getInstance().getByWord(word);
        if(w!=null)
        {
            w.setDefinition(newDefinition);
            return Response.ok().build();
        }
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    //permette di rimuovere una parola data
    @Path("remove/{word}")
    @DELETE
    @Produces({"application/json", "application/xml"})
    public Response removeWord(@PathParam("word") String word){
        Word w = Dictionary.getInstance().getByWord(word);
        if(w!=null)
        {
            Dictionary.getInstance().remove(w);
            return Response.ok().build();
        }
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

}
