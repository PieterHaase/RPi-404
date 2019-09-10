#===============================#
# Projekt RPi-404 ver 2.0       #
# Stefan Wyrembek, Pieter Haase #
# 11.09.2019                    #
#===============================#

from queue import Queue
from threading import Thread
import RPi.GPIO as GPIO
import paho.mqtt.client as mqtt
from gpiozero  import PWMOutputDevice
import time

from radar import radarStart
from radar import calibrate
from camera import cameraMove
from camera import kal
from drive import drive
import autodrive
import videostream

app_angle = 193
autoDrive = False

# Variablen für die Steuerung des RPi
forwardLeft = 0
forwardRight = 0
reverseLeft = 0
reverseRight = 0


videostream.start()   # startet den Videostream aus OpenCV


# MQTT
url = "broker.mqttdashboard.com"
topic = "haw/dmi/mt/its/ss18/rpi-404"                    # Kanal für Systembefehle, wie z.B. Autodrive aktivieren
topicCamera = "haw/dmi/mt/its/ss18/rpi-404/camera"       # Kanal für die Steuerung der Kamerabewegung
topicSteering = "haw/dmi/mt/its/ss18/rpi-404/steering"   # Kanal für den Empfang von Fahrbefehlen
topicRadar = "haw/dmi/mt/its/ss18/rpi-404/radar"         # Kanal zum Senden der Radar-Daten


# Funktion zum Verbinden mit MQTT und abonnieren der verwendeten Topics
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe(topic)
    client.subscribe(topicSteering)
    client.subscribe(topicCamera)
    client.subscribe(topicRadar)
    

# Funktion, die aufgerufen wird, wenn über MQTT eine Nachricht empfangen wird
def on_message(client, userdata, msg):
    payload = msg.payload.decode('utf-8')
    
    # wenn Nachricht über den Kanal "topic" empfangen wurde
    if msg.topic == topic:
        
        # Autodrive aktiviren
        if "carryMeHome" in payload:
            print("autoDrive")
            stri = 'C' + str(193)
            client.publish(topicCamera, stri)
            time.sleep(1)
            autodrive.setAutoDrive(True)
            global autoDrive
            autoDrive = True
            
        # Autodrive Stoppen
        if "stopDriving" in payload:
            global autoDrive
            autoDrive = False
            print("stopDriving")
            autodrive.stop()
            autodrive.reset()
        
    # wenn Nachricht über den Kanal "topicSteering" empfangen wurde
    elif msg.topic == topicSteering:
        global forwardLeft
        global forwardRight
        global reverseLeft
        global reverseRight
        global autoDrive
        if autoDrive == False:
            if "drive" in payload:
                payload = payload[5:len(payload)]
                
                # Aufteilen der Steuerbefehle für links und rechts
                if "L" in payload:
                    rightValue = float(payload[1:len(payload)])
                    if rightValue <= 0:
                        reverseLeft = -rightValue
                        forwardLeft = 0
                    else:
                        forwardLeft = rightValue
                        reverseLeft = 0
                if "R" in payload:
                    leftValue = float(payload[1:len(payload)])
                    if leftValue <= 0:
                        reverseRight = -leftValue
                        forwardRight = 0
                    else:
                        forwardRight = leftValue
                        reverseRight = 0
            
    # wenn nachricht über den Kanal "topicRadar" empfangen wurde        
    elif msg.topic == topicRadar:
        # Kalibrierungs-Befehl für das Radar
        if "calibrate" in payload:
            calibrate()
            client.publish(topicRadar, "calibration complete")
    
    # wenn nachricht über den Kanal "topicRadar" empfangen wurde
    elif msg.topic == topicCamera:
        # Kalibrierungs-Befehl für die Kamera
        if "calibrate" in payload:
            kal()
            client.publish(topicCamera, "calibration complete")
         
        # Verarbeitung der Befehle zum Drehen der Kamera 
        elif len(payload) <= 5:
            global app_angle
            global previous_angle
            app_angle = int(payload[1:len(payload)])
            

# Verbinden mit MQTT
print("connecting to mqtt")
client = mqtt.Client()
client.on_connect = on_connect # Callbacks registrieren
client.on_message = on_message	
client.connect(url, 1883, 60)
client.loop_start() # Abarbeiten


# starten des Threads für die Steuerung des Radars
print("starting radar thread")
radarQ = Queue(maxsize=0)
radarThread = Thread(target=radarStart, args=(radarQ,))
radarThread.start()
time.sleep(2)


# Kalibirierung der Kamera
kal()


# Hauptschleife
print("starting main loop")
while True:
    global forwardLeft
    global forwardRight
    global reverseLeft
    global reverseRight
    
    # Bewegen der Kamera entsprechend des über MQTT empfangenen Winkels
    cameraMove(app_angle)
    # Senden der Daten für Winkel und Distanz an die App mittels MQTT
    client.publish(topicRadar, radarQ.get())    
    
    # Falls das selbständige Fahren aktiviert ist, so werden die Steuerbefehle der Motoren aus dem Skript für das automatische Fahren geholt
    if autoDrive == True:
        forwardLeft = autodrive.getForwardLeft()
        forwardRight = autodrive.getForwardRight()
        reverseLeft = autodrive.getReverseLeft()
        reverseRight = autodrive.getReverseRight()
        objectReached = autodrive.getObjectReached()
        
        # Überprüfen, ob das Objekt mittels Autodrive-Funktion erreicht wurde
        if objectReached == True:
            client.publish(topic, "arrived at home")
            autodrive.setAutoDrive(False)
            autoDrive = False
            autodrive.reset()
    
    # Übermittlung der Steuerbefehle an die Motoren
    drive(forwardLeft, forwardRight, reverseLeft, reverseRight)

    

def getAutoDriveQ():
    global autoDriveQ
    return autoDriveQ
