package haw_hamburg.rpi_404;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import processing.core.*;

public class LeftControlPanel extends PApplet {

    private float displayCenterX;
    private float button1CenterY = 120;
    private float buttonDiameter;
    private float button2CenterY;
    private int backgroundColor = Color.parseColor(Settings.backgroundColor);
    private int buttonColor = Color.parseColor(Settings.accent1Color);
    private int pressedColor = Color.parseColor(Settings.accent3Color);
    private TextView calibrationOverlay;
    private MQTTService mqttService;
    private boolean autoDrive = false;
    private MainActivity actitiy;
    private PFont font;
    private int fontSize = 25;
    private int textColor = Color.parseColor(Settings.accent2Color);

    public LeftControlPanel(MainActivity activity, TextView calibrationOverlay, MQTTService mqttService){
        this.actitiy = activity;
        this.calibrationOverlay = calibrationOverlay;
        this.mqttService = mqttService;
        // Schriftarten generieren
        font = createFont("Arial", fontSize);

    }


    public void settings(){
        fullScreen();
    }

    public void setup() {

        displayCenterX = displayWidth / 2;
        button1CenterY = 120;
        buttonDiameter = displayWidth / (float) 1.3;
        button2CenterY = 120 + 20 + buttonDiameter;
        background(backgroundColor);
        drawGradientEllipse(displayCenterX, button1CenterY, buttonDiameter, buttonColor);
        drawGradientEllipse(displayCenterX, button2CenterY, buttonDiameter, buttonColor);

        fill(textColor);
        textFont(font,fontSize);
        textAlign(CENTER);
        text ("CAL", displayCenterX, button1CenterY + fontSize / 3);
        text ("AUTO", displayCenterX, button2CenterY + fontSize / 3);
    }

    public void draw()      // Wird kontinuierlich entsprechend der FrameRate aufgerufen
    {

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

    public void mousePressed(){
        float dx1 = mouseX - displayCenterX;
        float dy1 = mouseY - button1CenterY;
        if (abs(dx1) < buttonDiameter/2 && abs(dy1) < buttonDiameter/2){              // Prüfen, ob Touch-Event innerhalb des Buttons liegt
            if (actitiy.getAutoDrive() == false && actitiy.getCalibration() == false) {
                mqttService.sendCalibrate();
                actitiy.setCalibration(true);
                calibrationOverlay.setText("Calibrating...");
                //calibrationOverlay.setVisibility(View.VISIBLE);
                background(backgroundColor);
                drawGradientEllipse(displayCenterX, button1CenterY, buttonDiameter, pressedColor);
                drawGradientEllipse(displayCenterX, button2CenterY, buttonDiameter, buttonColor);
            }
        }

        float dx2 = mouseX - displayCenterX;
        float dy2 = mouseY - button2CenterY;
        if (abs(dx2) < buttonDiameter/2 && abs(dy2) < buttonDiameter/2){              // Prüfen, ob Touch-Event innerhalb des Buttons liegt
            if (actitiy.getAutoDrive() == false) {
                mqttService.sendHome();
                actitiy.setAutoDrive(true);
                calibrationOverlay.setText("Auto Drive");
                //calibrationOverlay.setVisibility(View.VISIBLE);
                background(backgroundColor);
                drawGradientEllipse(displayCenterX, button1CenterY, buttonDiameter, buttonColor);
                drawGradientEllipse(displayCenterX, button2CenterY, buttonDiameter, pressedColor);
            }
            else {
                actitiy.setAutoDrive(false);
                calibrationOverlay.setText("");
                mqttService.stopDriving();
                background(backgroundColor);
                drawGradientEllipse(displayCenterX, button1CenterY, buttonDiameter, buttonColor);
                drawGradientEllipse(displayCenterX, button2CenterY, buttonDiameter, buttonColor);
            }

        }
        fill(textColor);
        textFont(font,fontSize);
        text ("CAL", displayCenterX, button1CenterY + fontSize / 3);
        text ("AUTO", displayCenterX, button2CenterY + fontSize / 3);
    }

    public void mouseReleased(){                                                        // Bei Ende des Touch/Drag-Events
        background(backgroundColor);
        if (actitiy.getCalibration() == true){
            drawGradientEllipse(displayCenterX, button1CenterY, buttonDiameter, pressedColor);
        }
        else {
            drawGradientEllipse(displayCenterX, button1CenterY, buttonDiameter, buttonColor);
        }
        if (actitiy.getAutoDrive() == true) {
            drawGradientEllipse(displayCenterX, button2CenterY, buttonDiameter, pressedColor);
        }
        else {
            drawGradientEllipse(displayCenterX, button2CenterY, buttonDiameter, buttonColor);
        }
        fill(textColor);
        textFont(font,fontSize);
        text ("CAL", displayCenterX, button1CenterY + fontSize / 3);
        text ("AUTO", displayCenterX, button2CenterY + fontSize / 3);
    }

    public void calibrationFinished(){
        calibrationOverlay.setText("");
        background(backgroundColor);
        drawGradientEllipse(displayCenterX, button1CenterY, buttonDiameter, buttonColor);
        drawGradientEllipse(displayCenterX, button2CenterY, buttonDiameter, buttonColor);
        fill(textColor);
        textFont(font,fontSize);
        text ("CAL", displayCenterX, button1CenterY + fontSize / 3);
        text ("AUTO", displayCenterX, button2CenterY + fontSize / 3);
    }

    public void arrivedHome(){
        Log.i("ZZYX", "läuft! - 4");
        //actitiy.setAutoDrive(false);
        calibrationOverlay.setText("");
        background(backgroundColor);
        drawGradientEllipse(displayCenterX, button1CenterY, buttonDiameter, buttonColor);
        drawGradientEllipse(displayCenterX, button2CenterY, buttonDiameter, buttonColor);
        fill(textColor);
        textFont(font,fontSize);
        text ("CAL", displayCenterX, button1CenterY + fontSize / 3);
        text ("AUTO", displayCenterX, button2CenterY + fontSize / 3);
    }
}
