/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htm;

import graph.AbstractNetworkNode;
import graph.NodeInterface;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 *
 * @author farmetta
 */
public class MyColumn extends AbstractNetworkNode {
    private double value;
    private ArrayList<MySynapse> synapses;
    private boolean active;
    private double boostGlobal;
    private double boostInactivite;
    private LinkedList<Boolean> lastActivations;
    // Nombre de TRUE dans la liste (pour éviter les calculs inutiles)
    private int nbActivations;

    
    public MyColumn(NodeInterface _node) {
        super(_node);
        synapses = new ArrayList<MySynapse>();
        lastActivations = new LinkedList<Boolean>();
        nbActivations = 0;
        boostGlobal = 1;
        boostInactivite = 1;
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

    public double getBoostGlobal() {
        return boostGlobal;
    }

    public void updateBoostGlobal(){
        boostGlobal *= 1.2;
    }

    public void reiniBoostGlobal(){
        boostGlobal = 1;
    }

    public double getBoostInactivite() {
        return boostInactivite;
    }

    public void setBoostInactivite(double boostInactivite) {
        this.boostInactivite = boostInactivite;
        if (this.boostInactivite < 1){
            this.boostInactivite = 1;
        }
    }

    public void updateLastActivations(int tailleMax){
        // On ajoute le statut actuel dans la liste
        lastActivations.addLast(active);
        // Si liste pleine, on enlève les anciens éléments
        while (lastActivations.size() > tailleMax){
            if (lastActivations.removeFirst()){
                // Si l'élément retiré était une activation, on l'enlève du nombre d'activations
                // Variable dynamique pour éviter de recompter le nombre de true dans la liste à chaque appel
                nbActivations --;
            }
        }
    }

    public double getFreqActivation(){
        if (lastActivations.isEmpty())
            return 0;
        return nbActivations / lastActivations.size();
    }
}


