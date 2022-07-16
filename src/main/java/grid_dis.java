import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class grid_dis {
    HashSet<Integer> finished = new HashSet<>();
    HashMap<Integer, HashSet<Integer>> possible_in = new HashMap<>();
    HashMap<Integer, HashSet<Integer>> possible_out = new HashMap<>();
    String data_file = "NYC/ny";
    File file=new File(data_file+"_grid");

    public static void main(String[] args) throws IOException, InterruptedException {
        grid_dis gen = new grid_dis();
        gen.core();
    }
    public void core() throws IOException, InterruptedException {
        //6s to run for hn
        //9.349 to run for xian
        //50s to run for nyc
        //2min 18s to run for chengdu 3'16
        //17min 41s to run for chengdu large
        //String data_file = "./NYC/ny";

        long starTime = System.currentTimeMillis();
        int grid_num = 50;
        if(!file.exists()) {
            file.mkdir();
        }else {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (int i = 0; i < fileList.length; i++) {
                    this.finished.add(Integer.parseInt(fileList[i].getName().split("\\.")[0]));
                }
            }
        }

        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new FileInputStream(data_file+"_graph"+grid_num+"_j.json"));
        ArrayList<ArrayList<Integer>> Edges = gson.fromJson(in,
                new TypeToken<ArrayList<ArrayList<Integer>>>() {
                }.getType());
        in.close();


        for (ArrayList<Integer> Edge : Edges) {
            int x, y, c;
            x = Edge.get(0);
            y = Edge.get(1);
            c = Edge.get(2);
            if (c == 0) {
                System.out.println("zero cost");
                System.out.println(Edge);
            }
            if (!Edge.get(3).equals(Edge.get(4))) {
                int area1 = Edge.get(3);
                if (possible_in.get(area1) == null) {
                    HashSet<Integer> temp = new HashSet<>();
                    temp.add(x);
                    possible_in.put(area1, temp);
                } else {
                    possible_in.get(area1).add(x);
                }
                int area2 = Edge.get(4);
                if (possible_out.get(area2) == null) {
                    HashSet<Integer> temp = new HashSet<>();
                    temp.add(y);
                    possible_out.put(area2, temp);
                } else {
                    possible_out.get(area2).add(y);
                }
            }
        }

        /*
        //------for running serialized
        for (Integer origins : possible_in.keySet()) {
            if (!finished.contains(origins)) {
                new single(origins, possible_in.get(origins), possible_out, data_file);
                System.out.println("finish grid "+origins);
            }
        }
        */


        //-----for running parallel
        ForkJoinPool customThreadPool = new ForkJoinPool(10);
        customThreadPool.submit(()-> {
                    possible_in.keySet().parallelStream().forEach(origins ->
                    {
                        if (!finished.contains(origins)) {
                            try {
                                new single(origins, possible_in.get(origins), possible_out, data_file);//, graph, reverseGraph));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("finish grid " + origins);
                        }
                    });
                });
        customThreadPool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        HashMap<Integer, HashMap<Integer, Integer>> all = new HashMap<>(possible_in.keySet().size());
        for(Integer origins: possible_in.keySet()){
            try {
                in = new InputStreamReader(new FileInputStream(data_file+"_grid/"+origins+".json"));
                HashMap<Integer, Integer> sub = gson.fromJson(in,
                        new TypeToken<HashMap<Integer, Integer>>() {
                        }.getType());
                in.close();
                all.put(origins, sub);
            }catch (Exception e){
                System.out.println(e.toString());
            }
        }
        String jsonObject = gson.toJson(all);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(data_file+"_inter_region_cost"+grid_num+"_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        System.out.println("finished");

        long endTime = System.currentTimeMillis();
        long Time = endTime - starTime;
        System.out.println("time cost = " + Time);
        System.exit(0);
    }
}


class single  {
    /** Construct a task with a specified character and number of
     * times to print the character
     */
    CHSP.Vertex[] graph;
    public single(int id, final HashSet<Integer> origins, final HashMap<Integer,HashSet<Integer>> possible_out,
                  final String data_file) throws IOException {
        Gson gson = new Gson();
        HashMap<Integer,Integer> temp = new HashMap<>(); // The character to print

        InputStreamReader in = new InputStreamReader(new FileInputStream(data_file+"_graph_h_j.json"));
        graph = gson.fromJson(in, new TypeToken<CHSP.Vertex[]>(){ }.getType());
        in.close();


        for (Integer origin:origins){
            ArrayList< Integer > distanceFromSource = new ArrayList<>(graph.length);
            dijkstra_lengths(graph.length, origin, distanceFromSource);
            for (Integer aims:possible_out.keySet()){
                if (!aims.equals(id)){
                    for (Integer aim:possible_out.get(aims)){
                        int dis = distanceFromSource.get(aim);
                        if(temp.getOrDefault(aims, 999999)>dis){
                            temp.put(aims,dis);
                        }
                    }
                }
            }
        }
        System.out.println("finish "+id+" saving");
        String jsonObject = gson.toJson(temp);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(data_file+"_grid/"+id+".json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

    }

    private void dijkstra_lengths(int N, int S, ArrayList< Integer > distanceFromSource) {
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        for(int i = 0; i < N; i++) distanceFromSource.add( i, 1000000);

        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(this.graph.length, comp);
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

            for(int i = 0; i < graph[ u ].outEdges.size(); i++) {
                v = graph[ u ].outEdges.get( i );
                alt = distanceFromSource.get( u ) + graph[ u ].outECost.get( i );
                if( alt < distanceFromSource.get( v ) )
                { 	distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                }
            }
        }
    }
}
