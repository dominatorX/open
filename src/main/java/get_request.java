import com.csvreader.CsvReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class get_request {
    String file="./NYC/ny";
    void get() throws IOException {
        //22m51s to run
        // change your output name here
        String req_file = "./NYC/yellow_tripdata_2013-12.csv";
        String output_name = file+"Req30.json";
        String whole_month_data = file+"_output_req";

        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new FileInputStream(file+"_rtree_j.json"));
        ArrayList<double[]> nodes = gson.fromJson(in,
                new TypeToken<ArrayList<double[]>>() {
                }.getType());
        in.close();
        System.out.println(nodes.size());
        Rtree_search_only tree = new Rtree_search_only();
        tree.min_child=5;
        tree.max_child=10;

        for (int num=0;num<nodes.size();num++)
            tree.insertion(new Rtree_search_only.Rec(nodes.get(num)[0], nodes.get(num)[0],
                    nodes.get(num)[1], nodes.get(num)[1], num));

        System.out.println("finish loading");

        ArrayList<int[]> tasks = new ArrayList<>();
        ArrayList<int[]> all_tasks = new ArrayList<>();
        CsvReader f = new CsvReader(req_file);
        f.readHeaders();
        int node1, node2, counter = 0;
        while (f.readRecord()) {
            if (f.get(3).equals("nan")) continue;

            try {
                if (Integer.parseInt(f.get(1).split(" ")[0].split("-")[2]) != 1) {
                    node1 = tree.nearest(Double.parseDouble(f.get(6)), Double.parseDouble(f.get(5)));
                    node2 = tree.nearest(Double.parseDouble(f.get(10)), Double.parseDouble(f.get(9)));
                    int[]temp_task = {Integer.parseInt(f.get(1).split(" ")[0].split("-")[2]),node1,
                            Integer.parseInt(f.get(1).split(" ")[1].split(":")[0]) * 60 +
                            Integer.parseInt(f.get(1).split(" ")[1].split(":")[1]), node2,
                            Integer.max(1, Integer.parseInt(f.get(3)))};
                    all_tasks.add(temp_task);
                    counter++;
                    if(counter%1000000==0) System.out.println(counter);
                    if (Integer.parseInt(f.get(1).split(" ")[0].split("-")[2]) == 30) {
                        int[] task = {temp_task[2]*60 +
                                Integer.parseInt(f.get(1).split(" ")[1].split(":")[2]),
                                node1, node2, temp_task[4]};
                        tasks.add(task);
                    }
                }
            } catch (Exception NumberFormatException) {
                System.out.println(f.get(1)+" "+f.get(2)+" "+f.get(3)+" "+f.get(5)+" "+f.get(6)+" "+f.get(9)+" "+f.get(10));
            }
        }
        System.out.println("finish read req, totally "+all_tasks.size());
        String jsonObject = gson.toJson(all_tasks);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(whole_month_data));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        ShortestPathLRU SPC = new ShortestPathLRU();
        SPC.intializeFrequent(file, whole_month_data);
        SPC.init(file);

        System.out.println("finish prepare LRU");

        ArrayList<Request> outputreq = new ArrayList<>();
        for(int[] req_info: tasks) {
            if (req_info[1] == req_info[2]) continue;

            int dist = SPC.dis(req_info[1], req_info[2]);
            if (dist == -1) continue;

            Request request = new Request();
            request.init(req_info[1], req_info[2], req_info[0],
                    req_info[3], dist);
            outputreq.add(request);
        }
        compReq comp = new compReq();
        outputreq.sort(comp);
        jsonObject = gson.toJson(outputreq);
        out = new OutputStreamWriter(new FileOutputStream(output_name));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        System.out.println("finish output request in json");
    }

    class compReq implements Comparator<Request> {
        @Override
        public int compare(Request o1, Request o2) {
            return Integer.compare(o1.tr, o2.tr);
        }
    }

    public static void main(String[] args) throws IOException {
        get_request req = new get_request();
        req.get();
    }
}

