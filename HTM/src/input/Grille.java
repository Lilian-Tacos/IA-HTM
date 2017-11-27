package input;

import java.awt.*;

public class Grille {
    private int taille;

    public Grille(int taille, Point debut){
        this.taille = taille;
    }

    public int getTaille() {
        return taille;
    }
}
