AdHoc-Monitor
-------------------------

The monitor system consist of two processes: the monitor node process and the monitor visualisation process. The monitor node process collects the information about the nodes and sends it to the monitor visualization process. Therefore, the monitor node process should run on each node. The monitor visualization process receives the data from the monitor node processes and combines them into one visualization. 

The monitor system uses the local wifi network to communicate between the monitor node processes and the monitor visualization process. Therefore, it's important that the nodes and the monitor visualization process are on the __same local wifi network__.

Because the time on the nodes can differ from that of the monitor visualization process, the [Precision Time Protocol](https://en.wikipedia.org/wiki/Precision_Time_Protocol) is used to synchronize the time between the nodes and the monitor visualization process. This is done using a TCP connection between the nodes and the monitor visualization process.

To send the data from the nodes to the monitor visualization process a UDP connection is used.

The monitor visualization process  
-------------------------------------
For the monitor visualization process a python program is available. The python program provides a web based user interface.

The python program runs on python 2.7 and uses [eventlet](https://github.com/eventlet/eventlet), [flask](http://flask.pocoo.org/) and [python-socketio](https://github.com/miguelgrinberg/python-socketio) to serve a web based UI.

To install the dependencies using pip:
```
pip install eventlet
pip install flask
pip install python-socketio
```
__Before running the program, it is important to change the ip in [config.ini](config.ini) into the local ip address of the device the monitor visualization process is running on!__

To run the monitor visualization program:
```
Python -u monitorService.py
```

The UI is on default served on port 3000: [http://localhost:3000/](http://localhost:3000/)

The settings of the monitor visualization program can be modified in [config.ini](config.ini).

```
[Connection]
ip = 192.168.1.4            # Local IP address of te monitor visualization program
udp_port = 7000             # UDP Port for the node data packets
tcp_port = 7001             # TCP Port for the time synchronization
udp_maxpacketsize = 8096    # The packet size of the node data packets
tcp_maxpacketsize = 8096    # The packet size of the time synchronization packets

[Settings]
packetlistlimit = 600       # The amounts of packets that are stored for each node
maxoffset = 1000            # The max time offset in milliseconds the program looks back to find a packet for a timestamp
arraylenght = 10000         # The length of the charts interval in milliseconds

[UI]
port = 3000                 # Port on which the UI is served
defaultdelay = 1000         # Default delay for the UI
statuscolors = {'idle': 'orange', 'processing': 'green', 'starting': 'lightgreen'} # Status and color pairs
statusunknown = gray        # Node color if the status is unknown (not in statuscolors)
```

The monitor node process
-----------------------------
For the monitor node process an android module is available, which can be used for android ad hoc networks. 

The android monitor service should be started and bind to the android application. After that, the monitor can be started. 
The following code can be used do this:
```java
 /* Start monitor service. */
Intent mAdhocMonitorIntent = new Intent(this, AdhocMonitorService.class);
startService(mAdhocMonitorIntent);

/* Bind monitor service. */
bindService(mAdhocMonitorIntent, new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        Log.d("MonitorService", "Adhoc Monitor service is connected");
        AdhocMonitorBinder adhocMonitorBinder = (AdhocMonitorBinder) service;
        AdhocMonitorService mMonitor = adhocMonitorBinder.getService();

        /* Starts the monitor. */
        mMonitor.startMonitor(mAddress, "192.168.1.4");
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        Log.d("MonitorService", "Adhoc Monitor service is disconnected");
    }
}, BIND_AUTO_CREATE);
```

In the code above the monitor is started on the default ports, to start the monitor with custom ports (in this example 7000 for UDP and 7001 for TCP) the following code can be used:
```java
mMonitor.startMonitor(mAddress, "192.168.1.4", 7000, 7001);
```

The montitor can also be started with an error listener, with both the default ports and the custom ports method:
```java
mMonitor.startMonitor(mAddress, "192.168.1.4", new AdhocMonitorService.MonitorErrorListener() {
    @Override
    public void onError(String errorMsg) {
        Log.d("MonitorService", errorMsg);
    }
});
```

