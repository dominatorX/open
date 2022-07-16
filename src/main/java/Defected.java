import java.util.*;

class Defected {
    Comparator<CHSP.Vertex> PQcomp = new CHSP.PriorityQueueComp();
    PriorityQueue<CHSP.Vertex> queue;

    public void contractNode(CHSP.Vertex[] graph, CHSP.Vertex vertex, int contractId){
        ArrayList<Integer> inEdges = vertex.inEdges;
        ArrayList<Integer> inECost = vertex.inECost;
        ArrayList<Integer> outEdges = vertex.outEdges;
        ArrayList<Integer> outECost = vertex.outECost;

        vertex.contracted=true;

        int inMax = 0;						//stores the max distance out of uncontracted inVertices of the given vertex.
        int outMax = 0;						//stores the max distance out of uncontracted outVertices of the given vertex.

        for(int i=0; i<inECost.size();i++){
            if(graph[inEdges.get(i)].contracted){
                continue;
            }
            if(inMax<inECost.get(i)){
                inMax = inECost.get(i);
            }
        }

        for(int i=0; i<outECost.size();i++){
            if(graph[outEdges.get(i)].contracted){
                continue;
            }
            if(outMax<outECost.get(i)){
                outMax = outECost.get(i);
            }
        }

        int max = inMax+outMax; 				//total max distance.

        for(int i=0;i<inEdges.size();i++){
            int inVertex = inEdges.get(i);

            int incost = inECost.get(i);

            dijkstra(graph,inVertex,max,contractId,i); 	//finds the shortest distances from the inVertex to all the outVertices.

            for(int j=0;j<outEdges.size();j++){
                int outVertex = outEdges.get(j);
                int outcost = outECost.get(j);
                if(graph[outVertex].distance.contractId != contractId || graph[outVertex].distance.sourceId != i ||graph[outVertex].distance.distance>incost+outcost) {
                    vertex.contracted=false;
                    return;
                }
            }
        }
    }

    private void dijkstra(CHSP.Vertex[] graph, int source, int maxcost, int contractId, int sourceId){
        this.queue = new PriorityQueue<>(graph.length,PQcomp);

        graph[source].distance.distance = 0;
        graph[source].distance.contractId = contractId;
        graph[source].distance.sourceId = sourceId;

        queue.clear();
        queue.add(graph[source]);

        while(queue.size()!=0){
            CHSP.Vertex vertex = queue.poll();
            if(vertex.distance.distance > maxcost){
                return;
            }
            relaxEdges(graph,vertex.vertexNum,contractId,queue,sourceId);
        }
    }

    private void relaxEdges(CHSP.Vertex[] graph, int vertex, int contractId, PriorityQueue<CHSP.Vertex> queue, int sourceId){
        ArrayList<Integer> vertexList = graph[vertex].outEdges;
        ArrayList<Integer> costList = graph[vertex].outECost;

        for(int i=0;i<vertexList.size();i++){
            int temp = vertexList.get(i);
            int cost = costList.get(i);
            if(graph[temp].contracted){
                continue;
            }
            if(checkId(graph,vertex,temp) || graph[temp].distance.distance > graph[vertex].distance.distance + cost){
                graph[temp].distance.distance = graph[vertex].distance.distance + cost;
                graph[temp].distance.contractId = contractId;
                graph[temp].distance.sourceId = sourceId;

                queue.remove(graph[temp]);
                queue.add(graph[temp]);
            }
        }
    }

    private boolean checkId(CHSP.Vertex[] graph, int source, int target) {
        return graph[source].distance.contractId != graph[target].distance.contractId || graph[source].distance.sourceId != graph[target].distance.sourceId;
    }
}
