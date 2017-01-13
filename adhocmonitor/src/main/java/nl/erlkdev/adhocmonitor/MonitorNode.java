package nl.erlkdev.adhocmonitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created on 18-4-2016.
 */
public class MonitorNode {

    /* MonitorNode settings*/
    private long monitorTimeOffset = 0;

    /* node data */
    private String address;
    private NodeStatus nodeStatus = NodeStatus.IDLE;
    private ConcurrentHashMap<String, int[]> currentNeighbours = new ConcurrentHashMap<>();
    private JSONObject customValues = new JSONObject();


    /* IO stats */
    private int bytesSend = 0;
    private int bytesRecv = 0;
    private Map<Long, Double> ioTotalArray = new ConcurrentHashMap<>();
    private Map<Long, Double> ioSendArray = new ConcurrentHashMap<>();
    private Map<Long, Double> ioReceivedArray = new ConcurrentHashMap<>();

    /* Performance and cpu load */
    private int processTicks = 0;
    private Map<Long, Double> processArray = new ConcurrentHashMap<>();
    private Map<Long, Double> cpuUsageArray = new ConcurrentHashMap<>();
    private Map<Long, Double> cpuTotalUsageArray = new ConcurrentHashMap<>();


    /**
     * Constructor to create a MonitorNode
     *
     * @param address the address of the node
     */
    public MonitorNode(String address) {
        this.address = address;
        ioThread.start();
        processThread.start();
        cpuUsageThread.start();
    }


    /**
     * Function to set the monitor time offset
     * This offset is used to mach the timestamps of the packets with the monitor.
     *
     * @param monitorTimeOffset The time offset of the node compared to the monitor.
     */
    protected void setMonitorTimeOffset(long monitorTimeOffset){
        this.monitorTimeOffset = monitorTimeOffset;
    }


    /**
     * Function to convert the node information to a JSON object
     *
     * @return A JSON object of the node information
     */
    protected JSONObject getJson(){
        try {
            JSONObject parent = new JSONObject();

            /* Adds the metadata to the JSON object */
            parent.put("address", address);
            parent.put("status", nodeStatus.toString());
            parent.put("timeStamp", getMonitorTime());

            /* Adds the chart information to the JSON object */
            parent.put("ioTotalChart", mapToJSON(ioTotalArray));
            parent.put("ioRecvChart", mapToJSON(ioReceivedArray));
            parent.put("ioSendChart", mapToJSON(ioSendArray));
            parent.put("speedChart", mapToJSON(processArray));
            parent.put("cpuUsageChart", mapToJSON(cpuUsageArray));
            parent.put("cpuTotalUsageChart", mapToJSON(cpuTotalUsageArray));


            /* Converts the neighbour map to a JSON array. */
            JSONArray neighboursArray = new JSONArray();
            Enumeration<String> neighboursKeys = currentNeighbours.keys();
            while (neighboursKeys.hasMoreElements()){
                String key = neighboursKeys.nextElement();

                /* Creates a JSON object of the neighbour information*/
                JSONObject neighbourObject = new JSONObject();
                neighbourObject.put("address", key);
                neighbourObject.put("bytesSend", currentNeighbours.get(key)[0]);
                neighbourObject.put("bytesReceived", currentNeighbours.get(key)[1]);
                neighbourObject.put("dataSend", currentNeighbours.get(key)[2]);

                /* Set isDataSend to false. */
                currentNeighbours.get(key)[2] = 0;

                /* Adds the object to the array. */
                neighboursArray.put(neighbourObject);
            }

            /* Adds the neighbour array and the customValues to the JSON object. */
            parent.put("neighbours", neighboursArray);
            parent.put("customValues", customValues);

            return parent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Converts a Map to a JSON object
     *
     * @param map The map
     * @return A JSON object of the map
     */
    private JSONObject mapToJSON(Map<Long, Double> map){
        JSONObject jsonObject = new JSONObject();
        try {
            for (Map.Entry<Long, Double> entry : map.entrySet()){
                    jsonObject.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.clear();
        return jsonObject;
    }


    /**
     * Function to get the monitor time
     *
     * @return The monitor time
     */
    private long getMonitorTime(){
        return System.currentTimeMillis() - monitorTimeOffset;
    }


    /**
     * Private function to set the custom value
     *
     * @param key the key of the custom value
     * @param value the value of the custom value
     * @param <T> the type of the custom value
     */
    private <T> void setCustomValuePrivate(String key, T value){
        try {
            this.customValues.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Function to set a custom String value
     *
     * @param key the key of the custom value
     * @param value the String value of the custom value
     */
    public void setCustomValue(String key, String value){
        setCustomValuePrivate(key, value);
    }


    /**
     * Function to set a custom Integer value
     *
     * @param key the key of the custom value
     * @param value the Integer value of the custom value
     */
    public void setCustomValue(String key, Integer value){
        setCustomValuePrivate(key, value);
    }


    /**
     * Function to set a custom Double value
     *
     * @param key the key of the custom value
     * @param value the Double value of the custom value
     */
    public void setCustomValue(String key, Double value){
        setCustomValuePrivate(key, value);
    }


    /**
     * Function to set a custom Boolean value
     *
     * @param key the key of the custom value
     * @param value the Boolean value of the custom value
     */
    public void setCustomValue(String key, Boolean value){
        setCustomValuePrivate(key, value);
    }


    /**
     * Function to set a custom Long value
     *
     * @param key the key of the custom value
     * @param value the Long value of the custom value
     */
    public void setCustomValue(String key, Long value){
        setCustomValuePrivate(key, value);
    }


    /**
     * Function to set the node status
     *
     * @param nodeStatus
     */
    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }


    /**
     * Function to set the current neighbours of the node
     *
     * @param currentNeighbours
     */
    public void setCurrentNeighbours(String[] currentNeighbours) {
        ConcurrentHashMap<String, int[]> tmpNeighbours = new ConcurrentHashMap<>();
        for (String neighbour: currentNeighbours) {
            int[] io = new int[] {0, 0, 0};
            if(this.currentNeighbours.containsKey(neighbour)){
                io = this.currentNeighbours.get(neighbour);
            }
            tmpNeighbours.put(neighbour, io);
        }
        this.currentNeighbours = tmpNeighbours;
    }


    /**
     * Function to register data that is send to a neighbour
     *
     * @param address the address of the neighbour
     * @param byteAmount the byte amount of the data that is send
     */
    public void addSendIO(String address, int byteAmount){
        currentNeighbours.get(address)[0] += byteAmount;
        currentNeighbours.get(address)[2] = 1;
        bytesSend += byteAmount;
    }


    /**
     * Function to register data that is received from a neighbour
     *
     * @param address the address of the neighbour
     * @param byteAmount the byte amount of the data that is received
     */
    public void addRecieveIO(String address, int byteAmount){
        currentNeighbours.get(address)[1] += byteAmount;
        bytesRecv += byteAmount;
    }


    /**
     * Function to do a process tick
     * This is used to measure the relative performance
     */
    public void incrProcessTicks(){
        processTicks++;
    }


    /**
     * Thread to collect the IO information of the node.
     * This data is used to create an IO graph
     */
    private Thread ioThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                    /* Stores the start time and the start values */
                    long start = System.currentTimeMillis();
                    long startSend = bytesSend;
                    long startReceived = bytesRecv;
                    long startTotal = bytesSend + bytesRecv;

                    /* Waits for 200 milliseconds */
                    Thread.sleep(200);

                    /* Stores the stop time and the stop value */
                    long stop = System.currentTimeMillis();
                    long stopSend = bytesSend;
                    long stopReceived = bytesRecv;
                    long stopTotal = bytesSend + bytesRecv;

                    /* Calculate the difference between the start and stop time */
                    long totalTime = stop - start;

                    /* Calculates for each value the value per second */
                    double bytesIOSend = (double)(stopSend - startSend) / (double)totalTime;
                    double bytesIOReceived = (double)(stopReceived - startReceived) / (double)totalTime;
                    double bytesIOTotal = (double)(stopTotal - startTotal) / (double)totalTime;

                    /* Checks for each array if the array is bigger than 10,
                     * if that is the case it drops the oldest value */
                    if(ioTotalArray.size() > 10){
                        ioTotalArray.clear();
                    }
                    if(ioSendArray.size() > 10){
                        ioSendArray.clear();
                    }
                    if(ioReceivedArray.size() > 10){
                        ioReceivedArray.clear();
                    }

                    /* Stores the new time value pairs */
                    ioTotalArray.put(getMonitorTime(), bytesIOTotal);
                    ioSendArray.put(getMonitorTime(), bytesIOSend);
                    ioReceivedArray.put(getMonitorTime(), bytesIOReceived);

                }catch (Exception ignore){ }
            }
        }
    });


    /**
     * Thread to collect the performance information of the node.
     * This data is used to create an performance graph
     */
    private Thread processThread = new Thread(new Runnable() {

        @Override
        public void run() {
            while(true){
                try {
                    /* Stores the start time and the start value */
                    long start = System.currentTimeMillis();
                    long startTicks = processTicks;

                    /* Waits for 200 milliseconds */
                    Thread.sleep(200);

                    /* Stores the stop time and the stop value */
                    long stop = System.currentTimeMillis();
                    long stopTicks = processTicks;

                    /* Calculate the difference between the start and stop time and value */
                    long total = stop - start;
                    long totalTicks = stopTicks - startTicks;

                    /* Calculates the ticks per second */
                    double processSpeed = (float)totalTicks / (float) total;

                    /* If the array is bigger than 10, it drops the oldest value */
                    if(processArray.size() > 10){
                        processArray.clear();
                    }
                    /* Stores the new time value pair */
                    processArray.put(getMonitorTime(), processSpeed);
                }catch (Exception ignore){ }
            }
        }
    });


    /**
     * Thread to collect the CPU usage information of the node.
     * This data is used to create an CPU usage graph
     */
    private Thread cpuUsageThread = new Thread(new Runnable() {

        @Override
        public void run() {
            while(true){
                try {
                    int pid;
                    long idle1, idle2, cpu1, cpu2, up1, up2;
                    String[] toks, stat;

                    /* Gets the pid id */
                    pid = android.os.Process.myPid();

                    /* Reads the first line of /proc/stat and splits it into toks */
                    toks = (new RandomAccessFile("/proc/stat", "r")).readLine().split(" +");

                    /* Gets the idle and CPU time */
                    idle1 = Long.parseLong(toks[4]) + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
                    cpu1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3]);

                    /* Reads the first line of /proc/:pid/stat and calculates uptime */
                    stat = (new RandomAccessFile("/proc/" + pid + "/stat", "r")).readLine().split("\\s");
                    up1 = Long.parseLong(stat[13]) + Long.parseLong(stat[14]);

                    /* Sleeps for 200 milliseconds */
                    try {
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /* Reads the first line of /proc/:pid/stat and calculates uptime */
                    stat = (new RandomAccessFile("/proc/" + pid + "/stat", "r")).readLine().split("\\s");
                    up2 = Long.parseLong(stat[13]) + Long.parseLong(stat[14]);

                    /* Reads the first line of /proc/stat  and splits it into toks */
                    toks = (new RandomAccessFile("/proc/stat", "r")).readLine().split(" +");

                    /* Gets the idle and CPU time */
                    idle2 = Long.parseLong(toks[4]) + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
                    cpu2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3]);

                    /* Calculates the CPU usage */
                    double cpuUsage = 100.0 * (double)(up2 - up1) / (double)((cpu2 + idle2) - (cpu1 + idle1));
                    double cpuTotalUsage = 100.0 * (double) (cpu2 - cpu1) / (double)((cpu2 + idle2) - (cpu1 + idle1));


                    /* Checks for each array if the array is bigger than 10,
                     * if that is the case it drops the oldest value */
                    if(cpuUsageArray.size() > 10){
                        cpuUsageArray.clear();
                    }

                    if(cpuTotalUsageArray.size() > 10){
                        cpuTotalUsageArray.clear();
                    }

                    /* Stores the new time value pairs */
                    cpuUsageArray.put(getMonitorTime(), cpuUsage);
                    cpuTotalUsageArray.put(getMonitorTime(), cpuTotalUsage);
                }catch (Exception ignore){ }
            }
        }
    });


}
