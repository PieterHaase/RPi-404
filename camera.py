#===============================#
# Projekt RPi-404 ver 2.0       #
# Stefan Wyrembek, Pieter Haase #
# 11.09.2019                    #
#===============================#

import time
import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

# Festlegung der Pins für den Motor
A = 5
B = 12
C = 23
D = 20

# Festlegen der Pins als Ausgang
GPIO.setup(A, GPIO.OUT)
GPIO.setup(B, GPIO.OUT)
GPIO.setup(C, GPIO.OUT)
GPIO.setup(D, GPIO.OUT)

# Pin 16 für die Lichtschranke als Eingang festlegen
GPIO.setup(16, GPIO.IN, pull_up_down = GPIO.PUD_DOWN)

current_state = 0
calibrated = False
app_angle = 0


def GPIO_SETUP(a,b,c,d):
    GPIO.output(A, a)
    GPIO.output(B, b)
    GPIO.output(C, c)
    GPIO.output(D, d)
    time.sleep(0.001)


# Linksdrehung des Motors um eine Anzahl Schritte (steps)
def leftTurn(steps):
    stepCount = 0
    while stepCount <= steps:
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
        global current_state
        current_state -= 1

            
# Rechtsdrehung des Motors um eine Anzahl Schritte (steps) 
def rightTurn(steps):
    stepCount = 0
    while stepCount <= steps:
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
        global current_state
        current_state += 1


# Funktion zur Kalibrierung der Kameradrehung
def kal():
        print("calibrating camera")
        global calibrated
        calibrated = False
        while GPIO.input(16) == GPIO.HIGH:
                leftTurn(1)
        rightTurn(steps(155))
        global current_state
        current_state = 193
        time.sleep(0.2)
        calibrated = True
	

# Drehen der Kamera
def cameraMove(app_angle):
    global current_state
    
    if calibrated == True:
        
        if "app_angle" in str(app_angle):
            app_angle = int(str(app_angle)[9:len(app_angle)])
        motor_angle = app_angle - current_state
        
        if motor_angle > 1:
                rightTurn(1)        
        elif motor_angle < -1:
                leftTurn(1)
        else:
            time.sleep(0.001)


# Funktion zum Umrechnen von Motorschritten in Drehwinkel
def winkel(steps):
    winkel = 360 / 512 * steps
    return winkel


# Funktion zum Umrechnen von Drehwinkeln in Motorschritte
def steps(winkel):
    steps = 512 / 360 * winkel
    return steps
        