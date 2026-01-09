package com.projet.javafx;

/**
 * Cette classe est nécessaire pour contourner un problème de vérification du
 * runtime JavaFX
 * lors du lancement depuis certains IDE ou via un JAR.
 * En ne faisant pas hériter cette classe de 'Application', on évite que la JVM
 * ne cherche
 * les modules JavaFX sur le module-path avant le démarrage.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
