import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;

public class DeadlockDetectionResolution implements Directions {
    private static final Lock intersectionLock = new ReentrantLock();
	private static final List<RobotOp> robotsInIntersection = new ArrayList<RobotOp>();    

	public static void main(String[] args) {
        // Set up the world
        World.setSize(10, 10);
        World.setVisible(true);
		World.setDelay(20);
		World.showSpeedControl(true, true); //Needed to make them start

        // Create 10 robots, all heading to the intersection at (5, 5)
        RobotOp[] robots = new RobotOp[10];
		
        for (int i = 0; i < 10; i++) {
			robots[i] = new RobotOp(1, 8, East, 0); // Place robots along the first row
        }

        // Start all robots towards the intersection
        for (RobotOp robot : robots) {
            new Thread(() -> {
				boolean cruzar = false;
				
				while(!robot.facingNorth())
					robot.turnLeft();
                while (true) {
                    if ((robot.street() == 5 && robot.avenue() == 4 && robot.facingEast()) || (robot.street() == 5 && robot.avenue() == 6 && robot.facingWest()) || (robot.street() == 4 && robot.avenue() == 5 && robot.facingNorth()))
					{
                        try {
                            // Request access to the intersection
                            intersectionLock.lock();
							robotsInIntersection.add(robot);
							if (robotsInIntersection.size() > 1)
							{
								System.out.println("Deadlock detected! Resolving...");
								robot.turnLeft();
								robot.turnLeft();
								robot.move();
								robot.move();
							}
							else
							{
								System.out.println("Entrando a la intersección");
								// Move into the intersection
								robot.move();
								// Exit the intersection
								robot.move();
								cruzar = true;
							}
                        } finally {
							System.out.println("finally");

                            // Release the lock after exiting the intersection
                            // Release the lock after exiting the intersection
                            robotsInIntersection.remove(robot);
                            intersectionLock.unlock();
                        }
						if(cruzar)
							break;
                    }
					else
						robot.move();
					if (robot.street() < 5 && !robot.facingNorth())
                    {
						while(!robot.facingNorth())
							robot.turnLeft();
					}
                    if (robot.street() == 5 && robot.avenue() < 5 && !robot.facingEast())
                    {
						while(!robot.facingEast())
							robot.turnLeft();
					}
                    if (robot.street() == 5 && robot.avenue() > 5 && !robot.facingWest())
                    {
						while(!robot.facingWest())
							robot.turnLeft();
					}
				}
            }).start();
        }
    }
}

class RobotOp extends Robot implements Directions {
	public RobotOp(int street, int avenue, Direction direction, int beeps)
	{
		super(street, avenue, direction, beeps);
	}
	
	public RobotOp(int street, int avenue, Direction direction, int beeps, Color color)
	{
		super(street, avenue, direction, beeps, color);
	}
	
	public int street()
	{
		String mensaje = this.toString();
		int posicion = mensaje.indexOf("street: ");
		int posFinal = mensaje.indexOf(")", posicion);
		return Integer.valueOf(mensaje.substring(posicion+8, posFinal));
	}
	
	public int avenue()
	{
		String mensaje = this.toString();
		int posicion = mensaje.indexOf("avenue: ");
		int posFinal = mensaje.indexOf(")", posicion);
		return Integer.valueOf(mensaje.substring(posicion+8, posFinal));
	}
}
