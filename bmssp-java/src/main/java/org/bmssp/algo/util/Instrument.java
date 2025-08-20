package org.bmssp.algo.util;

/**
 * Instrumentation class for tracking algorithm performance metrics
 */
public class Instrument {
    private long relaxations = 0;
    private long heapOps = 0;

    public void incrementRelaxations() {
        relaxations++;
    }

    public void incrementHeapOps() {
        heapOps++;
    }

    public long getRelaxations() {
        return relaxations;
    }

    public long getHeapOps() {
        return heapOps;
    }

    public void reset() {
        relaxations = 0;
        heapOps = 0;
    }

    @Override
    public String toString() {
        return String.format("Instrumentation{relaxations=%d, heapOps=%d}", relaxations, heapOps);
    }
}
