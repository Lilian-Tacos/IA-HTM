/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htm;

import graph.AbstractNetworkEdge;
import graph.EdgeInterface;
import java.util.Random;

/**
 *
 * @author farmetta
 */
public class MySynapse extends AbstractNetworkEdge {
    
    private double currentValue = new Random().nextDouble();
    private final double THRESHOLD = 0.5;
    private MyNeuron neuron;
    
    
    protected MySynapse(EdgeInterface _edge, MyNeuron n) {
        super(_edge);
        currentValueUdpate(0);
        neuron = n;
    }
    
    public void currentValueUdpate(double delta) {
        currentValue += delta;
        
        if (currentValue > 1) {
            currentValue = 1;
        }
        if (currentValue < 0) {
            currentValue = 0;
        }
        
        if (currentValue >= THRESHOLD) {
            getEdge().setState(EdgeInterface.State.ACTIVATED);
        } else {
            getEdge().setState(EdgeInterface.State.DESACTIVATED);
        }
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public MyNeuron getNeuron() {
        return neuron;
    }

}
