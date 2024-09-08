import kareltherobot.*;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class Racer extends Robot {
    private static final int PICKUP_AVENUE = 19;
    private static final int PICKUP_STREET = 8;
    private static Random random = new Random();
    private int[][] stops = {{5, 5}, {10, 10}, {15, 15}, {20, 20}}; // Coordenadas de paradas
    private int destinationStreet; 
    private int destinationAvenue; 

    public Racer(int street, int avenue, Direction direction, int beepers, Color color, int maxBeepers, String id,
            int number, int destStreet, int destAvenue) {
        super(street, avenue, direction, beepers, color);
        this.destinationStreet = destStreet; 
        this.destinationAvenue = destAvenue; 
        World.setupThread(this);
    }

    public void startJourney(KarelWorldParser map) {
        while (true) {
            moveToLocation(PICKUP_STREET, PICKUP_AVENUE);
            if (!nextToABeeper()) {
                turnOff(); 
                break;
            }
            pickBeeper();
            int[] stop = stops[random.nextInt(stops.length)];
            moveToLocation(stop[0], stop[1]);
            putBeeper();
            moveToLocation(PICKUP_STREET, PICKUP_AVENUE);
        }
        moveToLocation(1, 1); // Regreso al parqueadero
        turnOff();
    }

    private void moveToLocation(int targetStreet, int targetAvenue) {
        moveToStreet(targetStreet);
        moveToAvenue(targetAvenue);
    }

    private void moveToStreet(int targetStreet) {
        if (getStreet() < targetStreet) {
            while (facingSouth() && getStreet() != targetStreet) {
                move();
            }
            while (facingNorth() && getStreet() != targetStreet) {
                move();
            }
        } else if (getStreet() > targetStreet) {
            while (facingNorth() && getStreet() != targetStreet) {
                move();
            }
            while (facingSouth() && getStreet() != targetStreet) {
                move();
            }
        }
    }

    private void moveToAvenue(int targetAvenue) {
        if (getAvenue() < targetAvenue) {
            while (facingWest() && getAvenue() != targetAvenue) {
                move();
            }
            while (facingEast() && getAvenue() != targetAvenue) {
                move();
            }
        } else if (getAvenue() > targetAvenue) {
            while (facingEast() && getAvenue() != targetAvenue) {
                move();
            }
            while (facingWest() && getAvenue() != targetAvenue) {
                move();
            }
        }
    }

    private boolean facingDirection(int targetStreet, int targetAvenue) {
        if (getStreet() < targetStreet && facingNorth()) return true;
        if (getStreet() > targetStreet && facingSouth()) return true;
        if (getAvenue() < targetAvenue && facingEast()) return true;
        if (getAvenue() > targetAvenue && facingWest()) return true;
        return false;
    }

    private int getStreet() {
        // Implementa una forma de obtener la calle actual si es posible
        return 0; // Placeholder
    }

    private int getAvenue() {
        // Implementa una forma de obtener la avenida actual si es posible
        return 0; // Placeholder
    }
}

class RobotFactory implements Directions {
    public static Racer[] createRobots(String[] args, KarelWorldParser map) {
        List<Color> colores = Arrays.asList(Color.blue, Color.red, Color.green, Color.yellow);
        int[][] startPositions = {{1, 1}, {5, 1}, {1, 5}, {5, 5}, {2, 2}, {6, 6}, {7, 8}, {3, 3}}; // Posiciones iniciales variadas
        Racer[] racers = new Racer[startPositions.length];

        for (int i = 0; i < racers.length; i++) {
            racers[i] = new Racer(startPositions[i][0], startPositions[i][1], East, 0, colores.get(i % colores.size()), 10, "Robot" + i, i, 10, 10);
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

        for (Racer racer : racers) {
            new Thread(() -> racer.startJourney(map)).start();
        }
    }
}
