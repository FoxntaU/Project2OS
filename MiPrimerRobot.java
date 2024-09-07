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

    public void race(Section[] sections) {
        int latestStreet = sections[this.number].startStreet;
        int latestAvenue = sections[this.number].startAvenue;
        moveToLocation(sections[this.number].startStreet, sections[this.number].startAvenue);
        boolean finished = false;
        Section mySection = sections[this.number];

        while (true) {
            // Section{startAvenue=3, endAvenue=10, startStreet=1, endStreet=8}
            if (nextToABeeper()) {
                pickBeeper();
                beepersInTheBag++;        
            }
            //Regresar posicion inicial 
            else if (finished){
                if (beepersInTheBag != 0) {
                    deliverBeepers();
                }
                returnToStart();
                break;
            }

            else if (this.currentAvenue == 10 && !nextToABeeper()){
                System.out.println("acabe la fila");
                latestStreet = latestStreet + 1;
                mySection.latestStreetVisited = latestStreet;
                moveToLocation(latestStreet, latestAvenue);
                if (beepersInTheBag != maxBeepers) {
                    turnTo("EAST");                    
                }
            }

            else{
                move();
            }

            //DICE QUE SE ACABO LA SECCION
            if ((this.currentAvenue == mySection.endAvenue && this.currentStreet == mySection.endStreet && !nextToABeeper()) || mySection.isFinished){

                mySection.isFinished = true;
                List<Section> unfinishedSection = UnfinishedSectionsFilter(sections);
                deliverBeepers();
                if (unfinishedSection.isEmpty()){
                    finished = true;
                }
                else{
                    mySection = unfinishedSection.get(0);
                    latestStreet = mySection.latestStreetVisited;
                }
            }

            if (beepersInTheBag == maxBeepers) {
                deliverBeepers();
                moveToLocation(latestStreet, latestAvenue);
                beepersInTheBag = 0;
            }

        }

        turnOff();

    }

    public synchronized void pickBeeper() {
        super.pickBeeper();
    }
    
    public static  List<Section> UnfinishedSectionsFilter(Section[] sections)
    {
        return Arrays.stream(sections).filter(section -> !section.isFinished).collect(Collectors.toList());
    }

    public void pickBeepersUntilBagIsFull() {
        
    }

    public void deliverBeepers() {
        System.out.println("Heading to deliver beepers to (" + deliveryPosition[0] + ", " + deliveryPosition[1] + ")");
        moveToLocation(deliveryPosition[0], deliveryPosition[1]);
        while (anyBeepersInBeeperBag()) {
            putBeeper();
            System.out.println("Delivered a beeper to (" + deliveryPosition[0] + ", " + deliveryPosition[1] + ")");
        }
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

    public void run(Section[] sections) {
        race(sections);
        System.out.println(
                "I'm not gonna run away, I never go back on my word! That's my nindo: my ninja way! - Naruto Uzumaki");
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
        int r = 1; // Valor por defecto
        boolean e = false;

        // Procesar los argumentos de la línea de comandos
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-r") && i + 1 < args.length) {
                try {
                    r = Integer.parseInt(args[i + 1]);
                    if (r != 1 && r != 2 && r != 4) {
                        System.out.println("El valor de -r debe ser 1, 2, o 4");
                        return null;
                    }
                    i++; // Saltar al siguiente argumento
                } catch (NumberFormatException ex) {
                    System.out.println("El valor de -r debe ser un número");
                    return null;
                }
            } else if (args[i].equals("-e")) {
                e = true;
            }
        }

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

        if (e) {
            // -e es un parámetro así que todos los robots tienen el mismo número de beepers
            System.out.println("All robots have the same number of beepers: ");
            int n = quantity_of_beepers[(int) (Math.random() * quantity_of_beepers.length)];
            for (int i = 0; i < r; i++) {
                String colorWithIndex = colorNames.get(i % colorNames.size()) + "-" + (i + 1);
                racers[i] = new Racer(start_position[i], 2, East, 0, colores.get(i % colores.size()), n, colorWithIndex,
                        i);
            }
        } else {
            // -e no es un parámetro así que todos los robots tienen diferentes números de
            // beepers
            System.out.println("All robots have different number of beepers: ");
            for (int i = 0; i < r; i++) {
                String colorWithIndex = colorNames.get(i % colorNames.size()) + "-" + (i + 1);
                int n = quantity_of_beepers[(int) (Math.random() * quantity_of_beepers.length)];
                racers[i] = new Racer(start_position[i], 2, East, 0, colores.get(i % colores.size()), n, colorWithIndex,
                        i);
            }
        }

        System.out.println('\n');
        // Imprimir todos los parámetros de los robots
        for (int i = 0; i < r; i++) {
            String colorWithIndex = colorNames.get(i % colorNames.size()) + "-" + (i + 1);
            System.out.println("Robot " + colorWithIndex + " has " + racers[i].getMaxBeepers() + " beepers max."
                    + "and has to deliver: " + "street " + racers[i].getMaxBeepers() + ", avenue " + 1);
        }

        System.out.println('\n');

        return racers;
    }

    public static void startRobots(Racer[] racers, Section[] sections) {
        // Iniciar los robots
        Thread[] threads = new Thread[racers.length];
        for (int i = 0; i < racers.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> racers[index].run(sections));
            threads[i].start();
        }
    }
}

class BeepersFactory {
    private static List<int[]> beeperLocations = new ArrayList<>();


    public static void addBeeperLocation(int street, int avenue) {
        synchronized (beeperLocations) {
            int[] location = { street, avenue };
            beeperLocations.add(location);
        }
    }

    public static void generateBeepersRandomly(String[] args) {
        int min_avenue = 3;
        int min_street = 1;
        int range_street = 8 - min_street + 1;
        int range_avenue = 10 - min_avenue + 1;
        int r = 1;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-r") && i + 1 < args.length) {
                try {
                    r = Integer.parseInt(args[i + 1]);
                    if (r != 1 && r != 2 && r != 4) {
                        System.out.println("El valor de -r debe ser 1, 2, o 4");
                        return;
                    }
                    i++; // Saltar al siguiente argumento
                } catch (NumberFormatException ex) {
                    System.out.println("El valor de -r debe ser un número");
                    return;
                }
            }
        }



        int num_beepers = r * 100; // Usar args para encontrar la cantidad de beepers
        for (int j = 0; j < num_beepers; j++) {
            int street = (int) (Math.random() * range_street + min_street);
            int avenue = (int) (Math.random() * range_avenue + min_avenue);
            World.placeBeepers(street, avenue, 1);
            addBeeperLocation(street, avenue);
        }
    }
}

class Section {
    public int startAvenue;
    public int endAvenue;
    public int startStreet;
    public int endStreet;
    public boolean isFinished;
    public int latestStreetVisited;

    public Section(int startAvenue, int endAvenue, int startStreet, int endStreet) {
        this.startAvenue = startAvenue;
        this.endAvenue = endAvenue;
        this.startStreet = startStreet;
        this.endStreet = endStreet;
        this.latestStreetVisited = startStreet;
        this.isFinished = false;
    }

    public boolean beepersExist() {
        return false;
    }

    @Override
    public String toString() {
        return "Section{" +
                "startAvenue=" + startAvenue +
                ", endAvenue=" + endAvenue +
                ", startStreet=" + startStreet +
                ", endStreet=" + endStreet +
                ", latestStreetVisited="+ latestStreetVisited+
                '}';
    }
}

class SectionFactory {

    public static Section[] createSections(int numberOfSections) {
        Section[] sections = new Section[numberOfSections];
        int avenueInicio = 3;
        int avenueFin = 10;
        int streetStep = 8 / numberOfSections;

        for (int i = 0; i < numberOfSections; i++) {
            int streetInicio = i * streetStep + 1;
            int streetFin = (i + 1) * streetStep;
            sections[i] = new Section(avenueInicio, avenueFin, streetInicio, streetFin);
        }

        return sections;
    }
}

public class MiPrimerRobot {
    public static void main(String[] args) {
        BeepersFactory.generateBeepersRandomly(args);
        World.readWorld("Mundo.kwld");
        World.showSpeedControl(true);
        World.setVisible(true);

        Racer[] racers = RobotFactory.createRobots(args);
        Section[] sections = SectionFactory.createSections(racers.length);

        System.out.println("Sections: ");
        for (Section section : sections) {
            System.out.println(section);
        }

        System.out.println('\n');

        System.out.println("Start the searching:  " + '\n');

        if (racers != null) {
            RobotFactory.startRobots(racers, sections);
        }
    }
}
