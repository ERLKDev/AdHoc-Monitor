'''
Webservice for the UI.

The constructWebUI starts a flask Webservice.
The UI is a webpage and can be loaded in the browser.

The webservice creates a IO socket to communicate with the UI.
'''
import socketio
import eventlet
import thread
import time
from flask import Flask, render_template
import json
from networkGraph import NetworkGraph


sio = socketio.Server()
app = Flask(__name__)
networkGraph = None

# Constructs and start the UI web service
def constructWebUI(myNetworkGraph, PORT=3000):
    global app
    global sio
    global networkGraph

    # Places the networkGraph in a global variable
    networkGraph = myNetworkGraph

    # wrap Flask application with socketio's middleware
    app = socketio.Middleware(sio, app)

    # deploy as an eventlet WSGI server
    eventlet.wsgi.server(eventlet.listen(('', PORT)), app)

    print("serving at port", PORT)

# Recieves a request to render the UI.
@app.route('/')
def index():
    #Serve the client-side application
    return render_template('index.html')

# Recieves a request for the networkGraph data over the IO socket.
@sio.on('reqData')
def reqData(sid):
    # Sends the networkGraph data over the IO socket.
    sio.emit('nodeData', networkGraph.generateGraph())

# Recieves a request to modify the delay.
@sio.on('delay')
def message(sid, data):
    networkGraph.setDelay(data)

    