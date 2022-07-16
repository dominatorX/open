import com.google.gson.Gson;
import javafx.util.Pair;

import java.util.ArrayList;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class RouteTree {
    int time, capacity, size, earliest_finish = 0;
    ArrayList<Pair<Integer, Integer>> current_path;
    RouteNode route;

    public void init(int start_time, int cap_worker, ArrayList<int[]> request_info) {
        this.time = start_time;
        this.capacity = cap_worker;
        this.route = new RouteNode();
        RouteNode current = this.route;
        for (int idx=0;idx<request_info.size();idx++) {
            current.element = request_info.get(idx);
            if(idx!=request_info.size()-1) {
                current.firstChild = new RouteNode();
                current.firstChild.parent = current;
                current = current.getFirstChild();
            }
        }

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
        this.size = request_info.size();
        this.current_path = new ArrayList<>();
    }

    void update(int current_time, ShortestPathLRU SPC) {
        if (this.time < current_time) {
            if (this.size == 1) {
                size_1_route(current_time);
            } else {
                RouteNode next = this.route.getFirstChild();
                if (next.element[2]>current_time && this.route.element[0] != next.element[0]){
                    //only need to update for first node which is different from second one
                    if (this.current_path.get(this.current_path.size()-1).getValue()<current_time){
                        //head need to remove
                        remove_head_until_valid(null, current_time, SPC);
                        return;
                    }else {
                        //directly update according to current path
                        if(this.current_path.get(0).getValue()<current_time) {
                            while (this.current_path.get(0).getValue() < current_time) {
                                this.current_path.remove(0);
                            }
                            this.route.element[0] = this.current_path.get(0).getKey();
                            this.route.element[1] = this.current_path.get(0).getValue();
                            this.route.element[2] = this.current_path.get(0).getValue();
                            this.route.element[3] = 0;
                            this.route.element[4] = 6666;
                            this.route.element[5] = 0;
                            this.time = current_time;
                            return;
                        }
                    }
                }

                while (this.route.getFirstChild().element[2] <= current_time) {
                    // remove until head is new enough
                    this.route.removeHead();
                    this.size -= 1;
                    if (this.size == 1) {
                        size_1_route(current_time);
                        return;
                    }
                }

                remove_same_head(current_time);
                if(this.size == 1) return;

                this.current_path = new ArrayList<>();
                int[] temp = SPC.current_loc(this.route.element[0], this.route.getFirstChild().element[0],
                        this.route.element[2], current_time, this.current_path);
                remove_head_until_valid(temp, current_time, SPC);
            }
        }
    }

    void size_1_route(int current_time){
        if(this.route.element[2]<current_time) {
            this.route.element[1] = current_time;
            this.route.element[2] = current_time;
        }
        this.route.element[3] = 0;
        this.route.element[4] = 6666;
        this.route.element[5] = 0;
        this.time = current_time;
        this.earliest_finish = current_time;
        this.current_path = new ArrayList<>();
    }

    void remove_same_head(int current_time){
        while (this.route.element[0] == this.route.getFirstChild().element[0]) {
            this.route.removeHead();
            this.size -= 1;
            if(this.size==1){
                size_1_route(current_time);
                break;
            }
        }
    }

    void remove_head_until_valid(int [] temp, int current_time, ShortestPathLRU SPC){
        while (temp == null) {
            this.route.removeHead();
            this.size -= 1;
            if (this.size != 1) {
                remove_same_head(current_time);
            }
            if (this.size == 1) {
                size_1_route(current_time);
                return;
            }
            this.current_path = new ArrayList<>();
             temp = SPC.current_loc(this.route.element[0], this.route.getFirstChild().element[0],
                    this.route.element[2], current_time, this.current_path);
        }
        this.route.element[0] = temp[0];
        this.route.element[1] = temp[1];
        this.route.element[2] = temp[1];
        this.route.element[3] = 0;
        this.route.element[4] = 6666;

        this.time = temp[1];
    }

    void print_tour(){
        StringBuilder info = new StringBuilder();
        info.append(this.route.element[0]);
        info.append("(");
        info.append(this.route.element[2]);
        info.append(",");
        info.append(this.route.element[3]);
        info.append(")");
        int i;
        RouteNode next = this.route.getFirstChild();
        for (i=1;i<this.size;i++) {

            int[] aim = next.element;
            next = next.getFirstChild();
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

    void copy_whole_tree(RouteNode new_tree, RouteNode old_tree){
        new_tree.element = old_tree.element.clone();
        if (old_tree.firstChild != null){
            new_tree.firstChild = new RouteNode();
            new_tree.firstChild.parent = new_tree;
            copy_whole_tree(new_tree.firstChild, old_tree.firstChild);
        }
        if (old_tree.nextSibling != null){
            new_tree.nextSibling = new RouteNode();
            new_tree.nextSibling.parent = new_tree.parent;
            copy_whole_tree(new_tree.nextSibling, old_tree.nextSibling);
        }
    }

    void copy_tree_update_arr(RouteNode new_tree, RouteNode old_tree, int detour){
        new_tree.element = old_tree.element.clone();
        new_tree.element[2] += detour;
        if (old_tree.firstChild != null){
            new_tree.firstChild = new RouteNode();
            new_tree.firstChild.parent = new_tree;
            copy_whole_tree(new_tree.firstChild, old_tree.firstChild);
        }
        if (old_tree.nextSibling != null){
            new_tree.nextSibling = new RouteNode();
            new_tree.nextSibling.parent = new_tree.parent;
            copy_whole_tree(new_tree.nextSibling, old_tree.nextSibling);
        }
    }

    void tree_insert_child(RouteNode parent, RouteNode child){
        child.nextSibling = parent.firstChild;
        child.parent = parent;
        parent.firstChild = child;
    }

    boolean insert_end_multi_choice(RouteNode laterNode, ShortestPathLRU SPC, Request request, int dis1){
        boolean has_insert = false;

        int arr1 = laterNode.element[2];
        int dis2, detour;
        boolean has_route_flag2, other_reach_flag, insertable;

        RouteNode endNode = new RouteNode();
        endNode.element = new int[]{request.le, request.td, arr1+dis1, -request.a,
                request.td-arr1-dis1, laterNode.element[5]-request.a};

        RouteNode next = laterNode.firstChild;
        while (next != null){
            dis2 = SPC.dis(request.le, next.element[0]);
            detour = dis1 + dis2 + arr1 - next.element[2];
            has_route_flag2 = dis2 != -1;
            other_reach_flag = detour <= next.element[4];

            insertable = other_reach_flag && has_route_flag2;
            if(insertable){
                has_insert = true;

                RouteNode temp = new RouteNode();
                copy_tree_update_arr(temp, next, detour);
                tree_insert_child(endNode, temp);
            }

            next = next.nextSibling;
        }
        if (has_insert){
            tree_insert_child(laterNode, endNode);
            return true;
        }else {
            return false;
        }
    }

    boolean insert_end_later(RouteNode laterNode, ShortestPathLRU SPC, Request request){
        laterNode.element[5] += request.a;
        int pick1 = laterNode.element[5];
        int arr1 = laterNode.element[2];
        int dis1 = SPC.dis(laterNode.element[0], request.le);

        boolean no_route_flag1 = dis1 == -1;
        boolean s_late_flag = arr1 + dis1 > request.td;
        boolean no_capacity_flag = pick1 > this.capacity;
        if (no_route_flag1 || s_late_flag || no_capacity_flag) {
            return false;
        }
        boolean has_insert_here;
        if (laterNode.firstChild != null) {
            has_insert_here = insert_end_multi_choice(laterNode, SPC, request, dis1);
        }else {
            laterNode.firstChild = new RouteNode();
            RouteNode endNode = laterNode.firstChild;
            endNode.element = new int[]{request.le, request.td, arr1+dis1, -request.a,
                    request.td-arr1-dis1, pick1-request.a};
            endNode.parent = laterNode;
            return true;
        }

        RouteNode next = laterNode.firstChild;
        if (has_insert_here) { next = next.nextSibling; }

        ArrayList<RouteNode> candidates = new ArrayList<>();
        boolean has_insert_child = false;
        while (next != null){
            if (insert_end_later(next, SPC, request)){
                has_insert_child = true;
                candidates.add(next);
            }
            next = next.nextSibling;
        }

        if(has_insert_child) {
            next = laterNode.firstChild;
            if (has_insert_here) {
                next = next.nextSibling;
            }
            for (RouteNode candidate : candidates) {
                next.nextSibling = candidate;
                candidate.nextSibling = null;
                next = candidate;
            }
        }

        return has_insert_here||has_insert_child;
    }

    boolean insert_end(RouteNode sourceNode, RouteNode laterNode, ShortestPathLRU SPC,
                       Request request, int dis1, int dis){
        //check next
        boolean insert_in_next;
        int arr1 = sourceNode.element[2];
        int dis2 = SPC.dis(request.le, laterNode.element[0]);
        int detour = arr1 + dis + dis2 - laterNode.element[2];

        insert_in_next = (dis2 != -1 && detour <= laterNode.element[4]);
        if (insert_in_next){
            RouteNode endNode = new RouteNode();
            endNode.element = new int[]{request.le, request.td, arr1+dis, -request.a,
                    request.tp-arr1, sourceNode.element[5] - request.a};
            endNode.nextSibling = sourceNode.firstChild;
            sourceNode.firstChild = endNode;
            endNode.parent = sourceNode;
            endNode.firstChild = new RouteNode();
            copy_tree_update_arr(endNode.firstChild, laterNode, detour);
        }

        //check all the following
        dis2 = SPC.dis(request.ls, laterNode.element[0]);
        detour = dis2 + arr1 - laterNode.element[2];
        RouteNode laterNodeCopy = new RouteNode();
        copy_tree_update_arr(laterNodeCopy, laterNode, detour);
        laterNodeCopy.nextSibling = null;
        boolean insert_in_later = insert_end_later(laterNodeCopy, SPC, request);
        if (insert_in_later){
            tree_insert_child(sourceNode, laterNodeCopy);
        }
        return insert_in_later || insert_in_next;
    }

    boolean insert_multi_choice(RouteNode new_tree, ShortestPathLRU SPC, Request request, int dis1, int dis){
        boolean has_insert = false;
        int arr1 = new_tree.element[2];
        int dis2, detour;
        boolean has_route_flag2, other_reach_flag, insertable;

        RouteNode sourceNode = new RouteNode();
        sourceNode.element = new int[]{request.ls, request.tp, arr1+dis1, request.a,
                request.tp-arr1-dis1, new_tree.element[5]+request.a};

        RouteNode next = new_tree.firstChild;
        while (next != null){
            dis2 = SPC.dis(request.ls, next.element[0]);
            detour = dis1 + dis2 + arr1 - next.element[2];
            has_route_flag2 = dis2 != -1;
            other_reach_flag = detour <= next.element[4];
            insertable = other_reach_flag && has_route_flag2;
            if(insertable){
                has_insert = has_insert || insert_end(sourceNode, next, SPC, request, dis1, dis);
            }
            //System.out.println("insert "+request.ls+" between "+new_tree.element[0]+" "+next.element[0]);
            next = next.nextSibling;
        }
        if (has_insert){
            tree_insert_child(new_tree, sourceNode);
            return true;
        }else {
            return false;
        }
    }

    boolean insert_source(RouteNode new_tree, ShortestPathLRU SPC, Request request, int dis){
        int arr1 = new_tree.element[2];
        int dis1 = SPC.dis(new_tree.element[0], request.ls);
        int pick1 = new_tree.element[5] + request.a;

        boolean no_route_flag1 = dis1 == -1;
        boolean s_late_flag = arr1 + dis1 > request.tp;
        boolean capacity_flag = pick1 <= this.capacity;

        if (no_route_flag1 || s_late_flag) {
            return false;
        }

        boolean has_insert_here = false;
        if(capacity_flag){
            if (new_tree.firstChild != null) {
                has_insert_here = insert_multi_choice(new_tree, SPC, request, dis1, dis);
            }else {
                new_tree.firstChild = new RouteNode();
                RouteNode sourceNode = new_tree.firstChild;
                sourceNode.element = new int[]{request.ls, request.tp, arr1+dis1, request.a,
                        request.tp-arr1-dis1, pick1};
                sourceNode.parent = new_tree;
                sourceNode.firstChild = new RouteNode();
                RouteNode endNode = sourceNode.firstChild;
                endNode.element = new int[]{request.le, request.td, arr1+dis1+dis, -request.a,
                        request.tp-arr1-dis1, pick1 - request.a};
                endNode.parent = sourceNode;
                return true;
            }
        }

        RouteNode next = new_tree.firstChild;
        if (has_insert_here) { next = next.nextSibling; }

        ArrayList<RouteNode> candidates = new ArrayList<>();
        boolean has_insert_child = false;
        while (next != null){
            if (insert_source(next, SPC, request, dis)){
                has_insert_child = true;
                candidates.add(next);
            }
            next = next.nextSibling;
        }

        if(has_insert_child) {
            next = new_tree.firstChild;
            if (has_insert_here) {
                next = next.nextSibling;
            }
            for (RouteNode candidate : candidates) {
                next.nextSibling = candidate;
                candidate.nextSibling = null;
                next = candidate;
            }
        }

        return has_insert_here||has_insert_child;
    }

    int update_all_slack(RouteNode tree, int depth){
        //System.out.println(tree.element[0]+" "+tree.element[1]);
        if (tree.firstChild == null){
            if (depth!=this.size+2){
                return -2;
            }

            tree.element[4] = tree.element[1] - tree.element[2];
            return tree.element[4];
        }else {
            //System.out.println("child of "+tree.element[0]+" "+tree.element[1]);

            RouteNode child = tree.firstChild;
            tree.element[4] = tree.element[1] - tree.element[2];

            ArrayList<RouteNode> candidates = new ArrayList<>();
            int child_slack = -1;
            while (child != null){
                int slack = update_all_slack(child, depth+1);
                if (slack>=0){
                    child_slack = max(slack, child_slack);
                    candidates.add(child);
                }
                child = child.nextSibling;
            }

            //System.out.println("end of "+tree.element[0]+" "+tree.element[1]);
            if (child_slack >= 0){
                tree.firstChild = null;
                candidates.get(0).nextSibling = null;
                for (RouteNode candidate: candidates){
                    tree_insert_child(tree, candidate);
                }
                tree.element[4] = min(tree.element[4], child_slack);
                return tree.element[4];
            }else {
                return -1;
            }
        }
    }

    int find_earliest_finish(RouteNode tree, int depth){
        if (tree.firstChild == null){
            if (depth!=this.size+2){
                System.out.println("suppose depth "+(this.size+2)+". real depth "+depth);
            }
            return tree.element[2];
        }else {
            RouteNode child = tree.firstChild;
            int finish_time=100000;
            RouteNode fast = new RouteNode();
            while (child != null){
                int child_time = find_earliest_finish(child, depth+1);
                if (child_time<finish_time){
                    fast = child;
                    finish_time = child_time;
                }
                child = child.nextSibling;
            }

            RouteNode temp = new RouteNode();
            RouteNode first_child = tree.firstChild;
            temp.element = fast.element.clone();
            temp.firstChild = fast.firstChild;

            fast.element = first_child.element.clone();
            fast.firstChild = first_child.firstChild;

            first_child.element = temp.element.clone();
            first_child.firstChild = temp.firstChild;

            return finish_time;
        }
    }

    RouteTree insert(Request request, ShortestPathLRU SPC, int dis_){
        RouteTree newTree = new RouteTree();
        newTree.time = this.time;
        newTree.capacity = this.capacity;
        newTree.route = new RouteNode();
        newTree.size = this.size+2;
        newTree.current_path = this.current_path;
        copy_whole_tree(newTree.route, this.route);

        if (insert_source(newTree.route, SPC, request, dis_)){

            if(update_all_slack(newTree.route, 1) == -1){
                return null;
            }
            int earliest_finish = find_earliest_finish(newTree.route, 1);

            if (newTree.route.element[0] == newTree.route.firstChild.element[0]) {
                newTree.remove_same_head(request.tr);
            }
            if (newTree.size == 1){
                newTree.earliest_finish = earliest_finish;
                return newTree;
            }

            newTree.current_path = new ArrayList<>();
            int[] temp = SPC.current_loc(newTree.route.element[0], newTree.route.firstChild.element[0],
                    newTree.route.element[2], request.tr, newTree.current_path);
            newTree.remove_head_until_valid(temp, request.tr, SPC);
            newTree.earliest_finish = earliest_finish;
            return newTree;
        } else {
            return null;
        }
    }
}
