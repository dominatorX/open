import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class PartialSC {
    int[] score;
    ArrayList<HashMap<Integer, Integer>> alter;
    ArrayList<HashSet<Integer>> MS;
    ArrayList<Boolean> defect;
    int[] V_cost;
    int cover_k;
    private int min_cost=Integer.MAX_VALUE;
    private boolean[] best_SC;
    HashSet<ArrayList<Integer>> tasks = new HashSet<>();
    int processed = 0;

    public static class size_com implements Comparator<Pair<HashSet<Integer>,Integer>>{
        public int compare(Pair<HashSet<Integer>,Integer> node1, Pair<HashSet<Integer>,Integer> node2){
            return (Integer.compare(node2.getKey().size(), node1.getKey().size()));
        }
    }

    public static class dir_com implements Comparator<Pair<Integer,Integer>>{
        public int compare(Pair<Integer,Integer> node1, Pair<Integer,Integer> node2){
            return (Integer.compare(node2.getKey(), node1.getKey()));
        }
    }

    int Primal_Dual(boolean[] t_SC, int c_idx, final ArrayList<Integer> popped){
        // Sj are all the vertices except
        // Tj are all the elements except alter.get(c_idx)
        // kj is defect.size()-alter.get(c_idx).size
        // Let's start the dual
        int covered = 0;
        int set_used = 0;
        // double current_dual = 0.0;
        boolean[] set_left = new boolean[this.defect.size()];
        Arrays.fill(set_left, false);
        HashMap<Integer, HashSet<Integer>> t_MS = new HashMap<>();
        for(int i:popped){
            set_left[i] = true;
            t_MS.put(i, (HashSet<Integer>)MS.get(i).clone());
        }
        boolean[] ele_cover = new boolean[this.defect.size()];
        Arrays.fill(ele_cover, false);

        //Comparator<Pair<HashSet<Integer>,Integer>> comp = new size_com();
        Comparator<Pair<Integer,Integer>> comp = new dir_com();
        //PriorityQueue<Pair<HashSet<Integer>, Integer>> maxheap = new PriorityQueue<>(this.score.length, comp);
        PriorityQueue<Pair<Integer,Integer>> maxheap = new PriorityQueue<>(this.defect.size(), comp);

        // Sj is covered
        set_used += 1;
        set_left[c_idx] = false;
        t_SC[c_idx] = true;
        for (Integer e : t_MS.get(c_idx)) {
            covered++;
            ele_cover[e] = true;
            for (Integer served : this.alter.get(e).keySet()) {
                if (set_left[served]) t_MS.get(served).remove(e);
            }
        }

        for (int i:popped){
            if(i!=c_idx && !t_MS.get(i).isEmpty())maxheap.add(new Pair<>(t_MS.get(i).size(), i));
        }
        //t_MS.get(2).remove(5);
        //System.out.println(t_MS.get(2).size());
        //System.out.println(t_MS.get(5).size());
        //Pair<HashSet<Integer>, Integer> temp;
        Pair<Integer, Integer> temp;
        while (covered<cover_k) {
            //?how to implement in a fast way... naive method
            // We can keep a min heap according to its size
            // each round, all remaining set without max size are removable
            // in one round, check all set without intersection
            // mark the elements covered
            // go through MC to update
            temp = maxheap.poll();
            set_left[temp.getValue()] = false;
            if (temp.getKey() == 0) throw new ArithmeticException();
            // current_dual += 1.0/temp.getKey().size();
            set_used += 1;
            t_SC[temp.getValue()] = true;
            for (Integer e : t_MS.get(temp.getValue())) {
                covered++;
                ele_cover[e] = true;
                for (Integer served : this.alter.get(e).keySet()) {
                    if (set_left[served]) {
                        int old = t_MS.get(served).size();
                        maxheap.remove(new Pair<>(old, served));
                        t_MS.get(served).remove(e);
                        maxheap.add(new Pair<>(old-1, served));
                    }
                }
            }
        }
        V_cost[c_idx] = set_used;
        return set_used;
    }

    boolean[] PSC(double cover) {
        boolean[] SC = new boolean[this.defect.size()];
        Arrays.fill(SC, true);

        cover_k = (int)Math.ceil(this.defect.size()*cover);
        V_cost = new int[defect.size()];
        Arrays.fill(V_cost, Integer.MAX_VALUE);

        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();

        PriorityQueue<Pair<Integer, Integer>> minheap = new PriorityQueue<>(score.length, comp);
        for(int i=0;i<score.length;i++){
            if(!defect.get(i)) minheap.add( new Pair<>(-score[i], i) );
        }

        int cost = Integer.MAX_VALUE;
        ArrayList<Integer> popped = new ArrayList<>();
        HashSet<Integer> can_cover = new HashSet<>();
        int heapsize = minheap.size();
        int processed = 0;
        while (!minheap.isEmpty()){
            processed ++;
            if(processed % 5000==0) System.out.println("PSC processing "+processed+":"+heapsize);
            Pair<Integer,Integer> c_pair = minheap.poll();
            int c_id = c_pair.getValue();
            popped.add(c_id);
            can_cover.addAll(this.MS.get(c_id));
            if(can_cover.size()>=cover_k) {
                boolean[] t_SC = new boolean[this.defect.size()];
                Arrays.fill(t_SC, false);
                int c_cost = Primal_Dual(t_SC, c_id, popped);
                if (c_cost < cost) {
                    System.out.println("success for round "+c_id);
                    /*for (int id=0; id<t_SC.length;id++){
                        if(t_SC[id]) System.out.print(id+" ");
                    }*/
                    System.out.println(processed+ " vertices processed. We need "+c_cost+" cores.");
                    cost = c_cost;
                    SC = t_SC;
                }
            }
        }
        return SC;
        //json.dump(result, open("./NYC/lp_result.json", "w"))
    }

    boolean[] PSC_Para(double cover) {
        boolean[] SC = new boolean[this.defect.size()];
        Arrays.fill(SC, true);

        cover_k = (int)Math.ceil(this.defect.size()*cover);
        V_cost = new int[defect.size()];
        Arrays.fill(V_cost, Integer.MAX_VALUE);

        Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();

        PriorityQueue<Pair<Integer, Integer>> minheap = new PriorityQueue<>(score.length, comp);
        for(int i=0;i<score.length;i++){
            if(!defect.get(i)) minheap.add( new Pair<>(-score[i], i) );
        }
        HashSet<Integer> can_cover = new HashSet<>();
        ArrayList<Integer> popped = new ArrayList<>();
        /*
        //serialized
        while (!minheap.isEmpty()){
            processed ++;
            if(processed % 5000==0) System.out.println("PSC processing "+processed);
            Pair<Integer,Integer> c_pair = minheap.poll();
            int c_id = c_pair.getValue();
            popped.add(c_id);
            can_cover.addAll(this.MS.get(c_id));
            if(can_cover.size()>=cover_k) {
                boolean[] t_SC = new boolean[alter.size()];
                Arrays.fill(t_SC, false);
                int c_cost = Primal_Dual(t_SC, c_id, (ArrayList<Integer>)popped.clone());
                if(c_cost<min_cost) {
                    best_SC = t_SC;
                    min_cost = c_cost;
                }
            }
        }
        */
        /*
        int one_round = 1000;
        ForkJoinPool customThreadPool = new ForkJoinPool(10);
        System.out.println(minheap.size());
        while (!minheap.isEmpty()) {
            Pair<Integer, Integer> c_pair = minheap.poll();
            int c_id = c_pair.getValue();
            popped.add(0, c_id);
            can_cover.addAll(this.MS.get(c_id));
            if (can_cover.size() >= cover_k) {
                tasks.add((ArrayList<Integer>) popped.clone());
            }
            if (tasks.size() >= one_round){
                customThreadPool.submit(()->tasks.parallelStream().forEach(task->{
                    new single_(task.get(0), task);
                }));
                customThreadPool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                tasks = new HashSet<>();
                processed += one_round;
                System.out.println("PSC processing "+processed);
            }
        }
        if (!tasks.isEmpty()){
            customThreadPool.submit(()->tasks.parallelStream().forEach(task->{
                new single_(task.get(0), task);
            }));
            customThreadPool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        */
        // parallel
        //System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        while (!minheap.isEmpty()){
            processed ++;
            Pair<Integer,Integer> c_pair = minheap.poll();
            int c_id = c_pair.getValue();
            popped.add(c_id);
            can_cover.addAll(this.MS.get(c_id));
            if(can_cover.size()>=cover_k) {
                if(processed % 5000==0) System.out.println("PSC processing "+processed);
                executor.execute(new single(c_id, (ArrayList<Integer>)popped.clone()));
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }

        return best_SC.clone();


    }

    class single_  {
        boolean[] t_SC;
        int c_id;
        ArrayList<Integer> popped;

        private single_(int c_id, ArrayList<Integer> popped) {
            this.t_SC = new boolean[alter.size()];
            Arrays.fill(t_SC, false);
            this.c_id = c_id;
            this.popped = popped;
            int c_cost = Primal_Dual(t_SC, c_id, popped);
            update(c_cost);
        }

        private synchronized void update(int cost) {
            if(cost<min_cost) {
                best_SC = t_SC.clone();
                min_cost = cost;
            }
        }
    }

    class single implements Runnable {
        boolean[] t_SC;
        int c_id;
        ArrayList<Integer> popped;

        private single(int c_id, ArrayList<Integer> popped) {
            this.t_SC = new boolean[alter.size()];
            Arrays.fill(t_SC, false);
            this.c_id = c_id;
            this.popped = popped;
        }

        private synchronized void update(int cost) {
            if(cost<min_cost) {
                best_SC = t_SC.clone();
                min_cost = cost;
            }
            processed ++;
            if(processed % 5000==0) System.out.println("PSC processing "+processed);
        }

        @Override
        /** Override the run() method to tell the system
         * what task to perform
         */
        public void run() {
            int c_cost = Primal_Dual(t_SC, c_id, popped);
            update(c_cost);
        }
    }


}
