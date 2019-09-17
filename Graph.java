import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Graph {

    /**
     * Node of the graph, has an unique id.
     * Use adjacency list to store edges.
     */
    private class Node {
        int id;
        List<Edge> edges;

        public Node(int id) {
            this.id = id;
            this.edges = new LinkedList<>();
        }

        public void addEdge(Edge edge) {
            edges.add(edge);
        }

        public int getId() {
            return this.id;
        }
    }

    /**
     * Edge of graph, store the capacity and flow.
     * It's tricky to use residual edge to represent residual network.
     */
    private class Edge {
        int capacity;
        int flow;
        Node start;
        Node end;
        Edge residual;

        public Edge(Node start, Node end, int capacity, int flow) {
            this.capacity = capacity;
            this.flow = flow;
            this.start = start;
            this.end = end;
            this.residual = null;
        }

        /**
         * Calculate the residual capacity used in Ford-Fulkerson algorithm.
         * @return residual capacity.
         */
        public int residualCapacity() {
            return capacity - flow;
        }

        public void setResidual(Edge residual) {
            this.residual = residual;
        }
    }

    private List<Node> words;
    private List<Node> cubes;
    private Node start;
    private Node end;
    private int num;

    public Graph() {
        num = 0;
        words = new LinkedList<>();
        cubes = new LinkedList<>();
        start = new Node(num++);
        end = new Node(num++);
    }

    /**
     * Construct graph from input stream which maybe a file.
     * @param in input
     */
    public void init(InputStream in) {
        Scanner sc = new Scanner(in);
        int number = Integer.parseInt(sc.nextLine());
        String[] cubes = new String[number];
        for (int i = 0; i < number; i++) {
            cubes[i] = sc.nextLine();
        }
        String word = sc.nextLine();
        init(cubes, word);
    }

    /**
     * Add edge for the residual graph.
     * @param start the start of edge.
     * @param end the end of edge.
     * @param capacity the capacity of edge.
     * @param flow the initial flow of edge.
     */
    private void addEdge(Node start, Node end, int capacity, int flow) {
        Edge edge = new Edge(start, end, capacity, flow);
        // tricky: use the residual edge to construct residual graph.
        Edge residual = new Edge(end, start, 0, 0);
        edge.setResidual(residual);
        residual.setResidual(edge);
        start.addEdge(edge);
        end.addEdge(residual);
    }

    /**
     * Construct the residual graph.
     * @param cubesStr array of cube string.
     * @param word the target word.
     */
    public void init(String[] cubesStr, String word) {
        // init the word node.
        for (int i = 0; i < word.length(); i++) {
            Node tmp = new Node(num++);
            // create edge between the character and end.
            addEdge(tmp, end, 1, 0);
            words.add(tmp);
        }

        // init the cube node.
        for (int i = 0; i < cubesStr.length; i++) {
            Node cube = new Node(num++);
            for (int j = 0; j < word.length(); j++) {
                // create edge if cube has the character.
                if(cubesStr[i].contains(""+word.charAt(j))) {
                    addEdge(cube, words.get(j), 1, 0);
                }
            }
            // create edge between start and cube.
            addEdge(start, cube, 1, 0);
            cubes.add(cube);
        }

        // init the array used in breath-first search to record the path.
        previous = new Edge[num];
    }

    /**
     * Ford-Fulkerson method used to compute the capacity flow.
     * @return the list of cubes forming the input word separated by space.
     */
    public int[] ffa() {
        // calculate the max flow.
        int flowSum = 0;
        while(true) {
            // use bfs search the path from start to end in residual graph.
            int augment = bfs();
            if(augment == 0) {
                break;
            }
            // array of previous stores the path from start to end.
            for(int i = end.getId(); i != start.getId(); i = previous[i].start.getId()) {
                // send flow along the path.
                previous[i].flow += augment;
                // the flow might be returned later.
                previous[i].residual.flow -= augment;
            }
            flowSum += augment;
        }

        // Judge whether the word can be formed.
        if(flowSum != words.size()) {
            return new int[]{-1};
        } else {
            int wordsLength = words.size();
            int[] ans = new int[wordsLength];
            // Find the correlative cube of each character in the word.
            for(int i=0; i<wordsLength; ++i) {
                Node word = words.get(i);
                for(Edge edge : word.edges) {
                    // The flow between correlative cube and character is -1 in the residual graph.
                    if(edge.flow == -1) {
                        // calculate the id of cube.
                        ans[i] = edge.end.getId() - wordsLength - 1;
                        break;
                    }
                }
            }
            return ans;
        }
    }

    private Edge[] previous;

    public int bfs() {
        Queue<Node> queue = new LinkedList<>();
        queue.offer(start);
        int[] augments = new int[num];
        augments[start.getId()] = Integer.MAX_VALUE;

        while(!queue.isEmpty()) {
            Node cur = queue.poll();
            // iterate each edge from current node.
            for(Edge each : cur.edges) {
                int v = each.end.getId();
                // judge whether the edge is legal.
                if(each.residualCapacity() > 0 && augments[v] == 0) {
                    previous[v] = each;
                    augments[v] = Math.min(augments[each.start.getId()], each.residualCapacity());
                    queue.offer(each.end);
                }

            }
            // judge whether find the path from start to end.
            if(augments[end.getId()] != 0) {
                break;
            }
        }
        return augments[end.getId()];
    }

    public static void output(int[] ans, OutputStream out) {
        PrintStream stream = new PrintStream(out);
        for(int each: ans) {
            stream.print(each + " ");
        }
        stream.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        Graph graph = new Graph();
        // construct the residual graph.
        // Please use the correct path of the input file.
        graph.init(new FileInputStream(new File("C:\\Input.txt")));
        // use Ford-Fulkerson algorithm to judge whether the word can be formed.
        int[] ans = graph.ffa();
        // output the result.
        output(ans, System.out);
    }
}
