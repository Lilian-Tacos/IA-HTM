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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.StrictMath.abs;


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
    private int longueurMemoireActivations;
    
    
    public MyNetwork(GraphStreamBuilder _gb, Entree input) {
        nb = _gb;
        eb = _gb;
        this.input = input;
    }

    public void buildNetwork(int nbInputs, int nbColumns, int nbMaxColActive, int nbApprentissage, boolean splitColonnes, int longueurMemoireActivations, int voisinage) {
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
        else if (voisinage > 0){
            // Les colonnes sont reliées aux neuronnes proches
            for (int i = 0; i < nbColumns; i++) {
                MyColumn c = lstMC.get(i);
                // Calcul du neuronne centrale pour la colonne
                // On divise la colonne + 0.5 (pour ce centrer) par le total de colonne
                // On multiplie par le nombre de neuronnes
                // Cast en double pour la valeur exacte puis int pour arrondir au neurone le plus proche
                int centre = ((int) ((double) (((i + 0.5) / lstMC.size()) * lstMN.size())));
                // Pour chacun des neuronnes voisins
                for (int j = centre - voisinage; j < centre + voisinage; j++) {
                    // On relit ce neuronne à la colonne
                    if (j >= 0 && j < lstMN.size()){
                        MyNeuron n = lstMN.get(j);
                        EdgeInterface e = eb.getNewEdge(n.getNode(), c.getNode());
                        // Le synapse sait à quel neuronne il est reliée
                        MySynapse s = new MySynapse(e, n);
                        // Poid des synapses linéaire, 0.7 au centre, 0.3 au bord
                        s.currentValueUdpate(-1);
                        double diff = voisinage - abs(centre - j);
                        s.currentValueUdpate(0.3 + diff/voisinage * 0.4);
                        e.setAbstractNetworkEdge(s);
                        // La colonne connait tous ses synapses
                        c.addSynapse(s);
                    }
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
        // La longueur max de la liste des dernières activations, pour la fréquence et le boost
        this.longueurMemoireActivations = longueurMemoireActivations;
    }

    @Override
    public void run() {
        boolean loop = true;
        final double MIN_VALUE = 0.8, UP_SYNAPSE = 0.05;
        int nbLoops = 0;
        while (loop) {
            // On met à jour la valeur de l'entrée (random, ..., voir fonction updateInput)
            input.updateInput(nbLoops>this.nbApprentissage);

            // On met à jour les neuronnes en fonction de l'entrée
            input.positionToInput(lstMN);

            // 1) Calcul des valeurs des colonnes en fonction des entrées actives et du poids des synapses
            for (MyColumn c : lstMC) {
                double value = 0;
                for (MySynapse s : c.getSynapses()){
                    // Poids du synapse en prenant en compte le boost d'inactivitée
                    // ce boost permet d'activer virtuellement des liens, si jamais la colonne n'a aucun synapse actif par exemple
                    double poids = s.getCurrentValue() * c.getBoostInactivite();
                    // Si le neuronne et le synapse (en comptant le boost) sont actifs
                    if (poids > s.getTHRESHOLD() && s.getNeuron().isActive()){
                        // On ajoute le poids du synapse * le boost (compris entre treshold (0.5) et boost)
                        value += poids;
                    }
                }
                // Min overlap, la colonne doit dépasser un seuil pour s'activer
                if (value < MIN_VALUE){
                    value = 0;
                }
                // Boost si on a perdu plusieurs "concours" avec les autres colonnes
                else{
                    value *= c.getBoostGlobal();
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
                // On augmente son boost global pour qu'elle ait plus de chances la prochaine fois (boost *= 1.2)
                colMin.updateBoostGlobal();
            }
            // On active les colonnes restantes
            for (MyColumn c : toActive) {
                c.setActive(true);
                // On réinitialise leur boost global (=1)
                c.reiniBoostGlobal();
            }

            // 3) Apprentissage : On met à jour les poids des synapses des colonnes actives
            // Mise à jour sur les colonnes actives
            for (MyColumn c : toActive){
                for (MySynapse s : c.getSynapses()){
                    // On met à jour les poids des synapses des colonnes actives
                    // + 0.05 si le neuronne est actif, - 0.05 sinon
                    if (s.getNeuron().isActive()){
                        s.currentValueUdpate(UP_SYNAPSE);
                    }
                    else{
                        s.currentValueUdpate(-UP_SYNAPSE);
                    }
                }
            }
            // Boost d'inactivité
            // Frequence max des colonnes
            double maxFreq = lstMC.get(0).getFreqActivation();
            for (MyColumn c : lstMC){
                if (c.getFreqActivation() > maxFreq){
                    maxFreq = c.getFreqActivation();
                }
            }
            // Mise à jour du boost d'inactivité de toutes les colonnes
            for (MyColumn c : lstMC){
                // On met à jour la liste des dernières activités
                c.updateLastActivations(longueurMemoireActivations);
                // On boost les colonnes qui n'ont pas été activées "depuis longtemps"
                // 20 fois moins d'activations que la colonne la plus active
                if (c.getFreqActivation() < 0.05 * maxFreq) {
                    c.setBoostInactivite(c.getBoostInactivite() * 2);
                }
                // Si la colonne a été assez active on réinitialise son boost
                else {
                    c.setBoostInactivite(1);
                }
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
