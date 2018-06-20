package beans;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Array;
import java.util.*;

/* City representing the city */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class City
{
    // XmlElement sets the name of the entities
    @XmlElement(name = "nodesGrid")
    private Node[][] nodesGrid;

    @XmlElement(name = "nodesList")
    private List<Node> nodesList;

    @XmlElement(name = "globalStats")
    Set<Stat> globalStats;

    @XmlElement(name = "localStats")
    Map<Integer, TreeSet<Stat>> localStats;

    private static City instance;

    public City()
    {
        nodesGrid = new Node[100][100];
        nodesList = new ArrayList<Node>();
        globalStats = new TreeSet<Stat>();
        localStats = new HashMap<Integer, TreeSet<Stat>>();
    }

    public static synchronized City getInstance()
    {
        if(instance == null)
        {
            instance = new City();
        }
        return instance;
    }

    public Node[][] getNodesGrid()
    {
        Node[][] copy = new Node[100][100];

        synchronized (nodesGrid)
        {
            System.arraycopy(nodesGrid, 0, copy, 0, nodesGrid.length);
        }

        return copy;
    }

    public List<Node> getNodesList()
    {
        ArrayList<Node> copy = null;

        synchronized (nodesList)
        {
            copy = new ArrayList<>(nodesList);
        }

        return copy;
    }

    public void setNodesGrid(Node[][] nodesGrid)
    {
        synchronized (nodesGrid)
        {
            this.nodesGrid = nodesGrid;
        }
    }

    public synchronized List<Node> add(Node node)
    {
//        if (nodesGrid[node.getY()][node.getX()] == null) {
//            if (isAddable(node)) {
        nodesGrid[node.getY()][node.getX()] = node;
        nodesList.add(node);

        localStats.put(node.getId(), new TreeSet<Stat>());

        return nodesList;

    }

    public synchronized void remove(Node node)
    {
        nodesGrid[node.getY()][node.getX()] = null;
        nodesList.remove(node);
    }

    public synchronized boolean isAddable(Node node)
    {
        //Check if the given position is valid
        if (node.getX() < 0 || node.getX() >= 100 || node.getY() < 0 || node.getY() >= 100 || nodesGrid[node.getY()][node.getX()] != null)
            return false;

        //Check if there is already a node with the same ID, if so return false, otherwise continue with next check
        for (Node n : nodesList)
        {
            if (n.getId() == node.getId())
                return false;
        }

        //Check if there are nodesList in the 20x20 range around him, if so return false, otherwise return true
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                int dist = Math.abs(node.getX() - j) + Math.abs(node.getY() - i);
                if ((dist < 20 && nodesGrid[i][j] != null)) {
                    return false;
                }
            }
        }

        return true;
    }

    public synchronized boolean isAddable(int ID, int x, int y)
    {
        //Check if the given position is valid
        if (x < 0 || x >= 100 || y < 0 || y >= 100 || nodesGrid[y][x] != null)
            return false;

        //Check if there is already a node with the same ID, if so return false, otherwise continue with next check
        for (Node n : nodesList)
        {
            if (n.getId() == ID)
                return false;
        }

        //Check if there are nodesList in the 20x20 range around him, if so return false, otherwise return true
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                int dist = Math.abs(x - j) + Math.abs(y - i);
                if ((dist < 20 && nodesGrid[i][j] != null)) {
                    return false;
                }
            }
        }

        return true;
    }

    public Node getByID(int ID){

        List<Node> nodesCopy = getNodesList();

        for(Node n : nodesCopy)
            if(n.getId() == ID)
                return n;
        return null;
    }

    public void addGlobalStat(Stat stat)
    {
        synchronized (globalStats)
        {
            globalStats.add(stat);
        }
    }

    public void addLocalStats(Set<Stat> stats)
    {
        synchronized (localStats)
        {
            Stat[] statsArray = (Stat[]) stats.toArray(new Stat[stats.size()]);
            for (int i = 0; i < statsArray.length; i++)
            {
                localStats.get(statsArray[i].getNodeId()).add(statsArray[i]);
            }
        }
    }

    public Node getNearestNode(int x, int y)
    {
        ArrayList<Node> nodesListCopy = (ArrayList<Node>)getNodesList();
        Node nearestNode = null;
        int dist = Integer.MAX_VALUE;
        for (Node n : nodesListCopy)
        {
            int currDist = Math.abs(n.getX() - x) + Math.abs(n.getY() - y);
            if (currDist < dist)
            {
                dist = currDist;
                nearestNode = n;
            }
        }
        return nearestNode;
    }

    @Override
    public String toString()
    {
        String result = "";

        synchronized (nodesGrid)
        {
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    if (nodesGrid[i][j] != null) {
                        System.out.print("1 ");
                        result += "1 ";
                    } else {
                        System.out.print("0 ");
                        result += "0 ";
                    }
                }
                System.out.println();
                result += "\n";
            }
        }
        return result;
    }

    public Set<Stat> getNodeStats(int id, int n)
    {
        Set<Stat> nodesLastStats = new TreeSet<Stat>();

//        Node node = getByID(id);
//
//        Set<Stat> localStats = node.getLocalStats();
//        System.out.println(localStats);
//        Stat[] stats = (Stat[]) localStats.toArray(new Stat[localStats.size()]);

        Stat[] stats;

        synchronized (localStats.get(id))
        {
            stats = (Stat[]) localStats.get(id).toArray(new Stat[localStats.size()]);
        }

        int numStats = Math.min(n, stats.length);

        for (int i=0; i<numStats; i++)
            if(stats[stats.length-1-i].getNodeId() == id)
                nodesLastStats.add(stats[stats.length-1-i]);

        return nodesLastStats;
    }

    public Set<Stat> getGlobalStats(int n)
    {
        Set<Stat> nodesLastStats = new TreeSet<Stat>();

        Set<Stat> globalStatsCopy;

        synchronized (globalStats) {
            globalStatsCopy = new TreeSet<Stat>(globalStats);
        }

        Stat[] stats = (Stat[])globalStatsCopy.toArray(new Stat[globalStatsCopy.size()]);

        int numStats = Math.min(n, stats.length);

        for (int i=0; i<numStats; i++)
            nodesLastStats.add(stats[stats.length-1-i]);

        return nodesLastStats;
    }

    public Set<Stat> getLocalStats(int n)
    {
        Set<Stat> nodesLastStats = new TreeSet<Stat>();

        HashMap<Integer, TreeSet<Stat>> localStatsCopy;

        synchronized (localStats)
        {
            localStatsCopy = new HashMap<Integer, TreeSet<Stat>>(localStats);
        }

        TreeSet<Stat> lclStats = new TreeSet<Stat>();

        Set<Integer> nodes = localStatsCopy.keySet();

        Integer[] nodesArray = (Integer[])nodes.toArray(new Integer[nodes.size()]);

        for (int i = 0; i<nodesArray.length; i++)
        {
            Stat[] support = (Stat[])localStatsCopy.get(nodesArray[i]).toArray(new Stat[localStatsCopy.size()]);

            for (int j = 0; j<support.length; j++)
            {
                lclStats.add(support[j]);
            }
        }

        Stat[] stats = (Stat[])lclStats.toArray(new Stat[lclStats.size()]);

        int numStats = Math.min(n, stats.length);

        for (int i=0; i<numStats; i++)
            nodesLastStats.add(stats[stats.length-1-i]);

        return nodesLastStats;
    }

    public double getNodeStatsStandardDeviation(int id, int n)
    {
//        Node node = getByID(id);
//
//        Set<Stat> localStats = node.getLocalStats();
//        Stat[] stats = (Stat[]) localStats.toArray(new Stat[localStats.size()]);

        Stat[] stats;

        synchronized (localStats.get(id))
        {
            stats = (Stat[]) localStats.get(id).toArray(new Stat[localStats.size()]);
        }

        int numStats = Math.min(n, stats.length);

        double sum = 0;

        ArrayList<Stat> selectedStats = new ArrayList<Stat>();

        for (int i=0; i<numStats; i++)
            if(stats[stats.length-1-i].getNodeId() == id) {
                sum += stats[stats.length - 1 - i].getMean();
                selectedStats.add(stats[stats.length - 1 - i]);
            }

        double mean = sum/numStats;

        double value = 0;

        for (int i=0; i<numStats; i++)
            value += Math.pow((selectedStats.get(i).getMean() - mean), 2);

        double stdDev = Math.sqrt(value/numStats);

        return stdDev;
    }

    public double getNodeStatsMean(int id, int n)
    {
        Stat[] stats;
        synchronized (localStats.get(id))
        {
            stats = (Stat[]) localStats.get(id).toArray(new Stat[localStats.size()]);
        }

        int numStats = Math.min(n, stats.length);

        double sum = 0;

        for (int i=0; i<numStats; i++)
            if(stats[stats.length-1-i].getNodeId() == id) {
                sum += stats[stats.length - 1 - i].getMean();
            }

        double mean = sum/n;

        return mean;
    }

    public double getGlobalStatsStandardDeviation(int n)
    {
        Set<Stat> localStatsCopy;
        synchronized (globalStats) {
            localStatsCopy = new TreeSet<Stat>(globalStats);
        }

        Stat[] stats = (Stat[])localStatsCopy.toArray(new Stat[localStatsCopy.size()]);

        double sum = 0;

        int numStats = Math.min(n, stats.length);

        for (int i=0; i<numStats; i++)
            sum += stats[stats.length - 1 - i].getMean();

        double mean = sum/numStats;

        double value = 0;

        for (int i=0; i<numStats; i++)
            value += Math.pow((stats[stats.length - 1 - i].getMean() - mean), 2);

        double stdDev = Math.sqrt(value/numStats);

        return stdDev;
    }

    public double getGlobalStatsMean(int n)
    {
        Set<Stat> localStatsCopy;

        synchronized (globalStats) {
            localStatsCopy = new TreeSet<Stat>(globalStats);
        }

        Stat[] stats = (Stat[])localStatsCopy.toArray(new Stat[localStatsCopy.size()]);

        double sum = 0;

        int numStats = Math.min(n, stats.length);

        for (int i=0; i<numStats; i++)
            sum += stats[stats.length - 1 - i].getMean();

        double mean = sum/numStats;

        return mean;
    }
}
