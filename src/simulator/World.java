package simulator;
/**
 * COMP261 Assignment 4 2012
 *
 * Written by Alex Potanin, modified by Pondy
 * NB! No need to modify this code!
 **/

import java.awt.Graphics;
import java.awt.Color;
import javax.swing.*;
import java.util.*;
import java.io.*;

class Robot {
    public double x;
    public double y;
    public int id;
    public int numThings;
    public double angleFromTop;
    public RobotProgram p;

    public final static double d = 20;
}

class Wall {
    public double x1, y1, x2, y2;
}

class Thing {
    public double x;
    public double y;

    public final static double d = 10;
}

class Box {
    public double x;
    public double y;
    public int numThings;

    public final static double d = 25;
}

public class World {
    private boolean validWorldLoaded = false;
    private int width = 800;
    private int height = 600;
    private ArrayList<Robot> robots = new ArrayList<Robot>();
    private ArrayList<Wall> walls = new ArrayList<Wall>();
    private ArrayList<Thing> things = new ArrayList<Thing>();
    private ArrayList<Box> boxes = new ArrayList<Box>();
    private Stack<UndoAction> backtrack = new Stack<UndoAction>();
    static final int MoveTimestep = 1;
    static final int TurnTimestep = 1;


    public int getWidth() {
        return this.width;
        
    }

    public int getHeight() {
        return this.height;
    }

    public ArrayList<Integer> getRobotIDs() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Robot r : this.robots) {
            result.add(r.id);
        }
        return result;
    }

    public ArrayList<Integer> getRobotIDsWithValidProgramsLoaded() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Robot r : this.robots) {
            if (r.p.validProgramLoaded()) {
                result.add(r.id);
            }
        }
        return result;
    }

    public boolean validWorldLoaded() {
        return this.validWorldLoaded;
    }
    
    public boolean loadWorld(String fileName) {
        Scanner s = null;
        try {
            s = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Cannot load world file. Not found.",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            if (s!=null) s.close();
            return false;
        }

        int originalWidth = this.width;
        int originalHeight = this.height;
        ArrayList<Robot> originalRobots = this.robots;
        ArrayList<Wall> originalWalls = this.walls;
        ArrayList<Thing> originalThings = this.things;
        ArrayList<Box> originalBoxes = this.boxes;
        
        this.width = -1;
        this.height = -1;
        this.robots = new ArrayList<Robot>();
        this.walls = new ArrayList<Wall>();
        this.things = new ArrayList<Thing>();
        this.boxes = new ArrayList<Box>();

        int line = 0;
        int robotID = 1;

        class LoadWorldException extends Exception {
            public String errorTitle;
            public String errorMessage;
            public LoadWorldException(String errorTitle, String errorMessage) {
                this.errorTitle = errorTitle;
                this.errorMessage = errorMessage;
            }                
        }
        
        try {
            while (s.hasNextLine()) {
                line++;
                
                if (!s.hasNext()) { s.nextLine(); continue; } // Skip blank lines.
                
                String value = s.next();
                if (value.equals("WIDTH:")) {
                    if (this.width != -1)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Duplicate width entry!");
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing width entry!");
                    int width = s.nextInt();
                    if (width < 800)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Width has to be at least 800!");
                    this.width = width;
                } else if (value.equals("HEIGHT:")) {
                    if (this.height != -1)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Duplicate height entry!");
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing height entry!");
                    int height = s.nextInt();
                    if (height < 600)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Height has to be at least 600!");
                    this.height = height;
                } else if (value.equals("ROBOT:")) {
                    Robot r = new Robot();
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing robot entry!");
                    r.x = s.nextInt();
                    if (r.x < r.d/2 || r.x + r.d/2 > this.width)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: robot x entry out of range!");
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing robot entry!");
                    r.y = s.nextInt();
                    if (r.y < r.d/2 || r.y + r.d/2 > this.height)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: robot y entry out of range!");
                    r.id = robotID++;
                    r.numThings = 0;
                    r.angleFromTop = 0;
                    r.p = new RobotProgram();
                    this.robots.add(r);
                } else if (value.equals("WALL:")) {
                    Wall w = new Wall();
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing wall entry!");
                    w.x1 = s.nextInt();
                    if (w.x1 < 0 || w.x1 > this.width)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: wall x1 entry out of range!");
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing wall entry!");
                    w.y1 = s.nextInt();
                    if (w.y1 < 0 || w.y1 > this.height)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: wall y1 entry out of range!");
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing wall entry!");
                    w.x2 = s.nextInt();
                    if (w.x2 < 0 || w.x2 > this.width)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: wall x2 entry out of range!");
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing wall entry!");
                    w.y2 = s.nextInt();
                    if (w.y2 < 0 || w.y2 > this.height)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: wall y2 entry out of range!");
                    this.walls.add(w);
                } else if (value.equals("THING:")) {
                    Thing t = new Thing();
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing thing entry!");
                    t.x = s.nextInt();
                    if (t.x < t.d/2 || t.x + t.d/2 > this.width)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: thing x entry out of range!");
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing thing entry!");
                    t.y = s.nextInt();
                    if (t.y < t.d/2 || t.y + t.d/2 > this.height)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: thing y entry out of range!");
                    this.things.add(t);
                } else if (value.equals("BOX:")) {
                    Box b = new Box();
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing box entry!");
                    b.x = s.nextInt();
                    if (b.x < b.d/2 || b.x + b.d/2 > this.width)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: box x entry out of range!");
                    if (!s.hasNextInt())
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error parsing box entry!");
                    b.y = s.nextInt();
                    if (b.y < b.d/2 || b.y + b.d/2> this.height)
                        throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                     "Error: box y entry out of range!");
                    b.numThings = 0;
                    this.boxes.add(b);
                } else
                    throw new LoadWorldException("Error Parsing World (Line: " + line + ")",
                                                 "Unrecognised entry in the world file: " + value);
            }
            
            for (Wall w : this.walls) {
                for (Robot r : this.robots)
                    if (lineIntersectsCircle(w.x1, w.y1, w.x2, w.y2, r.x, r.y, r.d))
                        throw new LoadWorldException("Error Validating World",
                                                     "Robot (ID: " + r.id + ") intersects one of the walls!");
                for (Thing t : this.things)
                    if (lineIntersectsCircle(w.x1, w.y1, w.x2, w.y2, t.x, t.y, t.d))
                        throw new LoadWorldException("Error Validating World",
                                                     "Thing (X: " + t.x + ", Y: " + t.y +
                                                     ") intersects one of the walls!");
                for (Box b : this.boxes)
                    if (lineIntersectsCircle(w.x1, w.y1, w.x2, w.y2, b.x, b.y, b.d))
                        throw new LoadWorldException("Error Validating World",
                                                     "Box (X: " + b.x + ", Y: " + b.y +
                                                     ") intersects one of the walls!");
            }

            for (Robot r : this.robots) {
                for (Thing t : this.things)
                    if (circlesIntersect(r.x, r.y, r.d, t.x, t.y, t.d))
                        throw new LoadWorldException("Error Validating World",
                                                     "Robot (ID: " + r.id + ") intersects one of the things!");
                for (Box b : this.boxes)
                    if (circlesIntersect(r.x, r.y, r.d, b.x, b.y, b.d))
                        throw new LoadWorldException("Error Validating World",
                                                     "Robot (ID: " + r.id + ") intersects one of the boxes!");
            }
            
            for (Thing t : this.things)
                for (Box b : this.boxes)
                    if (circlesIntersect(t.x, t.y, t.d, b.x, b.y, b.d))
                        throw new LoadWorldException("Error Validating World",
                                                     "Thing (X: " + t.x + ", Y: " + t.y +
                                                     ") intersects one of the boxes!");

            if (this.robots.size() < 1) {
                throw new LoadWorldException("Error Validating World",
                                             "At least one ROBOT is required! None found.");
            }

            if (this.boxes.size() < 1) {
                throw new LoadWorldException("Error Validating World",
                                             "At least one BOX is required! None found.");
            }
        } catch (LoadWorldException e) {
            this.validWorldLoaded = false;
            this.width = originalWidth;
            this.height = originalHeight;
            this.robots = originalRobots;
            this.walls = originalWalls;
            this.things = originalThings;
            this.boxes = originalBoxes;
            if (s!=null) s.close();
            JOptionPane.showMessageDialog(null, e.errorMessage, e.errorTitle, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        this.addOutsideWalls();
        this.validWorldLoaded = true;
        if (s!=null) s.close();
        return true;
    }

    private void addOutsideWalls() {
    	Wall w1 = new Wall();
        w1.x1 = 0;
        w1.y1 = 0;
        w1.x2 = width;
        w1.y2 = 0;
        Wall w2 = new Wall();
        w2.x1 = 0;
        w2.y1 = 0;
        w2.x2 = 0;
        w2.y2 = height;
        Wall w3 = new Wall();
        w3.x1 = width;
        w3.y1 = 0;
        w3.x2 = width;
        w3.y2 = height;
        Wall w4 = new Wall();
        w4.x1 = 0;
        w4.y1 = height;
        w4.x2 = width;
        w4.y2 = height;
        this.walls.add(w1);
        this.walls.add(w2);
        this.walls.add(w3);
        this.walls.add(w4);
    }

	public void loadProgram(String fileName, int robotID, Graphics g) {
        File f = null;
        f = new File(fileName);

        for (Robot r : this.robots) {
            if (r.id == robotID) {
                RobotProgram rp = new RobotProgram();
                try {
                if (rp.parse(f)) {
                    r.p = rp; // Don't override a valid program (if any) with an invalid one!
                    JOptionPane.showMessageDialog(null, "Successfully loaded a program into robot ID: " +
                                                  robotID,
                                                  "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Error loading a program into robot ID: " + robotID,
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                }
                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(null, "Cannot load robot program file. Not found.",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                this.drawWorld(g);
                return;
            }
        }
    }
    public void backtrack(int robotID, Graphics g, int times){
    	while(times-- > 0 && backtrack.size() > 0){
    		UndoAction action = backtrack.pop();
    		action.execute(robotID, g);
    		backtrack.pop();
    	}
    }
    public void executeProgram(int robotID, Graphics g) {
        for (Robot r : this.robots) {
            if (r.id == robotID) {
                if (r.p.execute(r.id, this, g)) {
                    JOptionPane.showMessageDialog(null, "Successfully executed a program for robot ID: " +
                                                  robotID,
                                                  "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Error executing a program for robot ID: " + robotID,
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                }
                this.drawWorld(g);
                return;
            }
        }
    }

    private void drawWorld(Graphics g, double x1, double y1, double x2, double y2) {
        if (g == null) return;
        if (this.validWorldLoaded) {
            g.setColor(Color.WHITE);
            g.fillRect((int)Math.round(x1), (int)Math.round(y1),
		       (int)Math.round(x2-x1), (int)Math.round(y2-y1));

            // Now only draw things that cross the box! Remember that
            // only things inside the box could've moved by
            // assumption!
            for (Robot r : this.robots) {
                if (r.p.validProgramLoaded()) {
                    g.setColor(Color.ORANGE);
                } else {
                    g.setColor(Color.BLUE);
                }
                g.drawOval((int) Math.round(r.x - r.d/2), (int) Math.round(r.y - r.d/2),
			   (int) Math.round(r.d), (int) Math.round(r.d));
                g.drawString("ID: " + r.id + " (" + r.numThings + ")",
			     (int) Math.round(r.x-r.d),
			     (int) Math.round(r.y+r.d+2));
                
                // Figure out the line direction from r.angleFromTop.
                g.drawLine((int) Math.round(r.x - Math.sin(Math.PI*r.angleFromTop/180.0)*r.d/2.0),
                           (int) Math.round(r.y - Math.cos(Math.PI*r.angleFromTop/180.0)*r.d/2.0),
                           (int) Math.round(r.x),
                           (int) Math.round(r.y));
            }

            for (Wall w : this.walls) {
                g.setColor(Color.RED);
                g.drawLine((int) Math.round(w.x1), (int) Math.round(w.y1), (int) Math.round(w.x2), (int) Math.round(w.y2));
            }

            for (Thing t : this.things) {
                g.setColor(Color.GREEN);
                g.fillOval((int) Math.round(t.x - t.d/2), (int) Math.round(t.y - t.d/2), (int) Math.round(t.d), (int) Math.round(t.d));
            }

            for (Box b : this.boxes) {
                g.setColor(Color.BLACK);
                g.drawOval((int) Math.round(b.x - b.d/2), (int) Math.round(b.y - b.d/2), (int) Math.round(b.d), (int) Math.round(b.d));
                if (b.numThings < 10)
                    g.drawString("0" + b.numThings, (int) Math.round(b.x-6), (int) Math.round(b.y+5));
                else
                    g.drawString("" + b.numThings, (int) Math.round(b.x-6), (int) Math.round(b.y+5));
            }
            
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, this.width, this.height);
            g.setColor(Color.RED);
            g.drawString("No world is loaded.", 200, this.height/2);
        }
    }

    public void drawWorld(Graphics g) {
	drawWorld(g, 0, 0, this.width, this.height);
    }
   
   private boolean linesIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
      // Return false if either of the lines have zero length
      if (x1 == x2 && y1 == y2 ||
            x3 == x4 && y3 == y4){
         return false;
      }
      // Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III" book (http://www.graphicsgems.org/)
      double ax = x2-x1;
      double ay = y2-y1;
      double bx = x3-x4;
      double by = y3-y4;
      double cx = x1-x3;
      double cy = y1-y3;

      double alphaNumerator = by*cx - bx*cy;
      double commonDenominator = ay*bx - ax*by;
      if (commonDenominator > 0){
         if (alphaNumerator < 0 || alphaNumerator > commonDenominator){
            return false;
         }
      }else if (commonDenominator < 0){
         if (alphaNumerator > 0 || alphaNumerator < commonDenominator){
            return false;
         }
      }
      double betaNumerator = ax*cy - ay*cx;
      if (commonDenominator > 0){
         if (betaNumerator < 0 || betaNumerator > commonDenominator){
            return false;
         }
      }else if (commonDenominator < 0){
         if (betaNumerator > 0 || betaNumerator < commonDenominator){
            return false;
         }
      }
      if (commonDenominator == 0){
         // This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
         // The lines are parallel.
         // Check if they're collinear.
         double y3LessY1 = y3-y1;
         double collinearityTestForP3 = x1*(y2-y3) + x2*(y3LessY1) + x3*(y1-y2);   // see http://mathworld.wolfram.com/Collinear.html
         // If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
         if (collinearityTestForP3 == 0){
            // The lines are collinear. Now check if they overlap.
            if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 ||
                  x2 >= x3 && x2 <= x4 || x2 <= x3 && x2 >= x4 ||
                  x3 >= x1 && x3 <= x2 || x3 <= x1 && x3 >= x2){
               if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4 ||
                     y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4 ||
                     y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2){
                  return true;
               }
            }
         }
         return false;
      }
      return true;
   }
   
   public void turnTowardsClosestVisibleThing(int robotID, Graphics g){
	   double minDist = 5000.0;
	   Robot r = getRobot(robotID);
	   Thing closest = null;
	   for(Thing t : this.things){
		   if (distanceFromPointToPoint(r.x, r.y, t.x, t.y) < minDist && thingIsVisible(robotID, t)){
			   minDist = distanceFromPointToPoint(r.x, r.y, t.x, t.y);
			   closest = t;
		   }
	   }
	   if(closest != null){
		   
	        int newAngle = this.directionTowardsPoint(r.x, r.y, closest.x, closest.y);
            final int angle = newAngle -(int) Math.round(r.angleFromTop);
            this.turnRobot(r.id, angle, g);
            backtrack.add(new UndoAction(this) {
				public void execute(int robotID, Graphics g) {
					world.turnRobot(robotID, -angle, g);
				}
			});
	   }
		   
   }
   public boolean thingIsVisible(int robotID,  Thing t){
	   Robot r = getRobot(robotID);
	   for(Wall wall : this.walls){
		   if(linesIntersect(r.x, r.y, t.x, t.y, wall.x1, wall.y1, wall.x2, wall.y2))
			   return false;
	   }
	   return true;
   }
   
   public boolean canSeeThing(int robotID){
	   for(Thing t : this.things){
		  if(thingIsVisible(robotID, t))
			  return true;
	   }
	   return false;
   }
   
    private boolean lineIntersectsCircle(double x1, double y1, double x2, double y2,
                                         double x, double y, double d,
                                         boolean... tangentCountsAsIntersection) {
        // See:
        // http://mathworld.wolfram.com/Circle-LineIntersection.html
        // and check if intersection point is within segment or not,
        // since the original is for infinite line.

        // System.out.println("Checking if circle of diameter " + d + " at (" + x + "," + y + ") " +
        //                    " intersects line (" + x1 + "," + y1 + "," + x2 + "," + y2 + ")");
        
        // First make circle centre in (0,0) and adjust the line to be relative to that.
        x1 -= x;
        x2 -= x;
        y1 -= y;
        y2 -= y;

        // System.out.println("Checking if circle of diameter " + d + " at (" + x + "," + y + ") " +
        //                    " intersects line (" + x1 + "," + y1 + "," + x2 + "," + y2 + ")");

        // Now follow the instructions on MathWorld.
        double d_x = x2 - x1;
        double d_y = y2 - y1;
        double d_r_2 = d_x*d_x + d_y*d_y;
        double D = x1*y2 - x2*y1;

        double discriminant = (d/2)*(d/2)*d_r_2-D*D;

        if (discriminant < 0) {
            // System.out.println("NO");
            return false;
        }

        if (discriminant == 0) { // tangent!
            // System.out.println("TANGENT");
            if (tangentCountsAsIntersection.length == 0) {
                return false; // default is tangent does not count as intersection
            }
            for (boolean b : tangentCountsAsIntersection)
                if (b) {
                    break;
                } else {
                    return false; // tangent does not count as intersection
                }
        }

        // System.out.println("LINE DOES, BUT NEED TO CHECK SEGMENT!");
        
        // Need to check if the two intersection points are within
        // the segment or not.

        int sgn = (d_y < 0) ? -1 : 1;

        // System.out.println("d_x = " + d_x + " d_y = " + d_y + " d_r_2 = " + d_r_2 + " D = " + D);
        
        double xi1 = (D*d_y + sgn*d_x*Math.sqrt(discriminant))/d_r_2;
        double yi1 = (-D*d_x + Math.abs(d_y)*Math.sqrt(discriminant))/d_r_2;
        // System.out.println("POINT 1: " + xi1 + "," + yi1);

        boolean xInside = false;
        boolean yInside = false;
        if (x1 < x2) {
            xInside = (x1 -1 <= xi1 && xi1 -1 <= x2);
        } else {
            xInside = (x2 -1 <= xi1 && xi1 -1 <= x1);
        }
        if (y1 < y2) {
            yInside = (y1 -1 <= yi1 && yi1 -1 <= y2);
        } else {
            yInside = (y2 -1  <= yi1 && yi1 -1 <= y1);
        }
        if (xInside && yInside) return true;

        double xi2 = (D*d_y - sgn*d_x*Math.sqrt(discriminant))/d_r_2;
        double yi2 = (-D*d_x - Math.abs(d_y)*Math.sqrt(discriminant))/d_r_2;
        // System.out.println("POINT 2: " + xi2 + "," + yi2);

        if (x1 < x2) {
            xInside = (x1 - 1 <= xi2 && xi2 -1 <= x2);
        } else {
            xInside = (x2 - 1 <= xi2 && xi2 -1 <= x1);
        }
        if (y1 < y2) {
            yInside = (y1 - 1<= yi2 && yi2 -1 <= y2);
        } else {
            yInside = (y2 - 1 <= yi2 && yi2 -1 <= y1);
        }
        if (xInside && yInside) return true;

        return false;
    }

    private boolean circlesIntersect(double x1, double y1, double d1, double x2, double y2, double d2) {
        return ( (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2) )*4 < (d1 + d2)*(d1 + d2);
    }

    private boolean circlesClose(double x1, double y1, double d1, double x2, double y2, double d2) {
        return Math.hypot( (x1 - x2), (y1 - y2) ) < (d1 + d2)/2.0 + 2.0;
    }

    // http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
    private double distanceFromPointToLine(double x, double y, double x1, double y1, double x2, double y2) {
        return Math.abs((x2-x1)*(y1-y)-(x1-x)*(y2-y1))/Math.hypot((x2-x1),(y2-y1));
    }

    private double distanceFromPointToPoint(double x1, double y1, double x2, double y2) {
        return Math.hypot((x1 - x2),(y1 - y2) );
    }

    public void moveRobot(int id, int move, Graphics g) {
    	
        
        for (Robot r : this.robots) {
            if (r.id == id) {
                double x1 = r.x;
                double y1 = r.y;
                
                int pos = 0;

		double dx = Math.sin(Math.PI*r.angleFromTop/180.0);
		double dy = Math.cos(Math.PI*r.angleFromTop/180.0);

                movestep:
                while ((move > 0 && ++pos < move) || (move < 0 && --pos > move)) {
                    double oriX = r.x;
                    double oriY = r.y;

                    r.x = (x1 - pos*dx);
                    r.y = (y1 - pos*dy);

                    // Check for collisions before moving on!
                    if (r.x < r.d/2 || r.x > this.width-r.d/2 || r.y < r.d/2 || r.y > this.height-r.d/2) {
                        r.x = oriX; r.y = oriY; break movestep; // We need to stay within world!
                    }

                    for (Wall w : this.walls)
                        if (lineIntersectsCircle(w.x1, w.y1, w.x2, w.y2, r.x, r.y, r.d, true)) {
                            r.x = oriX; r.y = oriY; break movestep; // We need to avoid hitting walls!
                        }
                    
                    for (Thing t : this.things) {
                        if (circlesIntersect(r.x, r.y, r.d, t.x, t.y, t.d)) {
                            r.x = oriX; r.y = oriY; break movestep;
                        }
                    }

                    for (Box b : this.boxes) {
                        if (circlesIntersect(r.x, r.y, r.d, b.x, b.y, b.d)) {
                            r.x = oriX; r.y = oriY; break movestep;
                        }
                    }

                    for (Robot otherRobot : this.robots) {
                        if (otherRobot != r && circlesIntersect(r.x, r.y, r.d,
                                                                otherRobot.x, otherRobot.y, otherRobot.d)) {
                            r.x = oriX; r.y = oriY; break movestep;
                        }
                    }
                    
                    if (g != null) {
                        // Animate!
                        try {
                            Thread.sleep(MoveTimestep);
                            
                            int bound = 25;
                            this.drawWorld(g, r.x - bound, r.y - bound, r.x + bound, r.y + bound);
                        } catch (InterruptedException e) {}
                    }
                }
				final int reverse = -pos;

				backtrack.add(new UndoAction(this) {
					public void execute(int robotID, Graphics g) {
						world.moveRobot(robotID, reverse, g);
					}
				});
                this.drawWorld(g); // Final redraw is full one! Only animation cares about flicker!
                return;
            }
        }
    }

    public void turnRobot(int id, int turn, Graphics g) {
    	 final int undoTurn = -turn;
    	 backtrack.add(new UndoAction(this) {
				public void execute(int robotID, Graphics g) {
					world.turnRobot(robotID, undoTurn, g);
				}
			});
		if (turn < -180)
			turn += 360;
		else if (turn > 180)
			turn -= 360;
		
		
        for (Robot r : this.robots) {
            if (r.id == id) {
            	if(turn % 2 != 0){
        			turn--;
        			r.angleFromTop++;
        		}
                while (turn > 0) {
                    r.angleFromTop+=2;
                    turn-=2;


                    if (g != null) {
                        // Animate!
                        try {
                            Thread.sleep(10);
                            
                            int bound = 25;
                            this.drawWorld(g, r.x - bound, r.y - bound, r.x + bound, r.y + bound);
                        } catch (InterruptedException e) {}
                    }
                }
                while (turn < 0) {
                    r.angleFromTop-=2;
                    turn+=2;

                    if (g != null) {
                        // Animate!
                        try {
                            Thread.sleep(TurnTimestep);
                            
                            int bound = 25;
                            this.drawWorld(g, r.x - bound, r.y - bound, r.x + bound, r.y + bound);
                        } catch (InterruptedException e) {}
                    }
                }
                this.drawWorld(g); // Final redraw is full one! Only animation cares about flicker!
                return;
            }
        }
    }

    public void pickUp(int robotID, Graphics g) {
        for (Robot r : this.robots)
            if (r.id == robotID)
                for (Thing t : this.things) {
                    if (circlesClose(r.x, r.y, r.d, t.x, t.y, t.d) ) {
                        r.numThings++;
                        this.things.remove(t);
                        this.drawWorld(g);
                        return;
                    }
                }
    }

    public void drop(int robotID, Graphics g) {
        for (Robot r : this.robots)
            if (r.id == robotID)
                for (Box b : this.boxes) {
                    if (circlesClose(r.x, r.y, r.d, b.x, b.y, b.d) ) {
                        b.numThings += r.numThings;
                        r.numThings = 0;
                        this.drawWorld(g);
                        return;
                    }
                }
    }

    public boolean touchingWall(int robotID) {
        for (Robot r : this.robots)
            if (r.id == robotID) {
            	
                for (Wall w : this.walls) {
//                    if (distanceFromPointToLine(r.x, r.y, w.x1, w.y1, w.x2, w.y2) - r.d < 2.0) {
//                    	
//                        if (distanceFromPointToPoint(r.x, r.y, w.x1, w.y1) - r.d +
//                            distanceFromPointToPoint(r.x, r.y, w.x2, w.y2) - r.d <
//                            distanceFromPointToPoint(w.x1, w.y1, w.x2, w.y2) + 3.0) {
//                            return true;
//                        }
//                    }
                    // This is not going to be working for doubles:
                     if (lineIntersectsCircle(w.x1, w.y1, w.x2, w.y2, r.x, r.y, r.d + 2, true)) {
                        return true;
                     }
                }
                return false;
            }
        return false;
    }

    public boolean touchingThing(int robotID) {
        for (Robot r : this.robots)
            if (r.id == robotID) {
                for (Thing t : this.things) {
                    if (circlesClose(r.x, r.y, r.d, t.x, t.y, t.d) ) {
                        return true;
                    }
                }
                return false;
            }
        return false;
    }

    public boolean touchingBox(int robotID) {
        for (Robot r : this.robots)
            if (r.id == robotID) {
                for (Box b : this.boxes) {
                    if (circlesClose(r.x, r.y, r.d, b.x, b.y, b.d) ) {
                        return true;
                    }
                }
                return false;
            }
        return false;
    }

    public boolean touchingRobot(int robotID) {
        for (Robot r : this.robots)
            if (r.id == robotID) {
                for (Robot other : this.robots) {
                    if (r != other && circlesClose(r.x, r.y, r.d, other.x, other.y, other.d) ) {
                        return true;
                    }
                }
                return false;
            }
        return false;
    }

    public int numberOfThingsOnGround() {
        return this.things.size();
    }

    public void turnTowardsFirstBox(int robotID, Graphics g) {
        for (Robot r : this.robots)
            if (r.id == robotID) {
                Box b = this.boxes.get(0);
                int newAngle = this.directionTowardsPoint(r.x, r.y, b.x, b.y);
                final int angle = newAngle -(int) Math.round(r.angleFromTop);
                this.turnRobot(r.id, angle, g);
                backtrack.add(new UndoAction(this) {
					public void execute(int robotID, Graphics g) {
						world.turnRobot(robotID, -angle, g);
					}
				});
            }
    }

    public void turnTowardsFirstThing(int robotID, Graphics g) {
        if (this.things.size() == 0) return;
        for (Robot r : this.robots)
            if (r.id == robotID) {
                Thing t = this.things.get(0);
                int newAngle = this.directionTowardsPoint(r.x, r.y, t.x, t.y);
                final int angle = newAngle -(int) Math.round(r.angleFromTop);
                this.turnRobot(r.id, angle, g);
                backtrack.add(new UndoAction(this) {
					public void execute(int robotID, Graphics g) {
						world.turnRobot(robotID, -angle, g);
					}
				});
            }
    }

    private int directionTowardsPoint(double xOrigin, double yOrigin, double xTarget, double yTarget) {
        int angle1 = (int) Math.round(Math.acos((yOrigin - yTarget) /
                                               this.distanceFromPointToPoint(xOrigin, yOrigin,
                                                                             xTarget, yTarget)
                                               ) * 180.0 / Math.PI);
        int angle2 = (int) Math.round(Math.asin((xOrigin - xTarget) /
                                               this.distanceFromPointToPoint(xOrigin, yOrigin,
                                                                             xTarget, yTarget)
                                               ) * 180.0 / Math.PI);
        // System.out.println("angle1 = " + angle1 + " angle2 = " + angle2);
        return (angle2 > 0) ? angle1 : -angle1;
    }

    public int distanceToFirstThing(int robotID) {
        if (this.things.size() == 0) return -1;
        for (Robot r : this.robots)
            if (r.id == robotID) {
                Thing t = this.things.get(0);
                return (int) Math.round(this.distanceFromPointToPoint(r.x, r.y, t.x, t.y));
            }
        return -1;
    }
    public void turnTowardsClosestThing(int robotID, Graphics g) {
        if (this.things.size() == 0) return;
        for (Robot r : this.robots)
            if (r.id == robotID) {
            	Thing t = closestThing(robotID);
                int newAngle = this.directionTowardsPoint(r.x, r.y, t.x, t.y);
                final int angle = newAngle -(int) Math.round(r.angleFromTop);
                this.turnRobot(r.id, angle, g);
                backtrack.add(new UndoAction(this) {
					public void execute(int robotID, Graphics g) {
						world.turnRobot(robotID, -angle, g);
					}
				});
            }
    }

 

    public Thing closestThing(int robotID){
    	Thing closest = null;
    	if (this.things.size() == 0) return closest;
    	int dist = 5000;
        for (Robot r : this.robots)
            if (r.id == robotID) {
            	for(Thing t : this.things){
                int distToThing = (int) Math.round(this.distanceFromPointToPoint(r.x, r.y, t.x, t.y));
                if (distToThing < dist){
                		closest = t;
                		dist = distToThing;
                	}
            	}
            	break;
            }
        return closest;
    }
    public int distanceToClosestThing(int robotID) {
        if (this.things.size() == 0) return -1;
        for (Robot r : this.robots)
            if (r.id == robotID) {
                Thing t = closestThing(robotID);
                return (int) Math.round(this.distanceFromPointToPoint(r.x, r.y, t.x, t.y));
            }
        return -1;
    }


    public int distanceToFirstBox(int robotID) {
        for (Robot r : this.robots)
            if (r.id == robotID) {
                Box b = this.boxes.get(0);
                return (int) Math.round(this.distanceFromPointToPoint(r.x, r.y, b.x, b.y));
            }
        return -1;
    }
    public int distanceToVisibleThing(int robotID){
    	Robot r = getRobot(robotID);
    	Thing t = closestThing(robotID);
    	if (t != null)
    		return (int) Math.round(this.distanceFromPointToPoint(r.x, r.y, t.x, t.y));
    	return 0;
    }
    public Wall adjacentWall(int robotID){
    	 for (Robot r : this.robots)
             if (r.id == robotID) {
             	
                 for (Wall w : this.walls) {
                	 if(lineIntersectsCircle(w.x1, w.y1, w.x2, w.y2, r.x, r.y, r.d + 2, true))     {
                		 return w;
                         }
                     }
                 }
                 
             
    	 return null;
    }
    public Robot getRobot(int robotID){
    	for (Robot r : this.robots)
            if (r.id == robotID) {
            	return r;
            }
    	return null;
    }
    
    /**
     * Calculates the slope of the wall and a path alongside it for the robot
     * using the left hand rule.
     * The reverse boolean ensures the robot travels un the reverse direction.
     * @param robotID
     * @param g
     * @param reverse
     */
	public void travelAlongWall(int robotID, Graphics g, boolean reverse) {
		Wall w = adjacentWall(robotID);
		Robot r = getRobot(robotID);
		double ySlope, xSlope;
		
		try{
			
			/**
			 * Calculate line info
			 */
			xSlope = ((w.y2 - w.y1)/(w.x2 - w.x1));
			ySlope = ((w.x2 - w.x1)/(w.y2 - w.y1));
			double interceptX = w.y1 - xSlope * w.x1;
			double interceptY = w.x1 - ySlope * w.y1;
			double lineY = r.x * xSlope + interceptX;
			double lineX = r.y * ySlope + interceptY;
			double offsetX = r.x - lineX;
			double offsetY = r.y - lineY;
			boolean toFurthest = false;
			double gotoX, gotoY;
			double spaceX, spaceY, turn;
			double spacer = r.d + 2;
		    spaceX = -spacer;
            spaceY = -spacer;
            turn = 90;
            
            //Decide where we are heading.
            if(Double.isNaN(offsetX)){
                
                if(offsetY > 0){
                    System.out.println("Going right");
                    toFurthest = true;
                    spaceY = spacer;
                    spaceX = 0;
                
                }else{
                    System.out.println("Going left");
                    spaceY = -spacer;
                    spaceX = 0;
                }
            }
            else if(offsetX > 0){
                spaceX = spacer;
                if(offsetY > 0)
                {
                    toFurthest = true;
                    spaceY = spacer;
                }
                if(Double.isNaN(offsetY)){
                    System.out.println("Going up");
                    spaceX = spacer;
                    spaceY = 0;
                    toFurthest = true;
                    }
            }else if(offsetX < 0){
                
                if(offsetY > 0){
                    toFurthest = true;
                    spaceY = spacer;
                }
                if(Double.isNaN(offsetY)){
                    System.out.println("Going down");
                    spaceX = -spacer;
                    spaceY = 0;
                    toFurthest = false;
                }
            }
            if(reverse){
                toFurthest = ! toFurthest;
                turn *= -1;
            }
            
            //Get the coordinates of our end point.
            if(toFurthest){
                if(w.x2 > w.x1){
                    gotoX = w.x2;
                    gotoY = w.y2;
                }else if(w.x2 == w.x1){
                    if(w.y1 > w.y2){
                        gotoX = w.x2;
                        gotoY = w.y2;
                    }else{
                        gotoX = w.x1;
                        gotoY = w.y1;
                    }
                }else{
                    gotoX = w.x1;
                    gotoY = w.y1;
                }
            }else{
                if(w.x2 < w.x1){
                    gotoX = w.x2;
                    gotoY = w.y2;
                }else if(w.x2 == w.x1){
                    if(w.y1 > w.y2){
                        gotoX = w.x1;
                        gotoY = w.y1;
                    }else{
                        gotoX = w.x2;
                        gotoY = w.y2;
                    }
                }else{
                    gotoX = w.x1;
                    gotoY = w.y1;
                }
            }
			gotoX += spaceX/2;
			gotoY += spaceY/2;
			
			
			//Move to the end.
			moveRobot(robotID, -5, g);
			int angle = ((int) directionTowardsPoint(r.x, r.y, gotoX, gotoY) -(int) Math.round(r.angleFromTop));
			turnRobot(robotID,  angle, g);
			moveRobot(robotID, (int) ((int)distanceFromPointToPoint(r.x, r.y, gotoX, gotoY) + r.d), g);
			if(!touchingWall(robotID)){
				turnRobot(robotID,  (int) turn, g);
				moveRobot(robotID, (int) (2* r.d), g);
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
	}
}
