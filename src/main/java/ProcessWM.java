import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class ProcessWM {
    public static void main(String[] args) throws IOException {
        final double beta = 1.;       //cost of walking per second/cost of driving per second (alpha=1)
        final int walk_max = 240;    //max distance of walking
        final int not_en = 0;        //the number of vertices without max_can candidates
        final int num_nei = 100;     //the max neighbor a vertex can have
        final int max_can = 2;       //the max candidates a vertex can have including itself
        final int max_loss = 100;    //threshold to abandon a candidate
        final double cover = 0.9;    //percent of servable vertices with core vertices
        final int skip = 10;         //value of k for k-skip cover
        //25h 33m for chengdu 4h 12m
        //16.062s for xian
        //2h 5m for NYC
        //66s for hn
        //2min17s for sf
        long starTime = System.currentTimeMillis();
        String file_parameter = "./SF/hs_";
        String data_source = "./SF/sf";
        int grid_num = 100;

        Gson gson = new Gson();

        NodeFilter NF = new NodeFilter();
        NF.beta = beta;
        NF.walk_max = walk_max;
        NF.not_en = not_en;
        NF.num_nei = num_nei;
        NF.max_can = max_can;
        NF.max_loss = max_loss;
        InputStreamReader in = new InputStreamReader(new FileInputStream(data_source+"_graph_h_j.json"));
        NF.graph = gson.fromJson(in, new TypeToken<CHSP.Vertex[]>(){ }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(data_source+"_graph_p_j.json"));
        NF.graph_p = gson.fromJson(in, new TypeToken<CHSP.Vertex[]>(){ }.getType());
        in.close();
        int i, n = NF.graph.length;
        System.out.println(n);
        ArrayList<HashSet<Integer>> NearIns = new ArrayList<>(n);
        ArrayList<HashSet<Integer>> NearOuts = new ArrayList<>(n);
        ArrayList<Integer> dis_ins = new ArrayList<>(n);
        ArrayList<Integer> dis_outs = new ArrayList<>(n);

        Pair<Integer, HashSet<Integer>> info;
        for (i = 0; i < n; i++) {
            info = NF.dijkstra_lengths_in(n, i);
            NearIns.add((HashSet<Integer>) info.getValue().clone());
            dis_ins.add(info.getKey());
            info = NF.dijkstra_lengths_out(n, i);
            NearOuts.add((HashSet<Integer>) info.getValue().clone());
            dis_outs.add(info.getKey());
        }
        int[] score = new int[n];
        for (i = 0; i < n; i++) {
            score[i] = (dis_outs.get(i) + dis_ins.get(i) + (2 * NF.num_nei - NearIns.get(i).size() - NearOuts.get(i).size()) * 1000000) / NF.num_nei;
        }

        ArrayList<HashMap<Integer, Integer>> alter_node = new ArrayList<>(n);
        for (int node = 0; node < n; node++) {
            alter_node.add(NF.dijkstra_can_set(n, node, score));
        }
        System.out.print("1.finish MC. Only " + NF.not_en + " vertices need all " + NF.max_can + " neighbors as candidates\n");
        String jsonObject = gson.toJson(alter_node);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file_parameter + "alter.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(score);
        out = new OutputStreamWriter(new FileOutputStream(file_parameter + "score.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        //we want a sort on index from high score to low (cost decreasing)

        Comparator<Pair<Integer, Integer>> comp = new ShortestPathLRU.pair_com();

        PriorityQueue<Pair<Integer, Integer>> maxheap = new PriorityQueue<>(score.length, comp);
        for (i = 0; i < score.length; i++) {
            maxheap.add(new Pair<>(score[i], i));
        }

        boolean[] mark = new boolean[score.length];
        Arrays.fill(mark, false);

        in = new InputStreamReader(new FileInputStream(data_source + "_graph"+grid_num+"_j.json"));
        ArrayList<ArrayList<Integer>> Edges = gson.fromJson(in,
                new TypeToken<ArrayList<ArrayList<Integer>>>() {
                }.getType());
        in.close();

        System.out.println(n);

        CHSP.Vertex[] graph = new CHSP.Vertex[n];

        //initialize the graph.
        for (i = 0; i < n; i++) {
            graph[i] = new CHSP.Vertex(i);
        }

        //get edges
        for (ArrayList<Integer> Edge : Edges) {
            int x, y, c;
            x = Edge.get(0);
            y = Edge.get(1);
            c = Edge.get(2);

            graph[x].outEdges.add(y);
            graph[x].outECost.add(c);
            graph[y].inEdges.add(x);
            graph[y].inECost.add(c);
        }
        int extractNum = 0;
        Defected Con = new Defected();


        while (!maxheap.isEmpty()) {
            Pair<Integer, Integer> c_pair = maxheap.poll();
            int c_idx = c_pair.getValue();
            CHSP.Vertex vertex = graph[c_idx];
            if (mark[c_idx]) {
                continue;
            }
            boolean flag = true;
            for (int mc : alter_node.get(c_idx).keySet()) {
                if (mc != c_idx & !graph[mc].contracted) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                continue;
            }
            vertex.orderPos = extractNum;
            Con.contractNode(graph, vertex, extractNum++);
            if (vertex.contracted) {
                for (int mc : alter_node.get(c_idx).keySet()) {
                    mark[mc] = true;
                }
            }
        }

        int pruned = 0;
        ArrayList<Boolean> defective = new ArrayList<>();
        for (CHSP.Vertex node : graph) {
            if (node.contracted) {
                pruned++;
                defective.add(true);
            } else {
                defective.add(false);
            }
        }
        for (i = 0; i < n; i++) {
            HashMap<Integer, Integer> alter_s = alter_node.get(i);
            HashSet<Integer> de = new HashSet<>();
            for (int ver : alter_s.keySet()) {
                if (defective.get(ver)) {
                    de.add(ver);
                }
            }
            for (int ver : de) {
                alter_node.get(i).remove(ver);
            }
        }
        System.out.println(pruned);
        jsonObject = gson.toJson(defective);
        out = new OutputStreamWriter(new FileOutputStream(file_parameter + "defective.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(alter_node);
        out = new OutputStreamWriter(new FileOutputStream(file_parameter + "alterf.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        System.out.print("2.finish constructing defective vertices. " + pruned + " pruned.\n");

        /*
        InputStreamReader in = new InputStreamReader(new FileInputStream(data_source + "_graph"+grid_num+"_j.json"));
        ArrayList<ArrayList<Integer>> Edges = gson.fromJson(in,
                new TypeToken<ArrayList<ArrayList<Integer>>>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file_parameter + "score.json"));
        int[] score = gson.fromJson(in,
                new TypeToken<int[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file_parameter + "defective.json"));
        ArrayList<Boolean> defective = gson.fromJson(in,
                new TypeToken<ArrayList<Boolean>>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file_parameter + "alter.json"));
        ArrayList<HashMap<Integer, Integer>> alter_node = gson.fromJson(in,
                new TypeToken<ArrayList<HashMap<Integer, Integer>>>() {
                }.getType());
        in.close();
        int i, n = alter_node.size();
        */

        ArrayList<HashSet<Integer>> MS = new ArrayList<>();
        for (i = 0; i < n; i++) MS.add(new HashSet<>());
        for (i = 0; i < n; i++) for (int ele : alter_node.get(i).keySet()) MS.get(ele).add(i);

        PartialSC PSC_solver = new PartialSC();
        PSC_solver.score = score;
        PSC_solver.defect = defective;
        PSC_solver.alter = alter_node;
        PSC_solver.MS = MS;
        boolean[] Vco_ = PSC_solver.PSC_Para(cover);

        System.out.print("3.finish constructing partial set cover.\n");

        Kskip Skipper = new Kskip();
        Skipper.file = file_parameter;
        Skipper.result = Vco_;
        Skipper.V_cost = PSC_solver.V_cost;
        Skipper.def = defective;
        Skipper.alter = new ArrayList<>();
        Skipper.Edges = Edges;
        for (HashMap<Integer, Integer> temp : alter_node) {
            Skipper.alter.add((HashMap<Integer, Integer>) temp.clone());
        }
        Skipper.n = n;
        Skipper.min_serve = PSC_solver.cover_k;
        Skipper.MS = MS;

        Skipper.process(skip);

        System.out.print("4.finish constructing core vertices.\n");

        BuildGraph Builder = new BuildGraph();
        Builder.file = file_parameter;
        Builder.core = Skipper.cores;
        Builder.sub = ((List<Boolean>) Skipper.sub).toArray(new Boolean[n]);
        Builder.max_skip = skip;
        Builder.n = n;
        Builder.process(Edges, defective);

        System.out.print("5.finish building HS graph.\n");

        Check_SMD SMD = new Check_SMD();
        SMD.core = Skipper.cores;
        SMD.sub = ((List<Boolean>) Skipper.sub).toArray(new Boolean[n]);
        SMD.alter = alter_node;
        SMD.sub_core = Builder.sub_core;
        in = new InputStreamReader(new FileInputStream(file_parameter + "raw_HSgraph.json"));
        SMD.HSgraph = gson.fromJson(in,
                new TypeToken<CHSP.Vertex[]>() {
                }.getType());
        in.close();
        SMD.n = n;
        SMD.file = file_parameter;
        SMD.SMD();

        System.out.print("6.finish generate SMD.\n");

        ShortestPathHSG LRU = new ShortestPathHSG();
        LRU.file = file_parameter;
        LRU.graph_file = data_source;
        LRU.initializeFrequent(data_source+"_output_req");

        System.out.print("7.finish HMPO LRU init.\n");

        long endTime = System.currentTimeMillis();
        long Time = endTime - starTime;
        System.out.println("time cost = " + Time);
        //System.exit(0);
    }
}
