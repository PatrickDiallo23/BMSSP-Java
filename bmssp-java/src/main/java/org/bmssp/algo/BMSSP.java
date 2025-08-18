package org.bmssp.algo;

import org.bmssp.algo.graph.Edge;
import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;

public class BMSSP {
    public record Result(double Bprime, Set<Integer> Ufinal) {}

    public static Result run(Graph graph,
                             Map<Integer, Double> dist,
                             List<Edge> edges,
                             int l,
                             double B,
                             Set<Integer> S,
                             int n,
                             Instrument instr) {
        // parameter choices
        int tParam, kParam;
        if (n <= 2) { tParam = 1; kParam = 2; }
        else {
            tParam = Math.max(1, (int) Math.round(Math.pow(Math.log(Math.max(3, n)), 2.0 / 3.0)));
            kParam = Math.max(2, (int) Math.round(Math.pow(Math.log(Math.max(3, n)), 1.0 / 3.0)));
        }

        if (l <= 0) {
            if (S.isEmpty()) return new Result(B, new HashSet<>());
            BaseCase.Result br = BaseCase.run(graph, dist, B, S, kParam, instr);
            return new Result(br.Bprime(), br.Uo());
        }

        int pLimit = Math.max(1, 1 << Math.min(10, tParam));
        int kSteps = Math.max(1, kParam);
        FindPivots.Result piv = FindPivots.run(graph, dist, S, B, n, kSteps, pLimit, instr);
        Set<Integer> P = new HashSet<>(piv.P());
        Set<Integer> W = new HashSet<>(piv.W());

        int shift = Math.max(0, (l - 1) * tParam);
        int M = (shift >= 31) ? Integer.MAX_VALUE : (1 << shift);

        DataStructureD D = new DataStructureD(M, B, Math.max(1, Math.min(P.isEmpty() ? 1 : P.size(), 64)));
        for (int x : P) D.insert(x, dist.getOrDefault(x, Double.POSITIVE_INFINITY));

        double BprimeInitial = P.stream().mapToDouble(x -> dist.getOrDefault(x, Double.POSITIVE_INFINITY)).min().orElse(B);
        Set<Integer> U = new HashSet<>();
        List<Double> BprimeSubs = new ArrayList<>();

        int loopGuard = 0;
        int limit = (int) Math.min(Integer.MAX_VALUE, kParam * Math.pow(2, (double) l * Math.max(1, tParam)));
        while (U.size() < limit && !D.isEmpty()) {
            loopGuard++;
            if (loopGuard > 20000) break;
            DataStructureD.PullResult pr;
            try { pr = D.pull(); }
            catch (NoSuchElementException ex) { break; }
            double Bi = pr.Bi();
            Set<Integer> Si = pr.Si();

            Result sub = run(graph, dist, edges, l - 1, Bi, Si, n, instr);
            double BpSub = sub.Bprime();
            Set<Integer> Ui = sub.Ufinal();
            BprimeSubs.add(BpSub);
            U.addAll(Ui);

            // relax edges from Ui
            Map<Integer, Double> KforBatch = new HashMap<>();
            for (int u : Ui) {
                double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
                if (!Double.isFinite(du)) continue;
                for (Graph.Neighbor nb : graph.getAdj().get(u)) {
                    instr.incRelax();
                    double newd = du + nb.w();
                    if (newd <= dist.getOrDefault(nb.v(), Double.POSITIVE_INFINITY)) {
                        dist.put(nb.v(), newd);
                        if (Bi <= newd && newd < B) D.insert(nb.v(), newd);
                        else if (BpSub <= newd && newd < Bi) KforBatch.put(nb.v(), newd);
                    }
                }
            }
            for (int x : Si) {
                double dx = dist.getOrDefault(x, Double.POSITIVE_INFINITY);
                if (BpSub <= dx && dx < Bi) KforBatch.put(x, dx);
            }
            if (!KforBatch.isEmpty()) D.batchPrepend(KforBatch.entrySet());
        }

        double BprimeFinal = BprimeSubs.isEmpty() ? BprimeInitial : Math.min(BprimeInitial, BprimeSubs.stream().min(Double::compareTo).orElse(BprimeInitial));
        Set<Integer> Ufinal = new HashSet<>(U);
        for (int x : W) if (dist.getOrDefault(x, Double.POSITIVE_INFINITY) < BprimeFinal) Ufinal.add(x);
        return new Result(BprimeFinal, Ufinal);
    }
}

