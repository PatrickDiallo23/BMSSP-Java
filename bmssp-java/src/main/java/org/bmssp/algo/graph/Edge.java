package org.bmssp.algo.graph;

/** Simple Edge class (u->v with weight w). Implemented as an immutable POJO for compatibility. */
public final class Edge {
    private final int u;
    private final int v;
    private final double w;

    public Edge(int u, int v, double w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public int u() { return u; }
    public int v() { return v; }
    public double w() { return w; }

    @Override
    public String toString() { return "Edge{" + u + "->" + v + ":" + w + '}'; }
}
