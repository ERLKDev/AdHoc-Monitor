package nl.erlkdev.adhocmonitor;

/**
 * Created on 3-1-2017.
 *
 * NodeStatus enum to store the statuses of the node
 */
public enum NodeStatus {
    /* The different statuses of a node. */
    IDLE("idle"), PROCESSING("processing"), WAITING("waiting"), ERROR("error"), STARTING("starting");

    private final String name;

    /**
     * Constructor for the status enum
     * @param s The string name of the status
     */
    NodeStatus(String s) {
        name = s;
    }

    /**
     * Converts the NodeStatus into a string.
     * @return returns the string value of the status
     */
    public String toString() {
        return this.name;
    }
}
