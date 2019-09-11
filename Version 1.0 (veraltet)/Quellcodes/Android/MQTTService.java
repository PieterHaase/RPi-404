package haw_hamburg.rpi_404;

import android.os.Build;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static org.eclipse.paho.client.mqttv3.MqttClient.generateClientId;

//================================================================================//
// Projekt: RPi-404         Author: Pieter Haase                                  //
//--------------------------------------------------------------------------------//
// Diese Klasse ist für die Verbindung mit dem MQTT Broker zuständig              //
// Über sie findet sowohl das Senden als auch das Empfangen von Nachrichten statt //
//================================================================================//

public class MQTTService {

    private String url = "tcp://broker.mqttdashboard.com";
    private String topic = "haw/dmi/mt/its/ss18/rpi-404";
//    private String topicSensor = "haw/dmi/mt/its/ss18/rpi-404/sensor";
    private String topicCameraMovement = topic + "/cameraMovement";
    private String topicCamera = topic + "/camera";
    private String topicCameraAngle = topic + "/cameraAngle";
    private String topicSteering = topic + "/steering";
    private String topicRadar = topic + "radar";
    private MqttClient client;
    private MainActivity mainActivity;
//    private Radar radar;
    private CameraAngleBar cameraAngleBar;

    public MQTTService(MainActivity mainActivity){

        this.mainActivity = mainActivity;
//        this.radar = mainActivity.getRadar();
        this.cameraAngleBar = mainActivity.getCameraAngleBar();

        try {
            // Verbinden mit MQTT
            client = new MqttClient(url, generateClientId(), null);
            client.connect();

            // Herkunft der Verbindung in MQTT posten
            String str = "Connection received from " + android.os.Build.MANUFACTURER + " " + Build.DEVICE;
            client.publish(topic, str.getBytes(), 2, false);

            // Motoren bei Verbindung erst einmal stoppen; nur zur Sicherheit
            client.publish(topicSteering, "L0.0".getBytes(), 2, false);
            client.publish(topicSteering, "R0.0".getBytes(), 2, false);

            // Abonnieren der Kanäle für das Radar und die Kamera
            client.subscribe(topicRadar);
            client.subscribe(topicCamera);


            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) { Log.i("CAMERA", "CONNECTION LOST: " + throwable.toString());}

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());

                    // Message über den Radar-Kanal empfangen; Nachrichten werden im Format "Winkel,Distanz" empfangen
                    if (topic.equals(topicRadar)){
                        if (!payload.equals("calibrate")){
                            // Aufteilen des Strings und Zuweisung der Werte für Winkel und Distanz
                            int index1 = payload.indexOf(",");
                            String angle = payload.substring(0, index1);
                            String distance = payload.substring(index1+1, payload.length());
                            float fAngle = Float.parseFloat(angle);
                            float fDistance = Float.parseFloat(distance);
                            updateRadar(fAngle,fDistance);                  // Update des Radars mit den aktuellen Werten für Winkel und Distanz
                        }
                    }

                    // Message über den Kamera-Kanal empfangen
                    if (topic.equals(topicCamera)){{
                        if (!payload.equals("calibrate")){
                            String angle = payload.substring(1, payload.length());
                            cameraAngleBar.setCameraAngle(Float.parseFloat(angle)-135);     //Setzen der Position des Dreiecks-Sliders in der CameraAngleBar entsprechnde des aktuellen Kamerawinkels
                        }
                    }}
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken t) { }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Methode zum Senden von Steuerbefehlen für die Motoren auf der rechten Seite
    public void sendSteeringRight(float value){
        String str = "R" + value;
        try {
            client.publish(topicSteering, str.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Methode zum Senden von Steuerbefehlen für die Motoren auf der rechten Seite
    public void sendSteeringLeft(float value){
        String str = "L" + value;
        try {
            client.publish(topicSteering, str.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Methode zum Senden von Steuerbefehlen für die Kameradrehung
    public void sendCameraAngle(int angle){
        try {
            Log.i("CAMERA", "Sending angle");
            String str = "C" + angle;
            client.publish(topicCamera, str.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

 /*   public void setRadar(Radar radar) {
        this.radar = radar;
    }
*/

    // Methode zum Übergeben der aktuellen Werte für Winkel und Distanz in das Radar-Array in der MainActivity
    public void updateRadar(float fAngle, float fDistance){
        if(fDistance > 53)
            fDistance = 53;
//        radar.updateArray(Math.round(fAngle), Math.round(fDistance));
        mainActivity.updateArray(Math.round(fAngle), Math.round(fDistance));
    }

    // Methode zum Senden eines Kalibrierungs-Befehls um Kamera und Radar zu zentrieren
    public void sendCalibrate(){
        String str = "calibrate";
        try {
            client.publish(topicRadar, str.getBytes(), 0, false);
            client.publish(topicCameraMovement, str.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
