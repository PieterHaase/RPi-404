package haw_hamburg.rpi_404;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import processing.android.PFragment;

//================================================================================================//
// Projekt: RPi-404         Author: Pieter Haase                                                  //
//------------------------------------------------------------------------------------------------//
// Main-Funktion der App; sie initialisiert und verwaltet sämtliche PApletts und den MQTT Service //
//================================================================================================//

public class MainActivity extends AppCompatActivity {
    private LeftControlPanel leftControlPanel;
    private Radar radar;
    private Radar radarLarge;
    private Joystick joystick;
    private MQTTService mqttService;
    private CameraAngleBar cameraAngleBar;
    private MotionSensor sensorTest;
    private Separator separator;
    private Context context = MainActivity.this;


    private boolean autoDrive = false;
    private boolean calibration = false;
    private float[] radarArray = new float[270];        // Gemeinsames Array für die beiden Radars, in dem für sämtliche Winkel die gemessene Distanz gespeichert wird

    // vorherige X/Y Werte für Swipe-Geste
    private float previousTouchX1 = 0;
    private float previousTouchY1 = 0;
    private float previousTouchX2 = 0;
    private float previousTouchY2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   // Fullscreen ohne TitleBar
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_layout).setBackgroundColor(Color.parseColor(Settings.backgroundColor));

        // Separator initialisieren und einem View zuweisen
        separator = new Separator();
        View separatorView = findViewById(R.id.separator);
        PFragment separatorFragment = new PFragment(separator);
        separatorFragment.setView(separatorView,this);

        // Kleines Radar initialisieren und einem View zuweisen
        radar = new Radar(this, Radar.SMALL);
        View radarView = findViewById(R.id.radar);
        PFragment radarFragment = new PFragment(radar);
        radarFragment.setView(radarView, this);
        radar.resume();                                                 // Radar starten

        // Großes Radar initialisieren und einem View zuweisen
        radarLarge = new Radar(this, Radar.LARGE);
        View radarLargeView = findViewById(R.id.radarLarge);
        PFragment radarLargeFragment = new PFragment(radarLarge);
        radarLargeFragment.setView(radarLargeView, this);

        // MQTT initialisieren
        mqttService = new MQTTService(this);

        TextView calibrationOverlay = findViewById(R.id.calibrationOverlay);
        calibrationOverlay.setTextColor(Color.parseColor(Settings.accent3Color));
        calibrationOverlay.setShadowLayer(30,0,0,Color.parseColor(Settings.backgroundColor));

        // Linkes Control Panel initialisieren und einem View zuweisen
        leftControlPanel = new LeftControlPanel(this, calibrationOverlay, mqttService);
        View leftControlPanelView = findViewById(R.id.leftControlPanel);
        PFragment leftControlPanelFragment = new PFragment(leftControlPanel);
        leftControlPanelFragment.setView(leftControlPanelView, this);

        // Slider für die Kamerabewegung initialisieren und einem View zuweisen
        cameraAngleBar = new CameraAngleBar(this);
        View cameraAngleView = findViewById(R.id.cameraAngleBar);
        PFragment cameraAngleFragment = new PFragment(cameraAngleBar);
        cameraAngleFragment.setView(cameraAngleView,this);
        cameraAngleBar.setMqttService(mqttService);
        mqttService.setCameraAngleBar(cameraAngleBar);


        // Joystick initialisieren und einem View zuweisen
        joystick = new Joystick(this, mqttService);
        View joystickView = findViewById(R.id.joystick);
        PFragment joystickFragment = new PFragment(joystick);
        joystickFragment.setView(joystickView, this);

        // Hintergrundfarbe des Camera-Feeds setzen
        View videoFrame = findViewById(R.id.videostream);
        videoFrame.setBackgroundColor(Color.parseColor(Settings.backgroundColor));

        // Kleines Fenster für Camera-Feed initialisieren und einem View zuweisen
        WebView webView = findViewById(R.id.webViewLarge);
        VideoView videoView = new VideoView(this, webView);

        // Großes Fenster für Camera-Feed initialisieren und einem View zuweisen
        WebView webView2 = findViewById(R.id.webViewSmall);
        VideoView videoView2 = new VideoView(this, webView2);
        webView2.setVisibility(View.INVISIBLE);                                 // erst einmal unsichtbar

        // Motion Sensor Steuerung initialisieren
        sensorTest = new MotionSensor(this, cameraAngleBar);

        // Button Overlay des Camera-Feeds für Swipe-Geste konfigurieren
        Button button = findViewById(R.id.cameraButtonOverlay);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:                   // Bei Berührung X/Y-Werte merken
                        previousTouchX1 = event.getX();
                        previousTouchY1 = event.getY();
                        sensorTest.onButtonPressed();               // Aktivieren der Bewegungssensorsteuerung für die Kamera
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(event.getX() < previousTouchX1 && event.getY() < previousTouchY1){       // Wurde nach oben-links geswiped
                            if (webView.getVisibility() == View.VISIBLE){                           // Sichtbarkeit des Camera-Feeds togglen
                                webView2.setVisibility(View.VISIBLE);
                                webView.setVisibility(View.INVISIBLE);
                            } else {
                                webView2.setVisibility(View.INVISIBLE);
                                webView.setVisibility(View.VISIBLE);
                            }
                            if(!Settings.testMode)
                                radar.resume();
                            radarLarge.pause();

                        }
                        sensorTest.onButtonReleased();
                        return true;
                }
                return false;
            }
        });

        // Button Overlay des kleinen Radars für Swipe-Geste konfigurieren
        Button button2 = findViewById(R.id.radarButtonOverlay);
        button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:                 // Bei Berührung X/Y-Werte merken
                        previousTouchX2 = event.getX();
                        previousTouchY2 = event.getY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(event.getX() > previousTouchX2 && event.getY() > previousTouchY2){       // Wurde nach oben-links geswiped
                            if (webView.getVisibility() == View.VISIBLE){                           // Sichtbarkeit des Camera-Feeds togglen
                                webView2.setVisibility(View.VISIBLE);
                                webView.setVisibility(View.INVISIBLE);
                            } else {
                                webView2.setVisibility(View.INVISIBLE);
                                webView.setVisibility(View.VISIBLE);
                            }
                            if(!Settings.testMode)
                                radar.pause();
                            radarLarge.resume();
                        }
                        return true;
                }
                return false;
            }
        });



    }

    public Radar getRadar() {
        return radar;
    }

    public boolean getAutoDrive() { return autoDrive; }

    public void setAutoDrive(boolean autoDrive) {
        this.autoDrive = autoDrive;
        Log.i("ZZYX", "setAutoDrive");
        if (this.autoDrive == false){
            Log.i("ZZYX", "AutoDrive False");
            leftControlPanel.arrivedHome();
        }
    }

    public boolean getCalibration() { return calibration; }

    public void setCalibration(boolean calibration) {
        this.calibration = calibration;
        if (calibration == false){
            leftControlPanel.calibrationFinished();
        }
    }

    public void setRadarArray(float[] array){
        radarArray = array;
    }

    // Gemeinsames Winkel-Distanz Array für die beiden Radars Updaten
    public void updateArray(int angle, float distance){
        if (angle < 270){
            radar.updateArray(angle, distance);
            radarLarge.updateArray(angle, distance);
            radarArray[angle] = distance;
        }
    }

    public CameraAngleBar getCameraAngleBar() {
        return cameraAngleBar;
    }

    // Sensor-Listener bei Schließen der App stoppen
    public void onDestroy(){
        super.onDestroy();
        sensorTest.unregisterListeners();
    }

    //public void showToast(String message){
    //    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();  // CRASH hier - "Can't toast on a thread that has not called Looper.prepare()"
    //}
}
