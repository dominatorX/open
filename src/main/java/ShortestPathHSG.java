import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.io.*;
import java.util.*;

import static java.lang.Math.max;

public class ShortestPathHSG {
    private BiSP.Vertex[] graph;
    private BiSP.Vertex[] reverseGraph;
    public String file;
    public String graph_file;

    private HashMap<Integer,HashMap<Integer, ArrayList<Integer>>> edges = new HashMap<>();
    private ArrayList<Boolean> frequentPickup;
    private ArrayList<Boolean> frequentDrop;
    private HashMap< Integer, HashMap<Integer, Integer> > distanceFrequentNodes;
    private final int SD_size = 64*1024*1024;
    private final int SP_size = 4*1024*1024;
    private LinkedHashMap<String, Integer> cacheSD = new LinkedHashMap<String, Integer>(SD_size, 0.75f){
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > SD_size;
        }
    };
    private LinkedHashMap<String, ArrayList<Integer>> cacheSP = new LinkedHashMap<String, ArrayList<Integer>>(SP_size, 0.75f){
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > SP_size;
        }
    };
    private BuildGraph Builder;

    public void init() throws IOException {
        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new FileInputStream(graph_file+"_graph_o_j.json"));
        this.graph = gson.fromJson(in,
                new TypeToken<BiSP.Vertex[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_graph_r_j.json"));
        this.reverseGraph = gson.fromJson(in,
                new TypeToken<BiSP.Vertex[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_graph_h_j.json"));
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_edges_j.json"));
        this.edges = gson.fromJson(in,
                new TypeToken<HashMap<Integer,HashMap<Integer, ArrayList<Integer>>>>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"fre_dis_h.json"));
        distanceFrequentNodes = gson.fromJson(in,
                new TypeToken<HashMap< Integer, HashMap<Integer, Integer> > >() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"fre_pick_h.json"));
        frequentPickup = gson.fromJson(in,
                new TypeToken<ArrayList<Boolean>>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"fre_drop_h.json"));
        frequentDrop = gson.fromJson(in,
                new TypeToken<ArrayList<Boolean>>() {
                }.getType());
        in.close();

        Builder = new BuildGraph();
        in = new InputStreamReader(new FileInputStream(file+"core.json"));
        Builder.core = gson.fromJson(in,
                new TypeToken<boolean[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"sub.json"));
        Builder.sub = gson.fromJson(in,
                new TypeToken<Boolean[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"sub2sub.json"));
        Builder.sub_nodes = gson.fromJson(in,
                new TypeToken<HashMap<Integer, CHSP.Vertex>>(){ }.getType());
        in.close();

        in = new InputStreamReader(new FileInputStream(file+"sub2core.json"));
        Builder.sub_core = gson.fromJson(in,
                new TypeToken<HashMap<Integer, CHSP.Vertex>>(){ }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"HSgraph.json"));
        Builder.HSgraph = gson.fromJson(in,
                new TypeToken<CHSP.Vertex[]>(){ }.getType());
        in.close();
    }

    void initializeFrequent(String raw_request) throws IOException {
        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new FileInputStream(graph_file+"_graph_o_j.json"));
        this.graph = gson.fromJson(in,
                new TypeToken<BiSP.Vertex[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_graph_r_j.json"));
        this.reverseGraph = gson.fromJson(in,
                new TypeToken<BiSP.Vertex[]>() {
                }.getType());
        in.close();

        in = new InputStreamReader(new FileInputStream(graph_file+"_edges_j.json"));
        this.edges = gson.fromJson(in,
                new TypeToken<HashMap<Integer,HashMap<Integer, ArrayList<Integer>>>>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"core.json"));
        Builder = new BuildGraph();
        Builder.core = gson.fromJson(in,
                new TypeToken<boolean[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"sub.json"));
        Builder.sub = gson.fromJson(in,
                new TypeToken<Boolean[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(file+"sub2sub.json"));
        Builder.sub_nodes = gson.fromJson(in,
                new TypeToken<HashMap<Integer, CHSP.Vertex>>(){ }.getType());
        in.close();

        in = new InputStreamReader(new FileInputStream(file+"HSgraph.json"));
        Builder.HSgraph = gson.fromJson(in,
                new TypeToken<CHSP.Vertex[]>(){ }.getType());
        in.close();

        this.distanceFrequentNodes = new HashMap<>();

        int N = this.graph.length;
        this.frequentPickup = new ArrayList<>(Collections.nCopies(N,false));
        this.frequentDrop = new ArrayList<>(Collections.nCopies(N,false));

        ArrayList<Integer> pick = new ArrayList<>(Collections.nCopies(N,0));
        ArrayList<Integer> drop = new ArrayList<>(Collections.nCopies(N,0));

        in = new InputStreamReader(new FileInputStream(raw_request));
        ArrayList<int[]> requests = gson.fromJson(in,
                new TypeToken<ArrayList<int[]>>() {
                }.getType());
        in.close();
        for (int[] req: requests) {
            pick.set(req[1], pick.get(req[1]) + 1);
            drop.set(req[3], drop.get(req[3]) + 1);
        }

        ArrayList<Integer> pick_array = (ArrayList<Integer>) pick.clone();
        ArrayList<Integer> drop_array = (ArrayList<Integer>) drop.clone();
        Collections.sort(pick_array);
        Collections.sort(drop_array);

        int num_saved = 6000;
        int threshold = max(pick_array.get( max(0, pick_array.size()-num_saved) ), 1);
        for( int i = 0; i< N; i++ ) {
            if(  pick.get(i) >= threshold  && (Builder.core[i] || Builder.sub[i])) {
                frequentPickup.set( i , true );
            }
            else
                frequentPickup.set( i , false );
        }

        threshold = max(drop_array.get( max(0,drop_array.size()-num_saved) ), 1);

        for( int i = 0; i< N; i++ ) {
            if(  drop.get(i) >= threshold  && (Builder.core[i] || Builder.sub[i])) {
                frequentDrop.set( i , true );
            }
            else
                frequentDrop.set( i , false );
        }

        for( int i = 0; i< N; i++ ) {
            if( !frequentPickup.get(i))
                continue;
            distanceFrequentNodes.put(i, new HashMap<>());

            ArrayList< Integer > distanceFromSourceL = new ArrayList<>( N );
            dijkstra_lengths(N, i, distanceFromSourceL);

            for( int j = 0;  j < N; j++ ) {
                if( frequentDrop.get( j ) ) {
                    if(distanceFromSourceL.get( j ) != 1000000){
                        distanceFrequentNodes.get( i ).put(j, distanceFromSourceL.get( j ));
                    }else {
                        distanceFrequentNodes.get( i ).put(j, -1);
                    }
                }
            }
        }

        String jsonObject = gson.toJson(frequentPickup);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file+"fre_pick_h.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(frequentDrop);
        out = new OutputStreamWriter(new FileOutputStream(file+"fre_drop_h.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(distanceFrequentNodes);
        out = new OutputStreamWriter(new FileOutputStream(file+"fre_dis_h.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
    }

    public static class pair_com implements Comparator<Pair<Integer,Integer>>{
        public int compare(Pair<Integer,Integer> node1, Pair<Integer,Integer> node2){
            return (Integer.compare(node2.getKey(), node1.getKey()));
        }
    }

    private void dijkstra_lengths(int N, int S, ArrayList< Integer > distanceFromSource) {
        Comparator<Pair<Integer,Integer>> comp = new pair_com();
        ArrayList<Integer> prevNode = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
        { 	distanceFromSource.add( i, 1000000);
            prevNode.add(i, -1);
        }

        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(graph.length, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        while( dj.size() != 0 )
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
                    prevNode.set(v, u);
                }
            }
        }
    }

    public int dis(int l1, int l2) {
            if (l1 == l2) return 0;

            if( frequentPickup.get(l1) && frequentDrop.get(l2) ) return distanceFrequentNodes.get(l1).get(l2);
            String key = l1+"-"+l2;
            Integer ans = cacheSD.get(key);
            if (ans == null) {
                ans = Builder.BD.computeDist(l1, l2);
                cacheSD.put(key, ans);
            }
            return ans;
    }

    int [] current_loc(int l1, int l2, int start_time, int current_time, ArrayList<Pair<Integer, Integer>> current_path) {
        int [] current = new int[2];

        //int[] key = {l1, l2};
        String key = l1+"-"+l2;
        ArrayList<Integer> path = cacheSP.get(key);
        if (path == null) {
            path = BiSP.computePath(this.graph, this.reverseGraph, l1, l2);
            cacheSP.put(key, path);
        }

        int time_all = start_time;
        int i;
        boolean flag = true;
        for (i=0;i<path.size()-1;i++) {
            int cost = this.edges.get(path.get(i)).get(path.get(i + 1)).get(0);
            if(flag) {
                if (time_all >= current_time) {
                    current[0] = path.get(i);
                    current[1] = time_all;
                    //return current;
                    flag = false;
                    current_path.add(new Pair<>(path.get(i), time_all));
                }
            }else{
                current_path.add(new Pair<>(path.get(i), time_all));
            }
            time_all += cost;
        }
        if(flag){
            return null;
        }else {
            return current;
        }
    }

    public static void main(String[] args) throws IOException {
        ShortestPathHSG LRU = new ShortestPathHSG();
        LRU.file = "./NYC/default/";
        LRU.graph_file = "./NYC/ny";
        LRU.initializeFrequent("./NYC/ny_output_req");
    }
}
