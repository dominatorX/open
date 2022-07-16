import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

final class Request{
    int ls, le, tr, tp, td, p, a, dist;
    void init(int ls, int le, int tr, int a, int dist){
        this.ls=ls;
        this.le=le;
        this.tr=tr;
        this.a = a;
        this.dist = dist;
    }

    void fulfil(int p0, double df){
        this.td = this.tr + (int) (this.dist * df);
        this.p = p0 * this.dist;
        this.tp = this.td-this.dist;
    }

    public void fulfilHS(int p0, double df, HashMap<Integer, Integer> MP,
                         HashMap<Integer, Integer> DE, ShortestPathHSG SPC){
        int max_drive = (int) (this.dist * df);
        this.td = this.tr + max_drive;
        this.p = p0 * this.dist;
        this.tp = this.td - this.dist;
        for(int meet: MP.keySet()){
            int w_s = MP.get(meet);
            for(int dep: DE.keySet()){
                int dis = SPC.dis(meet, dep);
                int w_e = DE.get(dep);
                int new_tp = this.td - dis - w_e;
                if(dis!=-1 && dis+w_s+w_e <= max_drive && new_tp > this.tp){
                    this.tp = new_tp;
                }
            }
        }
    }

}
