/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htm;

import graph.AbstractNetworkNode;
import graph.NodeInterface;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author farmetta
 */
public class MyColumn extends AbstractNetworkNode {
    private double value;
    private ArrayList<MySynapse> synapses;
    private int center;

    /**
     * TODO : Au cours de l'apprentissage, chaque colonne doit atteindre un taux d'activation. 
     * Une colonnne est activée si elle reçoit suffisament de retours positif de ses synapses 
     * (le retour est positif si la synapse est active et que son entrée associée l'est également).
     * 
     * Pour l'apprentissage, parcourir les synapses en entrée, et faire évoluer les poids synaptiques adéquatement.
     * 
     */
    
    
    public MyColumn(NodeInterface _node) {
        super(_node);
        synapses = new ArrayList<MySynapse>();
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public ArrayList<MySynapse> getSynapses() {
        return synapses;
    }

    public void addSynapse(MySynapse synapse){
        synapses.add(synapse);
    }

    public int getCenter() {
        return center;
    }

    public void setCenter(int center) {
        this.center = center;
    }
}


