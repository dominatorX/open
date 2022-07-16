import com.google.gson.Gson;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class Check_SMD {
    boolean[] core;
    Boolean[] sub;
    ArrayList<HashMap<Integer, Integer>> alter;
    HashMap<Integer, CHSP.Vertex> sub_core;
    CHSP.Vertex[] HSgraph;
    int n;
    String file;
    ArrayList<Integer> SMD;
    ArrayList<Integer> checker;

    public void SMD() throws IOException {
        Gson gson = new Gson();
        checker = new ArrayList<>(n);
        SMD = new ArrayList<>(n);
        Loop: for(int idx=0;idx<n;idx++) {
            //System.out.println(idx);
            HashSet<Integer> VC = new HashSet<>();
            HashMap<Integer, HashMap<Integer, Integer>> CC = new HashMap<>();
            HashSet<Integer> Can = new HashSet<>();
            for (int v : alter.get(idx).keySet()) {
                if (core[v]) {
                    VC.addAll(HSgraph[v].inEdges);
                    Can.add(v);
                } else if (sub[v]) {
                    VC.addAll(sub_core.get(v).inEdges);
                    Can.add(v);
                }
            }
            for (int v : VC) {
                CC.put(v, new HashMap<>());
            }
            for (int ca : Can) {
                HashSet<Integer> VC_ = (HashSet<Integer>) VC.clone();
                Comparator<Pair<Integer, Integer>> comp = new ShortestPathLRU.pair_com();
                //ArrayList<Integer> prevNode = new ArrayList<>(N);
                ArrayList<Integer> distanceFromSource = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    distanceFromSource.add(i, 1000000);
                    //    prevNode.add(i, -1);
                }

                distanceFromSource.set(ca, 0);
                PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(n, comp);
                if (core[ca]) {
                    for (int i = 0; i < HSgraph[ca].inEdges.size(); i++) {
                        int v = HSgraph[ca].inEdges.get(i);

                        distanceFromSource.set(v, HSgraph[ca].inECost.get(i));
                        dj.add(new Pair<>(-HSgraph[ca].inECost.get(i), v));
                    }
                } else {
                    for (int i = 0; i < sub_core.get(ca).inEdges.size(); i++) {
                        int v = sub_core.get(ca).inEdges.get(i);

                        distanceFromSource.set(v, sub_core.get(ca).inECost.get(i));
                        dj.add(new Pair<>(-sub_core.get(ca).inECost.get(i), v));
                    }
                }

                Pair<Integer, Integer> x;
                int u, v;
                Integer alt;

                while( !dj.isEmpty() && !VC_.isEmpty())
                {
                    x = dj.poll();
                    u = x.getValue();
                    if(VC_.contains(u)){
                        CC.get(u).put(ca, distanceFromSource.get( u ));
                        VC_.remove(u);
                    }

                    if( distanceFromSource.get( u ) >= 1000000 )
                        break;

                    for(int i = 0; i < HSgraph[ u ].inEdges.size(); i++) {
                        v = HSgraph[ u ].inEdges.get( i );
                        alt = distanceFromSource.get( u ) + HSgraph[ u ].inECost.get( i );
                        if( alt < distanceFromSource.get( v ) ) {
                            distanceFromSource.set(v, alt);
                            dj.add( new Pair<>(-alt, v) );
                            //    prevNode.set(v, u);
                        }
                    }
                }
            }
            HashMap<Integer, Integer> V2MD = new HashMap<>();
            for(int ca:Can){
                V2MD.put(ca, 0);
            }
            for (int vc:VC){
                if (CC.get(vc).size()!=Can.size()){
                    //System.out.println(Can.size());
                    for(int temp: Can){
                        checker.add(temp);
                        SMD.add(1000000);
                        continue Loop;
                    }
                }else {
                    int low = Collections.min(CC.get(vc).values());
                    for(int vp:Can){
                        if (V2MD.get(vp)<CC.get(vc).get(vp)-low){
                            V2MD.put(vp, CC.get(vc).get(vp)-low);
                        }
                    }
                }
            }
            int ch = -1;
            int SMD_ = 1000000;
            for(int ca:Can){
                if (V2MD.get(ca)<SMD_){
                    ch = ca;
                    SMD_ = V2MD.get(ca);
                }
            }
            checker.add(ch);
            SMD.add(SMD_);
        }
        String jsonObject = gson.toJson(checker);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file+"checker.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(SMD);
        out = new OutputStreamWriter(new FileOutputStream(file+"SMD.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
    }
}
