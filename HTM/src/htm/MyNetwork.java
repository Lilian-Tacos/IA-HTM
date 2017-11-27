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
    private int nbApprentissage;
    
    
    public MyNetwork(GraphStreamBuilder _gb, Entree input) {
        nb = _gb;
        eb = _gb;
        this.input = input;
    }

    public void buildNetwork(int nbInputs, int nbColumns, int nbMaxColActive, int nbApprentissage, boolean splitColonnes) {
        this.NB_MAX_COL_ACTIVE = nbMaxColActive;
        
        // création des entrées
        lstMN = new ArrayList<MyNeuron>();
        for (int i = 0; i < nbInputs; i++) {
            NodeInterface ni = nb.getNewNode();
            MyNeuron n = new MyNeuron(ni);
            // On crée un espace au milieu pour délimité les 2 entrées
            n.getNode().setPosition(i + (i >= nbInputs/2? 2:0), 0);
            ni.setAbstractNetworkNode(n);
            lstMN.add(n);
        }
        // création des colonnes
        lstMC = new ArrayList<MyColumn>();
        for (int i = 0; i < nbColumns; i++) {
            NodeInterface ni = nb.getNewNode();
            MyColumn c = new MyColumn(ni);
            // On centre les colonnes pour un bel affichage
            c.getNode().setPosition((int) (double) ( ((i + 0.5) / nbColumns) * (lstMN.size() + 2)), nbInputs/2);
            ni.setAbstractNetworkNode(c);
            lstMC.add(c);
        }


        // Création des synapses
        if (splitColonnes){
            // La première moitié des colonnes est reliée à la première moitié des neuronnes
            for (int i = 0; i < nbColumns/2; i++) {
                MyColumn c = lstMC.get(i);
                for (int j = 0; j < nbInputs/2; j++) {
                    // On relie la colonne à la première moitié des neuronnes
                    MyNeuron n = lstMN.get(j);
                    EdgeInterface e = eb.getNewEdge(n.getNode(), c.getNode());
                    // Le synapse sait à quel neuronne il est reliée
                    MySynapse s = new MySynapse(e, n);
                    e.setAbstractNetworkEdge(s);
                    // La colonne connait tous ses synapses
                    c.addSynapse(s);
                }
            }
            // Et la deuxième moitié des colonnes est reliée à la deuxième moitié des neuronnes
            for (int i = nbColumns/2; i < nbColumns; i++) {
                MyColumn c = lstMC.get(i);
                for (int j = nbInputs/2; j < nbInputs; j++) {
                    // On relie la colonne à la deuxième moitié des neuronnes
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
        else {
            // Toutes les colonnes sont reliées à tous les neuronnes
            for (int i = 0; i < nbColumns; i++) {
                MyColumn c = lstMC.get(i);
                for (int j = 0; j < nbInputs; j++) {
                    // On relie la colonne à tous les neuronnes
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

        // Le nombre d'entrées successives sans animation afin de faire un apprentissage rapide
        this.nbApprentissage = nbApprentissage;
    }

    @Override
    public void run() {
        boolean loop = true;
        final double MIN_VALUE = 0.8;
        int nbLoops = 0;
        while (loop) {
            // On met à jour la valeur de l'entrée (random, ..., voir fonction uodateInput)
            input.updateInput();

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
                // On l'enlève des colonnes à activer
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
                    // On met à jour les poids des synapses
                    if (s.getNeuron().isActive()){
                        s.currentValueUdpate(0.075);
                    }
                    else{
                        s.currentValueUdpate(-0.075);
                    }
                }
            }
            // Mise à jour de toutes les colonnes
            // Mise à jour de la "fréquence d'activation" ( *1.1 si acitve, *0.9 sinon)
            // On recherche la plus petite valeur
            double minFreqActivation = lstMC.get(0).getFreqActivation();
            for (MyColumn c : lstMC){
                if (c.getFreqActivation() > minFreqActivation){
                    minFreqActivation = c.getFreqActivation();
                }
            }
            // Mise à jour de toutes les colonnes
            for (MyColumn c : lstMC){
                // On augmente la fréquence si la colonne est active, on la diminue sinon
                c.updateFreqActivation();
                // Boost >= 1
                // On met à jour le boost linéairement, il vaut 2.5 pour la colonne qui était la moins active
                c.setBoost(0.5 + 2 * minFreqActivation / c.getFreqActivation());
            }


            // Tempo pour voir l'animation, au début on laisse une phase d'apprentissage
            if(nbLoops>this.nbApprentissage) {
                try {
                    input.afficherEntree();
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MyNetwork.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            nbLoops++;
        }
    }
    
}
