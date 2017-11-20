package input;

import java.awt.*;
import java.util.HashMap;

public class Grille {
    private int taille;
    private boolean gagne;
    private HashMap<Point, Integer> grille;
    /*
        0 = case vide
        1 = position actuelle
        2 = objectif
        3 = mur (case inaccessible)
     */

    public Grille(int taille, Point debut, Point objectif){
        this.taille = taille;
        this.gagne = false;
        this.grille = new HashMap<Point, Integer>();
        for (int i=0; i<taille; i++){
            for (int j=0; j<taille; j++){
                Point n = new Point(i,j);
                grille.put(n, 0);
            }
        }
        grille.replace(debut, 1);
        if (objectif != null) {
            grille.replace(objectif, 2);
        }
    }

    public int getTaille() {
        return taille;
    }

    public boolean isGagne() {
        return gagne;
    }
}
