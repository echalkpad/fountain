package ws.finson.audiosp.app.attic;

import java.util.List;

/**
 * 
 *
 * @author Doug Johnson
 * @since Sep 2, 2014
 *
 */
public interface ComponentNode {
    
    /**
     * Get the parent node of this ComponentNode.  
     * @return the ComponentNode that is the parent of this object.
     */
    ComponentNode getParent();
    
    /**
     * Get child nodes of this node.
     * @return a List of the child ComponentNodes, if any.  May be empty, will not be null.
     */
    List<ComponentNode> getChildren();

}
