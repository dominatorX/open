import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class GridPrune{
    int [] node2region;
    HashMap <Integer, HashMap<Integer,Long>> time_list;
    public void init(String data_set, String grid_num) throws IOException {
        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new
                FileInputStream(data_set+"_node2region"+grid_num+"_j.json"));
        this.node2region  = gson.fromJson(in,
                new TypeToken<int[]>(){ }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(data_set+"_inter_region_cost"+grid_num+"_j.json"));
        this.time_list = gson.fromJson(in,
                new TypeToken<HashMap<Integer, HashMap<Integer,Long>>>(){ }.getType());
        in.close();
    }

    public void init(String data_set) throws IOException {
        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new
                FileInputStream(data_set+"_node2region_j.json"));
        this.node2region  = gson.fromJson(in,
                new TypeToken<int[]>(){ }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(data_set+"_inter_region_cost_j.json"));
        this.time_list = gson.fromJson(in,
                new TypeToken<HashMap<Integer, HashMap<Integer,Long>>>(){ }.getType());
        in.close();
    }

    public boolean reachable(int l1,int l2,int time_left){
        if (this.node2region[l1] == this.node2region[l2]) {
            return true;
        } else{
            try {
                return (this.time_list.get(this.node2region[l1]).get(this.node2region[l2]) <= time_left);
            }catch (Exception e){
                return false;
            }
        }
    }
}
