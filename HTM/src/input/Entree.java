package input;

import graph.NodeInterface;
import htm.MyNeuron;

import java.awt.*;
import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Entree {
    private Point position;
    private Point objectif;
    private Grille grille;

    public Entree(int taille, Point debut, Point objectif){
        this.position = debut;
        this.objectif = objectif;
        this.grille = new Grille(taille, debut, objectif);
    }

    public void move(int dirX, int dirY){
        setPosition(position.x + dirX,position.y + dirY);
    }

    // Traduit les coordonnées en une entrée binaire
    public void positionToInput(ArrayList<MyNeuron> neurons){
        // On veut qu'il y ait des intersections
        final int RANGE_NEURONNES = (int) ( 1.1 * ((double) neurons.size() / grille.getTaille()));
        // Le nombre de neuronnes pour représenter X et Y
        int neuronsPerX = neurons.size() /2;
        int neuronsPerY = neurons.size() - neuronsPerX;
        // On veut encoder X sur la première moitié des neuronnes et Y sur la 2ème
        // On désactive tous les neuronnes
        for (int i=0; i<neurons.size(); i++){
            neurons.get(i).setActive(false);
        }

        // On active les neuronnes correspondants pour X
        int centerPerX = (int) ((double) (position.x + 0.5) / grille.getTaille() * neuronsPerX);
        for(int i=centerPerX - RANGE_NEURONNES; i<=centerPerX + RANGE_NEURONNES; i++){
            if(i>=0 && i<neuronsPerX){
                neurons.get(i).setActive(true);
            }
        }
        // Pareil pour Y
        int centerPerY =  neuronsPerX + (int) ((double) (position.y + 0.5) / grille.getTaille() * neuronsPerY);
        for(int i=centerPerY - RANGE_NEURONNES; i<=centerPerY + RANGE_NEURONNES; i++){
            if(i>=neuronsPerX && i<neurons.size()){
                neurons.get(i).setActive(true);
            }
        }
    }

    public void setPosition(int x, int y){
        // Entre 0 et taille - 1
        x = min(x, grille.getTaille() -1);
        x = max(x,0);
        y = min(y, grille.getTaille() -1);
        y = max(y,0);
        position.setLocation(x,y);
    }

    public int getTaille(){
        return grille.getTaille();
    }
}
