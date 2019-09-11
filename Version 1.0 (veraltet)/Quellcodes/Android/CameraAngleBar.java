package haw_hamburg.rpi_404;

import android.graphics.Color;

import processing.core.PApplet;

//=========================================================================================//
// Projekt: RPi-404         Author: Pieter Haase                                           //
//-----------------------------------------------------------------------------------------//
// PApplet mit dem sich die Drehung der Kamera des RPi-404 über einen Slider steuern lässt //
//=========================================================================================//

public class CameraAngleBar extends PApplet {

    private MainActivity mainActivity;
    private MQTTService mqttService;

    // Laden der in der Klasse "Settings" definierten Farben
    private int backgroundColor = Color.parseColor(Settings.backgroundColor);
    private int lineColor = Color.parseColor(Settings.mainColor);
    private int triangleColor = Color.parseColor(Settings.mainColor);
    private int lineColor2 = Color.parseColor(Settings.accent1Color);

    // Position und Default-Größe des Slider-Dreiecks
    private int triangleXpos;
    private int triangleYpos;
    private int triangleWidth = 60;
    private int triangleHeight = 60;
    private int margin = 30;

    // Höhe und Strichstärke der Hintergrund-Skala
    private int lineHeight = 25;
    private int lineStrokeWeight = 2;

    private boolean touchDrag = false;
    private int previousCameraAngle = 0;
    private int cameraAngle = 135;


    public CameraAngleBar(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void settings(){
        fullScreen();
    }

    public void setup(){    // Wird beim Initialisieren aufgerufen
        frameRate(20);

        triangleXpos = width*cameraAngle/270;
        triangleYpos = height/2-lineHeight/4;

        triangleHeight = height-margin;
        triangleWidth = triangleHeight;

        // Zeichnen des Hintergrunds
        background(backgroundColor);
        strokeWeight(lineStrokeWeight);
        stroke(lineColor,100);
        drawLines(0,height/2,width,lineHeight/3,lineColor,width/24);
        stroke(lineColor2);
        drawLines(0,height/2-lineHeight/3*2,width,lineHeight,lineColor2,width/6);

        loadPixels();       // Laden des Hintergrunds in den Image-Buffer (Processing: pixels[])

    }

    public void draw(){     // Wird kontinuierlich entsprechend der FrameRate aufgerufen

        updatePixels();     // Laden und Zeichnen des Hintergrunds aus dem Image-Buffer

        // Slider-Dreieck zeichnen
        fill(lineColor,50);
        stroke(triangleColor);
        strokeWeight(2);
        triangleXpos = width*cameraAngle/270;
        triangle(triangleXpos-triangleWidth/2, triangleYpos+triangleHeight/2, triangleXpos+triangleWidth/2, triangleYpos+triangleHeight/2, triangleXpos, triangleYpos-triangleHeight/2);
    }

    // Methode zum Zeichnen der Hintergrund-Skala
    private void drawLines(int x, int y, float w, float h, int color, float interval) {
        for (int i = 0; i <= w; i++) {
            strokeWeight(2);
            line(x+i*interval+(width-interval*24)/2, y, x+i*interval+(width-interval*24)/2, y+h);
        }
    }

    // Kontollieren, ob ein Touch/Drag-Event auf dem Slider-Dreieck beginnt
    public void mousePressed(){
        if(mouseX > triangleXpos - triangleWidth && mouseX < triangleXpos + triangleWidth){
            triangleColor = Color.parseColor(Settings.accent3Color);
            touchDrag = true;
        }
    }

    // Verarbeiten von Drag-Events des Slider-Dreiecks
    public void mouseDragged(){
        if(touchDrag){
            int angle = mouseX * 270 / width;
            if(angle >= 0 && angle <= 270)
                sendCameraAngle(angle);             // Senden des Kamera-Steuerbefehls über MQTT
        }
    }

    public void mouseReleased(){
        triangleColor = Color.parseColor(Settings.mainColor);
        touchDrag = false;
    }

    public void setCameraAngle(float cameraAngle){ this.cameraAngle = Math.round(cameraAngle+135); }

    public float getCameraAngle() {
        return cameraAngle-135;
    }

    public void setMqttService(MQTTService mqttService) {
        this.mqttService = mqttService;
    }

    public void setTriangleColor(int triangleColor){
        this.triangleColor = triangleColor;
    }

    // Methode zum Senden des Kamera-Steuerbefehls über MQTT
    public void sendCameraAngle(int cameraAngle){
        if(cameraAngle != previousCameraAngle){
            mqttService.sendCameraAngle(cameraAngle);
        }
        previousCameraAngle = cameraAngle;
    }
}
