/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htm;
import graph.graphstream.GraphStreamBuilder;
import graph.graphstream.MyGraphStreamEdge;
import graph.graphstream.MyGraphStreamEdge2;
import graph.graphstream.MyGraphStreamNode;
import input.Entree;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.SingleGraph;

import java.awt.*;

/**
 *
 * @author farmetta
 */
public class HTM {

    public static void main(String[] args) {
        // Variables du réseau
        final int tailleGrille = 5, // Taille des entrées (entre 0 et taille -1)
                nbInputs = 40,      // Nombre de neuronnes
                nbColumns = 6,      // Nombre de colonnes
                nbMaxColActive = 2, // Nombre max de colonnes actives simultanément
                longueurMemoireActivations = 50, // Longueur max de la liste des dernières activations, pour la fréquence et le boost
                nbApprentissage = 0,  // Nombre d'entrées aléatoires avant l'affichage
                voisinage = 6;      // Moitié de la largeur du voisinage, si spliColonnes = false (0 = tous connectés)
        final boolean splitColonnes = false,    // Sépare le réseau en 2 sous réseau (un pour chacune des entrées)
                entreeManuel = false,   // Permet de saisir manuellement des entrées après l'apprentissage
                afficherSynapsesInactifs = true;    // Affiche les synapses inactif (valeur < treshold)

        Graph graph = new SingleGraph("graph"); // création du graphe
        graph.setNodeFactory(new NodeFactory<MyGraphStreamNode>() {
			public MyGraphStreamNode newInstance(String id, Graph graph) {
				return new MyGraphStreamNode((AbstractGraph) graph, id); // les noeuds seront de type MyGraphStreamNode
			}
		});

        if (afficherSynapsesInactifs){
            graph.setEdgeFactory(new EdgeFactory<MyGraphStreamEdge>() {
                @Override
                public MyGraphStreamEdge newInstance(String id, Node src, Node dst, boolean directed) {
                    return new MyGraphStreamEdge2(id, src, dst, directed); // les arrêtes seront du type MyGraphStreamEdge
                }
            });
        }
        else {
            graph.setEdgeFactory(new EdgeFactory<MyGraphStreamEdge>() {
                @Override
                public MyGraphStreamEdge newInstance(String id, Node src, Node dst, boolean directed) {
                    return new MyGraphStreamEdge(id, src, dst, directed); // les arrêtes seront du type MyGraphStreamEdge
                }
            });
        }

        GraphStreamBuilder gb = new GraphStreamBuilder(graph);
        MyNetwork mn = new MyNetwork(gb, new Entree(tailleGrille, new Point(0,0), entreeManuel));
        
        mn.buildNetwork(nbInputs, nbColumns, nbMaxColActive, nbApprentissage, splitColonnes, longueurMemoireActivations, voisinage);
        graph.display(false);


        new Thread(mn).start();
    }
    
}
