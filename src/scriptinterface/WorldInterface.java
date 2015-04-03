package scriptinterface;

import java.awt.Graphics;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import simulator.World;

/**
 * The WorldInterface is simply
 * a collection of maps between world values and built-in script constants.
 * THere are 3 types of built-ins.
 * 
 * Booleans, Integers and Functions
 * @author wouterken
 *
 */
abstract class WorldBool {	   abstract Boolean evaluate(int robotID, World world, Graphics g); }
abstract class WorldInt { 	   abstract int evaluate(int robotID, World world, Graphics g);		}
abstract class WorldAction {  abstract void evaluate(int robotID, World world, Graphics g, int value); }

public class WorldInterface {
	public static Map<String, WorldBool> worldBooleans = Collections.unmodifiableMap(new HashMap<String, WorldBool>() {
		private static final long serialVersionUID = 1L;
	{
        put("touching_thing", new WorldBool() { Boolean evaluate(int robotID, World world, Graphics g) {
        	return world.touchingThing(robotID);}});
        put("touching_box", new WorldBool() { Boolean evaluate(int robotID, World world, Graphics g) {
        	return world.touchingBox(robotID);}});
        put("touching_robot", new WorldBool() { Boolean evaluate(int robotID, World world, Graphics g) {
        	return world.touchingRobot(robotID);}});
        put("touching_wall", new WorldBool() { Boolean evaluate(int robotID, World world, Graphics g) {
        	return world.touchingWall(robotID);}});
        put("thing_is_visible", new WorldBool() { Boolean evaluate(int robotID, World world, Graphics g) {
        	return world.canSeeThing(robotID);}});
		 put("true", new WorldBool() { Boolean evaluate(int robotID, World world, Graphics g) {return true;}});
		 put("false", new WorldBool() { Boolean evaluate(int robotID, World world, Graphics g) {return false;}});
	     }
	});
	

	
	public static Map<String, WorldInt> worldInts = Collections.unmodifiableMap(new HashMap<String, WorldInt>() {
		private static final long serialVersionUID = 1L;
	{
        put("number_of_things_on_ground", new WorldInt() { int evaluate(int robotID, World world, Graphics g) {
        	return world.numberOfThingsOnGround();}});
        put("distance_to_first_thing", new WorldInt() { int evaluate(int robotID, World world, Graphics g) {
        	return world.distanceToFirstThing(robotID);}});
        put("distance_to_closest_thing", new WorldInt() { int evaluate(int robotID, World world, Graphics g) {
        	return world.distanceToClosestThing(robotID);}});
        put("distance_to_first_box", new WorldInt() { int evaluate(int robotID, World world, Graphics g) {
        	return world.distanceToFirstBox(robotID);}});
        put("distance_to_visible_thing", new WorldInt() { int evaluate(int robotID, World world, Graphics g) {
        	return world.distanceToFirstBox(robotID);}});
        }
	});
	
	public	static Map<String, WorldAction> worldActions = Collections.unmodifiableMap(new HashMap<String, WorldAction>() {
		private static final long serialVersionUID = 1L;
	{
        put("turn", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
				world.turnRobot(robotID, value, g);}});
        put("pickup", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.pickUp(robotID, g);}});
        put("drop", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.drop(robotID, g);}});
        put("move", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.moveRobot(robotID, value, g);}});
        put("backtrack", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.backtrack(robotID, g, value);}});
		put("turn_towards_first_thing", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.turnTowardsFirstThing(robotID, g);}});
		put("turn_towards_first_box", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.turnTowardsFirstBox(robotID, g);}});
		put("turn_towards_closest_thing", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.turnTowardsClosestThing(robotID, g);}});
		put("travel_along_wall", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.travelAlongWall(robotID, g, false);}});
		put("reverse_along_wall", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.travelAlongWall(robotID, g, true);}});
		put("turn_towards_visible_thing", new WorldAction() {void evaluate(int robotID, World world, Graphics g, int value) {
			world.turnTowardsClosestVisibleThing(robotID, g);}});
	    }
	});
}
