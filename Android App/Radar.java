package haw_hamburg.rpi_404;

import android.graphics.Color;
import processing.core.PApplet;
import processing.core.PFont;

//=================================================================================================================================//
// Projekt: RPi-404         Author: Pieter Haase                                                                                   //
//---------------------------------------------------------------------------------------------------------------------------------//
// PApplet welches die Darstellung der gemessenen Distanzwerte des Ultraschallsensors des RPi-404 im Stile eines Radars ermöglicht //
// Die gemessenen Abstände werden hierbei entsprechend des Winkels und der Distanz als Linie dargestellt                           //
// Zusätzlich informiert eine weiter Linie entsprechend dem aktuellen Winkel der Messung über die Position des Ultraschallsensors  //
//=================================================================================================================================//

public class Radar extends PApplet {

    // Laden der in der Klasse "Settings" definierten Farben
    private int backgroundColor = Color.parseColor(Settings.backgroundColor);
    private int radarColor = Color.parseColor(Settings.accent1Color);
    private int objectColor = Color.parseColor(Settings.accent3Color);
    private int lineColor = Color.parseColor(Settings.accent2Color);
    private int textColor = Color.parseColor(Settings.mainColor);
    private int titleColor = Color.parseColor(Settings.mainColor);

    private int size = 0;
    private int lightStroke = Settings.lineWidthFine;
    private int heavyStroke = Settings.lineWidthBold;
    public static final int SMALL = 1;
    public static final int LARGE = 2;

    private MainActivity activity;

    private float[] array = new float[270];
    private float previousAngle;
    private float previousDistance = 53;

    private float smoothLevel = 0.5f;      // Für Low-Pass Filter

    private int count = 0;
    private int radius;
    private int maxDistance = 50;
    private int objectDetail = 2;       // höher = weniger
    private int lineAngle = 0;
//    private int margin = 20;


    private PFont fontSmall;
    private int smallTextSize = 24;
    private PFont fontBig;
    private int bigTextSize = 32;
    private PFont fontTitle;
    private int titleTextSize = 28;

    private float currentDistance;
    private float random1 = 0;
    private float random2 = 0;

//    private boolean isMaximized = false;

    public Radar(MainActivity activity, int size){
        this.activity = activity;
        this.size = size;
        lightStroke = Settings.lineWidthFine * size;
        heavyStroke = Settings.lineWidthBold * size;
        smallTextSize = Settings.textSizeSmall * size;
        bigTextSize = Settings.textSizeBig * size;
    }

    public void settings(){
        fullScreen();
    }

    public void setup()     // Wird beim Initialisieren aufgerufen
    {
        for (int i=0; i<array.length; i++){ array[i] = 53; }    // Alle Distanz-werte außerhalb des Anzeigebereichs setzen

        activity.setRadarArray(array);

        // RADAR TEST --------------------------------          // Generation von Distanz-Zufallswerten für Testzwecke
        if(Settings.testMode && size == SMALL){
            for (int i=0; i<array.length; i++){
            array[i] = 35 + random(-0.2f,0.2f);
            }
        }
        activity.setRadarArray(array);
        // -------------------------------------------

        frameRate(24);

        background(backgroundColor);
        radius = min(width, height);

        // Schriftarten generieren
        fontTitle = createFont("Arial",titleTextSize);
        fontBig = createFont("Arial",bigTextSize);
        fontSmall = createFont("Arial",smallTextSize);

        drawRadar();        // Hintergrund zeichnen
        loadPixels();       // Hintergrund in den Image-Buffer laden

        if(size == LARGE){
            noLoop();                 // großes Radar anhalten, da erstmal nicht sichtbar
        }


    }

    public void draw()      // Wird kontinuierlich entsprechend der FrameRate aufgerufen
    {
        // RADAR TEST --------------------------------      // Generation von Distanz-Zufallswerten für Testzwecke
        if(Settings.testMode && size == SMALL){
            if(count == 0){
                random1 = round(random(0,90));
                random2 = round(random(0,90));
            }
            currentDistance = (0.4f *sin (radians(random1 + count*1.73f)+0.7f*cos(radians(random2+ count*4.37f))) +1) * 40;

            activity.updateArray(count, currentDistance);
            lineAngle = count;
            count++;
            if(count == array.length){
                count = 0;
            }
        }
        // -------------------------------------------

        updatePixels();     // Laden und Zeichnen des Hintergrunds aus dem Image-Buffer

        pushMatrix();
        translate(width/2, height/2);
        translate(0,radius/30);
        rotate(radians(-90));                 // 0° = vorne

        drawRadarLine(lineAngle);
        drawObjectLine();

        popMatrix();

    }

    // Zeichnen der Radarlinie, die den aktuellen Winkel des Ultraschallsensors anzeigt
    private void drawRadarLine(int angle){

        stroke(lineColor);
        noFill();
        currentDistance = array[angle];
        line(radius/maxDistance* 5 *sin(radians(angle-45))/2,
                radius/maxDistance* 5 *cos(radians(angle-45))/2,
                radius / maxDistance * 55 *sin(radians(angle-45))/2,
                radius / maxDistance * 55 *cos(radians(angle-45))/2
        );
        if(currentDistance < 50){
            stroke(lineColor,70);
            drawArc(currentDistance,-135,135);
        }

        noStroke();
        pushMatrix();
        textAlign(RIGHT);
        fill(textColor);
        rotate(radians(90));
        textFont(fontBig,bigTextSize);
        if(currentDistance < 53){
            currentDistance = (round(currentDistance*10));
            text(currentDistance/10 + " cm",xValue(-45,65) - 2*size,yValue(-45,65)+bigTextSize);
        }
        else
            text("- - -",xValue(-45,65) - 15*size,yValue(-45,65)+bigTextSize);
        text(lineAngle-135 + "°",xValue(45,65) + bigTextSize*3.5f,yValue(45,65)+bigTextSize);
        popMatrix();
    }

    // Hintergrund-Skala des Radars zeichnen
    private void drawRadar(){

        pushMatrix();
        translate(width/2, height/2);
        translate(0,radius/30);
        rotate(radians(-90));                 // 0° = vorne

        noFill();
        strokeWeight(lightStroke);
        stroke(textColor,200);
        drawArc(10,-135,225);
        drawLine(10,60,-90);
        drawLine(49,60,90);
        stroke(radarColor);
        drawLine(51,65,45);
        drawLine(51,65,-45);

        line(xValue(135,65),yValue(135,65),xValue(135,65),yValue(135,65)+smallTextSize*5);
        line(xValue(45,65),yValue(45,65),xValue(45,65),yValue(45,65)-smallTextSize*5);

        stroke(textColor,120);
        drawLine(10,55,135);
        drawLine(10,55,-135);
        drawLine(30,35,45);
        drawLine(30,35,-45);
        drawArc(30,-90,112.5f);

        stroke(textColor,30);
        drawArc(20,-135,-67.5f);
        drawArc(20,0,90);
        drawArc(40,-112.5f,0);
        drawArc(40,45,90);
        drawLine(10,45,0);
        drawLine(10,45,90);

        strokeWeight(heavyStroke);
        strokeCap(SQUARE);
        stroke(radarColor,200);
        drawArc(50,-135,135);
        strokeWeight(lightStroke);
        noStroke();

        fill(backgroundColor);
        rect(-smallTextSize - 5,-radius, smallTextSize + 5, radius);

        fill(textColor,180);
        textAlign(CENTER);
        pushMatrix();
        rotate(radians(90));
        drawText("10",10);
        drawText("20",20);
        drawText("30",30);
        drawText("40",40);
        drawText("50",50);
        popMatrix();

        drawAngleText("-45",-45);
        drawAngleText("0",0);
        drawAngleText("45",45);
        drawAngleText("90",90);

        popMatrix();

    }

    // Linie zum Anzeigen von Objekten auf dem Radar zeichnen
    private void drawObjectLine(){

        for (int i=0; i<array.length; i+=objectDetail){
            float angle = i - 135;
            float distance = array[i];
            float transformedObjectAngle = radians(angle+64);
            float objectDistance = radius / maxDistance * distance;
            float x = objectDistance*sin(transformedObjectAngle)/2;
            float y = objectDistance*cos(transformedObjectAngle)/2;

            if(i>0){
                float prevX = previousDistance*sin(previousAngle)/2;
                float prevY = previousDistance*cos(previousAngle)/2;

                if(distance <53){
                    beginShape();                                           // Innere Fläche
                    noStroke();
                    fill(objectColor,70);
                    vertex(x,y);
                    vertex(prevX,prevY);
                    vertex(radius / maxDistance * 48 *sin(previousAngle)/2,
                            radius / maxDistance * 48 *cos(previousAngle)/2);
                    vertex(radius / maxDistance * 48 *sin(transformedObjectAngle)/2,
                            radius / maxDistance * 48 *cos(transformedObjectAngle)/2);
                    endShape(CLOSE);

                    stroke(objectColor,200);           // Äußere Linie
                    line(x,y,prevX,prevY);
                    noStroke();
                }
            }
            previousAngle = transformedObjectAngle;
            previousDistance = objectDistance;
        }
    }

    private float xValue(float angle, float distance){
        return radius / maxDistance * distance *sin(radians(-angle+180))/2;
    }

    private float yValue(float angle, float distance){
        return radius / maxDistance * distance *cos(radians(-angle+180))/2;
    }

    private void drawArc(float distance, float startAngle, float endAngle){
        arc(0, 0, radius / maxDistance * distance, radius / maxDistance * distance, radians(startAngle), radians(endAngle));
    }

    private void drawLine(float from, float to, float angle){
        line(radius / maxDistance * from *sin(radians(-angle+90))/2,
                radius / maxDistance * from *cos(radians(-angle+90))/2,
                radius / maxDistance * to *sin(radians(-angle+90))/2,
                radius / maxDistance * to *cos(radians(-angle+90))/2);
    }

    private void drawTitle(){
        textAlign(LEFT);
        background(backgroundColor);
        textFont(fontTitle,titleTextSize);
        fill(titleColor);
        text("// Radar",5,titleTextSize);
        textFont(fontSmall,smallTextSize);
    }

    // Zeichnen der Texte für die Distanz-Skala
    private void drawText(String text, float distance){
        textFont(fontSmall,smallTextSize);
        text(text,-radius/maxDistance*distance/2, smallTextSize);
    }

    // Zeichnen der Winkel-Texte innerhalb des Radars
    private void drawAngleText(String text, float angle){
        pushMatrix();
        rotate(radians(117));           // 117?!
        noStroke();
        translate((radius-66-smallTextSize) *sin(radians(-angle+180))/2,(radius-66-smallTextSize) *cos(radians(-angle+180))/2);
        rotate(radians(angle));
        textAlign(CENTER);
        textFont(fontSmall,smallTextSize);
        text(text,0,0);

        popMatrix();
    }

    public void updateArray(int angle, float distance){
        if (angle < 270){
            if (distance > 50)
                distance = 50;
            if (distance < 0)
                distance = 5;
            distance = lowPass(distance,previousDistance);
            previousDistance = distance;
            array[angle] = distance;
            lineAngle = angle;

        }
    }

    // Low-Pass Filter zur Glättung der Distanzwerte; wird in der aktuellen Version nicht benutzt
    private float lowPass( float input, float output ) {
        output = output + smoothLevel * (input - output);
        return output;
    }

    public void pause(){
        noLoop();
    }

    public void resume(){
        loop();
    }
}

