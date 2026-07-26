import java.io.*;
import java.util.*;

public class GraphLibrary {
    /**
     * Loads a map from a file where each line is formatted as "key|value"
     * @param fileName path to the file to load
     * @return a map of key-value pairs from the file
     * @throws IOException if the file cannot be read
     */
    public static Map<String,String> loadMap(String fileName) throws IOException {
        Map<String,String> map = new HashMap<>();
        String line;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        }

        while((line = reader.readLine())!=null) {
            String[] parts = line.split("\\|");
            map.put(parts[0], parts[1]);
        }
        reader.close();
        return map;
    }

    /**
     * Builds a graph of actors connected by movies they appeared in together
     * @param fileName path to the movie-actors file
     * @return an undirected graph where vertices are actor names and edges are sets of shared movies
     * @throws IOException if the file cannot be read
     */
    public static Graph<String, Set<String>> createGraph(String fileName) throws IOException {
        String line;
        BufferedReader reader;
        Map<String, String> actorMap = loadMap("PS4/actors.txt");
        Map<String, String> movieMap = loadMap("PS4/movies.txt");
        Graph<String,Set<String>> movieGraph = new AdjacencyMapGraph<>();
        Map<String,Set<String>> movieToActors = new HashMap<>();
        for (String actor : actorMap.values()) {
            movieGraph.insertVertex(actor);
        }
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        }
        while((line = reader.readLine())!=null) {
            String[] parts = line.split("\\|");
            String movieName = movieMap.get(parts[0]);
            String actorName = actorMap.get(parts[1]);
            if (!movieToActors.containsKey(movieName)) {
                movieToActors.put(movieName, new HashSet<>());
            }
            movieToActors.get(movieName).add(actorName);

        }
        reader.close();
        for(String movie:movieToActors.keySet()) {
            List<String> actorList = new ArrayList<>(movieToActors.get(movie));
            for (int i = 0; i<actorList.size();i++) {
                for (int j = i+1; j<actorList.size();j++) {
                    if (movieGraph.hasEdge(actorList.get(i),actorList.get(j))) {
                        movieGraph.getLabel(actorList.get(i),actorList.get(j)).add(movie);
                    }
                    else {
                        Set<String> movies = new HashSet<>();
                        movies.add(movie);
                        movieGraph.insertUndirected(actorList.get(i), actorList.get(j), movies);
                    }
                }
            }
        }

        return movieGraph;
    }
    /**
     * Performs breadth-first search from a source vertex, building the shortest path tree
     * @param g the graph to search
     * @param source the starting vertex (center of the universe)
     * @return a directed graph where each vertex points to its parent in the shortest path tree
     * @throws Exception if the source vertex is not in the graph
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) throws Exception {
        if (!g.hasVertex(source)) throw new Exception("Vertex is not in tree");
        Set<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();
        Graph<V,E> graph = new AdjacencyMapGraph<>();
        graph.insertVertex(source);
        queue.add(source);
        visited.add(source);
        while(!queue.isEmpty()) {
            V vert = queue.remove();
            for(V v: g.outNeighbors(vert)) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    queue.add(v);
                    graph.insertVertex(v);
                    graph.insertDirected(v, vert, g.getLabel(v, vert));
                }
            }
        }
        return graph;
    }
    /**
     * Constructs the shortest path from a vertex back to the root of the tree
     * @param tree the shortest path tree from BFS
     * @param v the starting vertex
     * @return a list of vertices from v to the root
     * @throws Exception if the vertex is not in the tree
     */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v) throws Exception {
        if (!tree.hasVertex(v)) throw new Exception("Vertex is not in tree");
        List<V> path = new LinkedList<>();
        while (tree.outDegree(v) != 0) {
            path.add(v);
            v = tree.outNeighbors(v).iterator().next();
        }
        path.add(v);
        return path;
    }
    /**
     * Finds vertices in the graph that are not reachable from the BFS tree root
     * @param graph the full graph
     * @param subgraph the BFS shortest path tree
     * @return a set of vertices with infinite separation from the root
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
        Set<V> missing = new HashSet<>();
        for (V vert : graph.vertices()) {
            if(!subgraph.hasVertex(vert)) {
                missing.add(vert);
            }
        }
        return missing;
    }
    /**
     * Computes the average separation of all reachable vertices from the root
     * @param tree the shortest path tree from BFS
     * @param root the center of the universe
     * @return the average number of edges from the root to all reachable vertices
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root){
        if (!tree.hasVertex(root)) return -1;
        return count(root,0,tree)/(tree.numVertices()-1);
    }
    /**
     * Recursively sums all depths in the tree for use in averageSeparation
     * @param root current vertex being processed
     * @param depth current depth from the center
     * @param tree the shortest path tree
     * @return total sum of depths of all vertices in the subtree
     */
    public static <V,E> double count(V root,double depth,Graph<V,E> tree) {
        double total = depth;
        for (V vert : tree.inNeighbors(root)) {
            total += count(vert,depth+1,tree);
        }
        return total;
    }
    /**
     * The game. Returns void.
     */
    public static void playGame() throws Exception {
        String s = "Commands:\nc <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation\nd <low> <high>: list actors sorted by degree, with degree between low and high\ni: list actors with infinite separation from the current center\np <name>: find path from <name> to current center of the universe\ns <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high\nu <name>: make <name> the center of the universe\nq: quit game";
            System.out.println(s);
            Graph<String, Set<String>> graph = createGraph("PS4/movie-actors.txt");
            String source = "Kevin Bacon";
            Graph<String, Set<String>> tree = bfs(graph, source);
        System.out.println(source+" is now the center of the acting universe, connected to "+tree.numVertices()+"/"+graph.numVertices()+" actors with average separation "+averageSeparation(tree,source));

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String command = line.substring(0, 1);
                String argument = "";
                if (line.length() > 2) argument = line.substring(2);

                if (command.equals("c")) {
                    if (!argument.matches("-?\\d+")) {
                        System.out.println("Not an integer.");
                        playGame();
                    }
                    int n = Integer.parseInt(argument);
                    Map<String, Double> separations = new HashMap<>();
                    for (String actor : graph.vertices()) {
                        try {
                            Graph<String, Set<String>> actorTree = bfs(graph, actor);
                            if (actorTree.numVertices() > 1) {
                                separations.put(actor, averageSeparation(actorTree, actor));
                            }
                        } catch (Exception e) {
                            System.out.println("Could not compute separation for " + actor);
                            playGame();
                        }
                    }
                    List<String> sorted = new ArrayList<>(separations.keySet());

                    if (n < 0) {
                        sorted.sort((e1, e2) -> Double.compare(separations.get(e1), separations.get(e2)));
                        System.out.println("Here are the " + Math.abs(n) + " bottom average separations:");
                        playGame();
                    } else {
                        sorted.sort((e1, e2) -> Double.compare(separations.get(e2), separations.get(e1)));
                        System.out.println("Here are the " + Math.abs(n) + " top average separations:");
                    }

                    for (int i = 0; i < Math.abs(n) && i < sorted.size(); i++) {
                        System.out.println(i + 1 + ": " + sorted.get(i) + " (" + separations.get(sorted.get(i)) + ")");
                    }
                    playGame();
                } else if (command.equals("d")) {
                    String[] parts = argument.split(" ");
                    try {
                        int low = Integer.parseInt(parts[0]);
                        int high = Integer.parseInt(parts[1]);
                        if (low > high) {
                            System.out.println("Wrong order, try again.");
                            playGame();
                        }
                        List<String> actorDegrees = new ArrayList<>();
                        for (String actor : graph.vertices()) {
                            if (graph.outDegree(actor) >= low && graph.outDegree(actor) <= high) {
                                actorDegrees.add(actor);
                            }
                        }
                        actorDegrees.sort((a1, a2) -> graph.outDegree(a1) - graph.outDegree(a2));
                        System.out.println("Here are the actors between degrees " + low + " and " + high + ".");
                        System.out.println(actorDegrees);
                        playGame();
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input, please enter two numbers.");
                        playGame();
                    }

                } else if (command.equals("i")) {
                    System.out.println(missingVertices(graph, tree));
                    System.out.println("\n");
                } else if (command.equals("p")) {
                    try {
                        List<String> path = getPath(tree, argument);
                        System.out.println(argument + "'s number is " + (path.size() - 1));
                        for (int i = 0; i < path.size() - 1; i++) {
                            String actor = path.get(i);
                            String next = path.get(i + 1);
                            Set<String> movies = tree.getLabel(actor, next);
                            System.out.println(actor + " appeared in " + movies + " with " + next);
                        }
                    } catch (Exception e) {
                        System.out.println("Actor not found.");
                    }
                } else if (command.equals("s")) {
                    String[] parts = argument.split(" ");
                    try {
                        int low = Integer.parseInt(parts[0]);
                        int high = Integer.parseInt(parts[1]);
                        if (low > high) {
                            System.out.println("Wrong order, try again.");
                            playGame();
                        }
                        List<String> actorPath = new ArrayList<>();
                        for (String actor : tree.vertices()) {
                            int sep = getPath(tree, actor).size() - 1;
                            if (sep >= low && sep <= high) {
                                actorPath.add(actor);
                            }
                        }
                        final Graph<String, Set<String>> finalTree = tree;
                        actorPath.sort((a1, a2) -> {
                            try {
                                return getPath(finalTree, a1).size() - getPath(finalTree, a2).size();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                        System.out.println("Here are the actors' path sizes between " + low + " and " + high + ".");
                        System.out.println(actorPath);
                        playGame();
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input, please enter two numbers.");
                    }


                } else if (command.equals("u")) {
                    source = argument;
                    tree = bfs(graph, source);
                    System.out.println(source + " is now the center of the acting universe, connected to " + tree.numVertices() + "/" + graph.numVertices() + " actors with average separation " + averageSeparation(tree, source));
                } else if (command.equals("q")) {
                    System.out.println("Goodbye!");
                    return;
                }
            }
        }

    public static void main(String[] args) {
        try {
            playGame();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
