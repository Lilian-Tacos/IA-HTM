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
    private boolean active;
    private double boost;
    private double dutyCycle;

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
        dutyCycle = 1;
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

    public double getBoost() {
        return boost;
    }

    public void setBoost(double boost) {
        this.boost = boost;
        if (this.boost < 1){
            this.boost = 1;
        }
    }

    public double getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle;
        if (this.dutyCycle > 1){
            this.dutyCycle = 1;
        }
        else if (this.dutyCycle < 0.000001){
            this.dutyCycle = 0.000001;
        }
    }

    public void updateDutyCycle(){
        if (active){
            setDutyCycle(dutyCycle * 1.1);
        }
        else {
            setDutyCycle(dutyCycle * 0.9);
        }
    }
}


