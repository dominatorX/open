import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class NodeFilter {
    CHSP.Vertex[] graph;
    CHSP.Vertex[] graph_p;
    double beta;      //cost of walking per second/cost of driving per second (alpha=1)
    int walk_max;     //max distance of walking
    int not_en;       //the number of vertices without max_can candidates
    int num_nei;      //the number of neighbor to search for convenience
    int max_can;      //the max neighbor a vertex can have
    int max_loss;     //threshold to abandon a candidate

    Pair<Integer, HashSet<Integer>> dijkstra_lengths_in(int N, int S) {
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        //ArrayList<Integer> prevNode = new ArrayList<>(N);
        ArrayList< Integer > distanceFromSource = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
        { 	distanceFromSource.add( i, 1000000);
        //    prevNode.add(i, -1);
        }

        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(graph.length, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        HashSet<Integer> NearIn = new HashSet<>(this.num_nei);
        int dis_in = 0;
        while( dj.size() != 0 && NearIn.size()!=this.num_nei)
        {
            x = dj.poll();
            u = x.getValue();
            NearIn.add(u);
            dis_in -= x.getKey();

            if( distanceFromSource.get( u ) >= 1000000 )
                break;

            for(int i = 0; i < graph[ u ].inEdges.size(); i++) {
                v = graph[ u ].inEdges.get( i );
                alt = distanceFromSource.get( u ) + graph[ u ].inECost.get( i );
                if( alt < distanceFromSource.get( v ) )
                { 	distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                //    prevNode.set(v, u);
                }
            }
        }
        return new Pair<>(dis_in, NearIn);
    }

    Pair<Integer, HashSet<Integer>> dijkstra_lengths_out(int N, int S) {
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        //ArrayList<Integer> prevNode = new ArrayList<>(N);
        ArrayList< Integer > distanceFromSource = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
        { 	distanceFromSource.add( i, 1000000);
            //    prevNode.add(i, -1);
        }

        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(graph_p.length, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        HashSet<Integer> NearOut = new HashSet<>(this.num_nei);
        int dis_out = 0;
        while( dj.size() != 0 && NearOut.size()!=this.num_nei)
        {
            x = dj.poll();
            u = x.getValue();
            NearOut.add(u);
            dis_out -= x.getKey();

            if( distanceFromSource.get( u ) >= 1000000 )
                break;

            for(int i = 0; i < graph[ u ].outEdges.size(); i++) {
                v = graph[ u ].outEdges.get( i );
                alt = distanceFromSource.get( u ) + graph[ u ].outECost.get( i );
                if( alt < distanceFromSource.get( v ) )
                { 	distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                    //    prevNode.set(v, u);
                }
            }
        }
        return new Pair<>(dis_out, NearOut);
    }

    HashMap<Integer, Integer> dijkstra_can_set(int N, int S, int[]score) {
        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
        //ArrayList<Integer> prevNode = new ArrayList<>(N);
        ArrayList< Integer > distanceFromSource = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
        { 	distanceFromSource.add( i, 1000000);
            //    prevNode.add(i, -1);
        }

        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(this.graph_p.length, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        ArrayList<Integer> top_id = new ArrayList<>(this.max_can);
        ArrayList<Double> top_dis = new ArrayList<>(this.max_can);

        while( dj.size() != 0)
        {
            x = dj.poll();
            u = x.getValue();

            if( distanceFromSource.get( u ) >= this.walk_max )
                break;
            if(u != S) {
                if (top_id.size() < this.max_can-1) {
                    if (top_id.size() == 0) {
                        double va = distanceFromSource.get(u) * this.beta + score[u];
                        if (va < score[S] + this.max_loss) {
                            top_id.add(u);
                            top_dis.add(va);
                        }
                    } else {
                        double va = distanceFromSource.get(u) * this.beta + score[u];
                        if (va < score[S] + this.max_loss) {
                            for (int i = 0; i < top_id.size(); i++) {
                                if (va < top_dis.get(i)) {
                                    top_dis.add(i, va);
                                    top_id.add(i, u);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    double va = distanceFromSource.get(u) * this.beta + score[u];
                    if (va < score[S] + this.max_loss) {
                        for (int i = 0; i < this.max_can-1; i++) {
                            if (va < top_dis.get(i)) {
                                top_dis.add(i, va);
                                top_id.add(i, u);
                                top_dis.remove(this.max_can-1);
                                top_id.remove(this.max_can-1);
                                break;
                            }
                        }
                    }
                }
            }
            for(int i = 0; i < graph_p[ u ].outEdges.size(); i++) {
                v = graph_p[ u ].outEdges.get( i );
                alt = distanceFromSource.get( u ) + graph_p[ u ].outECost.get( i );
                if( alt < distanceFromSource.get( v ) )
                { 	distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                    //    prevNode.set(v, u);
                }
            }
        }
        HashMap<Integer, Integer> top = new HashMap<>(this.max_can);
        for(int id :top_id){
            top.put(id, distanceFromSource.get(id));
        }
        top.put(S, 0);
        if (top.size()<this.max_can){
            not_en++;
        }
        return top;
    }
}
