import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Kskip {
    boolean[] result;
    int[] V_cost;
    ArrayList<Boolean> def;
    ArrayList<HashMap<Integer, Integer>> alter;
    String file;
    int n;
    int min_serve;
    ArrayList<HashSet<Integer>> MS;
    ArrayList<Boolean> sub;
    boolean[] cores;
    ArrayList<ArrayList<Integer>> Edges;
    public static HashMap<Integer, HashSet<Integer>> check_ne(int N, int S, int max_skip, boolean[]core, BiSP.Vertex[]graph){
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        //ArrayList<Integer> prevNode = new ArrayList<>(N);
        ArrayList< Integer > distanceFromSource = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
        { 	distanceFromSource.add( i, 1000000);
            //    prevNode.add(i, -1);
        }

        HashMap<Integer, HashSet<Integer>> skip = new HashMap<>();
        for (int i=1;i<max_skip;i++){
            skip.put(i, new HashSet<>());
        }

        HashMap<Integer, Integer> skip_count = new HashMap<>();
        skip_count.put(S, 0);
        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(N, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        HashSet<Integer> unsettled = new HashSet<>();
        unsettled.add(S);

        while( !dj.isEmpty() && !unsettled.isEmpty())
        {
            x = dj.poll();
            u = x.getValue();

            if( distanceFromSource.get( u ) >= 1000000 )
                break;


            for(int i = 0; i < graph[ u ].adjList.size(); i++) {
                v = graph[ u ].adjList.get( i );
                alt = distanceFromSource.get( u ) + graph[ u ].costList.get( i );
                if( alt < distanceFromSource.get( v ) )
                { 	distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                    if(unsettled.contains(v)){
                        unsettled.remove(v);
                        skip.get(skip_count.get(v)).remove(v);
                        skip_count.put(v, max_skip+1);
                    }
                    if(unsettled.contains(u) && skip_count.get(u) < max_skip-1 && !core[v]){
                        unsettled.add(v);
                        skip_count.put(v, skip_count.get(u)+1);
                        skip.get(skip_count.get(u)+1).add(v);
                    }
                    //    prevNode.set(v, u);
                }
            }
            unsettled.remove(u);
        }
        return skip;
    }
    public boolean removable(int N, int S, int max_skip,
                                    BiSP.Vertex[]graph, BiSP.Vertex[] reverseGraph){
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        //ArrayList<Integer> prevNode = new ArrayList<>(N);
        ArrayList< Integer > distanceFromSource = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
        { 	distanceFromSource.add( i, 1000000);
            //    prevNode.add(i, -1);
        }

        HashMap<Integer, Integer> skip_count = new HashMap<>();
        skip_count.put(S, 1); // record the skip of each vertex, < than max_skip

        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(N, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        HashSet<Integer> unsettled = new HashSet<>();
        unsettled.add(S);

        while( !dj.isEmpty() && !unsettled.isEmpty())
        {
            x = dj.poll();
            u = x.getValue();

            if( distanceFromSource.get( u ) >= 1000000 )
                break;
            if(unsettled.contains(u) && skip_count.get(u)==max_skip) return false;

            for(int i = 0; i < reverseGraph[ u ].adjList.size(); i++) {
                v = reverseGraph[ u ].adjList.get( i );
                if(def.get(v))continue;
                alt = distanceFromSource.get( u ) + reverseGraph[ u ].costList.get( i );
                if( alt < distanceFromSource.get( v ) )
                { 	distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                    if(unsettled.contains(v)){
                        unsettled.remove(v);  // remove and add if it satisfies the next checking
                        skip_count.put(v, max_skip+1);
                    }
                    if(unsettled.contains(u) &&  !cores[v]){
                        //if(skip_count.get(u) < max_skip-1) {
                            unsettled.add(v);
                            skip_count.put(v, skip_count.get(u) + 1);

                    }
                    //    prevNode.set(v, u);
                }
            }
            if(unsettled.contains(u)){
                unsettled.remove(u);
                if(!safe(N, u, max_skip, graph)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean safe (int N, int S, int max_skip, BiSP.Vertex[]graph){
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        //ArrayList<Integer> prevNode = new ArrayList<>(N);
        ArrayList< Integer > distanceFromSource = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
        { 	distanceFromSource.add( i, 1000000);
            //    prevNode.add(i, -1);
        }

        HashMap<Integer, Integer> skip_count = new HashMap<>();
        skip_count.put(S, 1); // record the skip of each vertex, < than max_skip

        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(N, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        HashSet<Integer> unsettled = new HashSet<>();
        unsettled.add(S);

        while( !dj.isEmpty() && !unsettled.isEmpty())
        {
            x = dj.poll();
            u = x.getValue();

            if( distanceFromSource.get( u ) >= 1000000 )
                break;
            if(unsettled.contains(u) && skip_count.get(u) == max_skip){
                return false;
            }

            for(int i = 0; i < graph[ u ].adjList.size(); i++) {
                v = graph[ u ].adjList.get( i );
                if(def.get(v))continue;
                alt = distanceFromSource.get( u ) + graph[ u ].costList.get( i );
                if( alt < distanceFromSource.get( v ) )
                { 	distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                    if(unsettled.contains(v)){
                        unsettled.remove(v);  // remove and add if it satisfies the next checking
                        skip_count.put(v, max_skip+1);
                    }
                    if(unsettled.contains(u) &&  !cores[v]){//skip_count.get(u) <= max_skip-1 &&
                        unsettled.add(v);
                        skip_count.put(v, skip_count.get(u)+1);
                    }
                    //    prevNode.set(v, u);
                }
            }
            unsettled.remove(u);
        }
        return true;
    }

    void process (int skip) throws IOException {
        Gson gson = new Gson();

        int n = this.alter.size();

        BiSP.Vertex [] graph = new BiSP.Vertex[n];
        BiSP.Vertex [] reverseGraph = new BiSP.Vertex[n];

        //initialize the vertices without defective vertices.
        for(int i=0;i<n;i++){
            graph[i]=new BiSP.Vertex(i);
            reverseGraph[i]=new BiSP.Vertex(i);
        }

        //get the edges.
        for(ArrayList<Integer> Edge:Edges){
            int u, v;
            int w;
            u = Edge.get(0);  //start vertex
            v = Edge.get(1);   //end vertex
            w = Edge.get(2);   //weight of edge
           if (this.def.get(u) || this.def.get(v)){
                continue;
            }

            graph[u].adjList.add(v);
            graph[u].costList.add(w);

            reverseGraph[v].adjList.add(u);
            reverseGraph[v].costList.add(w);
        }

        int counter = 0;
        for (boolean flag: this.result) if(flag) counter ++;

        System.out.println("we use "+counter+ " vertices in PSC");

        // stage 1, V-Vco-Vde
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();

        PriorityQueue<Pair<Integer, Integer>> maxheap = new PriorityQueue<>(n-counter, comp);
        for(int i=0;i<n;i++){
            if (!this.result[i] && !this.def.get(i)) {
                maxheap.add(new Pair<>(this.V_cost[i], i));
            }
        }

        cores = new boolean[n];
        Arrays.fill(cores, true);
        for(int i=0;i<n;i++) if(this.def.get(i)) cores[i] = false;
        int process = 0;
        int serving = n;
        Pair<Integer, Integer> x;
        while (!maxheap.isEmpty()){
            x = maxheap.poll();
            int node = x.getValue();

            process++;
            if (process % 3000 == 0) {
                System.out.println(process);
            }

            cores[node] = false; // all passed, remove it
            cores[node] = !removable(n, node, skip, graph, reverseGraph);
            for (int served : MS.get(node)) {
                this.alter.get(served).remove(node); //check.get(served).remove(node);
                // it is assured that every vertex is served at the beginning even without def
                if (this.alter.get(served).isEmpty()) { //if (check.get(served).isEmpty()) {
                    serving--;
                }
            }
        }

        // stage 2, Vco
        maxheap = new PriorityQueue<>(counter, comp);
        for(int i=0;i<n;i++){
            if (this.result[i]) {
                maxheap.add(new Pair<>(this.V_cost[i], i));
            }
        }

        while (!maxheap.isEmpty()){
            x = maxheap.poll();
            int node = x.getValue();

            process++;
            if (process % 3000 == 0) {
                System.out.println(process);
            }

            int loss = 0;
            for (int served : MS.get(node)) {
                if (this.alter.get(served).size() == 1) {//if (check.get(served).size() == 1) {
                    loss++;
                }
            }
            if (serving - loss < min_serve) {
                continue;
            }


            cores[node] = false; // all passed, remove it
            cores[node] = !removable(n, node, skip, graph, reverseGraph);
            for (int served : MS.get(node)) {
                this.alter.get(served).remove(node); //check.get(served).remove(node);
                // it is assured that every vertex is served at the beginning even without def
                if (this.alter.get(served).isEmpty()) { //if (check.get(served).isEmpty()) {
                    serving--;
                }
            }
        }

        int sum = 0;
        for (boolean flag : cores) {
            if (flag) {
                sum++;
            }
        }
        sub = new ArrayList<>(n);
        for(int i=0;i<n;i++){
            if(cores[i] || this.def.get(i)){
                sub.add(false);
            }else {
                sub.add(true);
            }
        }
        System.out.println("We finally use "+sum+" core vertices");
        String jsonObject = gson.toJson(cores);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file+"core.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(sub);
        out = new OutputStreamWriter(new FileOutputStream(file+"sub.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
    }
}
