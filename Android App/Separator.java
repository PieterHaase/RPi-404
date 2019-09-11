package haw_hamburg.rpi_404;

import android.graphics.Color;
import processing.core.PApplet;

//======================================================================================================//
// Projekt: RPi-404         Author: Pieter Haase                                                        //
//------------------------------------------------------------------------------------------------------//
// Einfaches PApplet zum zeichnen einer horizontalen Trennlinie zwischen Radar/Joystick und Camera-Feed //
// Kann bei Bedarf durch aufwendigere Grafiken oder interaktiven Inhalt erweitert werden                //
//======================================================================================================//

public class Separator extends PApplet {

    // Laden der in der Klasse "Settings" definierten Farben
    private int backgroundColor = Color.parseColor(Settings.backgroundColor);
    private int mainColor = Color.parseColor(Settings.mainColor);

    public void settings(){
        fullScreen();
    }

    public void setup(){

        frameRate(3);                           // Niedrige Frame-Rate, da statischer Inhalt
    }

    public void draw(){
        drawLine();
    }

    // Zeichnen der Trennlinie
    private void drawLine(){
        background(backgroundColor);
        stroke(mainColor);
        strokeWeight(2);
        line(width/2,Settings.frameMargin,width/2,height-Settings.frameMargin);
    }
}
