import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class Insertion {
    double beta = 1.0;
    Pair<Pair<Integer, Integer>, Integer> Leinsertion(Route route, Request request, ShortestPathLRU SPC, int dis_) {
        route.update(request.tr, SPC);
        int[] idx_ = {-1, -1};
        int cost_ = Integer.MAX_VALUE;
        ArrayList<Integer> Dio = new ArrayList<>();
        ArrayList<Integer> Plc = new ArrayList<>();
        // route ddl, arr, slack, picked -> 1, 2, 4, 5
        int j;
        int cost_j;
        Dio.add(Integer.MAX_VALUE);
        Dio.add(Integer.MAX_VALUE);
        Plc.add(-1);
        Plc.add(-1);
        for (j = 1; j < route.size + 1; j++) {

            if (route.feasible(j, j, request, SPC, dis_)) {
                int cost_t = route.racost(j, j, request, SPC, dis_);
                if (cost_t < cost_) {
                    cost_ = cost_t;
                    idx_[0] = j;
                    idx_[1] = j;
                }
            }
            if (j > 1 && Dio.get(j) != Integer.MAX_VALUE) {
                int dis1 = SPC.dis(route.route.get(j - 1)[0], request.le);
                if(dis1!=-1) {
                    if (j == route.size) {
                        cost_j = dis1 + Dio.get(j);
                        if (cost_j + route.route.get(j - 1)[2] > request.td) {
                            cost_j = Integer.MAX_VALUE;
                        }
                    } else {
                        int dis2 = SPC.dis(request.le, route.route.get(j)[0]);
                        if(dis2!=-1) {
                            int arr = dis1 + route.route.get(j - 1)[2] + Dio.get(j);
                            cost_j = dis2 - route.route.get(j)[2] + arr;
                            if (cost_j > route.route.get(j)[4] || arr > request.td) {
                                cost_j = Integer.MAX_VALUE;
                            }
                        }else {
                            cost_j = Integer.MAX_VALUE;
                        }
                    }
                    if (cost_j < cost_) {
                        cost_ = cost_j;
                        idx_[0] = Plc.get(j);
                        idx_[1] = j;
                    }
                }
            }

            if (j == route.size) break;
            if (route.route.get(j)[2] > request.td) {
                break;
            }

            if (route.route.get(j)[5] + request.a > route.capacity ||
                    route.route.get(j - 1)[5] + request.a > route.capacity) {
                Dio.add(Integer.MAX_VALUE);
                Plc.add(-1);
            } else {
                int dis1 = SPC.dis(route.route.get(j - 1)[0], request.ls);
                int dis2 = SPC.dis(request.ls, route.route.get(j)[0]);
                if(dis1!=-1 && dis2!=-1) {
                    int det = dis1 + dis2 + route.route.get(j - 1)[2] - route.route.get(j)[2];
                    if (det > route.route.get(j)[4] || det > Dio.get(j)) {
                        Dio.add(Dio.get(j));
                        Plc.add(Plc.get(j));
                    } else {
                        Dio.add(det);
                        Plc.add(j);
                    }
                }else {
                    Dio.add(Dio.get(j));
                    Plc.add(Plc.get(j));
                }
            }
        }

        if (idx_[0] == -1 || cost_ > request.p) {
            return null;
        } else {
            return new Pair<>(new Pair<>(idx_[0], idx_[1]), cost_);
        }
    }

    Pair<Pair<Integer, Integer>,
            Pair<Pair<Integer, Integer>, Integer>> LeMDinsertion(Route route, Request request, ShortestPathLRU SPC,
                                                  HashMap<Integer, Integer> MP, HashMap<Integer, Integer> DE) {
        // linear DP for RP with meeting point
        route.updateRM(request.tr, SPC);
        int[] idx_ = {-1, -1};
        int cost_ = Integer.MAX_VALUE;
        int meet_ = -1;
        int dep_ = -1;
        for(Integer meet:MP.keySet()) {
            for(Integer dep:DE.keySet()) {
                int dis_ = SPC.dis(meet, dep);
                if (dis_ == -1) {
                    continue;
                }
                int walk_time = MP.get(meet);
                int walk_after = DE.get(dep);
                int ddl = request.td - walk_after;
                int arr_h = request.tr + walk_time;
                ArrayList<Integer> Dio = new ArrayList<>();
                ArrayList<Integer> Plc = new ArrayList<>();
                ArrayList<Integer> Cos = new ArrayList<>();
                // route ddl, arr, slack, picked -> 1, 2, 4, 5
                int j;
                int cost_j;
                Dio.add(Integer.MAX_VALUE);
                Dio.add(Integer.MAX_VALUE);
                Cos.add(Integer.MAX_VALUE);
                Cos.add(Integer.MAX_VALUE);
                Plc.add(-1);
                Plc.add(-1);
                for (j = 1; j < route.size + 1; j++) {

                    if (route.feasible_RMD(j, j, request, SPC, dis_, arr_h, meet, ddl, dep)) {
                        int cost_t = route.cost_RMD(j, j, SPC, dis_, meet, dep) + (int)((walk_time + walk_after)*beta);
                        if (cost_t < cost_) {
                            cost_ = cost_t;
                            idx_[0] = j;
                            idx_[1] = j;
                            meet_ = meet;
                            dep_ = dep;
                        }
                    }
                    if (j > 1 && Dio.get(j) != Integer.MAX_VALUE) {
                        if (j == route.size) {
                            int dis_f = SPC.dis(route.route.get(j - 1)[0], dep);
                            if(dis_f!=-1) {
                                cost_j = dis_f + Dio.get(j) + (int) ((walk_time + walk_after) * beta);
                                int allowed = route.route.get(j - 1)[7] - route.route.get(Plc.get(j))[7];
                                if (Integer.max(Cos.get(j) - allowed, 0) + route.route.get(j - 1)[6] + dis_f > ddl) {
                                    cost_j = Integer.MAX_VALUE;
                                }
                            }else {
                                cost_j = Integer.MAX_VALUE;
                            }
                        } else {
                            int dis1 = SPC.dis(route.route.get(j - 1)[0], dep);
                            int dis2 = SPC.dis(dep, route.route.get(j)[0]);
                            if(dis1!=-1 && dis2!=-1) {
                                int come = dis1 + route.route.get(j - 1)[6];
                                int dis_f = dis2 + come;
                                int allowed = route.route.get(j - 1)[7] - route.route.get(Plc.get(j))[7];
                                cost_j = (int) ((walk_time + walk_after) * beta) + dis_f - route.route.get(j)[2] + Dio.get(j);
                                if (Integer.max(Cos.get(j) - allowed, 0) + dis_f - route.route.get(j)[2] >
                                        route.route.get(j)[4] ||
                                        Integer.max(Cos.get(j) - allowed, 0) + come > ddl) {
                                    cost_j = Integer.MAX_VALUE;
                                }
                            }else {
                                cost_j = Integer.MAX_VALUE;
                            }
                        }
                        if (cost_j < cost_) {
                            cost_ = cost_j;
                            idx_[0] = Plc.get(j);
                            idx_[1] = j;
                            meet_ = meet;
                            dep_ = dep;
                        }
                    }

                    if (j == route.size) break;
                    if (route.route.get(j)[6] > ddl) {
                        break;
                    }

                    if (route.route.get(j)[5] + request.a > route.capacity ||
                            route.route.get(j - 1)[5] + request.a > route.capacity) {
                        Dio.add(Integer.MAX_VALUE);
                        Cos.add(Integer.MAX_VALUE);
                        Plc.add(-1);
                    } else {
                        int arr;
                        int sec;
                        int det;
                        int dis1 = SPC.dis(route.route.get(j - 1)[0], meet);
                        int dis2 = SPC.dis(meet, route.route.get(j)[0]);
                        if(dis1!=-1 && dis2!=-1) {
                            arr = route.route.get(j - 1)[6] + dis1;
                            sec = dis2 - route.route.get(j)[2];

                            det = arr + sec;
                            int time_cost = Integer.max(arr, request.tr + walk_time) + sec;
                            if (time_cost > route.route.get(j)[4] || det > Dio.get(j)) {
                                Dio.add(Dio.get(j));
                                Cos.add(Cos.get(j));
                                Plc.add(Plc.get(j));
                            } else {
                                Dio.add(det);
                                Cos.add(time_cost);
                                Plc.add(j);
                            }
                        }else {
                            Dio.add(Dio.get(j));
                            Cos.add(Cos.get(j));
                            Plc.add(Plc.get(j));
                        }
                    }
                }
            }
        }
        if (idx_[0] == -1 || cost_ > request.p) {
            return null;
        } else {
            return new Pair<>(new Pair<>(idx_[0], idx_[1]), new Pair<>(new Pair<>(meet_, dep_), cost_));
        }
    }

    Pair<Pair<Integer, Integer>,
            Pair<Pair<Integer, Integer>, Integer>> HSMDinsertion(Route route, Request request, ShortestPathHSG SPC,
                                                                 HashMap<Integer, Integer> MP, HashMap<Integer, Integer> DE,
                                                                 HashSet<Integer> Ne, HashMap<Integer, Integer> DV,
                                                                 int checker, int SMD) throws Throwable {
        // linear DP for RP with meeting point using HS
        route.updateHS(request.tr, SPC);
        int[] idx_ = {-1, -1};
        int max_check = route.size;
        /*if(request.ls==32800&&request.le==910&&route.size==4&&route.route.get(0)[0]==38451){
            System.out.println(gson.toJson(request));
            System.out.println(gson.toJson(route));
            System.out.println(gson.toJson(DV));
            System.out.println(gson.toJson(Ne));
            System.exit(0);
        }*/
        for (int i = 0; i < route.size; i++) {
            if (Ne.contains(route.route.get(i)[0])) {
                continue;
            }
            int shortest = SPC.dis(route.route.get(i)[0], checker);
            if (shortest == -1 || route.route.get(i)[6] > request.tp - shortest + SMD) {
                if (i == 0) {
                    DV.put(route.route.get(i)[0], request.tp - shortest + SMD);
                    return null;
                }
                max_check = i;
                break;
            }
        }

        int cost_ = Integer.MAX_VALUE;
        int meet_ = -1;
        int dep_ = -1;
        for(Integer meet:MP.keySet()) {
            for(Integer dep:DE.keySet()) {
                int dis_ = SPC.dis(meet, dep);
                if (dis_ == -1) {
                    continue;
                }
                int walk_time = MP.get(meet);
                int walk_after = DE.get(dep);
                int ddl = request.td - walk_after;
                int arr_h = request.tr + walk_time;
                ArrayList<Integer> Dio = new ArrayList<>();
                ArrayList<Integer> Plc = new ArrayList<>();
                ArrayList<Integer> Cos = new ArrayList<>();
                // route ddl, arr, slack, picked -> 1, 2, 4, 5
                int j;
                int cost_j;
                Dio.add(Integer.MAX_VALUE);
                Dio.add(Integer.MAX_VALUE);
                Cos.add(Integer.MAX_VALUE);
                Cos.add(Integer.MAX_VALUE);
                Plc.add(-1);
                Plc.add(-1);

                for (j = 1; j < route.size + 1; j++) {

                    if (route.feasible_HS(j, j, request, SPC, dis_, arr_h, meet, ddl, dep)) {
                        int cost_t = route.cost_HS(j, j, SPC, dis_, meet, dep) + (int)((walk_time + walk_after)*beta);
                        if (cost_t < cost_) {
                            cost_ = cost_t;
                            idx_[0] = j;
                            idx_[1] = j;
                            meet_ = meet;
                            dep_ = dep;
                        }
                    }
                    if (j > 1 && Dio.get(j) != Integer.MAX_VALUE) {
                        if (j == route.size) {
                            int dis_f = SPC.dis(route.route.get(j - 1)[0], dep);
                            if(dis_f!=-1) {
                                cost_j = dis_f + Dio.get(j) + (int) ((walk_time + walk_after) * beta);
                                int allowed = route.route.get(j - 1)[7] - route.route.get(Plc.get(j))[7];
                                if (Integer.max(Cos.get(j) - allowed, 0) + route.route.get(j - 1)[6] + dis_f > ddl) {
                                    cost_j = Integer.MAX_VALUE;
                                }
                            }else {
                                cost_j = Integer.MAX_VALUE;
                            }
                        } else {
                            int dis1 = SPC.dis(route.route.get(j - 1)[0], dep);
                            int dis2 = SPC.dis(dep, route.route.get(j)[0]);
                            if(dis1!=-1 && dis2!=-1) {
                                int come = dis1 + route.route.get(j - 1)[6];
                                int dis_f = dis2 + come;
                                int allowed = route.route.get(j - 1)[7] - route.route.get(Plc.get(j))[7];
                                cost_j = (int) ((walk_time + walk_after) * beta) + dis_f - route.route.get(j)[2] + Dio.get(j);
                                if (Integer.max(Cos.get(j) - allowed, 0) + dis_f - route.route.get(j)[2] >
                                        route.route.get(j)[4] ||
                                        Integer.max(Cos.get(j) - allowed, 0) + come > ddl) {
                                    cost_j = Integer.MAX_VALUE;
                                }
                            }else {
                                cost_j = Integer.MAX_VALUE;
                            }
                        }
                        if (cost_j < cost_) {
                            cost_ = cost_j;
                            idx_[0] = Plc.get(j);
                            idx_[1] = j;
                            meet_ = meet;
                            dep_ = dep;
                        }
                    }

                    if (j == route.size) break;
                    if (route.route.get(j)[6] > ddl)break;

                    if (route.route.get(j)[5] + request.a > route.capacity ||
                            route.route.get(j - 1)[5] + request.a > route.capacity) {
                        if(j > max_check) break;

                        Dio.add(Integer.MAX_VALUE);
                        Cos.add(Integer.MAX_VALUE);
                        Plc.add(-1);
                    } else {
                        if(j > max_check){
                            Dio.add(Dio.get(j));
                            Cos.add(Cos.get(j));
                            Plc.add(Plc.get(j));
                            continue;
                        }
                        int arr;
                        int sec;
                        int det;
                        int dis1 = SPC.dis(route.route.get(j - 1)[0], meet);
                        int dis2 = SPC.dis(meet, route.route.get(j)[0]);
                        if(dis1!=-1 && dis2!=-1) {
                            arr = route.route.get(j - 1)[6] + dis1;
                            sec = dis2 - route.route.get(j)[2];

                            det = arr + sec;
                            int time_cost = Integer.max(arr, request.tr + walk_time) + sec;
                            if (time_cost > route.route.get(j)[4] || det > Dio.get(j)) {
                                Dio.add(Dio.get(j));
                                Cos.add(Cos.get(j));
                                Plc.add(Plc.get(j));
                            } else {
                                Dio.add(det);
                                Cos.add(time_cost);
                                Plc.add(j);
                            }
                        }else {
                            Dio.add(Dio.get(j));
                            Cos.add(Cos.get(j));
                            Plc.add(Plc.get(j));
                        }
                    }
                }
            }
        }
        if (idx_[0] == -1 || cost_ > request.p) {
            return null;
        } else {
            return new Pair<>(new Pair<>(idx_[0], idx_[1]), new Pair<>(new Pair<>(meet_, dep_), cost_));
        }
    }
    Pair<Pair<Integer, Integer>,
            Pair<Pair<Integer, Integer>, Integer>> rawHSMDinsertion(Route route, Request request, ShortestPathHSG SPC,
                                                                 HashMap<Integer, Integer> MP, HashMap<Integer, Integer> DE) throws Throwable {
        // linear DP for RP with meeting point using HS cannot prune
        route.updateHS(request.tr, SPC);
        int[] idx_ = {-1, -1};

        int cost_ = Integer.MAX_VALUE;
        int meet_ = -1;
        int dep_ = -1;
        for(Integer meet:MP.keySet()) {
            for(Integer dep:DE.keySet()) {
                int dis_ = SPC.dis(meet, dep);
                if (dis_ == -1) {
                    continue;
                }
                int walk_time = MP.get(meet);
                int walk_after = DE.get(dep);
                int ddl = request.td - walk_after;
                int arr_h = request.tr + walk_time;
                ArrayList<Integer> Dio = new ArrayList<>();
                ArrayList<Integer> Plc = new ArrayList<>();
                ArrayList<Integer> Cos = new ArrayList<>();
                // route ddl, arr, slack, picked -> 1, 2, 4, 5
                int j;
                int cost_j;
                Dio.add(Integer.MAX_VALUE);
                Dio.add(Integer.MAX_VALUE);
                Cos.add(Integer.MAX_VALUE);
                Cos.add(Integer.MAX_VALUE);
                Plc.add(-1);
                Plc.add(-1);

                for (j = 1; j < route.size + 1; j++) {

                    if (route.feasible_HS(j, j, request, SPC, dis_, arr_h, meet, ddl, dep)) {
                        int cost_t = route.cost_HS(j, j, SPC, dis_, meet, dep) + (int)((walk_time + walk_after)*beta);
                        if (cost_t < cost_) {
                            cost_ = cost_t;
                            idx_[0] = j;
                            idx_[1] = j;
                            meet_ = meet;
                            dep_ = dep;
                        }
                    }
                    if (j > 1 && Dio.get(j) != Integer.MAX_VALUE) {
                        if (j == route.size) {
                            int dis_f = SPC.dis(route.route.get(j - 1)[0], dep);
                            if(dis_f!=-1) {
                                cost_j = dis_f + Dio.get(j) + (int) ((walk_time + walk_after) * beta);
                                int allowed = route.route.get(j - 1)[7] - route.route.get(Plc.get(j))[7];
                                if (Integer.max(Cos.get(j) - allowed, 0) + route.route.get(j - 1)[6] + dis_f > ddl) {
                                    cost_j = Integer.MAX_VALUE;
                                }
                            }else {
                                cost_j = Integer.MAX_VALUE;
                            }
                        } else {
                            int dis1 = SPC.dis(route.route.get(j - 1)[0], dep);
                            int dis2 = SPC.dis(dep, route.route.get(j)[0]);
                            if(dis1!=-1 && dis2!=-1) {
                                int come = dis1 + route.route.get(j - 1)[6];
                                int dis_f = dis2 + come;
                                int allowed = route.route.get(j - 1)[7] - route.route.get(Plc.get(j))[7];
                                cost_j = (int) ((walk_time + walk_after) * beta) + dis_f - route.route.get(j)[2] + Dio.get(j);
                                if (Integer.max(Cos.get(j) - allowed, 0) + dis_f - route.route.get(j)[2] >
                                        route.route.get(j)[4] ||
                                        Integer.max(Cos.get(j) - allowed, 0) + come > ddl) {
                                    cost_j = Integer.MAX_VALUE;
                                }
                            }else {
                                cost_j = Integer.MAX_VALUE;
                            }
                        }
                        if (cost_j < cost_) {
                            cost_ = cost_j;
                            idx_[0] = Plc.get(j);
                            idx_[1] = j;
                            meet_ = meet;
                            dep_ = dep;
                        }
                    }

                    if (j == route.size) break;
                    if (route.route.get(j)[6] > ddl)break;

                    if (route.route.get(j)[5] + request.a > route.capacity ||
                            route.route.get(j - 1)[5] + request.a > route.capacity) {
                        Dio.add(Integer.MAX_VALUE);
                        Cos.add(Integer.MAX_VALUE);
                        Plc.add(-1);
                    } else {
                        int arr;
                        int sec;
                        int det;
                        int dis1 = SPC.dis(route.route.get(j - 1)[0], meet);
                        int dis2 = SPC.dis(meet, route.route.get(j)[0]);
                        if(dis1!=-1 && dis2!=-1) {
                            arr = route.route.get(j - 1)[6] + dis1;
                            sec = dis2 - route.route.get(j)[2];

                            det = arr + sec;
                            int time_cost = Integer.max(arr, request.tr + walk_time) + sec;
                            if (time_cost > route.route.get(j)[4] || det > Dio.get(j)) {
                                Dio.add(Dio.get(j));
                                Cos.add(Cos.get(j));
                                Plc.add(Plc.get(j));
                            } else {
                                Dio.add(det);
                                Cos.add(time_cost);
                                Plc.add(j);
                            }
                        }else {
                            Dio.add(Dio.get(j));
                            Cos.add(Cos.get(j));
                            Plc.add(Plc.get(j));
                        }
                    }
                }
            }
        }
        if (idx_[0] == -1 || cost_ > request.p) {
            return null;
        } else {
            return new Pair<>(new Pair<>(idx_[0], idx_[1]), new Pair<>(new Pair<>(meet_, dep_), cost_));
        }
    }
}
