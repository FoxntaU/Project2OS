import kareltherobot.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;


class Racer extends Robot {
    private int number;
    private String id;
    private int maxBeepers;
    private int[] deliveryPosition;
    private int currentStreet;
    private int currentAvenue;
    private int startStreet;
    private int startAvenue;
    private int beepersInTheBag;

    public Racer(int street, int avenue, Direction direction, int beepers, Color color, int maxBeepers, String id, int number) {
        super(street, avenue, direction, beepers, color);
        this.id = id;
        this.number = number;
        this.maxBeepers = maxBeepers;
        this.deliveryPosition = new int[] { maxBeepers, 1 };
        this.currentStreet = street;
        this.currentAvenue = avenue;
        this.startStreet = street;
        this.startAvenue = avenue;
        this.beepersInTheBag = 0;
        World.setupThread(this);
    }

    public void moveToLocation(int targetStreet, int targetAvenue, KarelWorldParser map) {
        int[] start = {currentStreet, currentAvenue};
        int[] goal = {targetStreet, targetAvenue};
        List<int[]> path = map.bfs(start, goal);

        if (path != null) {
            for (int[] step : path) {
                moveToExactLocation(step[0], step[1]);
            }
        }
    }

    private void moveToExactLocation(int street, int avenue) {
        moveToStreet(street);
        moveToAvenue(avenue);
    }

    private void moveToStreet(int targetStreet) {
        while (currentStreet != targetStreet) {
            if (currentStreet < targetStreet) {
                turnTo("NORTH");
            } else {
                turnTo("SOUTH");
            }
            move();
        }
    }

    private void moveToAvenue(int targetAvenue) {
        while (currentAvenue != targetAvenue) {
            if (currentAvenue < targetAvenue) {
                turnTo("EAST");
            } else {
                turnTo("WEST");
            }
            move();
        }
    }

    private void turnTo(String direction) {
        while (!facingDirection(direction)) {
            turnLeft();
        }
    }

    private boolean facingDirection(String direction) {
        switch (direction) {
            case "NORTH": return facingNorth();
            case "SOUTH": return facingSouth();
            case "EAST":  return facingEast();
            case "WEST":  return facingWest();
            default: return false;
        }
    }
}

class RobotFactory implements Directions {
    public static Racer[] createRobots(String[] args) {
        List<Color> colores = Arrays.asList(Color.blue, Color.red, Color.green, Color.yellow);
        Racer[] racers = new Racer[4];
        for (int i = 0; i < 4; i++) {
            racers[i] = new Racer(1, i + 1, East, 0, colores.get(i), 10, "Robot" + i, i);
        }
        return racers;
    }
}

class KarelWorldParser {
    public boolean[][] matrix = new boolean[20][20];

    public KarelWorldParser(String worldText) {
        parseWorld(worldText);
    }

    private void parseWorld(String worldText) {
        String[] tokens = worldText.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("eastwestwalls")) {
                int street = Integer.parseInt(tokens[i + 1]) - 1;
                int avenue = Integer.parseInt(tokens[i + 2]) - 1;
                fillMatrix(street, avenue);
            } else if (tokens[i].equals("northsouthwalls")) {
                int avenue = Integer.parseInt(tokens[i + 1]) - 1;
                int street = Integer.parseInt(tokens[i + 2]) - 1;
                fillMatrix(street, avenue);
            }
        }
        printMatrix();
    }

    public void fillMatrix(int street, int avenue) {
        matrix[avenue][street] = true;
    }

    public void printMatrix() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (matrix[i][j]) {
                    System.out.print("X ");
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
    }

    public List<int[]> bfs(int[] start, int[] goal) {
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        boolean[][] visited = new boolean[20][20];
        Queue<int[]> queue = new LinkedList<>();
        Map<String, int[]> parent = new HashMap<>();

        queue.add(start);
        visited[start[0]][start[1]] = true;
        parent.put(start[0] + "," + start[1], null);

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            if (Arrays.equals(current, goal)) {
                return reconstructPath(parent, current);
            }

            for (int[] dir : directions) {
                int[] next = {current[0] + dir[0], current[1] + dir[1]};
                if (isValidMove(next, visited)) {
                    queue.add(next);
                    visited[next[0]][next[1]] = true;
                    parent.put(next[0] + "," + next[1], current);
                }
            }
        }
        return null;
    }

    private boolean isValidMove(int[] position, boolean[][] visited) {
        return position[0] >= 0 && position[0] < 20 && position[1] >= 0 && position[1] < 20 &&
               !visited[position[0]][position[1]] && !matrix[position[0]][position[1]];
    }

    private List<int[]> reconstructPath(Map<String, int[]> parent, int[] goal) {
        List<int[]> path = new LinkedList<>();
        for (int[] at = goal; at != null; at = parent.get(at[0] + "," + at[1])) {
            path.add(0, at);
        }
        return path;
    }
}

public class Main {
    public static void main(String[] args) {
        World.readWorld("Mundo.kwld");
        World.showSpeedControl(true);
        World.setVisible(true);

        KarelWorldParser map = new KarelWorldParser(World.asText(" "));
        Racer[] racers = RobotFactory.createRobots(args);
        racers[0].moveToLocation(10, 10, map); // ejemplo de robot utilizando la funcion BFS
    }
}
