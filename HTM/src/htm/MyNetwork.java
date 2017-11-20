/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htm;

import graph.EdgeBuilder;
import graph.EdgeInterface;
import graph.NodeBuilder;
import graph.NodeInterface;
import graph.graphstream.GraphStreamBuilder;
import input.Entree;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author farmetta
 */
public class MyNetwork implements Runnable {

    private NodeBuilder nb;
    private EdgeBuilder eb;
    
    private ArrayList<MyNeuron> lstMN;
    private ArrayList<MyColumn> lstMC;

    private Entree input;
    private int NB_MAX_COL_ACTIVE;
    
    
    public MyNetwork(GraphStreamBuilder _gb, Entree input) {
        nb = _gb;
        eb = _gb;
        this.input = input;
    }

    public void buildNetwork(int nbInputs, int nbColumns, int nbMaxColActive) {
        this.NB_MAX_COL_ACTIVE = nbMaxColActive;
        
        // création des entrées
        lstMN = new ArrayList<MyNeuron>();
        for (int i = 0; i < 2 * nbInputs; i++) {
            NodeInterface ni = nb.getNewNode();
            MyNeuron n = new MyNeuron(ni);
            n.getNode().setPosition(i, 0);
            ni.setAbstractNetworkNode(n);
            lstMN.add(n);
        }
        // création des colonnes
        lstMC = new ArrayList<MyColumn>();
        int nbColonnes = 2 * nbColumns;
        for (int i = 0; i < nbColonnes; i++) {
            NodeInterface ni = nb.getNewNode();
            MyColumn c = new MyColumn(ni);
            // On centre les colonnes pour un bel affichage
            c.getNode().setPosition((int) (double) ( ((i + 0.5) / nbColonnes) * lstMN.size()), nbInputs);
            ni.setAbstractNetworkNode(c);
            lstMC.add(c);
        }

        // Création des synapses
        // La première moitié des colonnes est reliée à la première moitiée des neuronnes
        for (int i=0; i<nbColumns; i++){
            MyColumn c = lstMC.get(i);
            for (int j=0; j<nbInputs; j++ ){
                // On relie la colonne à toute la première moitié des neuronnes via des synapses
                MyNeuron n = lstMN.get(j);
                EdgeInterface e = eb.getNewEdge(n.getNode(), c.getNode());
                // Le synapse sait à quel neuronne il est reliée
                MySynapse s = new MySynapse(e, n);
                e.setAbstractNetworkEdge(s);
                // La colonne connait tous ses synapses
                c.addSynapse(s);
            }
        }

        // La deuxième moitié des colonnes est reliée à la deuxième moitiée des neuronnes
        for (int i=nbColumns; i<lstMC.size(); i++){
            MyColumn c = lstMC.get(i);
            for (int j=nbInputs; j<lstMN.size(); j++ ){
                // On relie la colonne à toute la première moitié des neuronnes via des synapses
                MyNeuron n = lstMN.get(j);
                EdgeInterface e = eb.getNewEdge(n.getNode(), c.getNode());
                // Le synapse sait à quel neuronne il est reliée
                MySynapse s = new MySynapse(e, n);
                e.setAbstractNetworkEdge(s);
                // La colonne connait tous ses synapses
                c.addSynapse(s);
            }
        }
        
    }

    @Override
    public void run() {
        boolean loop = true;
        final double MIN_VALUE = 0.8;
        int nbLoops = 0;
        while (loop) {
            // On met à jour les neuronnes en fonction de l'entrée
            input.positionToInput(lstMN);

            // 1) Calcul des valeurs des colonnes en fonction des entrées actives et du poids des synapses
            for (MyColumn c : lstMC) {
                double value = 0;
                for (MySynapse s : c.getSynapses()){
                    // Si le neuronne et le synapse sont actifs
                    if (s.isActive() && s.getNeuron().isActive()){
                        // On ajoute le poids du synapse (compris entre treshold (0.5) et 1)
                        value += s.getCurrentValue();
                    }
                }
                // Min overlap
                if (value < MIN_VALUE){
                    value = 0;
                }
                // Boost
                else{
                    value *= c.getBoost();
                }
                c.setValue(value);
            }

            // 2) On active les colonnes
            // Calcul de la valeur à partir de laquelle les colonnes seront activéss
            ArrayList<MyColumn> toActive = new ArrayList<MyColumn>();
            for (MyColumn c : lstMC) {
                // On désactive toutes les colonnes
                c.setActive(false);
                // Si la valeur est supérieure à 0, la colonne est potentiellement active
                if (c.getValue() > 0){
                    toActive.add(c);
                }
            }
            // Si on a trop de colonnes actives on en désactive
            while (toActive.size() > NB_MAX_COL_ACTIVE){
                double minValue = toActive.get(0).getValue();
                MyColumn colMin = toActive.get(0);
                // On recherche la colonne avec la plus petite val min
                for (MyColumn c : toActive) {
                    if (c.getValue() < minValue){
                        minValue = c.getValue();
                        colMin = c;
                    }
                }
                toActive.remove(colMin);
            }
            // On active les colonnes restantes
            for (MyColumn c : toActive) {
                c.setActive(true);
            }

            // 3) Apprentissage : On met à jour les poids des synapses
            // Mise à jour sur les colonnes actives
            for (MyColumn c : toActive){
                for (MySynapse s : c.getSynapses()){
                    if (s.getNeuron().isActive()){
                        s.currentValueUdpate(0.075);
                    }
                    else{
                        s.currentValueUdpate(-0.075);
                    }
                }
            }
            // Mise à jour de toutes les colonnes
            // Calcul du minDutyCycle (the minimum desired firing rate for a cell)
            double minDutyCycle = lstMC.get(0).getDutyCycle();
            for (MyColumn c : lstMC){
                if (c.getDutyCycle() > minDutyCycle){
                    minDutyCycle = c.getDutyCycle();
                }
            }
            // 0.5 pour essayer de conserver le boost autour de 2
            minDutyCycle *= 0.5;
            // Mise à jour de toutes les colonnes
            for (MyColumn c : lstMC){
                // On augmente le dutyCycle si la colonne est active, on le diminue sinon
                c.updateDutyCycle();
                // Boost >= 1
                c.setBoost(c.getDutyCycle() / minDutyCycle);
            }



            Random rand = new Random();
            // On met à jour l'entrée pour le prochain tour
            input.setPosition(rand.nextInt(input.getTaille()), rand.nextInt(input.getTaille()));
            // Tempo pour voir l'animation, au début on laisse une phase d'apprentissage
            if(nbLoops>200) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MyNetwork.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            nbLoops++;
        }
    }
    
}
