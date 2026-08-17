package soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecordingRaw;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecordingRaw;
import static soloMapling.DebugUtilities.debugprint;

public class MapGraph {

    final int mapId;
    boolean drawGraphDot = false;
    private Graph<String, NamedEdge> graph;
    private List<String> mainAreas = new ArrayList<>();
    private List<String> connectors = new ArrayList<>();
    File directory;

    public MapGraph(int mapId) {
        this.mapId = mapId; // convertFMMapDirectoryID(mapId);
        this.directory = new File("src/main/java/soloMapling/ArtificialPlayer/BotMovementSystem/movementDataPackets/map" + this.mapId);
        this.setMainAreas();
        this.setConnectors();
        this.buildGraph();
    }

    //

//    private static final int[][] FM_MAP_RANGES = {
//            {910000001, 910000006, 910000001},
//            {910000007, 910000012, 910000007},
//            {910000013, 910000017, 910000013},
//            {910000018, 910000022, 910000018}
//    };
//
//    private static int convertFMMapDirectoryID(int mapId) {
//        for (int[] range : FM_MAP_RANGES) {
//            if (mapId >= range[0] && mapId <= range[1]) {
//                return range[2];
//            }
//        }
//        return mapId; // not FM Map, return given
//    }

    public List<String> getMainAreaOfPoint(Point pt) {
        return getElementsContainingPoint(getMainAreas(), pt);
    }

    public List<String> getConnectorOfPoint(Point pt) {
        return getElementsContainingPoint(getConnectors(), pt);
    }

    private List<String> getElementsContainingPoint(List<String> elements, Point pt) {
        List<String> elementsWithPoint = new ArrayList<>();
        for (String element : elements) {
            try {
                MovementRecordingRaw mcr = getMovementRecordingRaw(getMapId(), element);
                if (mcr.isPointWithinRecording(pt)) {
                    elementsWithPoint.add(element);
                }
            } catch (Exception e) {
                // Handle exception, perhaps log it
                System.err.println("Error processing element: " + element);
                e.printStackTrace();
            }
        }
        return elementsWithPoint;
    }

    //

    public static List<String> getPathChain(GraphPath<String, NamedEdge> graph) {
        if (!isValidGraph(graph)) {
            return new ArrayList<>();
        }

        List<String> pathChain = new ArrayList<>();
        pathChain.add(graph.getVertexList().get(0));
        for (int i = 0; i < graph.getEdgeList().size(); i++) {
            NamedEdge edge = graph.getEdgeList().get(i);
            String nextVertex = graph.getVertexList().get(i + 1);
            pathChain.add(edge.toString());
            pathChain.add(nextVertex);
        }
        return pathChain;
    }

    /**
     * Soft-validates a GraphPath. Returns false and debug-logs on any problem so the
     * caller can skip the path gracefully. A null / malformed graph just means the bot
     * won't walk for this tick — not a game-breaking condition, so no exceptions.
     */
    private static boolean isValidGraph(GraphPath<String, NamedEdge> graph) {
        if (graph == null) {
            debugprint("MapGraph.getPathChain: graph is null, skipping.");
            return false;
        }
        if (graph.getVertexList() == null || graph.getEdgeList() == null) {
            debugprint("MapGraph.getPathChain: vertex or edge list is null, skipping.");
            return false;
        }
        if (graph.getVertexList().size() != graph.getEdgeList().size() + 1) {
            debugprint("MapGraph.getPathChain: vertex/edge size mismatch, skipping. "
                    + "vertices=" + graph.getVertexList().size()
                    + ", edges=" + graph.getEdgeList().size());
            return false;
        }
        return true;
    }

    int getMapId() {
        return this.mapId;
    }

    public List<String> getMainAreas() {
        return mainAreas;
    }

    public List<String> getConnectors() {
        return connectors;
    }

    public int getNumMainAreas() {
        return getMainAreas().size();
    }

    public void setMainAreas() {
        if (!directory.exists()) {
            debugprint("MapGraph: no movement-packet dir for map " + mapId);
            return;
        }

        File[] files = directory.listFiles();
        HashSet<String> uniqueMainAreas = new HashSet<>();

        for (File file : files) {
            String fileName = file.getName();
            // Check if the file is a main area file (e.g., m1.bin, m2.csv)
            if (fileName.startsWith("m") && fileName.contains(".")) {
                int dotIndex = fileName.indexOf('.');
                String mainArea = fileName.substring(0, dotIndex);
                uniqueMainAreas.add(mainArea);
            }
        }

        // Convert the set to a list and clear the existing list before adding
        mainAreas.clear();
        mainAreas.addAll(uniqueMainAreas);
    }

    public void setConnectors() {
        if (!directory.exists()) {
            debugprint("MapGraph: no movement-packet dir for map " + mapId);
            return;
        }

        File[] files = directory.listFiles();
        HashSet<String> uniqueConnectors = new HashSet<>();

        for (File file : files) {
            String fileName = file.getName();
            // Check if the file is a connector file (e.g., c1-4.bin, c4-1.csv)
            if (fileName.startsWith("c") && fileName.contains("-")) {
                int dotIndex = fileName.indexOf('.');
                String connector = fileName.substring(0, dotIndex);
                uniqueConnectors.add(connector);
            }
        }

        // Convert the set to a list and clear the existing list before adding
        connectors.clear();
        connectors.addAll(uniqueConnectors);
    }

    public void addVertexFromMainAreas() {
        for (String node : getMainAreas()) {
            graph.addVertex(node);
        }
    }

    public void addEdgesFromConnectors() {
        for (String connector : getConnectors()) {
            // Remove the 'c' prefix
            String withoutPrefix = connector.substring(1);

            // Split by hyphen to get the node numbers and optional suffix
            String[] parts = withoutPrefix.split("-");

//            debugprint("withoutPrefix", withoutPrefix, "parts", Arrays.stream(parts).toList());

            // Get start and end nodes
            String startNode = "m" + parts[0];

            // The end node might have a suffix (a or b), so remove it
            String endNode = "m" + parts[1].replaceAll("[abcdefgh]$", "");

            // Remove "_social" notation
            endNode = endNode.replaceAll("_social", "");

//            debugprint("startnode", startNode, "endnode", endNode);

            // Verify and get the actual node names from mainAreas
            String actualStartNode = findActualNodeName(startNode);
            String actualEndNode = findActualNodeName(endNode);

            if (actualStartNode == null) {
                debugprint("WARNING: Start node not found in mainAreas:", startNode);
                continue; // Skip this connector
            }

            if (actualEndNode == null) {
                debugprint("WARNING: End node not found in mainAreas:", endNode);
                continue; // Skip this connector
            }

//            debugprint("Verified nodes - start:", actualStartNode, "end:", actualEndNode);

            // Add the edge to the graph using the actual node names
            addNamedEdge(graph, actualStartNode, actualEndNode, connector);
        }
    }

    /**
     * Finds the actual node name in mainAreas.
     * Matches exact names first, then checks for names with suffixes like "_social".
     *
     * @param nodeName The node name to search for (e.g., "m4", "m5")
     * @return The actual node name from mainAreas, or null if not found
     */
    private String findActualNodeName(String nodeName) {
        // First, check for exact match
        for (String mainArea : getMainAreas()) {
            if (mainArea.equals(nodeName)) {
                return mainArea;
            }
        }

        // If no exact match, check for names with suffixes (e.g., "m5_social")
        for (String mainArea : getMainAreas()) {
            if (mainArea.startsWith(nodeName + "_")) {
                return mainArea;
            }
        }

        // Not found
        return null;
    }

    public void buildGraph() {
        this.graph = new DirectedMultigraph<>(NamedEdge.class);
        addVertexFromMainAreas();
        addEdgesFromConnectors();

//        printGraphNodesVertices();
        if (drawGraphDot) {
            drawGraph(graph);
        }
    }

    public List<GraphPath<String, NamedEdge>> getAllPaths(int startNode, int endNode, int maxPathLength) {
        String start = "m" + startNode;
        String end = "m" + endNode;
        return getAllPaths(start, end, maxPathLength);
    }

    public List<GraphPath<String, NamedEdge>> getAllPaths(String startNode, String endNode, int maxPathLength) {
        if (this.graph == null) {
            buildGraph();
        }

        AllDirectedPaths<String, NamedEdge> allPaths = new AllDirectedPaths<>(graph);
        try {
            return allPaths.getAllPaths(startNode, endNode, true, maxPathLength);
        } catch (Exception e) {
            return null;
        }
    }

    public GraphPath<String, NamedEdge> getShortestPath(String startNode, String endNode) {
        if (graph == null) {
            buildGraph();
        }

        DijkstraShortestPath<String, NamedEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        return dijkstraAlg.getPath(startNode, endNode);
    }

    public GraphPath<String, NamedEdge> getRandomPath(String startNode, String endNode) {
        return getRandomPath(startNode, endNode, false);
    }

    public GraphPath<String, NamedEdge> getRandomPath(String startNode, String endNode, Boolean removeMaxLengthPaths) {
        if (graph == null) {
            buildGraph();
        }
        List<GraphPath<String, NamedEdge>> allPaths = getAllPaths(startNode, endNode, 10);
//        debugprint("Before removeMaxLength Paths");
//        for (GraphPath<String, NamedEdge> path : allPaths) {
//            debugprint("Path: ", path);
//        }

        if (removeMaxLengthPaths) {
            allPaths = removeMaxLengthPaths(allPaths);
//            debugprint("After removeMaxLength Paths");
//            for (GraphPath<String, NamedEdge> path : allPaths) {
//                debugprint("Path: ", path);
//            }
        }

        try {
            return allPaths.get(new java.util.Random().nextInt(allPaths.size()));
        } catch (Exception e) {
            return null;
        }
    }

    private List<GraphPath<String, NamedEdge>> removeMaxLengthPaths(List<GraphPath<String, NamedEdge>> allPaths) {
        // If list is empty, return empty list
        if (allPaths == null) {
            return null;
        }
        if (allPaths.isEmpty()) {
            return new ArrayList<>();
        }
        if (allPaths.size() == 1) {
            return allPaths;
        }

        // Find the maximum path length
        int maxLength = allPaths.stream()
                .mapToInt(path -> path.getVertexList().size())
                .max()
                .getAsInt();

        // Check if all paths have the same length
        boolean allSameLength = allPaths.stream()
                .allMatch(path -> path.getVertexList().size() == maxLength);

        // If all paths are the same length, return the original list
        if (allSameLength) {
            return new ArrayList<>(allPaths);
        }

        // Otherwise, filter out paths with maximum length, keeping all others
        return allPaths.stream()
                .filter(path -> path.getVertexList().size() < maxLength)
                .collect(Collectors.toList());
    }


    // todo getShortestDurationPath
    // shortest path based on time duration of connectors & main areas

    // Helper method to add a named edge
    private void addNamedEdge(Graph<String, NamedEdge> graph,
                              String source,
                              String target,
                              String edgeName) {
        NamedEdge edge = new NamedEdge(edgeName);
        graph.addEdge(source, target, edge);
    }

    public void drawGraph(Graph<String, NamedEdge> graph) {
        // View graph.dot using this url:
        // https://dreampuf.github.io/GraphvizOnline/

        // Create DOT exporter
        DOTExporter<String, NamedEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) ->
                Map.of("label", DefaultAttribute.createAttribute(v)));
        exporter.setEdgeAttributeProvider((e) ->
                Map.of("label", DefaultAttribute.createAttribute(e.toString())));

        // Export to DOT file
        try {
            Writer writer = new FileWriter("graph.dot");
            exporter.exportGraph(graph, writer);

            // Also print to console
            StringWriter stringWriter = new StringWriter();
            exporter.exportGraph(graph, stringWriter);
            System.out.println("DOT format output:");
            System.out.println(stringWriter.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printGraphNodesVertices() {
        // Print the graph structure
        System.out.println("Vertices: " + graph.vertexSet());
        System.out.println("Edges: " + graph.edgeSet());

        // Print each vertex and its connected vertices
        for (String vertex : graph.vertexSet()) {
            System.out.println("\nConnections for " + vertex + ":");
            System.out.println("Outgoing edges: " + graph.outgoingEdgesOf(vertex));
            System.out.println("Incoming edges: " + graph.incomingEdgesOf(vertex));
        }
    }

}

class NamedEdge {
    private String name;

    public NamedEdge(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}