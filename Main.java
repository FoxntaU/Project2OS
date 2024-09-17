import kareltherobot.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.Map;

class Racer extends Robot implements Runnable {
    public static Set<Point> occupiedPositions = new HashSet<>();
    private static volatile boolean flag = false;
    private int number; // Número de robot
    private String id; // Identificador del robot color
    private int maxBeepers; // Máximo de beepers que puede llevar
    private int[] deliveryPosition; // Posición de entrega
    private int currentStreet; // Calle actual
    private int currentAvenue; // Avenida actual
    private int followStreet; // Calle a seguir
    private int followAvenue; // Avenida a seguir
    private int startStreet; // Calle de inicio
    private int startAvenue; // Avenida de inicio
    private int beepersInTheBag; // Beepers en la bolsa
    private boolean[][] laberinto; // Laberinto
    private Point beeperLocation = new Point(8, 19); // Posición fija del beeper
    private String parada = "parada1";
    private static final Semaphore semaphorep1 = new Semaphore(1);
    private static final Semaphore semaphorep2 = new Semaphore(1);
    private static final Semaphore semaphorep3 = new Semaphore(1);
    private static final Semaphore semaphorep4 = new Semaphore(1);

    public Racer(int street, int avenue, Direction direction, int beepers, Color color, int maxBeepers, String id,
            int number, boolean[][] laberinto) {

        super(street, avenue, direction, beepers, color);
        this.id = id;
        this.number = number;
        this.maxBeepers = maxBeepers;
        this.deliveryPosition = new int[] { maxBeepers, 1 };
        this.currentStreet = street;
        this.currentAvenue = avenue;
        this.followStreet = street;
        this.followAvenue = avenue + 1;
        this.startStreet = street;
        this.startAvenue = avenue;
        this.beepersInTheBag = 0;
        this.laberinto = laberinto;
        World.setupThread(this);
        synchronized (occupiedPositions) {
            occupiedPositions.add(new Point(currentStreet, currentAvenue));
        }
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

    // Implementación de las instrucciones para las paradas
    public void irAParadaAleatoria() {
        Random random = new Random();

        // Paradas definidas
        Point parada1 = new Point(16, 6); // Parada 1
        Point parada2 = new Point(12, 7); // Parada 2
        Point parada3 = new Point(8, 8); // Parada 3
        Point parada4 = new Point(18, 19); // Parada 4

        int paradaAleatoria = random.nextInt(4) + 1;

        // Movimiento según parada asignada al robot
        switch (paradaAleatoria) {
            case 1:
                // Parada 1: (16, 6) → (14, 3) → (6, 7) → (19, 1)
                parada = "parada1";
                moveToLocation(new Point(currentStreet, currentAvenue), parada1);
                putBeeper();
                moveToLocation(parada1, new Point(14, 3));
                moveToLocation(new Point(14, 3), new Point(6, 7));
                moveToLocation(new Point(6, 7), new Point(19, 1));
                break;
            case 2:
                // Parada 2: (9, 8) → (9, 13) → (10, 14) → (12, 7) → (6, 6) → (10, 10) → (19, 1)
                parada = "parada2";
                moveToLocation(new Point(currentStreet, currentAvenue), new Point(9, 8));
                moveToLocation(new Point(9, 8), new Point(9, 13));
                moveToLocation(new Point(9, 13), new Point(10, 16));
                moveToLocation(new Point(10, 16), parada2);
                putBeeper();
                moveToLocation(parada2, new Point(6, 6));
                moveToLocation(new Point(6, 6), new Point(6, 10));
                moveToLocation(new Point(6, 10), new Point(19, 1));
                break;
            case 3:
                // Parada 3: (7, 8) → (10, 10) → (19, 1)
                parada = "parada3";
                moveToLocation(new Point(currentStreet, currentAvenue), parada3);
                putBeeper();
                moveToLocation(parada3, new Point(10, 10));
                moveToLocation(new Point(10, 10), new Point(19, 1));
                break;
            case 4:
                // Parada 4: (9, 8) → (9, 13) → (18, 19) → (19, 1)
                parada = "parada4";
                moveToLocation(new Point(currentStreet, currentAvenue), new Point(9, 8));
                moveToLocation(new Point(9, 8), new Point(9, 13));
                moveToLocation(new Point(9, 13), parada4);
                putBeeper();
                moveToLocation(parada4, new Point(19, 1));
                break;
        }
    }
    public static synchronized void setFlag(boolean value) {
        flag = value;
    }

    public static synchronized boolean getFlag() {
        return flag;
    }

    public void realizarTareas() {
        boolean firsttime = true;
        while (true) {
            if (firsttime) {
                moveToLocation(new Point(currentStreet, currentAvenue), beeperLocation);
                firsttime = false;
            } else {
                moveToLocation(new Point(currentStreet, currentAvenue), new Point(1, 19));
                if (flag) {
                    break;
                }
                moveToLocation(new Point(currentStreet, currentAvenue), beeperLocation);
            }
            if (nextToABeeper()) {
                recogerBeeper();
                if(!nextToABeeper()) {
                    setFlag(true);
                }
            }
            if (flag) {
                moveToLocation(new Point(currentStreet, currentAvenue), new Point(6, 8));
                moveToLocation(new Point(currentStreet, currentAvenue), new Point(6, 8));
                moveToLocation(new Point(currentStreet, currentAvenue), new Point(18, 10));
                moveToLocation(new Point(currentStreet, currentAvenue), new Point(19, 1));
            } else {
                irAParadaAleatoria();
            }
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
                followStreet = currentStreet + 1;
                followAvenue = currentAvenue;
                return facingNorth();
            case "SOUTH":
                followStreet = currentStreet - 1;
                followAvenue = currentAvenue;
                return facingSouth();
            case "EAST":
                followStreet = currentStreet;
                followAvenue = currentAvenue + 1;
                return facingEast();
            case "WEST":
                followStreet = currentStreet;
                followAvenue = currentAvenue - 1;
                return facingWest();
            default:
                return false;
        }
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

    private static final Map<String, Point[]> semaforoPuntos = Map.of(
        "parada1", new Point[]{new Point(18, 5), new Point(18, 7)}, // Espera y desbloqueo
        "parada2", new Point[]{new Point(10, 8), new Point(10, 6)},
        "parada3", new Point[]{new Point(6, 7), new Point(6, 9)},
        "parada4", new Point[]{new Point(10, 16), new Point(10, 14)}
    );

    private Semaphore getSemaforo() {
        switch (parada) {
            case "parada1":
                return semaphorep1;
            case "parada2":
                return semaphorep2;
            case "parada3":
                return semaphorep3;
            case "parada4":
                return semaphorep4;
            default:
                throw new IllegalStateException("Parada no válida");
        }
    }
    
    private Point getPuntoEspera() {
        return semaforoPuntos.get(parada)[0];
    }
    
    private Point getPuntoDesbloqueo() {
        return semaforoPuntos.get(parada)[1];
    }

    public void bloquearParada() {
        try {
            Semaphore semaforo = getSemaforo();
            semaforo.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void liberarParada() {
        try {
            Semaphore semaforo = getSemaforo();
            semaforo.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void move() {

        System.out.println(
                "Robot " + id + " (" + number + ") en la posición (" + currentStreet + ", " + currentAvenue + ")");

        Point nextPosition = new Point(followStreet, followAvenue);
        Point currentPosition = new Point(currentStreet, currentAvenue);

        if (currentPosition.equals(getPuntoEspera())) {
            System.out.println("Robot " + id + " está en el punto de espera. Intentando adquirir el semáforo...");
            bloquearParada(); // Intenta adquirir el semáforo correspondiente
            System.out.println("Semáforo adquirido. Robot " + id + " continúa.");
        }

        synchronized (occupiedPositions) {
            // Verifica si la posición está ocupada
            if (occupiedPositions.contains(nextPosition)) {
                System.out
                        .println("La posición (" + followStreet + ", " + followAvenue + ") está ocupada. Esperando...");
                // Espera hasta que la posición esté libre
                while (occupiedPositions.contains(nextPosition)) {
                    try {
                        occupiedPositions.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Mueve el robot a la nueva posición
            occupiedPositions.remove(new Point(currentStreet, currentAvenue)); // Libera la posición actual
            super.move();
            updatePosition();
            occupiedPositions.add(nextPosition); // Marca la nueva posición como ocupada

            // Notifica a los demás robots que la posición está libre
            occupiedPositions.notifyAll();
        }

        if (new Point(currentStreet, currentAvenue).equals(getPuntoDesbloqueo())) {
            System.out.println("Robot " + id + " ha llegado al punto de desbloqueo. Liberando semáforo...");
            liberarParada(); // Libera el semáforo correspondiente
            System.out.println("Semáforo liberado. Robot " + id + " continúa.");
        }

    }

    @Override
    public void run() {
        realizarTareas();
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
                new Point(7, 12), new Point(7, 18),
                new Point(6, 12), new Point(6, 18), 
                new Point(5, 12), new Point(5, 18),
                new Point(4, 12), new Point(4, 18),
                new Point(3, 12),
                new Point(2, 12)
        };

        Racer[] racers = new Racer[numRobots];
        Random random = new Random();
        List<Point> posicionesDisponibles = new ArrayList<>(Arrays.asList(parqueadero)); // Convierte el array en una
                                                                                         // lista

        for (int i = 0; i < numRobots; i++) {
            int indexAleatorio = random.nextInt(posicionesDisponibles.size());
            Point posicionInicial = posicionesDisponibles.remove(indexAleatorio);

            int colorIndex = i % colores.size();
            racers[i] = new Racer(posicionInicial.x, posicionInicial.y, East, 0, colores.get(colorIndex),
                    quantity_of_beepers[colorIndex],
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
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
