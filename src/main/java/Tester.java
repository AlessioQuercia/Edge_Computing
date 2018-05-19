import beans.City;
import beans.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Tester
{
    private static final String GRID_XML = "./grid-jaxb.xml";

    public static void main(String[] args)
    {
        //Create nodesGrid
        Node[][] nodesGrid = new Node[100][100];

        //Create nodes
        Node node1 = new Node(1, "prova1", 8080, 8080, 0, 0);
        nodesGrid[node1.getY()][node1.getX()] = node1;

        Node node2 = new Node(2, "prova2", 8080, 8080, 50, 50);
        nodesGrid[node2.getY()][node2.getX()] = node2;

        Node node3 = new Node(3, "prova3", 8080, 8080, 99, 99);
        nodesGrid[node3.getY()][node3.getX()] = node3;

        //Create city, assigning nodes
        City city = new City();
        city.setNodesGrid(nodesGrid);
        city.add(node3);

        // create JAXB context and instantiate marshaller
        JAXBContext context = null;
        Marshaller m = null;
        try {
            context = JAXBContext.newInstance(City.class);
            m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Write to System.out
            m.marshal(city, System.out);

            // Write to File
            m.marshal(city, new File(GRID_XML));


        } catch (JAXBException e) {
            e.printStackTrace();
        }

        // get variables from our xml file, created before
        System.out.println();
        System.out.println("Output from our XML File: ");
        Unmarshaller um = null;
        City city2 = null;
        try {
            um = context.createUnmarshaller();
            city2 = (City) um.unmarshal(new FileReader(
                    GRID_XML));
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        Node[][] nodesGrid2 = city2.getNodesGrid();
        for (int row = 0; row < nodesGrid2.length; row++)
        {
            for (int col = 0; col < nodesGrid2[0].length; col++)
            {
                System.out.print(nodesGrid2[row][col].getId() + " ");
            }

            System.out.println();
        }

    }
}
