#===============================#
# Projekt RPi-404 ver 2.0       #
# Stefan Wyrembek, Pieter Haase #
# 11.09.2019                    #
#===============================#

import time
import RPi.GPIO as GPIO
from gpiozero import PWMOutputDevice
import paho.mqtt.client as mqtt
import multiprocessing
import cv2
import numpy as np

objectFound = False
objectInSight = False
timeoutCounter = 15
foundCounter = 0
autoDrive = False
objectReached = False
rec_mid_alt = 0
showWindows = False
showTrackbars = False
font = cv2.FONT_HERSHEY_COMPLEX

forwardLeftValue = 0
forwardRightValue = 0
reverseLeftValue = 0
reverseRightValue = 0

# VideoCamera-Klasse, welche für das Streaming des Bildes aus OpenCV benötigt wird
class VideoCamera(object):
    def __init__(self):
        # Verarbeitung von Bildern von "Device 0" (Webcam)
        self.video = cv2.VideoCapture(0)
        # Um Bilder von einem Video zu verarbeiten, z.B.:
        #self.video = cv2.VideoCapture('video.mp4')
    
    def __del__(self):
        self.video.release()
    
    def get_frame(self):
        # Die Verarbeitung des Bildes erfolgt innerhalb der Funktion "autodrive"
        image = autodrive(self.video)
        # Codierung des verarbeiteten Bildes in JPEG
        ret, jpeg = cv2.imencode('.jpg', image)
        return jpeg.tobytes()

    
def autodrive(cap):
    
    # Farbwerte, die bei der Maskierung die oberen und unteren grenzwerte dieser festlegen
    l_h = 138
    l_s = 250
    l_v = 140
    u_h = 180
    u_s = 255
    u_v = 255
    
    areaVar = 600        # Minimale Fläche einer erkannten Kontur
    approxVar = 4        # Grad der Konturen-Approximation
    saturation = 1000    # Grad der Sättigungserhöhung
    
    percentage = 100     # Distanz zwischen Objekt und Bildmitte
    speed = 0.8          # Variable zum anpassen der Geschwindigkeit sämtlicher Fahrbefehle der Autodrive Funktion
    
    global autoDrive
    global objectFound
    global objectInSight
    global objectReached
    
    global forwardLeftValue
    global forwardRightValue
    global reverseLeftValue
    global reverseRightValue
    
    global timeoutCounter
    global foundCounter
    
    # Anzeigen von Slidern zur manuellen Einstellung von Sättigung, Farbwerten für die Maskierung, usw.
    if showTrackbars == True:
        saturation = cv2.getTrackbarPos("Saturation", "Trackbars")
        l_h = cv2.getTrackbarPos("L-H", "Trackbars")
        l_s = cv2.getTrackbarPos("L-S", "Trackbars")
        l_v = cv2.getTrackbarPos("L-V", "Trackbars")
        u_h = cv2.getTrackbarPos("U-H", "Trackbars")
        u_s = cv2.getTrackbarPos("U-S", "Trackbars")
        u_v = cv2.getTrackbarPos("U-V", "Trackbars")
        areaVar = cv2.getTrackbarPos("Area", "Trackbars")
        approxVar = cv2.getTrackbarPos("Approx", "Trackbars")
        
    _, frame = cap.read()
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV).astype("float32")
    
    # Erhöhung der Sättigung
    (h, s, v) = cv2.split(hsv)
    s = s*saturation / 100
    s = np.clip(s,0,255)
    hsv = cv2.merge([h,s,v])
    satBoost = cv2.cvtColor(hsv.astype("uint8"), cv2.COLOR_HSV2BGR)

    # Erstellen eines Arrays mit Farbwerten, welche die obere und untere Grenze für die Maskierung bestimmen
    lower_red = np.array([l_h, l_s, l_v])
    upper_red = np.array([u_h, u_s, u_v])

    # Maskierung
    mask = cv2.inRange(hsv, lower_red, upper_red)
    res = cv2.bitwise_and(satBoost,satBoost, mask= mask)
    
    # Ermittlung der Höhe/Breite des Kamerabildes (in Pixeln) und Berechnung der horizontalen Bildmitte 
    frame_width = cap.get(cv2.CAP_PROP_FRAME_WIDTH)
    frame_height = cap.get(cv2.CAP_PROP_FRAME_HEIGHT)
    img_mid = frame_width/2

    # Erkennung von Konturen
    cnts, contours, _ = cv2.findContours(mask, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    cnt = cnts[0]
       
    # Sobald in der App Autodrive aktiviert wurde
    if autoDrive == True:
    
        # Für jede erkannte Kontur wird die Fläche bestimmt und der Umriss approximiert
        for cnt in contours:
            area = cv2.contourArea(cnt)
            approx = cv2.approxPolyDP(cnt, approxVar/100*cv2.arcLength(cnt, True), True)
            
            # Erstellen eines Rechtecks, welches den Umriss einschließt
            a, b, h, w = cv2.boundingRect(approx)
            x = approx.ravel()[0]
            y = approx.ravel()[1]
            
            # Nimmt das umschließende Rechteck mehr als 97% der Bildhöhe oder der Bildweite ein, hat der RPi das Objekt erreicht
            if w > frame_width * 0.97 or h > frame_height * 0.97:
                            forwardLeftValue = 0
                            reverseLeftValue = 0
                            forwardRightValue = 0
                            reverseRightValue = 0
                            #global objectReached
                            objectReached = True
                            global autoDrive
                            autoDrive = False
                            print("object reached")
                            #print(objectReached)


            # Besitzt die erkannte Kontur eine Fläche größer als die, weleche mit "areaVar" festgelegt, werden dessen Umrisse gezeichnet
            if area > areaVar:
                cv2.drawContours(frame, [approx], 0, (255, 0, 0), 5)

                # Besitzt die erkannte Kontur 3 Eckpunkte (ist somit ein Dreieck) wird dessen umschließendes Rechteck gezeichnet und dessen Mitte berechnet
                if len(approx) == 3:
                    
                    rec_mid_x = int((a+h/2))
                    rec_mid_y = int((b+w/2))
                    cv2.rectangle(frame,(a,b),(a+h,b+w),(255,255,0),5)
                    
                    # Berechnung der Distanz zwischen der Mitte des umschließenden Rechtecks und der horizontalen Bildmitte (in Pixeln)
                    distance_to_middle = img_mid - rec_mid_x
                    # Umrechnung der Distanz in eine Prozentzahl (0% = Rechteck ist in der Bildmitte, 100% = Rechteck befindet sich am äußersten rechten Bildrand, -100% = äußerster linker Bildrand)
                    percentage = round(distance_to_middle / img_mid * 100, 0)
                    
                    # Wurde das Objekt gefunden, fährt der RPi darauf zu
                    # Entsprechend der prozentualen Distanz des Objektes zur Bildmitte wird eine Lenkbewegung veranlasst
                    if objectFound == True:
                        print(percentage)
                        if objectReached == False:
                            if percentage < 0:
                                if objectInSight == True and objectReached == False:                       
                                   right = ((100-percentage) * speed) * 0.01
                                   forwardLeftValue = 1 * speed
                                   reverseLeftValue = 0
                                   forwardRightValue = 0.5 * right
                                   reverseRightValue = 0
                                   state = "right"
                                   print("right drive")
                                   #print(100 -percentage)
                     
                            if percentage > 0:
                                if objectInSight == True and objectReached == False:
                                   left = ((100+percentage) * speed) * 0.01
                                   forwardLeftValue = 0.5 * left
                                   reverseLeftValue = 0
                                   forwardRightValue = 1 * speed
                                   reverseRightValue = 0
                                   state = "left"
                                   print("left drive")
                                   #print(100 +percentage)
                                   
                    # Das Objekt gilt erst als "gefunden" nachdem es in mindestens 2 aufeinanderfolgenden Frames erkannt wurde
                    # Dies soll verhindern, dass der RPi bei einer Fehlerkennung in einem einzelnen Frame sofort losfährt               
                    else:
                        if foundCounter >= 2:
                            objectFound = True
                            print("found object")
                        else:
                            foundCounter = foundCounter + 1
                            print(foundCounter)
                        
                        
                           
                # Besitzt die erkannte Kontur mehr oder weniger als 3 Eckpunkte bedeutet das, das Objekt ist außerhalb des Sichtfelds
                else:
                    objectInSight == False
                    print("out of sight")
                    
                    # Wurde das Objekt noch nicht gefunden, so dreht sich der RPi um seine eigene Achse und "sucht" das Objekt
                    if objectFound == False:
                        forwardLeftValue = 0
                        reverseLeftValue = 0.57 * speed
                        forwardRightValue = 0.57 * speed
                        reverseRightValue = 0
                        print("searching")
                    
                    # Wurde das Objekt schon einmal gefunden, dreht sich der RPi ebenfalls um die eigene Achse
                    # die Richtung hängt dabei jedoch davon ab, ob sich das Objekt vorher in der linken oder der rechten Bildhälfte befand
                    else:
                        if percentage < -50:
                           forwardLeftValue = 0.57 * speed
                           reverseLeftValue = 0
                           forwardRightValue = 0
                           reverseRightValue = 0.57 * speed
                           print("right turn")
                           print(percentage)
                           timeoutCounter = timeoutCounter + 1
                           
                        elif percentage > 50:
                           forwardLeftValue = 0
                           reverseLeftValue = 0.57 * speed
                           forwardRightValue = 0.57 * speed
                           reverseRightValue = 0
                           print("left turn")
                           print(percentage)
                           timeoutCounter = timeoutCounter + 1
                           
                        # Muss der RPi länger als 60 frames nach dem Objekt suchen, nachdem es schon einmal gefunden wurde, so erfolgt ein Timeout und der RPi stoppt 
                        if timeoutCounter >= 60:
                            if objectFound == True:
                                forwardLeftValue = 0
                                reverseLeftValue = 0
                                forwardRightValue = 0
                                reverseRightValue = 0
                                print("TimeoutStop")
                    
    # Bei Bedarf anzeigen eines extra Fensters mit Kamerabild und den Slidern zu manuellen wahl der oben erwähnten Parameter    
    if showWindows == True:
        cv2.imshow("Frame", frame)
    if showTrackbars == True:
        #cv2.imshow("Mask", mask)
        cv2.imshow('res',res)
        #cv2.imshow('hsv',hsv)
    
    # Die Funktion gibt das verarbeitete Bild zurück, damit dieses von "Flask" gestreamt werden kann
    return frame

    
def getForwardLeft():
    global forwardLeftValue
    return forwardLeftValue

def getReverseLeft():
    global reverseLeftValue
    return reverseLeftValue

def getForwardRight():
    global forwardRightValue
    return forwardRightValue

def getReverseRight():
    global reverseRightValue
    return reverseRightValue

def getObjectReached():
    global objectReached
    return objectReached

def setObjectReached(b):
    global objectReached
    objectReached = b
    
def setAutoDrive(b):
    global autoDrive
    autoDrive = b
    
    
# Funktion um die Autodrive Funktion zu stoppen und die manuelle Steuerung wieder zu aktivieren
def stop():
    #ojectReached = True
    forwardLeftValue = 0
    reverseLeftValue = 0
    forwardRightValue = 0
    reverseRightValue = 0
    global autoDrive
    autoDrive = False
    reset()
    
# Methode zum Zurücksetzen der Autodrive Funktion; es werden alle zum erneuten Ausführen der der Autodrive Funktion notwendigen Variablen in deren Ausgangszustand versetzt
def reset():
    global objectReached
    objectReached = False
    global foundCounter
    foundCounter = 0
    global timeoutCounter
    timeoutCounter = 0
    global objectFound
    objectFound = False
    global objectInSight
    objectInSight = False
    

def getVideo():
    global cap
    return cap
#----------------------------------------------------------------------------------------------
#while True:
    #autodrive()
