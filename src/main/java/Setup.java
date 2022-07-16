import com.csvreader.CsvReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static java.lang.Math.max;

public class Setup {
    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();

        //36.392 to run
        String data_type = "./NYC/ny";
        final double [] range_ = {40.51d, 40.91d, -74.26d, -73.7d};
        final double [] speed = {0d, 3.3528d, 4.4704, 5.588d, 6.7056d, 7.8232d, 8.9408d, 1d}; //1m/s for speed of human
        String filename = "./NYC/new-york-latest_edges.txt.csv";
        InputStreamReader in = new InputStreamReader(new FileInputStream(data_type+"_node2loc.json"));
        HashMap<String,ArrayList<Double>> nodes = gson.fromJson(in,
                new TypeToken<HashMap<String,ArrayList<Double>>>() {
                }.getType());
        in.close();

        ArrayList<ArrayList<Double>> lalo2id = new ArrayList<>();
        HashMap<String,ArrayList<Integer>> r_nodes_reverse = new HashMap<>();
        ArrayList<ArrayList<Integer>> Edges = new ArrayList<>();
        HashMap<Integer, HashMap<Integer, Integer>> Edges_p = new HashMap<>();

        int weight, weight_h;

        ArrayList<Double> loc;
        int total_nodes = 0;
        CsvReader f = new CsvReader(filename);
        f.readHeaders();
        int same = 0;
        int not_same = 0;
        double diff1;
        double diff2;
        int area_1;
        int area_2;
        int idx_1;
        int idx_2;
        int type1, type2, type3;
        double len;
        int grid_num = 50;

        ArrayList<Integer> temp;
        HashMap<Integer, HashMap<Integer,Integer>> found = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> found_p = new HashMap<>();
        while (f.readRecord()) {
            if (f.get(3).equals("nan")){
                continue;
            }
            len = Double.parseDouble(f.get(3));
            type1 = Integer.parseInt(f.get(4));
            type2 = Integer.parseInt(f.get(5));

            type3 = Integer.parseInt(f.get(8));
            if (nodes.get(f.get(1))!=null && nodes.get(f.get(2))!=null && len > 0) {
                if (type1 != 0 || type2 != 0 || type3 != 0) {
                    if (r_nodes_reverse.get(f.get(1))==null) {
                        loc = nodes.get(f.get(1));
                        diff1 = (loc.get(0) - range_[0]) * grid_num;
                        diff2 = (loc.get(1) - range_[2]) * grid_num;
                        area_1 = (int)(diff1*14*grid_num/25 + diff2);
                        idx_1 = total_nodes;

                        ArrayList<Integer> info = new ArrayList<Integer> ();
                        info.add(total_nodes);
                        info.add(area_1);
                        r_nodes_reverse.put(f.get(1), info);
                        lalo2id.add(loc);

                        total_nodes += 1;
                        if (total_nodes % 50000 == 0) {
                            System.out.printf("we have %d nodes\n", total_nodes);
                        }
                    } else {
                        temp = r_nodes_reverse.get(f.get(1));
                        idx_1 = temp.get(0);
                        area_1 = temp.get(1);
                    }
                    if (r_nodes_reverse.get(f.get(2))==null) {
                        loc = nodes.get(f.get(2));
                        diff1 = (loc.get(0) - range_[0]) * grid_num;
                        diff2 = (loc.get(1) - range_[2]) * grid_num;
                        area_2 = (int)(diff1*14*grid_num/25 + diff2);
                        idx_2 = total_nodes;

                        ArrayList<Integer> info = new ArrayList<Integer> ();
                        info.add(total_nodes);
                        info.add(area_2);

                        r_nodes_reverse.put(f.get(2), info);
                        lalo2id.add(loc);

                        //Rectangle pi = new Rectangle(loc.get(0), loc.get(1), loc.get(0), loc.get(1));
                        //id_nodes.add(pi, total_nodes);
                        total_nodes += 1;
                        if (total_nodes % 50000 == 0) {
                            System.out.printf("we have %d nodes\n", total_nodes);
                        }
                    } else {
                        temp = r_nodes_reverse.get(f.get(2));
                        idx_2 = temp.get(0);
                        area_2 = temp.get(1);
                    }

                    if(type3 != 0) {
                        weight_h = max((int) (len / speed[7] + 0.5), 1);
                        int i1, i2;
                        if (idx_1 < idx_2) {
                            i1 = idx_1;
                            i2 = idx_2;
                        } else {
                            i2 = idx_1;
                            i1 = idx_2;
                        }
                        HF:
                        if (found_p.get(i1) != null && found_p.get(i1).contains(i2)) {
                            if (weight_h < Edges_p.get(i1).get(i2)) {
                                Edges_p.get(i1).put(i2, weight_h);
                            }
                            break HF;
                        } else if (found_p.get(i1) != null) {
                            found_p.get(i1).add(i2);
                        } else {
                            HashSet<Integer> temp_ = new HashSet<>();
                            temp_.add(i2);
                            found_p.put(i1, temp_);
                        }
                        if (Edges_p.get(i1) != null) {
                            Edges_p.get(i1).put(i2, weight_h);
                        } else {
                            HashMap<Integer, Integer> temp_d = new HashMap<>();
                            temp_d.put(i2, weight_h);
                            Edges_p.put(i1, temp_d);
                        }
                    }

                    if (area_1 == area_2) {
                        IF1: if (type1 != 0) {
                            weight = max(((int) (len / speed[type1] + 0.5)), 1);
                            if(found.get(idx_1)!=null && found.get(idx_1).get(idx_2)!=null){
                                if(weight < Edges.get(found.get(idx_1).get(idx_2)).get(2)){
                                    Edges.get(found.get(idx_1).get(idx_2)).set(2, weight);
                                }
                                break IF1;
                            }else if(found.get(idx_1)!=null){
                                found.get(idx_1).put(idx_2, Edges.size());
                            }else {
                                HashMap<Integer,Integer>temp_ = new HashMap<>();
                                temp_.put(idx_2,Edges.size());
                                found.put(idx_1,temp_);
                            }
                            ArrayList<Integer> Edge = new ArrayList<Integer>();
                            Edge.add(idx_1);
                            Edge.add(idx_2);
                            Edge.add(weight);
                            Edge.add(0);
                            Edge.add(0);
                            Edges.add(Edge);
                        }
                        IF2: if (type2 != 0) {
                            weight = max(((int) (len / speed[type2] + 0.5)), 1);
                            if(found.get(idx_2)!=null && found.get(idx_2).get(idx_1)!=null){
                                if(weight < Edges.get(found.get(idx_2).get(idx_1)).get(2)){
                                    Edges.get(found.get(idx_2).get(idx_1)).set(2, weight);
                                }
                                break IF2;
                            }else if(found.get(idx_2)!=null){
                                found.get(idx_2).put(idx_1, Edges.size());
                            }else {
                                HashMap<Integer,Integer>temp_ = new HashMap<>();
                                temp_.put(idx_1,Edges.size());
                                found.put(idx_2,temp_);
                            }
                            ArrayList<Integer> Edge = new ArrayList<Integer>();
                            Edge.add(idx_2);
                            Edge.add(idx_1);
                            Edge.add(weight);
                            Edge.add(0);
                            Edge.add(0);
                            Edges.add(Edge);
                        }
                        same += 1;
                    } else {
                        IF3: if (type1 != 0) {
                            weight = max(((int) (len / speed[type1] + 0.5)), 1);
                            if(found.get(idx_1)!=null && found.get(idx_1).get(idx_2)!=null){
                                if(weight < Edges.get(found.get(idx_1).get(idx_2)).get(2)){
                                    Edges.get(found.get(idx_1).get(idx_2)).set(2, weight);
                                }
                                break IF3;
                            }else if(found.get(idx_1)!=null){
                                found.get(idx_1).put(idx_2, Edges.size());
                            }else {
                                HashMap<Integer,Integer>temp_ = new HashMap<>();
                                temp_.put(idx_2,Edges.size());
                                found.put(idx_1,temp_);
                            }
                            ArrayList<Integer> Edge = new ArrayList<Integer>();
                            Edge.add(idx_1);
                            Edge.add(idx_2);
                            Edge.add(weight);
                            Edge.add(area_1);
                            Edge.add(area_2);

                            Edges.add(Edge);
                        }
                        IF4:if (type2 != 0) {
                            weight = max(((int) (len / speed[type2] + 0.5)), 1);
                            if(found.get(idx_2)!=null && found.get(idx_2).get(idx_1)!=null){
                                if(weight < Edges.get(found.get(idx_2).get(idx_1)).get(2)){
                                    Edges.get(found.get(idx_2).get(idx_1)).set(2, weight);
                                }
                                break IF4;
                            }else if(found.get(idx_2)!=null){
                                found.get(idx_2).put(idx_1, Edges.size());
                            }else {
                                HashMap<Integer,Integer>temp_ = new HashMap<>();
                                temp_.put(idx_1,Edges.size());
                                found.put(idx_2,temp_);
                            }
                            ArrayList<Integer> Edge = new ArrayList<Integer>();
                            Edge.add(idx_2);
                            Edge.add(idx_1);
                            Edge.add(weight);
                            Edge.add(area_2);
                            Edge.add(area_1);
                            Edges.add(Edge);
                        }
                        not_same += 1;
                    }
                }
            }
        }

        System.out.printf("finish recording, same %d, not same %d.\n", same, not_same);

        /*
        String jsonObject = gson.toJson(r_nodes_reverse);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(data_type+"_nodes_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        */
        int [] node2region = new int[total_nodes];
        //HashMap <Integer, HashSet<Integer>> region2node = new HashMap <Integer, HashSet<Integer>>();
        //HashSet<Integer> region = new HashSet<Integer>();

        for(ArrayList<Integer> infos : r_nodes_reverse.values()) {
            /*region.add(infos.get(1));
            if (region2node.get(infos.get(1)) != null) {
                region2node.get(infos.get(1)).add(infos.get(0));
            } else {
                HashSet<Integer> new_area = new HashSet<>();
                new_area.add(infos.get(0));
                region2node.put(infos.get(1), new_area);
            }*/
            node2region[infos.get(0)] = infos.get(1);
        }

        String jsonObject = gson.toJson(node2region);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(data_type+"_node2region"+grid_num+"_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        //System.exit(2);
        /*
        jsonObject = gson.toJson(region2node);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_region2node_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(region);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_regions_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        */


        jsonObject = gson.toJson(Edges);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph"+grid_num+"_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(Edges_p);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_p.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(lalo2id);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_rtree_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        System.out.println("1.finish saving graph");

        int n = lalo2id.size();	//number of vertices in the graph.

        CHSP.Vertex[] graph = new CHSP.Vertex[n];
        CHSP.Vertex[] graph_p = new CHSP.Vertex[n];

        //initialize the graph.
        for(int i=0;i<n;i++){
            graph[i] = new CHSP.Vertex(i);
            graph_p[i] = new CHSP.Vertex(i);
        }

        //get edges
        for (ArrayList<Integer> Edge:Edges) {
            int x, y, c;
            x = Edge.get(0);
            y = Edge.get(1);
            c = Edge.get(2);

            graph[x].outEdges.add(y);
            graph[x].outECost.add(c);
            graph[y].inEdges.add(x);
            graph[y].inECost.add(c);
        }
        for (int i1: Edges_p.keySet()){
            for (int i2: Edges_p.get(i1).keySet()){
                weight = Edges_p.get(i1).get(i2);
                graph_p[i1].outEdges.add(i2);
                graph_p[i1].outECost.add(weight);
                graph_p[i2].inEdges.add(i1);
                graph_p[i2].inECost.add(weight);
                graph_p[i2].outEdges.add(i1);
                graph_p[i2].outECost.add(weight);
                graph_p[i1].inEdges.add(i2);
                graph_p[i1].inECost.add(weight);
            }
        }

        CHSP.PreProcess process = new CHSP.PreProcess();
        process.processing(graph);
        CHSP.PreProcess process_h = new CHSP.PreProcess();
        process_h.processing(graph_p);

        jsonObject = gson.toJson(graph);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_h_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(graph_p);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_p_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        System.out.println("2.finish generating CH graph");

        BiSP.Vertex [] bi_graph = new BiSP.Vertex[n];
        BiSP.Vertex [] reverseGraph = new BiSP.Vertex[n];
        HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> edges = new HashMap<>();

        //initialize the vertices.
        for(int i=0;i<n;i++){
            bi_graph[i]=new BiSP.Vertex(i);
            reverseGraph[i]=new BiSP.Vertex(i);
        }

        //get the edges.
        for(ArrayList<Integer> Edge:Edges){
            int u, v;
            int w;
            u = Edge.get(0);  //start vertex
            v = Edge.get(1);   //end vertex
            w = Edge.get(2);   //weight of edge

            bi_graph[u].adjList.add(v);
            bi_graph[u].costList.add(w);

            reverseGraph[v].adjList.add(u);
            reverseGraph[v].costList.add(w);

            if (edges.get(u)==null){
                HashMap<Integer,ArrayList<Integer>> temp0 = new HashMap<>();
                edges.put(u, temp0);
            }
            ArrayList<Integer> temp1 = new ArrayList<>(3);
            temp1.add(w);
            if (Edge.get(3).equals(Edge.get(4))){
                temp1.add(-1000);
                temp1.add(-1000);
            }else {
                temp1.add(Edge.get(3));
                temp1.add(Edge.get(4));
            }
            edges.get(u).put(v,temp1);
        }
        jsonObject = gson.toJson(edges);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_edges_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(bi_graph);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_o_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(reverseGraph);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_r_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        System.out.println("3.finish generating Bi graph");
    }
}
