import kareltherobot.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public Racer(int street, int avenue, Direction direction, int beepers, Color color, int maxBeepers, String id,
            int number) {
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
        World.setupThread(this);
    }



    public void returnToStart() {
        System.out.println("Returning to start at (" + startStreet + ", " + startAvenue + ")");
        moveToLocation(startStreet, startAvenue);
        System.out.println("Returned to start at (" + startStreet + ", " + startAvenue + ")");
    }

    private boolean outOfBoundaries() {
        return currentStreet < 1 || currentStreet > 8 || currentAvenue < 1 || currentAvenue > 10;
    }

    private void moveToStreet(int targetStreet) {
        if (currentStreet < targetStreet) {
            if (!facingNorth()) {
                turnTo("NORTH");
            }
        } else if (currentStreet > targetStreet) {
            if (!facingSouth()) {
                turnTo("SOUTH");
            }
        }

        while (currentStreet != targetStreet) {
            if (frontIsClear()) {
                move();
            } else {
                break;
            }
        }
    }

    private void moveToAvenue(int targetAvenue) {
        if (currentAvenue < targetAvenue) {
            if (!facingEast()) {
                turnTo("EAST");
            }
        } else if (currentAvenue > targetAvenue) {
            if (!facingWest()) {
                turnTo("WEST");
            }
        }

        while (currentAvenue != targetAvenue) {
            if (frontIsClear()) {
                move();
            } else {
                break;
            }
        }
    }

    private void moveToLocation(int targetStreet, int targetAvenue) {
        moveToStreet(targetStreet);
        moveToAvenue(targetAvenue);
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


    // Obtener la ubicación actual
    public int[] getCurrentPosition() {
        return new int[] { currentStreet, currentAvenue };
    }

    // Actualizar la ubicación al moverse
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

    // Mover y actualizar posición
    @Override
    public void move() {
        super.move();
        updatePosition();
    }
}

class RobotFactory implements Directions {
    public static Racer[] createRobots(String[] args) {
        int r = 4;

        // Crear robots
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
        Racer[] racers = new Racer[r];
        int[] start_position = { 3, 5, 6, 7 };

        for (int i = 0; i < r; i++) {
            racers[i] = new Racer(1, start_position[i], East, 0, colores.get(i), quantity_of_beepers[i], colorNames.get(i),
                    i);
        }

        return racers;
    }
}

public class Main {
    public static void main(String[] args) {
        World.readWorld("Mundo.kwld");
        World.showSpeedControl(true);
        World.setVisible(true);

        Racer[] racers = RobotFactory.createRobots(args);


        System.out.println("Start the searching:  " + '\n');


}
}
