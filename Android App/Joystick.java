package haw_hamburg.rpi_404;

import android.graphics.Color;

//import org.eclipse.paho.android.service.MqttService;

import processing.core.*;

//==========================================================================================================//
// Projekt: RPi-404         Author: Pieter Haase                                                            //
//----------------------------------------------------------------------------------------------------------//
// PApplet für einen Virtuellen Joystick zur Steuerung des RPi-404                                          //
// Berechnet anhand der X- und Y- Werte eines Touch-Events Werte für Winkel und Stärke der Joystickbewegung //
// Zusätzlich rechnet sie die Werte für Winkel und Stärke in Steuerbefehle für die Motoren am RPi-404 um    //
//==========================================================================================================//

public class Joystick extends PApplet {

    // Laden der in der Klasse "Settings" definierten Farben
    private int joystickColor = Color.parseColor(Settings.mainColor);
    private int touchColor = Color.parseColor(Settings.accent1Color);
    private int backgroundColor = Color.parseColor(Settings.backgroundColor);

    private float joyDisplayCenterX;    // X-Wert Joystick Mitte
    private float joyDisplayCenterY;    // Y-Wert Joystick Mitte
    private float joystickDiameter;     // Äußerer Durchmesser des Joysticks
    private float touchDiameter;        // Durchmesser des Inneren "Sticks"

    private float maxJoyRange;     // Maximaler Joystick-Bereich
    private float curJoyAngle;     // Aktueller Winkel des Joysticks
    private float curJoyRange;     // Aktueller Abstand des Joysticks von der Mitte
    private float curJoyAngleDeg;  // Aktueller Winkel des Joysticks in Grad
    private float curJoyStrength;  // Aktuelle Stärke des Steuerbefehls in Prozent

    private boolean validClick = false;
    private int textSize = 20;

    private float previousValueForwardLeft = 0.0f;
    private float previousValueForwardRight = 0.0f;
    private float forwardLeft = 0.0f;
    private float forwardRight = 0.0f;
    private MQTTService mqttService;
    private MainActivity activity;

    public Joystick(MainActivity activity, MQTTService mqttService){
        this.activity = activity;
        this.mqttService = mqttService;
    }

    public void settings(){
        fullScreen();
    }

    public void setup() {       // Wird beim Initialisieren aufgerufen

        frameRate(24);

        // Berechnung der Größe des Joysticks
        joystickDiameter = min(displayWidth, displayHeight);
        touchDiameter = joystickDiameter / 3;
        joyDisplayCenterX = displayWidth / 2;
        joyDisplayCenterY = displayHeight / 2;
        maxJoyRange = joystickDiameter / 2 - touchDiameter / 2;

        // Hintergrund und äußeren Joystickbereich zeichnen und in den Image-Buffer laden
        background(backgroundColor);
        drawGradientEllipse(joyDisplayCenterX, joyDisplayCenterY, joystickDiameter, joystickColor);  //Joystick Background
        loadPixels();

    }

    public void draw()      // Wird kontinuierlich entsprechend der FrameRate aufgerufen
    {
        updatePixels();                                                     // Laden und Zeichnen des Hintergrunds aus dem Image-Buffer
        drawTouch(joyDisplayCenterX, joyDisplayCenterY, curJoyAngle);       // Inneren "Stick" zeichnen
    }

    public void mousePressed(){
        touchColor = Color.parseColor(Settings.accent3Color);
        if (activity.getAutoDrive() == false) {
            float dx = mouseX - joyDisplayCenterX;
            float dy = mouseY - joyDisplayCenterY;
            if (abs(dx) < joystickDiameter/2 && abs(dy) < joystickDiameter/2){              // Prüfen, ob Touch-Event innerhalb des Joystickbereichs liegt
                curJoyAngle = atan2(dy, dx);
                curJoyRange = dist(mouseX, mouseY, joyDisplayCenterX, joyDisplayCenterY);
                if (curJoyRange > maxJoyRange)                                              // Bewegung des inneren "Sticks" auf den Joystickdurchmesser begrenzen
                    curJoyRange = maxJoyRange;
                validClick = true;                                                          // Touch Event liegt innerhalb des Joystickbereichs
                sendMotorInput();
            }
        }
    }

    public void mouseDragged(){
        if (activity.getAutoDrive() == false) {
            float dx = mouseX - joyDisplayCenterX;
            float dy = mouseY - joyDisplayCenterY;
            if (validClick){                                                                // Setzen der aktuellen Position des inneren "Sticks"
                curJoyAngle = atan2(dy, dx);
                curJoyRange = dist(mouseX, mouseY, joyDisplayCenterX, joyDisplayCenterY);
                if (curJoyRange > maxJoyRange)
                    curJoyRange = maxJoyRange;
                sendMotorInput();                                                           // Motor-Steuerbefehle berechnen und senden
            }
        }
    }

    public void mouseReleased(){                                                        // Bei Ende des Touch/Drag-Events
        touchColor = Color.parseColor(Settings.accent1Color);
        curJoyAngle = 0;                                                                // Zurücksetzen der Stick-Position zur Mitte
        curJoyRange = 0;
        validClick = false;
        sendMotorInput();                                                               // Motoren stoppen
    }

    // Zeichnen des inneren "Sticks"
    void drawTouch(float x, float y, float a)
    {
        pushMatrix();
        translate(x, y);
        rotate(a);
        drawGradientEllipse((int)curJoyRange, 0, (int)touchDiameter, touchColor);
        fill(touchColor, 50);
        ellipse(curJoyRange, 0, touchDiameter, touchDiameter);
        popMatrix();
    }

    // Zeichnen einer Ellipse, die nach innen Transparenter wird
    void drawGradientEllipse(float xPos, float yPos, float diameter, int rgb){
        noStroke();
        noFill();
        strokeWeight(1);
        for (int i=(int)diameter; i>(int) diameter/1.3; i--){
            float iteration = diameter - i + 1;
            float alpha = 50 / (iteration / 10);
            stroke(rgb,(int) alpha);
            //stroke(0);
            ellipse(xPos, yPos, i, i);
        }
        fill(rgb, 30);
        noFill();
        noStroke();
    }
/*
    void drawJoystick(float joyDisplayCenterX, float joyDisplayCenterY){
        fill(joystickColor, 160);
        noStroke();
        ellipse(joyDisplayCenterX, joyDisplayCenterY, joystickDiameter, joystickDiameter);
    }

    void drawText(int textHorizPos, int textVertPos){
        //textHorizPos = 50;
        PFont font = createFont("Arial",textSize,true); // Arial, 16 point, anti-aliasing on
        textFont(font);

//        textVertPos = (int)(joyDisplayCenterY - 50);
        fill(255, 125);
        textVertPos += textSize;
        text("A ", textHorizPos, textVertPos);
        textVertPos += textSize;
        text("S :", textHorizPos, textVertPos);

        textHorizPos += 50;
        textVertPos -= textSize;
        text(curJoyAngleDeg, textHorizPos, textVertPos);
        textVertPos += textSize;
        text(curJoyStrength, textHorizPos, textVertPos);
    }

    void drawMotorInput(int textHorizPos, int textVertPos){
        //textHorizPos = 50;
        PFont font = createFont("Arial",textSize,true); // Arial, 16 point, anti-aliasing on
        textFont(font);

//        textVertPos = (int)(joyDisplayCenterY - 50);
        fill(255, 125);
        textVertPos += textSize;
        text("L ", textHorizPos, textVertPos);
        textVertPos += textSize;
        text("R ", textHorizPos, textVertPos);

        textHorizPos += 50;
        textVertPos -= textSize;
        text(forwardLeft, textHorizPos, textVertPos);
        textVertPos += textSize;
        text(forwardRight, textHorizPos, textVertPos);
    }
*/
    //public void setMQTTClient(MqttClient client){this.client = client;}

    void sendMotorInput(){
        curJoyAngleDeg = curJoyAngle * 180 / PI;
        curJoyStrength = curJoyRange * 100 / maxJoyRange;
        curJoyAngleDeg += 90;
        if(curJoyAngleDeg > 180){
            curJoyAngleDeg -=360;                           // damit Joystick bei 0° nach vorn zeigt
        }

        // Berechnung der Motorsteuerungs-Befehle ------------- ##

        // Berechnung der Steuerbefehle für rechte und linke Seite basierend auf Winkel des Sticks
        if(curJoyAngleDeg <= 0){
            forwardRight = 1.0f;
            forwardLeft = (curJoyAngleDeg + 45) / 45;
        }
        if(curJoyAngleDeg < -90){
            forwardRight = (curJoyAngleDeg + 90 + 45) / 45;
            forwardLeft = -1.0f;
        }
        if(curJoyAngleDeg > 0){
            forwardRight = -((curJoyAngleDeg - 45) / 45);
            forwardLeft = 1.0f;
        }
        if(curJoyAngleDeg > 90){
            forwardRight = -1.0f;
            forwardLeft = -((curJoyAngleDeg - 90 - 45) / 45);
        }

        // Einberechnung der Stärke des Steuerbefehls basierend auf Abstand des Sticks zur Joystickmitte
        forwardRight = forwardRight * curJoyStrength / 100;
        forwardLeft = forwardLeft * curJoyStrength / 100;

        forwardRight = Math.round(forwardRight*10)/10.0f;      // Runden auf 1 Nachkommastelle
        forwardLeft = Math.round(forwardLeft*10)/10.0f;

        // ---------------------------------------------------- ##

//        mTextViewForwardRight.setText(Float.toString(forwardRight));
//        mTextViewForwardLeft.setText(Float.toString(forwardLeft));

        // Senden der Steuerbefehle über MQTT ----------------- ##
        if (forwardLeft != previousValueForwardLeft){
            mqttService.sendSteeringLeft(forwardLeft);
            previousValueForwardLeft = forwardLeft;
        }
        if (forwardRight != previousValueForwardRight){
            mqttService.sendSteeringRight(forwardRight);
            previousValueForwardRight = forwardRight;
        }
        // ---------------------------------------------------- ##
    }
}
