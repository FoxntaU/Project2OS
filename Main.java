import kareltherobot.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.Semaphore;

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
    private int destinationStreet; 
    private int destinationAvenue; 

    public Racer(int street, int avenue, Direction direction, int beepers, Color color, int maxBeepers, String id,
            int number, int destStreet, int destAvenue) {
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
        this.destinationStreet = destStreet; 
        this.destinationAvenue = destAvenue; 
        World.setupThread(this);
    }

    public void startJourney(KarelWorldParser map) { 
        moveToLocation(destinationStreet, destinationAvenue, map);
    }

    private void moveToLocation(int targetStreet, int targetAvenue, KarelWorldParser map) {
        moveToStreet(targetStreet);
        moveToAvenue(targetAvenue);
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

    @Override
    public void move() {
        super.move();
        updatePosition();
    }

    private void updatePosition() {
        if (facingNorth()) {
            currentStreet++;
        } else if (facingSouth()) {
            currentStreet--;
        } else if (facingEast()) {
            currentAvenue++;
        } else if (facingWest()) {
            currentAvenue--;
        }
    }
}

class RobotFactory implements Directions {
    public static Racer[] createRobots(String[] args, KarelWorldParser map) {
        List<Color> colores = Arrays.asList(Color.blue, Color.red, Color.green, Color.yellow);
        int[][] destinations = {{5, 5}, {6, 6}, {7, 7}, {8, 8}}; 
        Racer[] racers = new Racer[colores.size()];

        for (int i = 0; i < racers.length; i++) {
            racers[i] = new Racer(1, i + 1, East, 0, colores.get(i), 10, "Robot" + i, i, destinations[i][0], destinations[i][1]);
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
}

public class Main {
    public static void main(String[] args) {
        World.readWorld("Mundo.kwld");
        World.showSpeedControl(true);
        World.setVisible(true);

        KarelWorldParser map = new KarelWorldParser(World.asText(" "));
        Racer[] racers = RobotFactory.createRobots(args, map);

        // Start each robot's journey
        for (Racer racer : racers) {
            racer.startJourney(map);
        }
    }
}
