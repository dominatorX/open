import javafx.util.Pair;

import java.util.ArrayList;

import static java.lang.Integer.min;

public class Route {
    int time, capacity, size;
    ArrayList<Pair<Integer, Integer>> current_path;
    ArrayList<int[]> route;

    public void init(int start_time, int cap_worker, ArrayList<int[]> request_info) {
        this.time = start_time;
        this.capacity = cap_worker;
        this.route = request_info;
        /** route: location, ddl, arr, capacity shift, slack, picked, departure, awt -> 0, 1, 2, 3, 4, 5, 6, 7
         * location is current location
         * ddl is the latest time to arrive here
         * arr is the arriving time of this location
         * capacity shift is how many capacity are occupied to pick/drop this request, +/- for pick/drop
         * slack is how long we can detour before arriving here
         * picked is how many seats are occupied by now
         * departure is the real departure time, which could be the time neither passenger or car arrive
         * awt (accumulated waiting time) is the total waiting time of driver for passengers to walking, including this loc
          */
        this.size = this.route.size();
        this.current_path = new ArrayList<>();
    }

    void update(int current_time, ShortestPathLRU SPC) {
        if (this.time < current_time) {
            if (this.size == 1) {
                if(this.route.get(0)[2]<current_time) {
                    this.route.get(0)[1] = current_time;
                    this.route.get(0)[2] = current_time;
                }
                this.route.get(0)[4] = 6666;
                this.route.get(0)[5] = 0;
                this.time = current_time;
            } else {
                if (this.route.get(1)[2]>current_time && this.route.get(0)[0] != this.route.get(1)[0]){
                    if (this.current_path.get(this.current_path.size()-1).getValue()<current_time){
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size != 1) {
                            while (this.route.get(0)[0] == this.route.get(1)[0]) {
                                this.route.remove(0);
                                this.size -= 1;
                                if(this.size==1){
                                    break;
                                }
                            }
                        }
                        if (this.size == 1) {
                            if(this.route.get(0)[2]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                            }
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            return;
                        }
                        this.current_path = new ArrayList<>();
                        this.time = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[2], current_time, this.current_path)[1];
                        return;
                    }else {
                        if(this.current_path.get(0).getValue()<current_time) {
                            while (this.current_path.get(0).getValue() < current_time) {
                                this.current_path.remove(0);
                            }
                            this.route.get(0)[0] = this.current_path.get(0).getKey();
                            this.route.get(0)[1] = this.current_path.get(0).getValue();
                            this.route.get(0)[2] = this.current_path.get(0).getValue();
                            this.route.get(0)[3] = 0;
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            return;
                        }
                    }
                }

                while (this.route.get(1)[2] <= current_time) {
                    this.route.remove(0);
                    this.size -= 1;
                    if (this.size == 1) {
                        if(this.route.get(0)[2]<current_time) {
                            this.route.get(0)[1] = current_time;
                            this.route.get(0)[2] = current_time;
                        }
                        this.route.get(0)[3] = 0;
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[5] = 0;
                        this.current_path = new ArrayList<>();
                        break;
                    }
                }
                if (this.size != 1) {
                    while (this.route.get(0)[0]==this.route.get(1)[0]){
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size == 1) {
                            if(this.route.get(0)[2]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                            }
                            this.route.get(0)[3] = 0;
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            this.current_path = new ArrayList<>();
                            return;
                        }
                    }
                    this.current_path = new ArrayList<>();
                    int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[2],
                            current_time, this.current_path);
                    if(temp!=null){
                        this.route.get(0)[0] = temp[0];
                        this.route.get(0)[1] = temp[1];
                        this.route.get(0)[2] = temp[1];
                        this.route.get(0)[3] = 0;
                        this.route.get(0)[4] = 6666;

                        this.time = temp[1];
                    }else{
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size != 1) {
                            while (this.route.get(0)[0] == this.route.get(1)[0]) {
                                this.route.remove(0);
                                this.size -= 1;
                                if(this.size==1){
                                    break;
                                }
                            }
                        }
                        if (this.size == 1) {
                            if(this.route.get(0)[2]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                            }
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            return;
                        }
                        this.current_path = new ArrayList<>();
                        this.time = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0],
                                this.route.get(0)[2], current_time, this.current_path)[1];
                    }

                }else {
                    if(this.route.get(0)[2]<current_time) {
                        this.route.get(0)[1] = current_time;
                        this.route.get(0)[2] = current_time;
                    }
                    this.route.get(0)[3] = 0;
                    this.route.get(0)[4] = 6666;
                    this.route.get(0)[5] = 0;
                    this.current_path = new ArrayList<>();
                    this.time = current_time;
                }
            }
        }
    }

    void updateRM(int current_time, ShortestPathLRU SPC) {
        if (this.time < current_time) {
            if (this.size == 1) {
                if(this.route.get(0)[6]<current_time) {
                    this.route.get(0)[1] = current_time;
                    this.route.get(0)[2] = current_time;
                    this.route.get(0)[6] = current_time;
                }
                this.route.get(0)[4] = 6666;
                this.route.get(0)[5] = 0;
                this.time = current_time;
            } else {
                if (this.route.get(1)[2]>current_time && this.route.get(0)[0] != this.route.get(1)[0]){

                    if(this.current_path.isEmpty())print_tour_RM();
                    if (this.current_path.get(this.current_path.size()-1).getValue()<current_time){
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size != 1) {
                            while (this.route.get(0)[0] == this.route.get(1)[0] && this.route.get(0)[6]==this.route.get(0)[2]) {
                                this.route.remove(0);
                                this.size -= 1;
                                if(this.size==1){
                                    break;
                                }
                            }
                        }
                        if (this.size == 1) {
                            if(this.route.get(0)[6]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                                this.route.get(0)[6] = current_time;
                            }
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            return;
                        }
                        this.current_path = new ArrayList<>();
                        if(this.route.get(0)[0] == this.route.get(1)[0]){
                            this.time = current_time;
                        }else {
                            this.time = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[6], current_time, this.current_path)[1];
                        }
                        return;
                    }else {
                        if(this.current_path.get(0).getValue()<current_time) {
                            while (this.current_path.get(0).getValue() < current_time) {
                                this.current_path.remove(0);
                            }
                            this.route.get(0)[0] = this.current_path.get(0).getKey();
                            this.route.get(0)[1] = this.current_path.get(0).getValue();
                            this.route.get(0)[2] = this.current_path.get(0).getValue();
                            this.route.get(0)[3] = 0;
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.route.get(0)[6] = this.current_path.get(0).getValue();
                            this.time = current_time;
                            return;
                        }
                    }
                }

                while (this.route.get(1)[2] <= current_time) {
                    this.route.remove(0);
                    this.size -= 1;
                    if (this.size == 1) {
                        if(this.route.get(0)[6]<current_time) {
                            this.route.get(0)[1] = current_time;
                            this.route.get(0)[2] = current_time;
                            this.route.get(0)[6] = current_time;
                        }
                        this.route.get(0)[3] = 0;
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[5] = 0;
                        this.current_path = new ArrayList<>();
                        break;
                    }
                }
                if (this.size != 1) {
                    while (this.route.get(0)[0]==this.route.get(1)[0] && this.route.get(0)[6]==this.route.get(0)[2]){
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size == 1) {
                            if(this.route.get(0)[6]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                                this.route.get(0)[6] = current_time;
                            }
                            this.route.get(0)[3] = 0;
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            this.current_path = new ArrayList<>();
                            return;
                        }
                    }
                    this.current_path = new ArrayList<>();
                    if(this.route.get(0)[0]==this.route.get(1)[0] && this.route.get(0)[6]!=this.route.get(0)[2]){
                        this.time = current_time;
                        return;
                    }

                    int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[6],
                            current_time, this.current_path);
                    if(temp!=null) {
                        this.route.get(0)[0] = temp[0];
                        this.route.get(0)[1] = temp[1];
                        this.route.get(0)[2] = temp[1];
                        this.route.get(0)[3] = 0;
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[6] = temp[1];
                        this.time = temp[1];
                    }else{
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size != 1) {
                            while (this.route.get(0)[0] == this.route.get(1)[0] && this.route.get(0)[6]==this.route.get(0)[2]) {
                                this.route.remove(0);
                                this.size -= 1;
                                if(this.size==1){
                                    break;
                                }
                            }
                        }

                        if (this.size == 1) {
                            if(this.route.get(0)[6]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                                this.route.get(0)[6] = current_time;
                            }
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            this.current_path = new ArrayList<>();
                            return;
                        }
                        this.current_path = new ArrayList<>();
                        if(this.route.get(0)[0]==this.route.get(1)[0] && this.route.get(0)[6]!=this.route.get(0)[2]){
                            this.time = current_time;
                            return;
                        }
                        this.time = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0],
                                this.route.get(0)[6], current_time, this.current_path)[1];
                    }

                }else {
                    if(this.route.get(0)[6]<current_time) {
                        this.route.get(0)[1] = current_time;
                        this.route.get(0)[2] = current_time;
                        this.route.get(0)[6] = current_time;
                    }
                    this.route.get(0)[3] = 0;
                    this.route.get(0)[4] = 6666;
                    this.route.get(0)[5] = 0;
                    this.current_path = new ArrayList<>();
                    this.time = current_time;
                }
            }
        }
    }

    void updateHS(int current_time, ShortestPathHSG SPC) throws Throwable {
        if (this.time < current_time) {
            if (this.size == 1) {
                if(this.route.get(0)[6]<current_time) {
                    this.route.get(0)[1] = current_time;
                    this.route.get(0)[2] = current_time;
                    this.route.get(0)[6] = current_time;
                }
                this.route.get(0)[4] = 6666;
                this.route.get(0)[5] = 0;
                this.time = current_time;
            } else {
                if (this.route.get(1)[2]>current_time && this.route.get(0)[0] != this.route.get(1)[0]){
                    if (this.current_path.get(this.current_path.size()-1).getValue()<current_time){
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size != 1) {
                            while (this.route.get(0)[0] == this.route.get(1)[0] && this.route.get(0)[6]==this.route.get(0)[2]) {
                                this.route.remove(0);
                                this.size -= 1;
                                if(this.size==1){
                                    break;
                                }
                            }
                        }
                        if (this.size == 1) {
                            if(this.route.get(0)[6]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                                this.route.get(0)[6] = current_time;
                            }
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            return;
                        }
                        this.current_path = new ArrayList<>();
                        if(this.route.get(0)[0] == this.route.get(1)[0]){
                            this.time = current_time;
                        }else {
                            this.time = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[6], current_time, this.current_path)[1];
                        }
                        return;
                    }else {
                        if(this.current_path.get(0).getValue()<current_time) {
                            while (this.current_path.get(0).getValue() < current_time) {
                                this.current_path.remove(0);
                            }
                            this.route.get(0)[0] = this.current_path.get(0).getKey();
                            this.route.get(0)[1] = this.current_path.get(0).getValue();
                            this.route.get(0)[2] = this.current_path.get(0).getValue();
                            this.route.get(0)[3] = 0;
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.route.get(0)[6] = this.current_path.get(0).getValue();
                            this.time = current_time;
                            return;
                        }
                    }
                }

                while (this.route.get(1)[2] <= current_time) {
                    this.route.remove(0);
                    this.size -= 1;
                    if (this.size == 1) {
                        if(this.route.get(0)[6]<current_time) {
                            this.route.get(0)[1] = current_time;
                            this.route.get(0)[2] = current_time;
                            this.route.get(0)[6] = current_time;
                        }
                        this.route.get(0)[3] = 0;
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[5] = 0;
                        this.current_path = new ArrayList<>();
                        break;
                    }
                }
                if (this.size != 1) {
                    while (this.route.get(0)[0]==this.route.get(1)[0] && this.route.get(0)[6]==this.route.get(0)[2]){
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size == 1) {
                            if(this.route.get(0)[6]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                                this.route.get(0)[6] = current_time;
                            }
                            this.route.get(0)[3] = 0;
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            this.current_path = new ArrayList<>();
                            return;
                        }
                    }
                    this.current_path = new ArrayList<>();
                    if(this.route.get(0)[0]==this.route.get(1)[0] && this.route.get(0)[6]!=this.route.get(0)[2]){
                        this.time = current_time;
                        return;
                    }

                    int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[6],
                            current_time, this.current_path);
                    if(temp!=null) {
                        this.route.get(0)[0] = temp[0];
                        this.route.get(0)[1] = temp[1];
                        this.route.get(0)[2] = temp[1];
                        this.route.get(0)[3] = 0;
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[6] = temp[1];
                        this.time = temp[1];
                    }else{
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.size != 1) {
                            while (this.route.get(0)[0] == this.route.get(1)[0] && this.route.get(0)[6]==this.route.get(0)[2]) {
                                this.route.remove(0);
                                this.size -= 1;
                                if(this.size==1){
                                    break;
                                }
                            }
                        }

                        if (this.size == 1) {
                            if(this.route.get(0)[6]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                                this.route.get(0)[6] = current_time;
                            }
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            this.current_path = new ArrayList<>();
                            return;
                        }
                        this.current_path = new ArrayList<>();
                        if(this.route.get(0)[0]==this.route.get(1)[0] && this.route.get(0)[6]!=this.route.get(0)[2]){
                            this.time = current_time;
                            return;
                        }
                        this.time = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0],
                                this.route.get(0)[6], current_time, this.current_path)[1];
                    }

                }else {
                    if(this.route.get(0)[6]<current_time) {
                        this.route.get(0)[1] = current_time;
                        this.route.get(0)[2] = current_time;
                        this.route.get(0)[6] = current_time;
                    }
                    this.route.get(0)[3] = 0;
                    this.route.get(0)[4] = 6666;
                    this.route.get(0)[5] = 0;
                    this.current_path = new ArrayList<>();
                    this.time = current_time;
                }
            }
        }
    }

    void print_tour(){
        StringBuilder info = new StringBuilder();
        info.append(this.route.get(0)[0]);
        info.append("(");
        info.append(this.route.get(0)[2]);
        info.append(",");
        info.append(this.route.get(0)[3]);
        info.append(")");
        int i;
        for (i=1;i<this.size;i++) {
            int[] aim = this.route.get(i);
            info.append("-->");
            info.append(aim[0]);
            info.append("(");
            info.append(aim[2]);
            info.append(",");
            info.append(aim[3]);
            info.append(")");
        }
        System.out.println(info);
    }

    void print_tour_RM(){
        StringBuilder info = new StringBuilder();
        info.append(this.route.get(0)[0]);
        info.append("(");
        info.append(this.route.get(0)[2]);
        info.append(",");
        info.append(this.route.get(0)[3]);
        info.append(",");
        info.append(this.route.get(0)[6]);
        info.append(")");
        int i;
        for (i=1;i<this.size;i++) {
            int[] aim = this.route.get(i);
            info.append("-->");
            info.append(aim[0]);
            info.append("(");
            info.append(aim[2]);
            info.append(",");
            info.append(aim[3]);
            info.append(",");
            info.append(aim[6]);
            info.append(")");
        }
        System.out.println(info);
    }

    boolean feasible(int idx1, int idx2, Request request, ShortestPathLRU SPC, int dis_) {
        if (idx1 == idx2) {
            int arr1 = this.route.get(idx1 - 1)[2];
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            if (dis1 == -1) {
                return false;
            }
            if (arr1 + dis1 > request.td - dis_) {
                return false;
            }
            if (this.size > idx1) {
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                if (dis2 == -1) {
                    return false;
                }
                int detour = dis_ + dis1 + dis2 + arr1 - this.route.get(idx1)[2];
                if (detour > this.route.get(idx1)[4]) {
                    return false;
                }
            }
            return this.route.get(idx1 - 1)[5] + request.a <= this.capacity;
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            if (dis1 == -1) {
                return false;
            }
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            if (dis2 == -1) {
                return false;
            }
            int arr1 = this.route.get(idx1 - 1)[2];
            int detour1 = dis2 + dis1 + arr1 - this.route.get(idx1)[2];
            if (arr1 + dis1 > request.td - dis_) {
                return false;
            }
            if (detour1 > this.route.get(idx1)[4]) {
                return false;
            }
            int route_idx;
            for (route_idx = idx1 - 1; route_idx < idx2; route_idx++) {
                if (this.route.get(route_idx)[5] + request.a > this.capacity) {
                    return false;
                }
            }

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);
            if (dis3 == -1) {
                return false;
            }
            int arr2 = this.route.get(idx2 - 1)[2];
            if (arr2 + detour1 + dis3 > request.td) {
                return false;
            }
            if (this.size > idx2) {
                int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
                if (dis4 == -1) {
                    return false;
                }
                int detour2 = dis3 + dis4 + arr2 - this.route.get(idx2)[2] + detour1;
                return (detour2 <= this.route.get(idx2)[4]);
            }
        }
        return true;
    }

    boolean feasible_RMD(int idx1, int idx2, Request request, ShortestPathLRU SPC, int dis_, int arr_h, int meet, int ddl, int dep) {
        if (idx1 == idx2) {
            int dep1 = this.route.get(idx1 - 1)[6];
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            if (dis1 == -1) {
                return false;
            }
            int departure = Integer.max(arr_h, dep1+dis1);
            if (departure > ddl - dis_) {
                return false;
            }
            if (this.size > idx1) {
                int dis2 = SPC.dis(dep, this.route.get(idx1)[0]);
                if (dis2 == -1) {
                    return false;
                }
                int detour = dis_ + dis2 + departure - this.route.get(idx1)[2];
                if (detour > this.route.get(idx1)[4]) {
                    return false;
                }
            }
            return this.route.get(idx1 - 1)[5] + request.a <= this.capacity;
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            if (dis1 == -1) {
                return false;
            }
            int dis2 = SPC.dis(meet, this.route.get(idx1)[0]);
            if (dis2 == -1) {
                return false;
            }
            int dep1 = this.route.get(idx1 - 1)[6];
            int departure = Integer.max(dep1+dis1, arr_h);
            int detour1 = dis2 + departure - this.route.get(idx1)[6];
            if (detour1 + this.route.get(idx1)[6] - this.route.get(idx1)[2]> this.route.get(idx1)[4]) {
                return false;
            }
            int route_idx;
            for (route_idx = idx1 - 1; route_idx < idx2; route_idx++) {
                if (this.route.get(route_idx)[5] + request.a > this.capacity) {
                    return false;
                }
            }

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], dep);
            if (dis3 == -1) {
                return false;
            }
            int dep2 = this.route.get(idx2 - 1)[6];
            int detour_s = detour1 - this.route.get(idx2-1)[7]+this.route.get(idx1-1)[7];
            // detour for second insertion index need to reduce the waiting time at the middle
            if (dep2 + detour_s + dis3 > ddl) {
                return false;
            }
            if (this.size > idx2) {
                int dis4 = SPC.dis(dep, this.route.get(idx2)[0]);
                if (dis4 == -1) {
                    return false;
                }
                int detour2 = dis3 + dis4 + dep2 - this.route.get(idx2)[2] + detour_s;
                return (detour2 <= this.route.get(idx2)[4]);
            }
        }
        return true;
    }

    boolean feasible_HS(int idx1, int idx2, Request request, ShortestPathHSG SPC, int dis_, int arr_h, int meet, int ddl, int dep) throws Throwable {
        if (idx1 == idx2) {
            int dep1 = this.route.get(idx1 - 1)[6];
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            if (dis1 == -1) {
                return false;
            }
            int departure = Integer.max(arr_h, dep1+dis1);
            if (departure > ddl - dis_) {
                return false;
            }
            if (this.size > idx1) {
                int dis2 = SPC.dis(dep, this.route.get(idx1)[0]);
                if (dis2 == -1) {
                    return false;
                }
                int detour = dis_ + dis2 + departure - this.route.get(idx1)[2];
                if (detour > this.route.get(idx1)[4]) {
                    return false;
                }
            }
            return this.route.get(idx1 - 1)[5] + request.a <= this.capacity;
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            if (dis1 == -1) {
                return false;
            }
            int dis2 = SPC.dis(meet, this.route.get(idx1)[0]);
            if (dis2 == -1) {
                return false;
            }
            int dep1 = this.route.get(idx1 - 1)[6];
            int departure = Integer.max(dep1+dis1, arr_h);
            int detour1 = dis2 + departure - this.route.get(idx1)[6];
            if (detour1 + this.route.get(idx1)[6] - this.route.get(idx1)[2]> this.route.get(idx1)[4]) {
                return false;
            }
            int route_idx;
            for (route_idx = idx1 - 1; route_idx < idx2; route_idx++) {
                if (this.route.get(route_idx)[5] + request.a > this.capacity) {
                    return false;
                }
            }

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], dep);
            if (dis3 == -1) {
                return false;
            }
            int dep2 = this.route.get(idx2 - 1)[6];
            int detour_s = detour1 - this.route.get(idx2-1)[7]+this.route.get(idx1-1)[7];
            // detour for second insertion index need to reduce the waiting time at the middle
            if (dep2 + detour_s + dis3 > ddl) {
                return false;
            }
            if (this.size > idx2) {
                int dis4 = SPC.dis(dep, this.route.get(idx2)[0]);
                if (dis4 == -1) {
                    return false;
                }
                int detour2 = dis3 + dis4 + dep2 - this.route.get(idx2)[2] + detour_s;
                return (detour2 <= this.route.get(idx2)[4]);
            }
        }
        return true;
    }

    int racost(int idx1, int idx2, Request request, ShortestPathLRU SPC, int dis_){
        if (idx1 == idx2) {
            if (this.size == 1) {
                return dis_ + SPC.dis(this.route.get(0)[0], request.ls);
            } else if (idx1 == this.size) {
                return dis_ + SPC.dis(this.route.get(this.size - 1)[0], request.ls);
            } else {
                int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                return dis_ + dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            }
        } else if (idx2 == this.size) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            int dis3 = SPC.dis(this.route.get(this.size - 1)[0], request.le);
            return cost_d1 + dis3;
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);
            int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
            return cost_d1 + dis3 + dis4 - this.route.get(idx2)[2] + this.route.get(idx2 - 1)[2];
        }
    }

    int cost_RMD(int idx1, int idx2, ShortestPathLRU SPC, int dis_, int meet, int dep){
        if (idx1 == idx2) {
            if (this.size == 1) {
                return dis_ + SPC.dis(this.route.get(0)[0], meet);
            } else if (idx1 == this.size) {
                return dis_ + SPC.dis(this.route.get(this.size - 1)[0], meet);
            } else {
                int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
                int dis2 = SPC.dis(dep, this.route.get(idx1)[0]);
                return dis_ + dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[6];
            }
        } else if (idx2 == this.size) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            int dis2 = SPC.dis(meet, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[6];
            int dis3 = SPC.dis(this.route.get(this.size - 1)[0], dep);
            return cost_d1 + dis3;
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            int dis2 = SPC.dis(meet, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[6];
            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], dep);
            int dis4 = SPC.dis(dep, this.route.get(idx2)[0]);
            return cost_d1 + dis3 + dis4 - this.route.get(idx2)[2] + this.route.get(idx2 - 1)[6];
        }
    }

    int cost_HS(int idx1, int idx2, ShortestPathHSG SPC, int dis_, int meet, int dep) throws Throwable {
        if (idx1 == idx2) {
            if (this.size == 1) {
                return dis_ + SPC.dis(this.route.get(0)[0], meet);
            } else if (idx1 == this.size) {
                return dis_ + SPC.dis(this.route.get(this.size - 1)[0], meet);
            } else {
                int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
                int dis2 = SPC.dis(dep, this.route.get(idx1)[0]);
                return dis_ + dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[6];
            }
        } else if (idx2 == this.size) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            int dis2 = SPC.dis(meet, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[6];
            int dis3 = SPC.dis(this.route.get(this.size - 1)[0], dep);
            return cost_d1 + dis3;
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            int dis2 = SPC.dis(meet, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[6];
            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], dep);
            int dis4 = SPC.dis(dep, this.route.get(idx2)[0]);
            return cost_d1 + dis3 + dis4 - this.route.get(idx2)[2] + this.route.get(idx2 - 1)[6];
        }
    }

    void rainsert(int idx1,int idx2,Request request,ShortestPathLRU SPC, int dis_) {
        if (idx1 == idx2) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            if (this.size > idx1) {
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                int detour = dis_ + dis1 + dis2 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];

                int num;
                for (num = idx1; num < this.size; num++) {
                    this.route.get(num)[2] += detour;
                    this.route.get(num)[4] -= detour;
                }
            }
            int pick1 = this.route.get(idx1 - 1)[5];
            int arr1 = this.route.get(idx1 - 1)[2];
            int slack1 = request.td - dis_ - arr1 - dis1;

            int[] temp0 = {request.ls, request.td - dis_, arr1 + dis1, request.a, slack1, pick1 + request.a};
            this.route.add(idx1, temp0);
            int[] temp1 = {request.le, request.td, arr1 + dis_ + dis1, -request.a, slack1, pick1};
            this.route.add(idx1 + 1, temp1);
            this.size += 2;

            int last_slack = this.route.get(this.size - 1)[4];
            int num;
            for (num = this.size - 2; num > 0; num--) {
                last_slack = min(this.route.get(num)[4], last_slack);
                this.route.get(num)[4] = last_slack;
            }
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);

            int detour1 = dis2 + dis1 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];

            int num;
            for (num = idx1; num < idx2; num++) {
                this.route.get(num)[2] += detour1;
                this.route.get(num)[4] -= detour1;
                this.route.get(num)[5] += request.a;
            }

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);

            if (this.size > idx2) {
                int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
                int detour2 = dis3 + dis4 + this.route.get(idx2 - 1)[2] - this.route.get(idx2)[2];

                for (num = idx2; num < this.size; num++) {
                    this.route.get(num)[2] += detour2;
                    this.route.get(num)[4] -= detour2;
                }
            }
            int arr1 = this.route.get(idx1 - 1)[2];
            int pick1 = this.route.get(idx1 - 1)[5];
            int arr2 = this.route.get(idx2 - 1)[2];
            int pick2 = this.route.get(idx2 - 1)[5];
            int slack2 = request.td - arr2 - dis3;

            int[] temp0 = {request.ls, request.td - dis_, arr1 + dis1, request.a, slack2, pick1 + request.a};
            this.route.add(idx1, temp0);
            int[] temp1 = {request.le, request.td, arr2 + dis3, -request.a, slack2, pick2 - request.a};
            this.route.add(idx2 + 1, temp1);
            this.size += 2;
            int last_slack = this.route.get(this.size - 1)[4];
            for (num = this.size - 2; num > 0; num--) {
                last_slack = min(last_slack, this.route.get(num)[4]);
                this.route.get(num)[4] = last_slack;
            }
        }

        if (idx1 == 1) {
            if (this.route.get(0)[0] == this.route.get(1)[0]) {
                while (this.route.get(0)[0] == this.route.get(1)[0]) {
                    this.route.remove(0);
                    this.size -= 1;
                    if (this.route.size() == 1) {
                        if (this.route.get(0)[2] < request.tr) {
                            this.route.get(0)[1] = request.tr;
                            this.route.get(0)[2] = request.tr;
                        }
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[5] = 0;
                        this.current_path = new ArrayList<>();
                        return;
                    }
                }
            }

            this.current_path = new ArrayList<>();
            int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[2],
                    request.tr, this.current_path);
            this.route.get(0)[0] = temp[0];
            this.route.get(0)[1] = temp[1];
            this.route.get(0)[2] = temp[1];
            this.route.get(0)[3] = 0;
            this.route.get(0)[4] = 6666;
            this.time = temp[1];
        }
    }

    void insert_RMD(int idx1,int idx2,Request request,ShortestPathLRU SPC, int dis_, int arr_h, int meet, int dep, int ddl) {
        if (idx1 == idx2) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            int dep1 = this.route.get(idx1 - 1)[6];
            int departure = Integer.max(dep1 + dis1, arr_h);
            int awt_shift = departure - dep1 - dis1;
            if (this.size > idx1) {
                int dis2 = SPC.dis(dep, this.route.get(idx1)[0]);
                int detour = dis_ + dis2 + departure - this.route.get(idx1)[2];

                int num;
                int remaining_detour = detour;
                for (num = idx1; num < this.size; num++) {
                    int wait = this.route.get(num)[6] - this.route.get(num)[2];
                    if (wait == 0) {
                        this.route.get(num)[2] += remaining_detour;
                        this.route.get(num)[4] -= remaining_detour;
                        this.route.get(num)[6] += remaining_detour;
                        this.route.get(num)[7] += remaining_detour - detour + awt_shift;
                    } else if (remaining_detour > wait) {
                        this.route.get(num)[2] += remaining_detour;
                        remaining_detour -= wait;
                        this.route.get(num)[4] -= remaining_detour;
                        this.route.get(num)[6] += remaining_detour;
                        this.route.get(num)[7] += remaining_detour - detour + awt_shift;
                    } else {
                        this.route.get(num)[2] += remaining_detour;
                        this.route.get(num)[7] += -detour + awt_shift;
                        for (int sub_num = num + 1; sub_num < this.size; sub_num++) {
                            this.route.get(num)[7] += -detour + awt_shift;
                        }
                        break;
                    }
                }
            }

            int slack1 = ddl - dis_ - departure;
            int pick1 = this.route.get(idx1 - 1)[5];

            int[] temp0 = {meet, ddl - dis_, dep1 + dis1, request.a, slack1, pick1 + request.a, departure, this.route.get(idx1 - 1)[7] + awt_shift};
            this.route.add(idx1, temp0);
            int[] temp1 = {dep, ddl, departure + dis_, -request.a, slack1, pick1, departure + dis_, this.route.get(idx1 - 1)[7] + awt_shift};
            this.route.add(idx1 + 1, temp1);
            this.size += 2;

            int last_slack = this.route.get(this.size - 1)[4];
            int num;
            for (num = this.size - 2; num > 0; num--) {
                last_slack = Integer.min(this.route.get(num)[1] - this.route.get(num)[2], last_slack + this.route.get(num)[6] - this.route.get(num)[2]);
                this.route.get(num)[4] = last_slack;
            }
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            int dis2 = SPC.dis(meet, this.route.get(idx1)[0]);
            int dep1 = this.route.get(idx1 - 1)[6];
            int pick1 = this.route.get(idx1 - 1)[5];
            int departure1 = Integer.max(dep1 + dis1, arr_h);
            int detour1 = dis2 + departure1 - this.route.get(idx1)[2];
            int awt_shift = departure1 - dep1 - dis1;
            int remaining_detour = detour1;
            int num;
            for (num = idx1; num < idx2; num++) {
                int wait = this.route.get(num)[6] - this.route.get(num)[2];
                if (wait == 0) {
                    this.route.get(num)[2] += remaining_detour;
                    this.route.get(num)[4] -= remaining_detour;
                    this.route.get(num)[6] += remaining_detour;
                    this.route.get(num)[7] += -detour1 + remaining_detour + awt_shift;
                    this.route.get(num)[5] += request.a;
                } else if (remaining_detour > wait) {
                    this.route.get(num)[2] += remaining_detour;
                    remaining_detour -= wait;
                    this.route.get(num)[4] -= remaining_detour;
                    this.route.get(num)[6] += remaining_detour;
                    this.route.get(num)[7] += -detour1 + remaining_detour + awt_shift;
                    this.route.get(num)[5] += request.a;
                } else {
                    this.route.get(num)[2] += remaining_detour;
                    this.route.get(num)[7] += -detour1 + awt_shift;
                    this.route.get(num)[5] += request.a;
                    for (int sub_num = num + 1; sub_num < this.size; sub_num++) {
                        this.route.get(num)[7] += -detour1 + awt_shift;
                    }
                    break;
                }
            }
            int dep2 = this.route.get(idx2 - 1)[6];
            int pick2 = this.route.get(idx2 - 1)[5];

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], dep);
            int slack2 = ddl - dis3 - dep2;

            if (this.size > idx2) {
                int dis4 = SPC.dis(dep, this.route.get(idx2)[0]);
                remaining_detour = dis3 + dis4 + dep2 - this.route.get(idx2)[2];
                for (num = idx2; num < this.size; num++) {
                    int wait = this.route.get(num)[6] - this.route.get(num)[2];
                    if (wait == 0) {
                        this.route.get(num)[2] += remaining_detour;
                        this.route.get(num)[4] -= remaining_detour;
                        this.route.get(num)[6] += remaining_detour;
                        this.route.get(num)[7] += remaining_detour - detour1 + awt_shift;
                    } else if (remaining_detour > wait) {
                        this.route.get(num)[2] += remaining_detour;
                        remaining_detour -= wait;
                        this.route.get(num)[4] -= remaining_detour;
                        this.route.get(num)[6] += remaining_detour;
                        this.route.get(num)[7] += remaining_detour - detour1 + awt_shift;
                    } else {
                        this.route.get(num)[2] += remaining_detour;
                        this.route.get(num)[7] += awt_shift - detour1;
                        for (int sub_num = num + 1; sub_num < this.size; sub_num++) {
                            this.route.get(num)[7] += -detour1 + awt_shift;
                        }
                        break;
                    }
                }
            }

            int[] temp0 = {meet, ddl - dis_, dep1 + dis1, request.a, slack2, pick1 + request.a, departure1, this.route.get(idx1 - 1)[7] + awt_shift};
            this.route.add(idx1, temp0);
            int[] temp1 = {dep, ddl, dep2 + dis3, -request.a, slack2, pick2 - request.a, dep2 + dis3, this.route.get(idx2 - 1)[7]};
            this.route.add(idx2 + 1, temp1);
            this.size += 2;
            int last_slack = this.route.get(this.size - 1)[4];
            for (num = this.size - 2; num > 0; num--) {
                last_slack = Integer.min(this.route.get(num)[1] - this.route.get(num)[2], last_slack + this.route.get(num)[6] - this.route.get(num)[2]);
                this.route.get(num)[4] = last_slack;
            }
        }

        if (idx1 == 1) {
            if (this.route.get(0)[0] == this.route.get(1)[0]) {
                if(this.route.get(0)[2] == this.route.get(0)[6]) {
                    while (this.route.get(0)[0] == this.route.get(1)[0] && this.route.get(0)[2] == this.route.get(0)[6]) {
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.route.size() == 1) {
                            if (this.route.get(0)[6] < request.tr) {
                                this.route.get(0)[1] = request.tr;
                                this.route.get(0)[2] = request.tr;
                                this.route.get(0)[6] = request.tr;
                            }
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.current_path = new ArrayList<>();
                            return;
                        }
                    }
                }
                if(this.route.get(0)[0] == this.route.get(1)[0]){
                    this.current_path = new ArrayList<>();
                    return;
                }
            }

            this.current_path = new ArrayList<>();
            int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[6],
                    request.tr, this.current_path);
            this.route.get(0)[0] = temp[0];
            this.route.get(0)[1] = temp[1];
            this.route.get(0)[2] = temp[1];
            this.route.get(0)[3] = 0;
            this.route.get(0)[4] = 6666;
            this.route.get(0)[6] = temp[1];
            this.time = temp[1];
        }
    }

    void insert_HS(int idx1,int idx2,Request request,ShortestPathHSG SPC, int dis_, int arr_h, int meet, int dep, int ddl) throws Throwable {
        if (idx1 == idx2) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            int dep1 = this.route.get(idx1 - 1)[6];
            int departure = Integer.max(dep1+dis1, arr_h);
            int awt_shift = departure - dep1-dis1;
            if (this.size > idx1) {
                int dis2 = SPC.dis(dep, this.route.get(idx1)[0]);
                int detour = dis_ + dis2 + departure - this.route.get(idx1)[2];

                int num;
                int remaining_detour = detour;
                for (num = idx1; num < this.size; num++) {
                    int wait = this.route.get(num)[6] - this.route.get(num)[2];
                    if (wait == 0) {
                        this.route.get(num)[2] += remaining_detour;
                        this.route.get(num)[4] -= remaining_detour;
                        this.route.get(num)[6] += remaining_detour;
                        this.route.get(num)[7] += remaining_detour-detour+awt_shift;
                    }else if(remaining_detour>wait){
                        this.route.get(num)[2] += remaining_detour;
                        remaining_detour -= wait;
                        this.route.get(num)[4] -= remaining_detour;
                        this.route.get(num)[6] += remaining_detour;
                        this.route.get(num)[7] += remaining_detour-detour+awt_shift;
                    }else {
                        this.route.get(num)[2] += remaining_detour;
                        this.route.get(num)[7] += -detour+awt_shift;
                        for(int sub_num = num+1; sub_num < this.size; sub_num++){
                            this.route.get(num)[7] += -detour+awt_shift;
                        }
                        break;
                    }
                }
            }

            int slack1 = ddl - dis_ - departure;
            int pick1 = this.route.get(idx1 - 1)[5];

            int[] temp0 = {meet, ddl - dis_, dep1 + dis1, request.a, slack1, pick1 + request.a, departure, this.route.get(idx1-1)[7]+awt_shift};
            this.route.add(idx1, temp0);
            int[] temp1 = {dep, ddl, departure + dis_, -request.a, slack1, pick1, departure+dis_, this.route.get(idx1-1)[7]+awt_shift};
            this.route.add(idx1 + 1, temp1);
            this.size += 2;

            int last_slack = this.route.get(this.size - 1)[4];
            int num;
            for (num = this.size - 2; num > 0; num--) {
                last_slack = Integer.min(this.route.get(num)[1]-this.route.get(num)[2], last_slack+this.route.get(num)[6]-this.route.get(num)[2]);
                this.route.get(num)[4] = last_slack;
            }
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], meet);
            int dis2 = SPC.dis(meet, this.route.get(idx1)[0]);
            int dep1 = this.route.get(idx1 - 1)[6];
            int pick1 = this.route.get(idx1 - 1)[5];
            int departure1 = Integer.max(dep1+dis1, arr_h);
            int detour1 = dis2 + departure1- this.route.get(idx1)[2];
            int awt_shift = departure1 - dep1-dis1;
            int remaining_detour = detour1;
            int num;
            for (num = idx1; num < idx2; num++) {
                int wait = this.route.get(num)[6] - this.route.get(num)[2];
                if(wait==0) {
                    this.route.get(num)[2] += remaining_detour;
                    this.route.get(num)[4] -= remaining_detour;
                    this.route.get(num)[6] += remaining_detour;
                    this.route.get(num)[7] += -detour1+remaining_detour+awt_shift;
                    this.route.get(num)[5] += request.a;
                }else if(remaining_detour>wait){
                    this.route.get(num)[2] += remaining_detour;
                    remaining_detour -= wait;
                    this.route.get(num)[4] -= remaining_detour;
                    this.route.get(num)[6] += remaining_detour;
                    this.route.get(num)[7] += -detour1+remaining_detour+awt_shift;
                    this.route.get(num)[5] += request.a;
                }else {
                    this.route.get(num)[2] += remaining_detour;
                    this.route.get(num)[7] += -detour1+awt_shift;
                    this.route.get(num)[5] += request.a;
                    for(int sub_num = num+1; sub_num < this.size; sub_num++){
                        this.route.get(num)[7] += -detour1+awt_shift;
                    }
                    break;
                }
            }
            int dep2 = this.route.get(idx2 - 1)[6];
            int pick2 = this.route.get(idx2 - 1)[5];

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], dep);
            int slack2 = ddl - dis3 - dep2;

            if (this.size > idx2) {
                int dis4 = SPC.dis(dep, this.route.get(idx2)[0]);
                remaining_detour = dis3 + dis4 + dep2 - this.route.get(idx2)[2];
                for (num = idx2; num < this.size; num++) {
                    int wait = this.route.get(num)[6] - this.route.get(num)[2];
                    if(wait==0) {
                        this.route.get(num)[2] += remaining_detour;
                        this.route.get(num)[4] -= remaining_detour;
                        this.route.get(num)[6] += remaining_detour;
                        this.route.get(num)[7] += remaining_detour-detour1+awt_shift;
                    }else if(remaining_detour>wait){
                        this.route.get(num)[2] += remaining_detour;
                        remaining_detour -= wait;
                        this.route.get(num)[4] -= remaining_detour;
                        this.route.get(num)[6] += remaining_detour;
                        this.route.get(num)[7] += remaining_detour-detour1+awt_shift;
                    }else {
                        this.route.get(num)[2] += remaining_detour;
                        this.route.get(num)[7] += awt_shift-detour1;
                        for(int sub_num = num+1; sub_num < this.size; sub_num++){
                            this.route.get(num)[7] += -detour1+awt_shift;
                        }
                        break;
                    }
                }
            }

            int[] temp0 = {meet, ddl - dis_, dep1 + dis1, request.a, slack2, pick1 + request.a, departure1, this.route.get(idx1-1)[7]+awt_shift};
            this.route.add(idx1, temp0);
            int[] temp1 = {dep, ddl, dep2 + dis3, -request.a, slack2, pick2 - request.a, dep2+dis3, this.route.get(idx2-1)[7]};
            this.route.add(idx2 + 1, temp1);
            this.size += 2;
            int last_slack = this.route.get(this.size - 1)[4];
            for (num = this.size - 2; num > 0; num--) {
                last_slack = Integer.min(this.route.get(num)[1]-this.route.get(num)[2], last_slack+this.route.get(num)[6]-this.route.get(num)[2]);
                this.route.get(num)[4] = last_slack;
            }
        }
        if (idx1 == 1) {
            if (this.route.get(0)[0] == this.route.get(1)[0]) {
                if(this.route.get(0)[2] == this.route.get(0)[6]) {
                    while (this.route.get(0)[0] == this.route.get(1)[0] && this.route.get(0)[2] == this.route.get(0)[6]) {
                        this.route.remove(0);
                        this.size -= 1;
                        if (this.route.size() == 1) {
                            if (this.route.get(0)[6] < request.tr) {
                                this.route.get(0)[1] = request.tr;
                                this.route.get(0)[2] = request.tr;
                                this.route.get(0)[6] = request.tr;
                            }
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.current_path = new ArrayList<>();
                            return;
                        }
                    }
                }
                if(this.route.get(0)[0] == this.route.get(1)[0]){
                    this.current_path = new ArrayList<>();
                    return;
                }
            }

            this.current_path = new ArrayList<>();
            int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[6],
                    request.tr, this.current_path);
            this.route.get(0)[0] = temp[0];
            this.route.get(0)[1] = temp[1];
            this.route.get(0)[2] = temp[1];
            this.route.get(0)[3] = 0;
            this.route.get(0)[4] = 6666;
            this.route.get(0)[6] = temp[1];
            this.time = temp[1];
        }
    }
}
