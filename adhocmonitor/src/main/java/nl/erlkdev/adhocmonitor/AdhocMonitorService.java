package nl.erlkdev.adhocmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created on 18-4-2016.
 */
public class AdhocMonitorService extends Service{
    private final String TAG = this.getClass().getSimpleName();

    /* Connection information. */
    private String address = null;
    private String HOST = null;
    private Integer UDP_PORT = null;
    private Integer TCP_PORT = null;
    private MonitorErrorListener monitorErrorListener = null;

    /* Configuration. */
    private boolean syncTimeEnabled = true;
    private int monitorInterval = 500;
    private long monitorTimeOffset = 0;

    /* Monitor sender thread. */
    private Timer mMonitorSender;

    /* Monitor node. */
    private MonitorNode monitorNode;


    /**
     * Constructor for the monitor service
     * Displays debug message that the service has started
     */
    public AdhocMonitorService() {
        Log.d(TAG, "Monitor service started");
    }

    /**
     * Function to enable or disable the time synchronization
     *
     * @param syncTimeEnabled Default value is true
     */
    public void setSyncTimeEnabled(boolean syncTimeEnabled) {
        this.syncTimeEnabled = syncTimeEnabled;
    }

    /**
     * Function that returns if time synchronization is enabled
     *
     * @return true if time synchronization is enabled, else false
     */
    public boolean isSyncTimeEnabled(){
        return syncTimeEnabled;
    }

    /**
     * Function to set the monitor interval
     *
     * @param monitorInterval Default value is 500 milliseconds
     */
    public void setMonitorInterval(int monitorInterval){
        this.monitorInterval = monitorInterval;
    }

    /**
     * Function that returns the interval on which the monitor sends the data
     *
     * @return The interval on which the monitor sends the data
     */
    public int getMonitorInterval() {
        return monitorInterval;
    }

    /**
     * Gets the ip address of the monitor system
     *
     * @return the ip address of the monitor system
     */
    public String getHOST(){
        return HOST;
    }

    /**
     * Gets the udp port used by the monitor system for transferring data
     *
     * @return the udp port number used by the monitor system
     */
    public Integer getUdpPORT() {
        return UDP_PORT;
    }

    /**
     * Gets the tcp port used by the monitor system to synchronize the time
     *
     * @return the tcp port number used by the monitor system
     */
    public Integer getTcpPORT() {
        return TCP_PORT;
    }

    /**
     * Gets the address of the node
     *
     * @return the address of the node
     */
    public String getAddress(){
        return address;
    }

    /**
     * Gets the monitor node
     * @return MonitorNode
     */
    public MonitorNode getMonitorNode(){
        return monitorNode;
    }

    /**
     * Returns the time offset between the monitor and the node
     *
     * @return the time offset between the monitor and the node
     */
    public long getMonitorTimeOffset(){
        return monitorTimeOffset;
    }


    /**
     * Stops the monitor
     */
    public void stopMonitor(){
        mMonitorSender.cancel();
    }


    /**
     * Starts the monitor process using the default ports
     *
     * If time synchronization is enabled it first syncs the time between the monitor and the node
     * It then start the send thread to send data to the monitor system on an interval
     */
    public void startMonitor(String address, String HOST) {
        startMonitor(address, HOST, 7000, 7001);
    }

    /**
     * Starts the monitor process with the monitor error listener using the default ports
     *
     * If time synchronization is enabled it first syncs the time between the monitor and the node
     * It then start the send thread to send data to the monitor system on an interval
     */
    public void startMonitor(String address, String HOST, MonitorErrorListener monitorErrorListener) {
        this.monitorErrorListener = monitorErrorListener;
        startMonitor(address, HOST, 7000, 7001);
    }

    /**
     * Starts the monitor process with the monitor error listener
     *
     * If time synchronization is enabled it first syncs the time between the monitor and the node
     * It then start the send thread to send data to the monitor system on an interval
     */
    public void startMonitor(String address, String HOST, int UDP_PORT, int TCP_PORT, MonitorErrorListener monitorErrorListener) {
        this.monitorErrorListener = monitorErrorListener;
        startMonitor(address, HOST, UDP_PORT, TCP_PORT);
    }

    /**
     * Starts the monitor process
     *
     * If time synchronization is enabled it first syncs the time between the monitor and the node
     * It then start the send thread to send data to the monitor system on an interval
     */
    public void startMonitor(String address, String HOST, int UDP_PORT, int TCP_PORT) {
        this.address = address;
        this.HOST = HOST;
        this.UDP_PORT = UDP_PORT;
        this.TCP_PORT = TCP_PORT;
        this.monitorNode = new MonitorNode(address);

        Thread startMonitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                /* Syncs the time between the monitor and the node if enabled. */
                if (syncTimeEnabled){
                    syncTime();
                }
                monitorNode.setMonitorTimeOffset(monitorTimeOffset);

                /* Starts the monitor send thread. */
                try {
                    mMonitorSender = startSendThread();
                } catch (UnknownHostException e) {
                    if (monitorErrorListener != null){
                        monitorErrorListener.onError("Host not found");
                    }
                }
            }
        });
        startMonitorThread.start();
    }

    /**
     * Thread to send data after to the monitor system on a fixed interval.
     * If the host couldn't be find it throws a exception.
     *
     * @return the send threads
     * @throws UnknownHostException
     */
    private Timer startSendThread() throws UnknownHostException {
        Timer monitorSender = new Timer();
        monitorSender.scheduleAtFixedRate(new TimerTask() {
            private InetAddress address = InetAddress.getByName(HOST);

            @Override
            public void run() {
                try {
                    /* Creates a JSON object of the data. */
                    JSONObject parent = new JSONObject();
                    parent.put("type", "data");
                    parent.put("node", monitorNode.getJson());

                    /* Opens a datagram socket and sends the json object as datagram package. */
                    DatagramSocket s = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(parent.toString().getBytes(), parent.toString().getBytes().length, address, UDP_PORT);
                    s.send(packet);
                    s.close();

                } catch (Exception e) {
                    if (monitorErrorListener != null) {
                        monitorErrorListener.onError("Data couldn't be send");
                    }
                }
            }

        }, 0, monitorInterval);

        return monitorSender;
    }

    /**
     * Function to synchronize the time between the node and the monitor
     * It uses the method of the precision time protocol: https://en.wikipedia.org/wiki/Precision_Time_Protocol
     */
    private void syncTime(){
        try{
            byte[] bytes = new byte[4048];
            long time1, time1d, time2, time2d;
            JSONObject packageJson;

            /* Creates a socket. */
            Socket s = new Socket();
            s.connect(new InetSocketAddress(HOST, TCP_PORT));

            /* Creates an in- and output stream. */
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            DataInputStream dis = new DataInputStream(s.getInputStream());

            /* Creates a json object and sends it. */
            JSONObject parent = new JSONObject();
            parent.put("type", "sync");

            time1 = System.currentTimeMillis();
            dos.write(parent.toString().getBytes());
            dos.flush();

            /* Waits until the a package is received. */
            while (dis.read(bytes) == -1);
            time2d = System.currentTimeMillis();

            /* Gets t1' and t2 from the JSON packet. */
            packageJson = new JSONObject(new String(bytes));
            time1d = Long.parseLong(((String) packageJson.get("time1d")).replace("L", ""));
            time2 = Long.parseLong(((String) packageJson.get("time2")).replace("L", ""));

            /* Calculates the offset. */
            monitorTimeOffset = -((time1d - time1 - time2d + time2) / 2);

            dis.close();
            dos.close();
            s.close();
        } catch (Exception e) {
            if (monitorErrorListener != null){
                monitorErrorListener.onError("Time synchronization failed");
            }
            e.printStackTrace();
        }
    }

    /**
     * Bind method for the adhoc monitor binder
     *
     * @param intent The monitor intent
     * @return Adhoc monitor binder
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AdhocMonitorBinder(this);
    }

    /**
     * Interface for a monitor error listener
     *
     * All the monitor errors will be send to the monitor error listener
     */
    public interface MonitorErrorListener {
        void onError(String errorMsg);
    }
}
