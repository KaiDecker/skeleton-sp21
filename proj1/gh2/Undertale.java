package gh2;

import java.io.IOException;

public class Undertale {
    public static void main(String[] args) {
        GuitarPlayer player = new GuitarPlayer(new java.io.File("C:/Learning/CS61B/skeleton-sp21/proj1/gh2/Fallen Down Reprise.mid"));
        player.play();
    }
}
