package haw_hamburg.rpi_404;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import processing.core.PApplet;
import processing.core.PFont;

import static android.content.Context.SENSOR_SERVICE;

//===========================================================================================================//
// Projekt: RPi-404         Author: Pieter Haase                                                             //
//-----------------------------------------------------------------------------------------------------------//
// Diese Klasse ist für die Einbindung des Motion Sensors zuständig                                          //
// Sie wertet die aktuelle Position des Smartphones aus und berechnet Werte für den Winkel von Drehungen     //
// Zu Testzwecken kann der Motion-Sensor als PApplet eingebunden werden um sich die Werte anzeigen zu lassen //
//===========================================================================================================//

public class MotionSensor extends PApplet implements SensorEventListener {

    private MainActivity activity;
    private CameraAngleBar cameraAngleBar;
    private SensorManager sensorManager;
    private Sensor rotationVector;
    private Sensor accelerometer;
    private Sensor magneticField;
    private static final int SENSOR_DELAY_MICROS = 50 * 1000; // 500ms

    // Variablen für Yaw/Pitch/Roll
    private float valueX;
    private float valueY;
    private float valueZ;
    private boolean initialRotationBool = true;

    // Startwerte für Yaw/Pitch/Roll bei starten der App
    private float initialRotationX = 0;
    private float initialRotationY = 0;
    private float initialRotationZ = 0;

    // Variablen für Low-Pass Filter
    private float smoothLevel = 0.1f;
    private float previousValueX = 0;
    private float smoothedX = 0;

    // Variablen für Kamerarotation
    private boolean rotating = false;
    private float cameraAngle = 0;
    private float previousCameraAngle = 0;

    private int fontSize = 38;

    private PFont font;
//    private float[] rotationMatrix = new float[16];
 //   private float[] remappedRotationMatrix = new float[16];

    // Matrizen für Rotation, Orientierung, usw.
    private float[] orientation = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] inclinationMatrix = new float[9];
    private float[] accelerometerMatrix = new float[3];
    private float[] magneticFieldMatrix = new float[3];

    public MotionSensor(MainActivity activity, CameraAngleBar cameraAngleBar){
        this.activity = activity;
        this.cameraAngleBar = cameraAngleBar;
        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
//        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_MICROS);
        sensorManager.registerListener(this, magneticField, SENSOR_DELAY_MICROS);
        sensorManager.registerListener(this, rotationVector, SENSOR_DELAY_MICROS);
    }

    // Für die Einbindung als PApplet -------------------------------------------------------------------
    public void settings(){
        fullScreen();
    }

    public void setup(){                      // Wird beim Initialisieren aufgerufen
        font = createFont("Arial", fontSize);
        textFont(font);
    }

    public void draw(){                       // Wird kontinuierlich entsprechend der FrameRate aufgerufen
        background(Color.parseColor(Settings.backgroundColor));
        fill(Color.parseColor(Settings.mainColor));
        pushMatrix();
        translate(width/6,height/3);
        text("Orientation: " + activity.getResources().getConfiguration().orientation,0,0);
        text("X: " + valueX + " Init: " + initialRotationX,0,fontSize+5);
        text("Y: " + valueY + " Init: " + initialRotationY,0,2*fontSize+5);
        text("Z: " + valueZ + " Init: " + initialRotationZ,0,3*fontSize+5);
        text("Angle: " + cameraAngle,0,4*fontSize+5);
        text("X1 : " + valueX,0,5*fontSize+5);
        text("X2: " + smoothedX,0,6*fontSize+5);
        popMatrix();
    }
    // --------------------------------------------------------------------------------------------------


    // Low-Pass Filter für gleichmäßigere Bewegung der Kamera
    private float lowPass( float input, float output ) {
            output = output + smoothLevel * (input - output);
        return output;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accelerometerMatrix, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magneticFieldMatrix, 0, 3);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
//                System.arraycopy(event.values, 0, rotationMatrix, 0, 3);                              // Deaktiviert, da in verwendetem Smartphone kein Gyroskop vorhanden
            default:
                return;
        }

//        SensorManager.getRotationMatrixFromVector(rotationMatrix,event.values);
        SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometerMatrix, magneticFieldMatrix);   // Aus den Sensorwerten eine Rotatonsmatrix berechnen
        SensorManager.getOrientation(rotationMatrix, orientation);                                                      // Rotationsmatrix in Yaw/Pitch/Roll umwandeln

        // Speicherung der Ausgangsposition des Smartphones bei Start der App
        if (initialRotationBool) {
            initialRotationX = orientation[0];
            initialRotationY = orientation[1];
            initialRotationZ = orientation[2];
            initialRotationBool = false;
        }

        // Runden der Werte auf zwei Nachkommastellen
        valueX = (float) Math.round(Math.toDegrees((orientation[0]-initialRotationX)*100))/100;
        valueY = (float) Math.round(Math.toDegrees((orientation[1]-initialRotationY)*100))/100;
        valueZ = (float) Math.round(Math.toDegrees((orientation[2]-initialRotationZ)*100))/100;

        // Glättung der Sensorwerte mittels LowPass-Filter
        smoothedX = lowPass(valueX,previousValueX);
        previousValueX = smoothedX;

        // Wird auf den entsprechenden Bereich innerhalb der App gedrückt, senden der Kamerasteuerungs-Befehle
        if(rotating){
            cameraAngle = previousCameraAngle + smoothedX;
            if(cameraAngle >= -135 && cameraAngle <= 135)
                cameraAngleBar.sendCameraAngle(Math.round(cameraAngle+135));
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {    }

    public void unregisterListeners() {
        sensorManager.unregisterListener(this);
    }

    // Aktivierung der Kameradrehung über Sensor, wenn in der App entsprechender Button gedrückt wird
    public void onButtonPressed(){
        cameraAngleBar.setTriangleColor(Color.parseColor(Settings.accent3Color));
        if (activity.getAutoDrive() == false && activity.getCalibration() == false) {
            previousValueX = 0;
            initialRotationBool = true;
            previousCameraAngle = cameraAngleBar.getCameraAngle();
            rotating = true;
        }
    }

    // Deaktivierung der Kameradrehung über Sensor, wenn Button losgelassen wird
    public void onButtonReleased(){
        previousCameraAngle = cameraAngle;
        cameraAngleBar.setTriangleColor(Color.parseColor(Settings.mainColor));
        rotating = false;
    }
}


