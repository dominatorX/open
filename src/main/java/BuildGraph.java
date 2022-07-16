import com.google.gson.Gson;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class BuildGraph {
    private long queryID = 1;
    boolean[] core;
    Boolean[] sub;
    String file;
    CHSP.Vertex[]HSgraph;
    HashMap<Integer, CHSP.Vertex> sub_nodes;
    HashMap<Integer, CHSP.Vertex>sub_core;
    int max_skip;
    int n;
    myBD BD = new myBD();

    private void super_edge_core(int S, CHSP.Vertex[]graph){
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        //ArrayList<Integer> prevNode = new ArrayList<>(n);
        ArrayList< Integer > distanceFromSource = new ArrayList<>(n);
        for(int i = 0; i < n; i++)
        { 	distanceFromSource.add( i, 1000000);
            //    prevNode.add(i, -1);
        }

        HashMap<Integer, HashSet<Integer>> skip = new HashMap<>();
        for (int i=1;i<=max_skip;i++){
            skip.put(i, new HashSet<>());
        }

        HashMap<Integer, Integer> skip_count = new HashMap<>();
        skip_count.put(S, 0);  // 1 more as we need to find length-k paths
        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(n, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        HashSet<Integer> unsettled = new HashSet<>();
        HashSet<Integer> unsettled_core = new HashSet<>();

        unsettled.add(S);
        HashSet<Integer> adj_core = new HashSet<>();

        while( !dj.isEmpty() && (!unsettled.isEmpty() || !unsettled_core.isEmpty()))
        {
            x = dj.poll();
            u = x.getValue();

            if( distanceFromSource.get( u ) >= 1000000 )
                break;

            if(u!=S) {
                if (unsettled.contains(u)) {
                    if (!sub[u]) {
                        System.out.println("fuck~");
                    }else {
                        sub_core.get(u).inEdges.add(S);
                        sub_core.get(u).inECost.add(distanceFromSource.get(u));
                    }
                }else if(adj_core.contains(u)){
                    HSgraph[S].outEdges.add(u);
                    HSgraph[S].outECost.add(distanceFromSource.get(u));
                    HSgraph[u].inEdges.add(S);
                    HSgraph[u].inECost.add(distanceFromSource.get(u));
                }
            }
            for(int i = 0; i < graph[ u ].outEdges.size(); i++) {
                v = graph[ u ].outEdges.get( i );
                alt = distanceFromSource.get( u ) + graph[ u ].outECost.get( i );
                if( alt < distanceFromSource.get( v ) ) {
                    distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                    if(unsettled.contains(v)) {
                        unsettled.remove(v);
                        skip.get(skip_count.get(v)).remove(v);
                        skip_count.put(v, max_skip + 1);
                    }
                    adj_core.remove(v);
                    unsettled_core.remove(v);
                    if(unsettled.contains(u) && skip_count.get(u) < max_skip){// && skip_count.get(u) < max_skip && !core.get(v)){
                        if(sub[v]) {
                            unsettled.add(v);
                            skip_count.put(v, skip_count.get(u) + 1);
                            skip.get(skip_count.get(u) + 1).add(v);
                        }else {
                            adj_core.add(v);
                            unsettled_core.add(v);
                        }
                    }
                    //    prevNode.set(v, u);
                }
            }
            unsettled.remove(u);
            unsettled_core.remove(u);
        }
    }

    private void super_edge_sub(int S, CHSP.Vertex[]graph){
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        //ArrayList<Integer> prevNode = new ArrayList<>(n);
        ArrayList< Integer > distanceFromSource = new ArrayList<>(n);
        for(int i = 0; i < n; i++)
        { 	distanceFromSource.add( i, 1000000);
            //    prevNode.add(i, -1);
        }

        HashMap<Integer, HashSet<Integer>> skip = new HashMap<>();
        for (int i=1;i<=max_skip;i++){
            skip.put(i, new HashSet<>());
        }

        HashMap<Integer, Integer> skip_count = new HashMap<>();
        skip_count.put(S, 1);  // path with length at most k-1
        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(n, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        HashSet<Integer> unsettled = new HashSet<>();
        HashSet<Integer> unsettled_core = new HashSet<>();
        unsettled.add(S);

        HashSet<Integer> adj_core = new HashSet<>();
        while( !dj.isEmpty() && (!unsettled.isEmpty() || !unsettled_core.isEmpty()))
        {
            x = dj.poll();
            u = x.getValue();
            //System.out.println(u+" cost "+distanceFromSource.get( u ));

            if( distanceFromSource.get( u ) >= 1000000 )
                break;


            if(u!=S){
                if(unsettled.contains(u)){
                    sub_nodes.get(S).outEdges.add(u);
                    sub_nodes.get(S).outECost.add(distanceFromSource.get( u ));
                    sub_nodes.get(u).inEdges.add(S);
                    sub_nodes.get(u).inECost.add(distanceFromSource.get( u ));
                }else if(adj_core.contains(u)) {
                    sub_core.get(S).outEdges.add(u);
                    sub_core.get(S).outECost.add(distanceFromSource.get(u));
                }
            }

            for(int i = 0; i < graph[ u ].outEdges.size(); i++) {
                v = graph[ u ].outEdges.get( i );
                alt = distanceFromSource.get( u ) + graph[ u ].outECost.get( i );
                if( alt < distanceFromSource.get( v ) )
                {
                    dj.remove(new Pair<>(-distanceFromSource.get(v), v));
                    distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                    if(unsettled.contains(v)){
                        unsettled.remove(v);
                        skip.get(skip_count.get(v)).remove(v);
                        skip_count.put(v, max_skip+1);
                    }
                    adj_core.remove(v);
                    unsettled_core.remove(v);
                    if(unsettled.contains(u) && skip_count.get(u) < max_skip) {// && skip_count.get(u) < max_skip && !core.get(v)){
                        if (sub[v]) {
                            unsettled.add(v);
                            skip_count.put(v, skip_count.get(u) + 1);
                            try{
                            skip.get(skip_count.get(u) + 1).add(v);}
                            catch (Exception e){
                                System.out.println(S);
                                System.out.println(v);
                                int a = 1/0;
                            }
                        } else {
                            adj_core.add(v);
                            unsettled_core.add(v);
                        }
                    }
                    //    prevNode.set(v, u);
                }
            }
            unsettled.remove(u);
            unsettled_core.remove(u);
        }
    }

    class myBD {
        Comparator<CHSP.Vertex> forwComp = new CHSP.forwComparator();
        Comparator<CHSP.Vertex> revComp = new CHSP.revComparator();
        PriorityQueue<CHSP.Vertex> forwQ;
        PriorityQueue<CHSP.Vertex> revQ;

        //main function that will compute distances.
        public int computeDist(int source, int target) {
            queryID++;
            if(source==target) return 0;
            HSgraph[source].distance.queryDist = 0;
            HSgraph[source].distance.forwqueryId = queryID;
            HSgraph[source].processed.forwqueryId = queryID;

            HSgraph[target].distance.revDistance = 0;
            HSgraph[target].distance.revqueryId = queryID;
            HSgraph[target].processed.revqueryId = queryID;

            forwQ = new PriorityQueue<>(HSgraph.length, forwComp);
            revQ = new PriorityQueue<>(HSgraph.length, revComp);

            if(sub[source] && sub[target])if (sub_nodes.get(source).outEdges.contains(target)) {
                return sub_nodes.get(source).outECost.get(sub_nodes.get(source).outEdges.indexOf(target));
            }

            int estimate = Integer.MAX_VALUE;
            CHSP.Vertex tar = HSgraph[target];
            //relaxEdges(source, "f", queryID);

            ArrayList<Integer> vertexList = HSgraph[source].outEdges;
            ArrayList<Integer> costList = HSgraph[source].outECost;
            HSgraph[source].processed.forwProcessed = true;
            HSgraph[source].processed.forwqueryId = queryID;
            for (int i = 0; i < vertexList.size(); i++) {
                int temp = vertexList.get(i);
                HSgraph[temp].distance.forwqueryId = HSgraph[source].distance.forwqueryId;
                HSgraph[temp].distance.queryDist = costList.get(i);
                forwQ.add(HSgraph[temp]);
            }
            vertexList = HSgraph[target].inEdges;
            costList = HSgraph[target].inECost;
            HSgraph[target].processed.revProcessed = true;
            HSgraph[target].processed.revqueryId = queryID;
            for (int i = 0; i < vertexList.size(); i++) {
                int temp = vertexList.get(i);
                HSgraph[temp].distance.revqueryId = HSgraph[target].distance.revqueryId;
                HSgraph[temp].distance.revDistance = costList.get(i);
                revQ.add(HSgraph[temp]);
            }


            //relaxEdges(target, "r", queryID);
            if (tar.processed.forwqueryId == queryID && tar.processed.forwProcessed) {
                if (tar.distance.revDistance + tar.distance.queryDist < estimate) {
                    estimate = tar.distance.queryDist + tar.distance.revDistance;
                }
            }

            while (forwQ.size() != 0 || revQ.size() != 0) {
                if (forwQ.size() != 0) {
                    CHSP.Vertex vertex1 = (CHSP.Vertex) forwQ.poll();
                    if (vertex1.distance.queryDist <= estimate) {
                        relaxEdges(vertex1.vertexNum, "f", queryID);
                    }
                    if (vertex1.processed.revqueryId == queryID && vertex1.processed.revProcessed) {
                        if (vertex1.distance.queryDist + vertex1.distance.revDistance < estimate) {
                            estimate = vertex1.distance.queryDist + vertex1.distance.revDistance;
                        }
                    }
                }

                if (revQ.size() != 0) {
                    CHSP.Vertex vertex2 = (CHSP.Vertex) revQ.poll();
                    if (vertex2.distance.revDistance <= estimate) {
                        relaxEdges(vertex2.vertexNum, "r", queryID);
                    }
                    if (vertex2.processed.forwqueryId == queryID && vertex2.processed.forwProcessed) {
                        if (vertex2.distance.revDistance + vertex2.distance.queryDist < estimate) {
                            estimate = vertex2.distance.queryDist + vertex2.distance.revDistance;
                        }
                    }
                }
            }

            if (estimate == Integer.MAX_VALUE) {
                return -1;
            }
            return estimate;
        }

        private void relaxEdges(int vertex, String str, long queryId) {
            if (str == "f") {
                ArrayList<Integer> vertexList = HSgraph[vertex].outEdges;
                ArrayList<Integer> costList = HSgraph[vertex].outECost;
                HSgraph[vertex].processed.forwProcessed = true;
                HSgraph[vertex].processed.forwqueryId = queryId;

                for (int i = 0; i < vertexList.size(); i++) {
                    int temp = vertexList.get(i);
                    int cost = costList.get(i);
                    if (HSgraph[vertex].orderPos < HSgraph[temp].orderPos) {
                        if (HSgraph[vertex].distance.forwqueryId != HSgraph[temp].distance.forwqueryId || HSgraph[temp].distance.queryDist > HSgraph[vertex].distance.queryDist + cost) {
                            HSgraph[temp].distance.forwqueryId = HSgraph[vertex].distance.forwqueryId;
                            HSgraph[temp].distance.queryDist = HSgraph[vertex].distance.queryDist + cost;

                            forwQ.remove(HSgraph[temp]);
                            forwQ.add(HSgraph[temp]);
                        }
                    }
                }
            } else {
                ArrayList<Integer> vertexList = HSgraph[vertex].inEdges;
                ArrayList<Integer> costList = HSgraph[vertex].inECost;
                HSgraph[vertex].processed.revProcessed = true;
                HSgraph[vertex].processed.revqueryId = queryId;

                for (int i = 0; i < vertexList.size(); i++) {
                    int temp = vertexList.get(i);
                    int cost = costList.get(i);

                    if (HSgraph[vertex].orderPos < HSgraph[temp].orderPos) {
                        if (HSgraph[vertex].distance.revqueryId != HSgraph[temp].distance.revqueryId || HSgraph[temp].distance.revDistance > HSgraph[vertex].distance.revDistance + cost) {
                            HSgraph[temp].distance.revqueryId = HSgraph[vertex].distance.revqueryId;
                            HSgraph[temp].distance.revDistance = HSgraph[vertex].distance.revDistance + cost;

                            revQ.remove(HSgraph[temp]);
                            revQ.add(HSgraph[temp]);
                        }
                    }
                }
            }
        }
    }

    public void process(ArrayList<ArrayList<Integer>> Edges, ArrayList<Boolean>defective) throws IOException {
        Gson gson = new Gson();
        CHSP.Vertex[] graph = new CHSP.Vertex[n];

        //initialize the graph.
        for(int i=0;i<n;i++){
            graph[i] = new CHSP.Vertex(i);
        }

        //get edges
        for (ArrayList<Integer> Edge:Edges) {
            int x, y, c;
            x = Edge.get(0);
            y = Edge.get(1);
            c = Edge.get(2);

            if(!defective.get(x) && !defective.get(y)) {
                graph[x].outEdges.add(y);
                graph[x].outECost.add(c);
                graph[y].inEdges.add(x);
                graph[y].inECost.add(c);
            }
        }

        sub_nodes = new HashMap<>(n);
        sub_core = new HashMap<>(n);
        HSgraph = new CHSP.Vertex[n];

        //initialize the graph.
        for(int i=0;i<n;i++){
            HSgraph[i] = new CHSP.Vertex(i);
            if(sub[i]){
                sub_core.put(i, new CHSP.Vertex(i));
                sub_core.get(i).orderPos = 0;
                sub_nodes.put(i, new CHSP.Vertex(i));
            }
        }

        for(int i=0;i<n;i++){
            if(core[i]){
                super_edge_core(i, graph);
            }else if(sub[i]){
                super_edge_sub(i, graph);
            }
        }
        System.out.println("generated");

        String jsonObject = gson.toJson(HSgraph);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file+"raw_HSgraph.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        CHSP.PreProcess process = new CHSP.PreProcess();
        process.processing(HSgraph);

        for(int i=0;i<n;i++){
            if(sub[i])HSgraph[i] = sub_core.get(i);
        }

        HashMap<Integer,HashSet<Integer>> core_sub = new HashMap<>();
        for(int i=0; i<sub_core.size();i++){
            if(sub_core.get(i)!=null) {
                for (int core_ : sub_core.get(i).outEdges) {
                    core_sub.putIfAbsent(core_, new HashSet<>());
                    core_sub.get(core_).add(i);
                }
            }
        }

        jsonObject = gson.toJson(HSgraph);
        out = new OutputStreamWriter(new FileOutputStream(file+"HSgraph.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(core_sub);
        out = new OutputStreamWriter(new FileOutputStream(file+"core2sub.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(sub_nodes);
        out = new OutputStreamWriter(new FileOutputStream(file+"sub2sub.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(sub_core);
        out = new OutputStreamWriter(new FileOutputStream(file+"sub2core.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
    }
}
