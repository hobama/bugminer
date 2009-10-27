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
    private HashCount<Node>                                 m_nodes;
    private HashCount<Connection>                           m_edges;
    private Graph<NodeWrapper, Connection>                  m_graph;
    private Transformer<Connection, Double>                 m_wtTansformer;
    private DijkstraShortestPath<NodeWrapper, Connection>   m_shortestPath;
    private int                                             m_maxConnection;  
    
    public JungAnalysis(CFGExtracter extracter, boolean bDirected, boolean bWeight)
    {
        try
        {
            // get nodes and edges
            m_nodes = extracter.getNodes();
            m_edges = extracter.getConnections();
            
            // create graph from CFGs
            m_graph = new SparseMultigraph<NodeWrapper, Connection>();
            System.out.println("Creating graph...");
            
            // add vertexes
            System.out.println("Adding vertices...");
            List<Node> nodes = m_nodes.getKeyList();
            for (Node node : nodes)
            {
                m_graph.addVertex(new NodeWrapper(node));
            }
            System.out.println(nodes.size() + " vertices added to graph.");
            
            // add edges
            m_maxConnection = 0;
            System.out.println("Adding edges...");
            EdgeType edgeType = bDirected ? EdgeType.DIRECTED : EdgeType.UNDIRECTED;
            List<Connection> edges = m_edges.getKeyList();
            for (Connection edge : edges)
            {
                NodeWrapper source = NodeWrapper.getNodeWrapper(edge.getSourceNode());
                NodeWrapper target = NodeWrapper.getNodeWrapper(edge.getTargetNode());              
                m_graph.addEdge(edge, 
                                source, 
                                target, 
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
    public List<SortItem<NodeWrapper>> getBetweennessCentrality()
    {
        BetweennessCentrality<NodeWrapper, Connection> betweeness = 
            new BetweennessCentrality<NodeWrapper, Connection>(m_graph, m_wtTansformer);
        return getCentrality(betweeness, false);
    }
    
    /**
     * Gets the normalized-betweenness centrality of every Node,
     * return in descending order
     */
    public List<SortItem<NodeWrapper>> getNormBetweennessCentrality(List<SortItem<NodeWrapper>> centralityResult)
    {
        List<SortItem<NodeWrapper>> results = new ArrayList<SortItem<NodeWrapper>>();
        for (SortItem<NodeWrapper> item: centralityResult)
        {
            // max centrality
            double maxCentrality = centralityResult.get(0).value;
            
            // normalize by dividing max value
            SortItem<NodeWrapper> normalized = new SortItem<NodeWrapper>(item.item, item.value / maxCentrality);
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
    public List<SortItem<NodeWrapper>> getClosenessCentrality()
    {
        if (m_shortestPath == null)
            m_shortestPath = new DijkstraShortestPath(m_graph, m_wtTansformer);

        ClosenessCentrality<NodeWrapper, Connection> closeness = 
            new ClosenessCentrality<NodeWrapper, Connection>(m_graph, m_shortestPath);
        // special implementation for closeness
        //List<SortItem<NodeWrapper>> results = getCentrality(closeness, true);
        
        // new DijkstraShortestPath every 500 nodes to keep 
        // the cache hashMap from getting too big!
        int nNodeFinished = 0;
        List<SortItem<NodeWrapper>> results = new ArrayList<SortItem<NodeWrapper>>();
        List<NodeWrapper> nodeWrappers = NodeWrapper.getList();
        for (NodeWrapper nodeWrapper : nodeWrappers)
        {
            double value = new Double(closeness.getVertexScore(nodeWrapper).toString());
            
            SortItem<NodeWrapper> result = new SortItem<NodeWrapper>(nodeWrapper, value);
            result.ascendingOrder = true;
            results.add(result);
            
            if (++nNodeFinished % 10 == 0 || nNodeFinished == nodeWrappers.size())
            {
                System.out.println("Finished: " + nNodeFinished + " / " + nodeWrappers.size());
            }
            
            // refresh DijkstraShortestPath object
            if (nNodeFinished % 500 == 0)
            {
                m_shortestPath = new DijkstraShortestPath(m_graph, m_wtTansformer);
                closeness = new ClosenessCentrality<NodeWrapper, Connection>(m_graph, m_shortestPath);
            }
        }
        Collections.sort(results);
        
        // put nodes with 0 farness at the bottom 
        for (SortItem<NodeWrapper> item: results)
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
    public List<SortItem<NodeWrapper>> getNormClosenessCentrality(List<SortItem<NodeWrapper>> centralityResult)
    {
        List<SortItem<NodeWrapper>> results = new ArrayList<SortItem<NodeWrapper>>();
        for (SortItem<NodeWrapper> item: centralityResult)
        {
            // max reciprocal value
            double maxCentrality = 1.0 / centralityResult.get(0).value;
                
            // normalized by taking reciprocal and then divided by max reciprocal value
            SortItem<NodeWrapper> normalized = new SortItem<NodeWrapper>(item.item, 1.0 / item.value / maxCentrality);
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
    public List<SortItem<NodeWrapper>> getDegreeCentrality()
    {
        DegreeScorer<NodeWrapper> degree = 
            new DegreeScorer<NodeWrapper>(m_graph);
        return getCentrality(degree, false);
    }
    
    /**
     * Gets the normalized-degree centrality of every Node,
     * return in descending order
     */
    public List<SortItem<NodeWrapper>> getNormDegreeCentrality(List<SortItem<NodeWrapper>> centralityResult)
    {
        List<SortItem<NodeWrapper>> results = new ArrayList<SortItem<NodeWrapper>>();
        for (SortItem<NodeWrapper> item: centralityResult)
        {
            // max centrality
            double maxCentrality = centralityResult.get(0).value;
            
            // normalize by dividing max value
            SortItem<NodeWrapper> normalized = new SortItem<NodeWrapper>(item.item, item.value / maxCentrality);
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
    public List<SortItem<NodeWrapper>> getEigenvectorCentrality()
    {
        EigenvectorCentrality<NodeWrapper, Connection> eigenvector = 
            new EigenvectorCentrality<NodeWrapper, Connection>(m_graph, m_wtTansformer);
        return getCentrality(eigenvector, false);
    }
    
    /**
     * Gets the pagerank centrality of every Node,
     * return in descending order
     */
    public List<SortItem<NodeWrapper>> getPageRankCentrality()
    {
        PageRank<NodeWrapper, Connection> pagerank = 
            new PageRank<NodeWrapper, Connection>(m_graph, m_wtTansformer, 0.0001);
        return getCentrality(pagerank, false);
    }
    
    /**
     * Gets the reachability centrality of every Node,
     * return in descending order
     */
    public List<SortItem<NodeWrapper>> getReachabilityCentrality()
    {  
        if (m_shortestPath == null)
            m_shortestPath = new DijkstraShortestPath(m_graph, m_wtTansformer);
        
        int nNodeFinished = 0;
        List<SortItem<NodeWrapper>> results = new ArrayList<SortItem<NodeWrapper>>();
        List<NodeWrapper> nodeWrappers = NodeWrapper.getList();
        for (NodeWrapper nodeWrapper : nodeWrappers)
        {
            // shortest steps between node and every other node
            double value = 0;
            for (NodeWrapper nodeWrapper2 : nodeWrappers)
            {
                Number step = m_shortestPath.getDistance(nodeWrapper, nodeWrapper2);
                if (step != null && step.intValue() > 0)
                {
                    value += 1.0 / step.intValue();
                }
            }
            
            SortItem<NodeWrapper> result = new SortItem<NodeWrapper>(nodeWrapper, value);
            result.ascendingOrder = false;
            results.add(result);
            
            if (++nNodeFinished % 10 == 0 || nNodeFinished == nodeWrappers.size())
            {
                System.out.println("Finished: " + nNodeFinished + " / " + nodeWrappers.size());
            }
            
            // refresh DijkstraShortestPath object
            if (nNodeFinished % 500 == 0)
            {
                m_shortestPath = new DijkstraShortestPath(m_graph, m_wtTansformer);
            }
        }
        Collections.sort(results);
        
        return results;
    }
    
    /**
     * Gets the normalized-reachability centrality of every Node,
     * return in descending order
     */
    public List<SortItem<NodeWrapper>> getNormReachabilityCentrality(List<SortItem<NodeWrapper>> centralityResult)
    {
        List<SortItem<NodeWrapper>> results = new ArrayList<SortItem<NodeWrapper>>();
        for (SortItem<NodeWrapper> item: centralityResult)
        {
            // max centrality
            double maxCentrality = centralityResult.get(0).value;
            
            // normalize by dividing max value
            SortItem<NodeWrapper> normalized = new SortItem<NodeWrapper>(item.item, item.value / maxCentrality);
            normalized.ascendingOrder = false;
            results.add(normalized);
        }
        //Collections.sort(results);
        
        return results;
    }
    
    private List<SortItem<NodeWrapper>> getCentrality(VertexScorer scorer, boolean ascendingOrder)
    {
        int nNodeFinished = 0;
        List<SortItem<NodeWrapper>> results = new ArrayList<SortItem<NodeWrapper>>();
        List<NodeWrapper> nodeWrappers = NodeWrapper.getList();
        for (NodeWrapper nodeWrapper : nodeWrappers)
        {
            double value = new Double(scorer.getVertexScore(nodeWrapper).toString());
            
            SortItem<NodeWrapper> result = new SortItem<NodeWrapper>(nodeWrapper, value);
            result.ascendingOrder = ascendingOrder;
            results.add(result);
            
            if (++nNodeFinished % 10 == 0 || nNodeFinished == nodeWrappers.size())
            {
                System.out.println("Finished: " + nNodeFinished + " / " + nodeWrappers.size());
            }
        }
        Collections.sort(results);
        
        return results;
    }
    
    // very slow!!!
    public void showGraph()
    {
        // create Layout<V, E>, 
        Layout<NodeWrapper, Connection> layout = new CircleLayout<NodeWrapper, Connection>(m_graph);
        layout.setSize(new Dimension(700,700));
        
        // create VisualizationComponent<V,E>
        VisualizationViewer<NodeWrapper, Connection> vv = 
            new VisualizationViewer<NodeWrapper, Connection>(layout);
        vv.setPreferredSize(new Dimension(750,750));
        // Show vertex and edge labels
        //vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        //vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse<NodeWrapper, Connection> gm = 
            new DefaultModalGraphMouse<NodeWrapper, Connection>();
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
        List<SortItem<NodeWrapper>> degree = analysis.getDegreeCentrality();
        List<SortItem<NodeWrapper>> ndegree = analysis.getNormDegreeCentrality(degree);        
        System.out.println("Calculation finished!\n");
        // closeness
        System.out.println("Calculating closeness centrality...");
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<NodeWrapper>> closeness = analysis.getClosenessCentrality();
        List<SortItem<NodeWrapper>> ncloseness = analysis.getNormClosenessCentrality(closeness);        
        System.out.println("Calculation finished!\n");  
        // reachability
        System.out.println("Calculating reachability centrality...");
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<NodeWrapper>> reachability = analysis.getReachabilityCentrality();
        List<SortItem<NodeWrapper>> nreachability = analysis.getNormReachabilityCentrality(reachability);        
        System.out.println("Calculation finished!\n");
        // betweenness
        System.out.println("Calculating betweenness centrality...");
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<NodeWrapper>> betweenness = analysis.getBetweennessCentrality();
        List<SortItem<NodeWrapper>> nbetweenness = analysis.getNormBetweennessCentrality(betweenness);        
        System.out.println("Calculation finished!\n");
        
        // generate output
        System.out.println("Output to file...");
        DecimalFormat format = new DecimalFormat("0.0000"); 
        Hashtable<NodeWrapper, String> output = new Hashtable<NodeWrapper, String>();
        for (int i = 0; i < degree.size(); i++)
        {
            SortItem<NodeWrapper> item     = degree.get(i);
            SortItem<NodeWrapper> nitem    = ndegree.get(i);
            String nodeName = item.item.m_node.toString().split("\n")[0];
            output.put(item.item, nodeName + "\t" + format.format(item.value) + "\t" + format.format(nitem.value));
        }
        for (int i = 0; i < closeness.size(); i++)
        {
            SortItem<NodeWrapper> item     = closeness.get(i);
            SortItem<NodeWrapper> nitem    = ncloseness.get(i);
            
            String str = output.get(item.item);
            output.put(item.item, str + "\t" + format.format(item.value) + "\t" + format.format(nitem.value));
        }
        for (int i = 0; i < reachability.size(); i++)
        {
            SortItem<NodeWrapper> item     = reachability.get(i);
            SortItem<NodeWrapper> nitem    = nreachability.get(i);
            
            String str = output.get(item.item);
            output.put(item.item, str + "\t" + format.format(item.value) + "\t" + format.format(nitem.value));
        }
        for (int i = 0; i < betweenness.size(); i++)
        {
            SortItem<NodeWrapper> item     = betweenness.get(i);
            SortItem<NodeWrapper> nitem    = nbetweenness.get(i);
            
            String str = output.get(item.item);
            output.put(item.item, str + "\t" + format.format(item.value) + "\t" + format.format(nitem.value));
        }
        
        // output to file
        File centralityFile = new File("./" + jarFile.getName() + "_centrality.txt");
        if (centralityFile.exists())
        {
            centralityFile.delete();
        }
        centralityFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(centralityFile));
        for (SortItem<NodeWrapper> item: degree)
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
        List<SortItem<NodeWrapper>> results = analysis.getClosenessCentrality();
        List<SortItem<NodeWrapper>> results2 = analysis.getNormClosenessCentrality(results);        
        System.out.println("Calculation finished!\n");
        for (int i = 0; i < results.size(); i++)
        {
            SortItem<NodeWrapper> sortItem = results.get(i);
            SortItem<NodeWrapper> sortItem2 = results2.get(i);         
            System.out.println("Rank " + nRank++ + ": ");
            System.out.println(sortItem.item.m_node + "\nValue: " + sortItem.value);
            System.out.println("Normalized Value: " + sortItem2.value);          
            System.out.println();
        } 
    }
}
