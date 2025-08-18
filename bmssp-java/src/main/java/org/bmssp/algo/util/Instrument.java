package org.bmssp.algo.util;

public class Instrument {
    private long relaxations = 0;
    private long heapOps = 0;
    public void reset() { relaxations = 0; heapOps = 0; }
    public void incRelax() { relaxations++; }
    public void addRelax(long k) { relaxations += k; }
    public void incHeap() { heapOps++; }
    public long getRelaxations() { return relaxations; }
    public long getHeapOps() { return heapOps; }
}
