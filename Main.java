import kareltherobot.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;

class Racer extends Robot implements Runnable { // Implementar Runnable para los hilos
    private int number; // Número de robot
    private String id; // Identificador del robot color 
    private int maxBeepers; // Máximo de beepers que puede llevar
    private int[] deliveryPosition; // Posición de entrega
    private int currentStreet; // Calle actual
    private int currentAvenue; // Avenida actual
    private int startStreet; // Calle de inicio
    private int startAvenue; // Avenida de inicio
    private int beepersInTheBag; // Beepers en la bolsa
    private boolean[][] laberinto; // Laberinto
    private Point beeperLocation = new Point(8, 19); // Posición fija del beeper

    public Racer(int street, int avenue, Direction direction, int beepers, Color color, int maxBeepers, String id,
            int number, boolean[][] laberinto) {

        super(street, avenue, direction, beepers, color);

        this.id = id;
        this.number = number;
        this.maxBeepers = maxBeepers;
        this.deliveryPosition = new int[] { maxBeepers, 1 }; // Calle en la posición 0 y la avenida en la posición 1
        this.currentStreet = street;
        this.currentAvenue = avenue;
        this.startStreet = street;
        this.startAvenue = avenue;
        this.beepersInTheBag = 0;
        this.laberinto = laberinto;
        World.setupThread(this);
    }

    public void moveToLocation(Point inicio, Point fin) {
        Laberinto lab = new Laberinto();
        List<Point> ruta = lab.buscarRuta(laberinto, inicio, fin);
        if (ruta != null && !ruta.isEmpty()) {
            System.out.println("Ruta transformada encontrada:");
            for (Point p : ruta) {
                System.out.println("(" + p.x + ", " + p.y + ")");
            }
        } else {
            System.out.println("No se encontró un camino.");
        }

        if (ruta == null || ruta.isEmpty()) {
            System.out.println("La ruta está vacía o es nula.");
            return;
        }

        for (int i = 1; i < ruta.size(); i++) {
            Point currentPoint = new Point(getCurrentPosition()[0], getCurrentPosition()[1]);
            Point nextPoint = ruta.get(i);
            String direction = getDirection(currentPoint, nextPoint);
            turnTo(direction);
            move();
        }
    }

    public void recogerBeeper() {
        moveToLocation(new Point(currentStreet, currentAvenue), beeperLocation);
        if (nextToABeeper()) {
            pickBeeper();
        }
    }

    public void irAParadaAleatoria() {
        Random random = new Random();

        Point[] parada1 = {
            new Point(15, 3), new Point(15, 4), new Point(15, 5), new Point(15, 6), new Point(15, 7), new Point(15, 8),
            new Point(16, 3), new Point(16, 4), new Point(16, 5), new Point(16, 6), new Point(16, 7), new Point(16, 8),
            new Point(17, 3), new Point(17, 4), new Point(17, 5), new Point(17, 7), new Point(17, 8)
        };

        Point[] parada2 = {
            new Point(10, 4), new Point(10, 5),
            new Point(11, 4), new Point(11, 5), new Point(11, 6), new Point(11, 8), new Point(11, 9),
            new Point(12, 4), new Point(12, 5), new Point(12, 6), new Point(12, 7), new Point(12, 8), new Point(12, 9),
            new Point(13, 4), new Point(13, 5), new Point(13, 6), new Point(13, 7), new Point(13, 8), new Point(13, 9)
        };

        Point[] parada3 = {
            new Point(7, 7), new Point(7, 9), 
            new Point(8, 7), new Point(8, 8), new Point(8, 9),
        };

        Point[] parada4 = {
            new Point(18, 18), new Point(18, 19),
            new Point(19, 18), new Point(19, 19)
        };

        Point[][] paradas = { parada1, parada2, parada3, parada4 };
        Point[] paradaSeleccionada = paradas[random.nextInt(paradas.length)];

        Point destino = paradaSeleccionada[random.nextInt(paradaSeleccionada.length)];
        moveToLocation(new Point(currentStreet, currentAvenue), destino);

        if (anyBeepersInBeeperBag()) {
            putBeeper();
        }
    }

    public void realizarTareas() {
        while (true) {
            moveToLocation(new Point(currentStreet, currentAvenue), beeperLocation);
            if (!nextToABeeper()) {
                break; 
            }
            recogerBeeper();
            irAParadaAleatoria();
        }
        moveToLocation(new Point(currentStreet, currentAvenue), new Point(startStreet, startAvenue));
        turnOff();
    }

    private String getDirection(Point current, Point next) {
        if (next.x > current.x) {
            return "NORTH";
        } else if (next.x < current.x) {
            return "SOUTH";
        } else if (next.y > current.y) {
            return "EAST";
        } else if (next.y < current.y) {
            return "WEST";
        } else {
            throw new IllegalArgumentException("Los puntos son iguales; no se puede determinar la dirección.");
        }
    }

    private void turnTo(String direction) {
        while (!facingDirection(direction)) {
            turnLeft();
        }
    }

    private boolean facingDirection(String direction) {
        switch (direction) {
            case "NORTH":
                return facingNorth();
            case "SOUTH":
                return facingSouth();
            case "EAST":
                return facingEast();
            case "WEST":
                return facingWest();
            default:
                return false;
        }
    }

    public int getMaxBeepers() {
        return this.maxBeepers;
    }

    public int[] getDeliveryPosition() {
        return this.deliveryPosition;
    }

    public int[] getCurrentPosition() {
        return new int[] { currentStreet, currentAvenue };
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

    @Override
    public void move() {
        super.move();
        updatePosition();
    }

    @Override
    public void run() {
        realizarTareas(); // Al ejecutar el hilo, el robot empieza a realizar tareas
    }
}

class RobotFactory implements Directions {
    public static Racer[] createRobots(int numRobots, boolean[][] laberinto) {
        List<Color> colores = new ArrayList<>();
        colores.add(Color.blue);
        colores.add(Color.red);
        colores.add(Color.green);
        colores.add(Color.yellow);

        List<String> colorNames = new ArrayList<>();
        colorNames.add("blue");
        colorNames.add("red");
        colorNames.add("green");
        colorNames.add("yellow");

        int[] quantity_of_beepers = { 1, 2, 4, 8 };

        Point[] parqueadero = {
            new Point(7, 12), new Point(7, 13), new Point(7, 14), new Point(7, 15), new Point(7, 16), new Point(7, 17), new Point(7, 18),
            new Point(6, 12), new Point(6, 13), new Point(6, 14), new Point(6, 15), new Point(6, 16), new Point(6, 17), new Point(6, 18),
            new Point(5, 12), new Point(5, 13), new Point(5, 14), new Point(5, 15), new Point(5, 16), new Point(5, 17), new Point(5, 18),
            new Point(4, 12), new Point(4, 13), new Point(4, 14), new Point(4, 15), new Point(4, 16), new Point(4, 17), new Point(4, 18),
            new Point(3, 12), new Point(3, 13), new Point(3, 14), new Point(3, 15), new Point(3, 16), new Point(3, 17), new Point(3, 18),
            new Point(2, 12), new Point(2, 13), new Point(2, 14), new Point(2, 15), new Point(2, 16), new Point(2, 17), new Point(2, 18)
        };

        Racer[] racers = new Racer[numRobots];
        Random random = new Random();

        for (int i = 0; i < numRobots; i++) {
            Point posicionInicial = parqueadero[random.nextInt(parqueadero.length)];
            int colorIndex = i % colores.size();
            racers[i] = new Racer(posicionInicial.x, posicionInicial.y, East, 0, colores.get(colorIndex), quantity_of_beepers[colorIndex],
                    colorNames.get(colorIndex), i, laberinto);
        }
        return racers;
    }
}

class PuntoInvalidoException extends RuntimeException {
    public PuntoInvalidoException(String mensaje) {
        super(mensaje);
    }
}

class KarelWorldParser {
    public String worldText;
    public boolean[][] matrix = new boolean[40][40];
    public KarelWorldParser(String worldText) {
        this.worldText = worldText;
        for (int i = 0; i < 40; i++) {
            for (int j = 0; j < 40; j++) {
                if (i == 39 || j == 0) {
                    matrix[i][j] = false;
                } else {
                    matrix[i][j] = true;
                }
            }
        }
        String[] tokens = worldText.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("eastwestwalls")) {
                int street = (Integer.parseInt(tokens[i + 1]) * 2);
                int avenue = (Integer.parseInt(tokens[i + 2]) * 2) - 1;
                fillMatrix(street, avenue);
                fillMatrix(street, avenue + 1);
                fillMatrix(street, avenue - 1);
            } else if (tokens[i].equals("northsouthwalls")) {
                int avenue = (Integer.parseInt(tokens[i + 1]) * 2);
                int street = (Integer.parseInt(tokens[i + 2]) * 2) - 1;
                fillMatrix(street, avenue);
                fillMatrix(street + 1, avenue);
                fillMatrix(street - 1, avenue);
            }
        }
        printMatrix();
    }
    public void fillMatrix(int street, int avenue) {
        if (street >= 0 && street < 40 && avenue >= 0 && avenue < 40) {
            matrix[39 - street][avenue] = false;
        } else {
            System.out.println("Error: Índice fuera de límites. Street: " + street + ", Avenue: " + avenue);
        }
    }
    public void printMatrix() {
        for (int i = 0; i < 40; i++) {
            for (int j = 0; j < 40; j++) {
                if (matrix[i][j]) {
                    System.out.print("  ");
                } else {
                    System.out.print("1 ");
                }
            }
            System.out.println();
        }
    }
}

class Point {
    int x, y;
    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Laberinto {
    private static final int[] row = { -2, 2, 0, 0 };
    private static final int[] col = { 0, 0, -2, 2 };
    
    private static boolean esValido(boolean[][] laberinto, boolean[][] visitado, int fila, int columna, int actualFila,
            int actualColumna) {
        int n = laberinto.length;
        if (fila < 0 || columna < 0 || fila >= n || columna >= n || !laberinto[fila][columna]
                || visitado[fila][columna]) {
            return false;
        }
        int intermedioFila = (fila + actualFila) / 2;
        int intermedioColumna = (columna + actualColumna) / 2;
        return laberinto[intermedioFila][intermedioColumna];
    }

    public static LinkedList<Point> bfs(boolean[][] laberinto, Point inicio, Point fin) {
        int n = laberinto.length;
        boolean[][] visitado = new boolean[n][n];
        Queue<Point> cola = new LinkedList<>();
        cola.add(inicio);
        visitado[inicio.x][inicio.y] = true;
        Point[][] predecesor = new Point[n][n];
        while (!cola.isEmpty()) {
            Point actual = cola.poll();
            if (actual.x == fin.x && actual.y == fin.y) {
                return reconstruirCamino(predecesor, inicio, fin);
            }
            for (int i = 0; i < 4; i++) {
                int nuevoX = actual.x + row[i];
                int nuevoY = actual.y + col[i];

                if (esValido(laberinto, visitado, nuevoX, nuevoY, actual.x, actual.y)) {
                    cola.add(new Point(nuevoX, nuevoY));
                    visitado[nuevoX][nuevoY] = true;
                    predecesor[nuevoX][nuevoY] = actual;
                }
            }
        }

        return null;
    }

    private static LinkedList<Point> reconstruirCamino(Point[][] predecesor, Point inicio, Point fin) {
        LinkedList<Point> camino = new LinkedList<>();
        Point paso = fin;
        while (paso != null) {
            camino.addFirst(paso);
            paso = predecesor[paso.x][paso.y];
        }
        if (camino.getFirst().x == inicio.x && camino.getFirst().y == inicio.y) {
            return camino;
        } else {
            return null;
        }
    }

    public List<Point> buscarRuta(boolean[][] laberinto, Point inicio, Point fin) {
        int pos_inicio_x = (int) (-2 * inicio.x + 40);
        int pos_inicio_y = 2 * inicio.y - 1;
        int pos_fin_x = (int) (-2 * fin.x + 40);
        int pos_fin_y = 2 * fin.y - 1;

        if (!laberinto[pos_inicio_x][pos_inicio_y] || !laberinto[pos_fin_x][pos_fin_y]) {
            throw new PuntoInvalidoException("Punto de inicio o fin no es válido en el laberinto.");
        }

        Point inicio_transformado = new Point(pos_inicio_x, pos_inicio_y);
        Point fin_transformado = new Point(pos_fin_x, pos_fin_y);

        LinkedList<Point> camino = bfs(laberinto, inicio_transformado, fin_transformado);
        List<Point> rutaTransformada = new LinkedList<>();

        if (camino != null) {
            for (Point p : camino) {
                int pos_x = (int) (-0.5 * p.x + 20);
                int pos_y = (p.y / 2) + 1;
                rutaTransformada.add(new Point(pos_x, pos_y));
            }
        }

        return rutaTransformada;
    }
}

public class Main {
    public static void main(String[] args) {
        World.readWorld("Mundo.kwld");
        World.showSpeedControl(true);
        World.setVisible(true);
        String worldText = World.asText(" ");
        KarelWorldParser map = new KarelWorldParser(worldText);
        boolean[][] laberinto = map.matrix;

        int numRobots = 8;

        Racer[] racers = RobotFactory.createRobots(numRobots, laberinto);

        System.out.println("Start the searching:  " + '\n');

        // Crear hilos para cada robot y ejecutarlos
        Thread[] threads = new Thread[numRobots];
        for (int i = 0; i < numRobots; i++) {
            threads[i] = new Thread(racers[i]); // Crear un hilo para cada robot
            threads[i].start(); // Iniciar el hilo
        }

        // Esperar a que todos los hilos terminen
        for (int i = 0; i < numRobots; i++) {
            try {
                threads[i].join(); // Espera a que cada hilo termine
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("All robots finished their tasks.");
    }
}
