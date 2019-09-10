#===============================#
# Projekt RPi-404 ver 2.0       #
# Stefan Wyrembek, Pieter Haase #
# 11.09.2019                    #
#===============================#

import time
import RPi.GPIO as GPIO
from queue import Queue

calibrated = False
currentPos = 0


GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)


# Pins für die Motorsteuerung festlegen
A= 4 
B= 17 
C= 27
D= 22


# GPIOs für die Motordrehung als Output festlegen
GPIO.setup(A, GPIO.OUT)
GPIO.setup(B, GPIO.OUT)
GPIO.setup(C, GPIO.OUT)
GPIO.setup(D, GPIO.OUT)

# GPIO Pins für den Echo-Sensor zuweisen (TRIGGER = Sender, ECHO = Empfänger)
GPIO_TRIGGER = 18
GPIO_ECHO = 24
 
# Richtung der GPIO-Pins festlegen (ECHO = in, Trigger = OUT)
GPIO.setup(GPIO_TRIGGER, GPIO.OUT)
GPIO.setup(GPIO_ECHO, GPIO.IN)
# Pin 21 für die Lichtschranke als Eingang festlegen
GPIO.setup(21, GPIO.IN, pull_up_down = GPIO.PUD_DOWN)
 

# Funktion zur Kalibrierung des Radars
def calibrate():
    global calibrated
    global currentPos
    print("calibrating radar...")
    time.sleep(0.1)    
    calibrated = False
    # Linksdrehung bis zum Erreichen der Lichtschranke
    while GPIO.input(21) == GPIO.HIGH:
        leftTurn(1)
    # Drehung in Startposition (10 Grad vor der Lichtschranke)
    rightTurn(steps(10))
    time.sleep(0.2)
    print("radar calibrated!")
    currentPos = 0
    calibrated = True
    
    
# Hauptfunktion
def radarStart(q):
    global queue
    queue = q
    calibrate()
    global currentPos
    global calibrated
    
    while True:
        
        if calibrated == True:
            while currentPos < steps(270):
                rightTurn(1)
                currentPos += 1
                # Messung der Distanz mittels des Echo-Sensors
                dist = distanz()
                # Anhängen des Messergebnisses in die Queue
                sendToQueue(q, currentPos, dist)
                
            while currentPos > 0:
                leftTurn(1)
                currentPos -= 1
                # Messung der Distanz mittels des Echo-Sensors
                dist = distanz()
                # Anhängen des Messergebnisses in die Queue
                sendToQueue(q, currentPos, dist)#print(currentPos)
               
        else:
            time.sleep(0.001)
            break
    

# Funktion zum ansprechen der GPIO-Pins für die Motordrehung
def GPIO_SETUP(a,b,c,d):
    GPIO.output(A, a)
    GPIO.output(B, b)
    GPIO.output(C, c)
    GPIO.output(D, d)
    time.sleep(0.002)


# Funktion zur Umrechnung von Motorschritten in Drehwinkel
def winkel(steps):
    winkel = 360 / 512 * steps
    return winkel

# Funktion zur Umrechnung von Drehwinkeln in  Motorschritte
def steps(winkel):
    steps = 512 / 360 * winkel
    return steps

# Rechtsdrehung des Radars
def rightTurn(steps):
    stepCount = 0
    while stepCount < steps:
        # Drehung des Motors
        GPIO_SETUP(0,0,0,0)  
        GPIO_SETUP(1,0,0,1)
        GPIO_SETUP(0,0,0,1)
        GPIO_SETUP(0,0,1,1)
        GPIO_SETUP(0,0,1,0)
        GPIO_SETUP(0,1,1,0)
        GPIO_SETUP(0,1,0,0)
        GPIO_SETUP(1,1,0,0)
        GPIO_SETUP(1,0,0,0)
        stepCount = stepCount + 1
            
            
# Linksdrehung des Radars    
def leftTurn(steps):
    stepCount = 0
    while stepCount < steps:
        # Drehung des Motors
        GPIO_SETUP(0,0,0,0)
        GPIO_SETUP(1,0,0,0)
        GPIO_SETUP(1,1,0,0)
        GPIO_SETUP(0,1,0,0)
        GPIO_SETUP(0,1,1,0)
        GPIO_SETUP(0,0,1,0)
        GPIO_SETUP(0,0,1,1)
        GPIO_SETUP(0,0,0,1)
        GPIO_SETUP(1,0,0,1)
        stepCount = stepCount + 1


# Anhängen des Messergebnisses in die Queue
def sendToQueue(q, currentPos, dist):
    dist = round(dist,2)
    stri = str(winkel(currentPos)) + ',' +str(dist)
    q.put(stri)
    

# Messung der Distanz mittels des Echo-Sensors
def distanz():
    # setze Trigger auf HIGH
    GPIO.output(GPIO_TRIGGER, True)
 
    # setze Trigger nach 0.01ms aus LOW
    time.sleep(0.00001)
    GPIO.output(GPIO_TRIGGER, False)
 
    SendeZeit = time.time()
    StartZeit = time.time()
    StopZeit = time.time()
 
    # speichere Startzeit
    timeout = 0
    Time = 0
    while GPIO.input(GPIO_ECHO) == 0 and Time <= 0.005:   
        StartZeit = time.time()
        Time = time.time()-SendeZeit

    # speichere Ankunftszeit
    SendeZeit = time.time()
    
    Time = 0
    if GPIO.input(GPIO_ECHO) != 0:
        while GPIO.input(GPIO_ECHO) == 1 and Time <= 0.005:
            StopZeit = time.time()
            Time = time.time()-SendeZeit       
    # Zeitdifferenz zwischen Start und Ankunft
    # mit der Schallgeschwindigkeit (34300 cm/s) multiplizieren
    # und durch 2 teilen, da hin und zurück
    TimeElapsed = StopZeit - StartZeit
    dist = (TimeElapsed * 34300) / 2
    return dist


def setCalibrated(b):
    global calibrated
    calibrated = b
