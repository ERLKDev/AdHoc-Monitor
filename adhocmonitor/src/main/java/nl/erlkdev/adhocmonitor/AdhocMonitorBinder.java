package nl.erlkdev.adhocmonitor;

import android.os.Binder;

/**
 * Created on 20-4-2016.
 */
public class AdhocMonitorBinder extends Binder{

    private AdhocMonitorService mAdhocMonitorService;

    /**
     * Constructor for the monitor binder
     *
     * @param adhocMonitorService The adhoc monitor service
     */
    public AdhocMonitorBinder(AdhocMonitorService adhocMonitorService) {
        this.mAdhocMonitorService = adhocMonitorService;
    }

    /**
     *
     * @return The adhoc monitor service
     */
    public AdhocMonitorService getService() {
        return this.mAdhocMonitorService;
    }
}
