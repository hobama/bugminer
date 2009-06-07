package hk.ust.cse.pag.cg.jung;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.scoring.*;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import edu.umd.cs.findbugs.ba.CFGBuilderException;
import hk.ust.cse.pag.cg.cfg.CFGExtracter;
import hk.ust.cse.pag.cg.cfg.Connection;
import hk.ust.cse.pag.cg.cfg.Node;
import hk.ust.cse.pag.cg.util.Const;
import hk.ust.cse.pag.cg.util.HashCount;


public class JungAnalysis
{
    private HashCount<Node>                         m_nodes;
    private HashCount<Connection>                   m_edges;
    private Graph<Node, Connection>                 m_graph;
    private Transformer<Connection, Double>         m_wtTansformer;
    private DijkstraShortestPath<Node, Connection>  m_shortestPath;
    private int                                     m_maxConnection;  
    
    public JungAnalysis(CFGExtracter extracter, boolean bDirected, boolean bWeight)
    {
        try
        {
            // get nodes and edges
            m_nodes = extracter.getNodes();
            m_edges = extracter.getConnections();
            
            // create graph from CFGs
            m_graph = new SparseMultigraph<Node, Connection>();
            System.out.println("Creating graph...");
            
            // add vertexes
            System.out.println("Adding vertices...");
            List<Node> nodes = m_nodes.getKeyList();
            for (Node node : nodes)
            {
                m_graph.addVertex(node);
            }
            System.out.println(nodes.size() + " vertices added to graph.");
            
            // add edges
            m_maxConnection = 0;
            System.out.println("Adding edges...");
            EdgeType edgeType = bDirected ? EdgeType.DIRECTED : EdgeType.UNDIRECTED;
            List<Connection> edges = m_edges.getKeyList();
            for (Connection edge : edges)
            {
                m_graph.addEdge(edge, 
                                edge.getSourceNode(), 
                                edge.getTargetNode(), 
                                edgeType);
                int count = m_edges.getCount(edge);
                if (count > m_maxConnection)
                {
                    m_maxConnection = count;
                }
            }
            System.out.println(edges.size() + " edges added to graph.");
            System.out.println("Max connections: " + m_maxConnection);            
            System.out.println("Graph creation finished!");
            
            // create weight transformer
            if (bWeight)
            {
                m_wtTansformer = new Transformer<Connection, Double>()
                {
                    public Double transform(Connection edge)
                    {
                        return new Double(m_maxConnection - m_edges.getCount(edge) + 1);
                    }
                };
            }
            else
            {
                m_wtTansformer = new Transformer<Connection, Double>()
                {
                    public Double transform(Connection edge)
                    {
                        return 1.0;
                    }
                };
            }
        }
        catch (Exception e)
        {
            System.err.println("Unable to create graph from CFGs!");
        }
    }
    
    /**
     * Gets the betweenness centrality of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getBetweennessCentrality()
    {
        BetweennessCentrality<Node, Connection> betweeness = 
            new BetweennessCentrality<Node, Connection>(m_graph, m_wtTansformer);
        return getCentrality(betweeness, false);
    }
    
    /**
     * Gets the normalized-betweenness centrality of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getNormBetweennessCentrality(List<SortItem<Node>> centralityResult)
    {
        List<SortItem<Node>> results = new ArrayList<SortItem<Node>>();
        for (SortItem<Node> item: centralityResult)
        {
            // max centrality
            double maxCentrality = centralityResult.get(0).value;
            
            // normalize by dividing max value
            SortItem<Node> normalized = new SortItem<Node>(item.item, item.value / maxCentrality);
            normalized.ascendingOrder = false;
            results.add(normalized);
        }
        //Collections.sort(results);
        
        return results;
    }
    
    /**
     * Gets the closeness centrality of every Node,
     * return in ascending order
     */
    public List<SortItem<Node>> getClosenessCentrality()
    {
        if (m_shortestPath == null)
            m_shortestPath = new DijkstraShortestPath(m_graph, m_wtTansformer);

        ClosenessCentrality<Node, Connection> closeness = 
            new ClosenessCentrality<Node, Connection>(m_graph, m_shortestPath);
        List<SortItem<Node>> results = getCentrality(closeness, true);
        
        // put nodes with 0 farness at the bottom 
        for (SortItem<Node> item: results)
        {
            if (item.value <= 0)
            {
                item.value = Double.POSITIVE_INFINITY;
            }
            else
            {
                // it was sorted in ascending order
                break;
            }
        }
        Collections.sort(results);
        
        return results;
    }
    
    /**
     * Gets the normalized-betweenness closeness of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getNormClosenessCentrality(List<SortItem<Node>> centralityResult)
    {
        List<SortItem<Node>> results = new ArrayList<SortItem<Node>>();
        for (SortItem<Node> item: centralityResult)
        {
            // max reciprocal value
            double maxCentrality = 1.0 / centralityResult.get(0).value;
                
            // normalized by taking reciprocal and then divided by max reciprocal value
            SortItem<Node> normalized = new SortItem<Node>(item.item, 1.0 / item.value / maxCentrality);
            normalized.ascendingOrder = false;
            results.add(normalized);
        }
        //Collections.sort(results);
        
        return results;
    }
    
    /**
     * Gets the degree centrality of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getDegreeCentrality()
    {
        DegreeScorer<Node> degree = 
            new DegreeScorer<Node>(m_graph);
        return getCentrality(degree, false);
    }
    
    /**
     * Gets the normalized-degree centrality of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getNormDegreeCentrality(List<SortItem<Node>> centralityResult)
    {
        List<SortItem<Node>> results = new ArrayList<SortItem<Node>>();
        for (SortItem<Node> item: centralityResult)
        {
            // max centrality
            double maxCentrality = centralityResult.get(0).value;
            
            // normalize by dividing max value
            SortItem<Node> normalized = new SortItem<Node>(item.item, item.value / maxCentrality);
            normalized.ascendingOrder = false;
            results.add(normalized);
        }
        //Collections.sort(results);
        
        return results;
    }
    
    /**
     * Gets the eigenvector centrality of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getEigenvectorCentrality()
    {
        EigenvectorCentrality<Node, Connection> eigenvector = 
            new EigenvectorCentrality<Node, Connection>(m_graph, m_wtTansformer);
        return getCentrality(eigenvector, false);
    }
    
    /**
     * Gets the pagerank centrality of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getPageRankCentrality()
    {
        PageRank<Node, Connection> pagerank = 
            new PageRank<Node, Connection>(m_graph, m_wtTansformer, 0.0001);
        return getCentrality(pagerank, false);
    }
    
    /**
     * Gets the reachability centrality of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getReachabilityCentrality()
    {  
        if (m_shortestPath == null)
            m_shortestPath = new DijkstraShortestPath(m_graph, m_wtTansformer);
        
        int nNodeFinished = 0;
        List<SortItem<Node>> results = new ArrayList<SortItem<Node>>();
        List<Node> nodes = m_nodes.getKeyList();
        for (Node node : nodes)
        {
            // shortest steps between node and every other node
            double value = 0;
            for (Node node2 : nodes)
            {
                Number step = m_shortestPath.getDistance(node, node2);
                if (step != null && step.intValue() > 0)
                {
                    value += 1.0 / step.intValue();
                }
            }
            
            SortItem<Node> result = new SortItem<Node>(node, value);
            result.ascendingOrder = false;
            results.add(result);
            
            if (++nNodeFinished % 10 == 0 || nNodeFinished == nodes.size())
            {
                System.out.println("Finished: " + nNodeFinished + " / " + nodes.size());
            }
        }
        Collections.sort(results);
        
        return results;
    }
    
    /**
     * Gets the normalized-reachability centrality of every Node,
     * return in descending order
     */
    public List<SortItem<Node>> getNormReachabilityCentrality(List<SortItem<Node>> centralityResult)
    {
        List<SortItem<Node>> results = new ArrayList<SortItem<Node>>();
        for (SortItem<Node> item: centralityResult)
        {
            // max centrality
            double maxCentrality = centralityResult.get(0).value;
            
            // normalize by dividing max value
            SortItem<Node> normalized = new SortItem<Node>(item.item, item.value / maxCentrality);
            normalized.ascendingOrder = false;
            results.add(normalized);
        }
        //Collections.sort(results);
        
        return results;
    }
    
    private List<SortItem<Node>> getCentrality(VertexScorer scorer, boolean ascendingOrder)
    {
        int nNodeFinished = 0;
        List<SortItem<Node>> results = new ArrayList<SortItem<Node>>();
        List<Node> nodes = m_nodes.getKeyList();
        for (Node node : nodes)
        {
            double value = new Double(scorer.getVertexScore(node).toString());
            
            SortItem<Node> result = new SortItem<Node>(node, value);
            result.ascendingOrder = ascendingOrder;
            results.add(result);
            
            if (++nNodeFinished % 10 == 0 || nNodeFinished == nodes.size())
            {
                System.out.println("Finished: " + nNodeFinished + " / " + nodes.size());
            }
        }
        Collections.sort(results);
        
        return results;
    }
    
    // very slow!!!
    public void showGraph()
    {
        // create Layout<V, E>, 
        Layout<Node, Connection> layout = new CircleLayout <Node, Connection>(m_graph);
        layout.setSize(new Dimension(700,700));
        
        // create VisualizationComponent<V,E>
        VisualizationViewer<Node, Connection> vv = 
            new VisualizationViewer<Node, Connection>(layout);
        vv.setPreferredSize(new Dimension(750,750));
        // Show vertex and edge labels
        //vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        //vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse<Node, Connection> gm = 
            new DefaultModalGraphMouse<Node, Connection>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm); 
        
        // create JFrame
        JFrame frame = new JFrame("Interactive Graph View 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }
    
    @Override
    public String toString()
    {
        return m_graph.toString();
    }
    
    class SortItem<T> implements Comparable<SortItem<T>>
    {
        public T        item;
        public double   value;
        public boolean  ascendingOrder = true;

        public SortItem(T item, double value)
        {
            this.item   = item;
            this.value  = value;
        }

        public int compareTo(SortItem<T> item)
        {
            if (ascendingOrder)
            {
                return new Double(value).compareTo(new Double(item.value));
            }
            else
            {
                return new Double(item.value).compareTo(new Double(value));
            }
        }
    }
    
    public static void main(String[] args) throws CFGBuilderException, IOException
    {
        // jarFile Path
        File jarFile = null;
        if (args.length > 0)
        {
            jarFile = new File(args[0]);
        }
        else
        {
            jarFile = new File(Const.CHECKOUT_DIR
                    //+ "/columba-svn/jar_repos/columba_r353.jar");
                    + "/J_Client.jar");
        }
        
        // create CFGExtracter
        CFGExtracter extracter = new CFGExtracter();
        System.out.println("Extracting Jar file...");
        extracter.extractInJarFile(jarFile);
        System.out.println("Extraction finished!");

        // create analyzer, and do analysis
        JungAnalysis analysis = new JungAnalysis(extracter, true, true);
       
        // centrality analysis
        // degree
        System.out.println("Calculating degree centrality...");
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<Node>> degree = analysis.getDegreeCentrality();
        List<SortItem<Node>> ndegree = analysis.getNormDegreeCentrality(degree);        
        System.out.println("Calculation finished!\n");
        // reachability
        System.out.println("Calculating reachability centrality...");
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<Node>> reachability = analysis.getReachabilityCentrality();
        List<SortItem<Node>> nreachability = analysis.getNormReachabilityCentrality(reachability);        
        System.out.println("Calculation finished!\n");
        // closeness
        System.out.println("Calculating closeness centrality...");
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<Node>> closeness = analysis.getClosenessCentrality();
        List<SortItem<Node>> ncloseness = analysis.getNormClosenessCentrality(closeness);        
        System.out.println("Calculation finished!\n");
        // betweenness
        System.out.println("Calculating betweenness centrality...");
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<Node>> betweenness = analysis.getBetweennessCentrality();
        List<SortItem<Node>> nbetweenness = analysis.getNormBetweennessCentrality(betweenness);        
        System.out.println("Calculation finished!\n");
        
        // generate output
        System.out.println("Output to file...");
        DecimalFormat format = new DecimalFormat("0.0000"); 
        Hashtable<Node, String> output = new Hashtable<Node, String>();
        for (int i = 0; i < degree.size(); i++)
        {
            SortItem<Node> item     = degree.get(i);
            SortItem<Node> nitem    = ndegree.get(i);
            String nodeName         = item.item.toString().split("\n")[0];
            output.put(item.item, nodeName + "\t" + format.format(item.value) + "\t" + format.format(nitem.value));
        }
        for (int i = 0; i < closeness.size(); i++)
        {
            SortItem<Node> item     = closeness.get(i);
            SortItem<Node> nitem    = ncloseness.get(i);
            
            String str = output.get(item.item);
            output.put(item.item, str + "\t" + format.format(item.value) + "\t" + format.format(nitem.value));
        }
        for (int i = 0; i < reachability.size(); i++)
        {
            SortItem<Node> item     = reachability.get(i);
            SortItem<Node> nitem    = nreachability.get(i);
            
            String str = output.get(item.item);
            output.put(item.item, str + "\t" + format.format(item.value) + "\t" + format.format(nitem.value));
        }
        for (int i = 0; i < betweenness.size(); i++)
        {
            SortItem<Node> item     = betweenness.get(i);
            SortItem<Node> nitem    = nbetweenness.get(i);
            
            String str = output.get(item.item);
            output.put(item.item, str + "\t" + format.format(item.value) + "\t" + format.format(nitem.value));
        }
        
        // output to file
        File centralityFile = new File("./centrality.txt");
        if (centralityFile.exists())
        {
            centralityFile.delete();
        }
        centralityFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(centralityFile));
        for (SortItem<Node> item: degree)
        {
            String str = output.get(item.item);
            if (str != null && str.length() > 0)
            {
                writer.write(str + "\n");
            }
        }
        writer.close();
        System.out.println("Output finished!");
    }
    
    public static void main2(String[] args) throws CFGBuilderException, IOException
    {
        // create CFGExtracter
        File jarFile = new File(Const.CHECKOUT_DIR
                //+ "/columba-svn/jar_repos/columba_r353.jar");
                + "/J_Client.jar");
        CFGExtracter extracter = new CFGExtracter();
        System.out.println("Extracting Jar file...");
        extracter.extractInJarFile(jarFile);
        System.out.println("Extraction finished!");

        // create analyzer, and do analysis
        JungAnalysis analysis = new JungAnalysis(extracter, true, true);
       
        // centrality analysis
        int nRank = 1;
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<Node>> results = analysis.getClosenessCentrality();
        List<SortItem<Node>> results2 = analysis.getNormClosenessCentrality(results);        
        System.out.println("Calculation finished!\n");
        for (int i = 0; i < results.size(); i++)
        {
            SortItem<Node> sortItem = results.get(i);
            SortItem<Node> sortItem2 = results2.get(i);         
            System.out.println("Rank " + nRank++ + ": ");
            System.out.println(sortItem.item + "\nValue: " + sortItem.value);
            System.out.println("Normalized Value: " + sortItem2.value);          
            System.out.println();
        } 
    }
}
