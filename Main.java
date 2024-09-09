import kareltherobot.*;
import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// Clase Point para almacenar coordenadas (calle, avenida)
class Point {
    int street, avenue;

    Point(int street, int avenue) {
        this.street = street;
        this.avenue = avenue;
    }

    @Override
    public String toString() {
        return "(" + street + ", " + avenue + ")";
    }
}

class Racer extends Robot {
    private static final int PICKUP_STREET = 8;  // Calle
    private static final int PICKUP_AVENUE = 19; // Avenida
    private static Random random = new Random();
    
    // Paradas 1, 2, 3 y 4 según las nuevas posiciones definidas
    private List<Point> parada1 = new LinkedList<>();
    private List<Point> parada2 = new LinkedList<>();
    private List<Point> parada3 = new LinkedList<>();
    private List<Point> parada4 = new LinkedList<>();

    private int destinationStreet; 
    private int destinationAvenue; 
    private int currentStreet; // Calle actual del robot
    private int currentAvenue; // Avenida actual del robot

    public Racer(int street, int avenue, Direction direction, int beepers, Color color, int maxBeepers, String id,
                 int number, int destStreet, int destAvenue) {
        super(street, avenue, direction, beepers, color);
        this.destinationStreet = destStreet; 
        this.destinationAvenue = destAvenue; 
        this.currentStreet = street;
        this.currentAvenue = avenue;
        World.setupThread(this);

        // Llenar las paradas con las posiciones sin beepers de referencia
        inicializarParadas();
    }

    private void inicializarParadas() {
        // Parada 1 (Calles 15-17, Avenidas 3-8)
        for (int street = 15; street <= 17; street++) {
            for (int avenue = 3; avenue <= 8; avenue++) {
                parada1.add(new Point(street, avenue));
            }
        }

        // Parada 2 (Calles 10-13, Avenidas 4-9)
        for (int street = 10; street <= 13; street++) {
            for (int avenue = 4; avenue <= 9; avenue++) {
                parada2.add(new Point(street, avenue));
            }
        }

        // Parada 3 (Calles 7-8, Avenidas 7-9)
        for (int street = 7; street <= 8; street++) {
            for (int avenue = 7; avenue <= 9; avenue++) {
                parada3.add(new Point(street, avenue));
            }
        }

        // Parada 4 (Calles 18-19, Avenidas 18-19)
        for (int street = 18; street <= 19; street++) {
            for (int avenue = 18; avenue <= 19; avenue++) {
                parada4.add(new Point(street, avenue));
            }
        }
    }

    public void startJourney(KarelWorldParser map) {
        while (true) {
            moveToLocation(PICKUP_STREET, PICKUP_AVENUE);
            if (!nextToABeeper()) {
                turnOff(); 
                break;
            }
            pickBeeper();
            Point stop = seleccionarParada(); // Selección aleatoria de parada
            moveToLocation(stop.street, stop.avenue);
            putBeeper();
            moveToLocation(PICKUP_STREET, PICKUP_AVENUE); // Regresa por más pasajeros
        }
        moveToLocation(7, 12); // Regreso al parqueadero (calle 7, avenida entre 12 y 16)
        turnOff();
    }

    // Método para seleccionar una parada aleatoria
    private Point seleccionarParada() {
        int parada = random.nextInt(4); // Seleccionar una de las 4 paradas
        switch (parada) {
            case 0:
                return parada1.get(random.nextInt(parada1.size()));
            case 1:
                return parada2.get(random.nextInt(parada2.size()));
            case 2:
                return parada3.get(random.nextInt(parada3.size()));
            default:
                return parada4.get(random.nextInt(parada4.size()));
        }
    }

    private void moveToLocation(int targetStreet, int targetAvenue) {
        moveToStreet(targetStreet);
        moveToAvenue(targetAvenue);
    }

    private void moveToStreet(int targetStreet) {
        if (currentStreet < targetStreet) {
            if (!facingNorth()) {
                turnTo("NORTH");
            }
            while (currentStreet < targetStreet) {
                if (frontIsClear()) {
                    move();
                    currentStreet++;
                } else {
                    break; // Stop if blocked
                }
            }
        } else if (currentStreet > targetStreet) {
            if (!facingSouth()) {
                turnTo("SOUTH");
            }
            while (currentStreet > targetStreet) {
                if (frontIsClear()) {
                    move();
                    currentStreet--;
                } else {
                    break; // Stop if blocked
                }
            }
        }
    }

    private void moveToAvenue(int targetAvenue) {
        if (currentAvenue < targetAvenue) {
            if (!facingEast()) {
                turnTo("EAST");
            }
            while (currentAvenue < targetAvenue) {
                if (frontIsClear()) {
                    move();
                    currentAvenue++;
                } else {
                    break; // Stop if blocked
                }
            }
        } else if (currentAvenue > targetAvenue) {
            if (!facingWest()) {
                turnTo("WEST");
            }
            while (currentAvenue > targetAvenue) {
                if (frontIsClear()) {
                    move();
                    currentAvenue--;
                } else {
                    break; // Stop if blocked
                }
            }
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
    public static Racer[] createRobots(int numberOfRobots, KarelWorldParser map) {
        List<Color> colores = Arrays.asList(Color.blue, Color.red, Color.green, Color.yellow);

        // Asegurar que el número mínimo de robots sea 8
        numberOfRobots = Math.max(numberOfRobots, 8);
        
        // Inicializar posiciones dentro del parqueadero (coordenadas de avenidas 12 a 16 y calles 7 a 2)
        int[][] startPositions = new int[numberOfRobots][2];
        
        int avenue = 12;
        int street = 7; // Comienza en la calle 7
        
        for (int i = 0; i < numberOfRobots; i++) {
            startPositions[i] = new int[] {street, avenue};
            avenue++;
            if (avenue > 16) {
                avenue = 12; // Reinicia las avenidas si se pasa del límite de 16
                street--;     // Baja a la siguiente calle
            }
            if (street < 2) {
                street = 2; // Evita que baje de la calle 2
            }
        }
        
        Racer[] racers = new Racer[numberOfRobots];
        for (int i = 0; i < numberOfRobots; i++) {
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
        int numberOfRobots = args.length > 0 ? Integer.parseInt(args[0]) : 8; // Definir el número de robots a partir de los argumentos
        Racer[] racers = RobotFactory.createRobots(numberOfRobots, map);

        for (Racer racer : racers) {
            new Thread(() -> racer.startJourney(map)).start();
        }
    }
}
