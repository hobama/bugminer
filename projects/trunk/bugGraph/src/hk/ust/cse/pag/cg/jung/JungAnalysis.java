package hk.ust.cse.pag.cg.jung;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
            System.out.println("Adding edges...");
            EdgeType edgeType = bDirected ? EdgeType.DIRECTED : EdgeType.UNDIRECTED;
            List<Connection> edges = m_edges.getKeyList();
            for (Connection edge : edges)
            {
                m_graph.addEdge(edge, 
                                edge.getSourceNode(), 
                                edge.getTargetNode(), 
                                edgeType);
            }
            System.out.println(edges.size() + " edges added to graph.");
            System.out.println("Graph creation finished!");
            
            // create weight transformer
            if (bWeight)
            {
                m_wtTansformer = new Transformer<Connection, Double>()
                {
                    public Double transform(Connection edge)
                    {
                        // !!!
                        return new Double(1.0 / m_edges.getCount(edge));
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
     * Gets the closeness centrality of every Node,
     * return in ascending order
     */
    public List<SortItem<Node>> getClosenessCentrality()
    {
        ClosenessCentrality<Node, Connection> closeness = 
            new ClosenessCentrality<Node, Connection>(m_graph, m_wtTansformer);
        return getCentrality(closeness, true);
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
        DijkstraShortestPath<Node, Connection> dij = 
            new DijkstraShortestPath(m_graph, m_wtTansformer);
        
        List<SortItem<Node>> results = new ArrayList<SortItem<Node>>();
        List<Node> nodes = m_nodes.getKeyList();
        for (Node node : nodes)
        {
            // shortest steps between node and every other node
            double value = 0;
            for (Node node2 : nodes)
            {
                int nStep = dij.getPath(node, node2).size();
                if (nStep > 0)
                {
                    value += 1.0 / nStep;
                }
            }
            
            SortItem<Node> result = new SortItem<Node>(node, value);
            result.ascendingOrder = false;
            results.add(result);
        }
        Collections.sort(results);
        
        return results;
    }
    
    private List<SortItem<Node>> getCentrality(VertexScorer scorer, boolean ascendingOrder)
    {
        List<SortItem<Node>> results = new ArrayList<SortItem<Node>>();
        List<Node> nodes = m_nodes.getKeyList();
        for (Node node : nodes)
        {
            double value = new Double(scorer.getVertexScore(node).toString());
            
            SortItem<Node> result = new SortItem<Node>(node, value);
            result.ascendingOrder = ascendingOrder;
            results.add(result);
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
        // create CFGExtracter
        File jarFile = new File(Const.CHECKOUT_DIR
                + "/columba-svn/jar_repos/columba_r353.jar");
        CFGExtracter extracter = new CFGExtracter();
        System.out.println("Extracting Jar file...");
        extracter.extractInJarFile(jarFile);
        System.out.println("Extraction finished!");

        // create analyzer, and do analysis
        JungAnalysis analysis = new JungAnalysis(extracter, true, true);
       
        // centrality analysis
        int nRank = 1;
        System.out.println("Calculating centrality for every vertex...");
        List<SortItem<Node>> results = analysis.getReachabilityCentrality();
        System.out.println("Calculation finished!\n");
        for (SortItem<Node> sortItem : results)
        {
            System.out.println("Rank " + nRank++ + ": ");
            System.out.println(sortItem.item + "\nValue: " + sortItem.value);
            System.out.println();
        }
    }
}
