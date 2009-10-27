package hk.ust.cse.pag.cg.jung;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import hk.ust.cse.pag.cg.cfg.Node;

public class NodeWrapper
{
    public int m_id;
    public Node m_node;
    public NodeWrapper(Node node)
    {
        m_node  = node;
        m_id    = s_nodeList.size();
        
        // add to hashtable
        s_nodeWrapperList.add(this);
        s_nodeList.put(m_node, this);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof NodeWrapper)
        {
            NodeWrapper nodeWrapper = (NodeWrapper) obj;

            if (nodeWrapper == null)
            {
                return false;
            }

            return m_id == nodeWrapper.m_id;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return m_id;
    }
    
    public static List<NodeWrapper> getList()
    {
        return s_nodeWrapperList;
    }
    
    public static NodeWrapper getNodeWrapper(Node node)
    {
        return s_nodeList.get(node);
    }
    
    public static ArrayList<NodeWrapper> s_nodeWrapperList = new ArrayList<NodeWrapper>();
    public static Hashtable<Node, NodeWrapper> s_nodeList = new Hashtable<Node, NodeWrapper>();  
}
