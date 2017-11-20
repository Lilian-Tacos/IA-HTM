/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htm;

import graph.AbstractNetworkNode;
import graph.NodeInterface;

/**
 *
 * @author farmetta
 */
public class MyNeuron  extends AbstractNetworkNode {
    private boolean active;

    
    public MyNeuron(NodeInterface _node) {
        super(_node);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if(this.active){
            this.getNode().setState(NodeInterface.State.ACTIVATED);
        }
        else{
            this.getNode().setState(NodeInterface.State.DESACTIVATED);
        }
    }
}
