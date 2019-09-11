#===============================#
# Projekt RPi-404 ver 2.0       #
# Stefan Wyrembek, Pieter Haase #
# 11.09.2019                    #
#===============================#

from gpiozero import PWMOutputDevice
import RPi.GPIO as GPIO
from time import sleep

GPIO.setmode(GPIO.BCM)

# Festlegung der Pins für die Ansteuerung der Motoren
PWM_FORWARD_LEFT_PIN = 6	# IN1 - Forward Drive
PWM_REVERSE_LEFT_PIN = 13	# IN2 - Reverse Drive
PWM_FORWARD_RIGHT_PIN = 19	# IN1 - Forward Drive
PWM_REVERSE_RIGHT_PIN = 26	# IN2 - Reverse Drive

# Setzen der Pins als PWMOutputDevice, welcher die Steuerung der Motoren mittels Pulsweiten-Modulation ermöglicht
forwardLeft = PWMOutputDevice(PWM_FORWARD_LEFT_PIN, True, 0, 1000)
forwardRight = PWMOutputDevice(PWM_FORWARD_RIGHT_PIN, True, 0, 1000)
reverseLeft = PWMOutputDevice(PWM_REVERSE_LEFT_PIN, True, 0, 1000)
reverseRight = PWMOutputDevice(PWM_REVERSE_RIGHT_PIN, True, 0, 1000)

# Funktion zur übergabe der Steuerwerte
def drive(forwardLeftM, forwardRightM, reverseLeftM, reverseRightM):
    
    forwardLeft.value = forwardLeftM
    forwardRight.value = forwardRightM
    reverseLeft.value = reverseLeftM
    reverseRight.value = reverseRightM