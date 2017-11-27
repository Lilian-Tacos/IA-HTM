package input;

import htm.MyNeuron;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Entree {
    private Point position;
    private Grille grille;

    public Entree(int taille, Point debut){
        this.position = debut;
        this.grille = new Grille(taille, debut);
    }

    public void move(int dirX, int dirY){
        setPosition(position.x + dirX,position.y + dirY);
    }

    // Traduit les coordonnées en une entrée binaire
    public void positionToInput(ArrayList<MyNeuron> neurons){
        // On veut qu'il y ait des intersections
        // NB neuronnes / taille pour avoir le nombre de neuronnes par entrée SANS intersection
        // / 2 car on a 2 entrées (X et Y)
        // * 1.5 pour créer une intersection (50% de neuronnes en commun entre 2 valeurs voisines)
        // / 2 car on prend cet écart à gauche et à droite du centre de la valeur
        // = 0.375 de coeff
        final int RANGE_NEURONNES = (int) ( 0.375 * ((double) neurons.size() / grille.getTaille()));
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

    public void updateInput(){
        updateInputRandom();
    }

    public void updateInputRandom(){
        Random rand = new Random();
        // On met à jour l'entrée pour le prochain tour
        this.setPosition(rand.nextInt(this.getTaille()), rand.nextInt(this.getTaille()));
    }

    public void afficherEntree(){
        System.out.println("x = "+position.x+" : y = "+position.y);
    }

}
