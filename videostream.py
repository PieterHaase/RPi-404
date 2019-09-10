#===============================#
# Projekt RPi-404 ver 2.0       #
# Stefan Wyrembek, Pieter Haase #
# 11.09.2019                    #
#===============================#

import subprocess
from threading import Thread
from flask import Flask, render_template, Response,session
from autodrive import VideoCamera

app = Flask(__name__)
@app.route('/')


def index():
    return render_template('index.html')


def gen(camera):
    while True:
        frame = camera.get_frame()       
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')
        

@app.route('/video_feed')


def video_feed():
    return Response(gen(VideoCamera()),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


def start():
    # Festlegen der eigenen IP des Pi als Host-URL
    url = subprocess.getoutput('hostname -I')
    print("stream started")
    print(url)
    # Starten eines eigenen Threads, der den Stream der Videobilder durchführt
    appThread = Thread(target=app.run, args=(url,))
    appThread.start()

