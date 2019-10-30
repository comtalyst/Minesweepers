package neuralnet2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

/*
Notes
	complete? Dec 13
	Neural Network will determine whether the agent should turn left or right (this means there are 2 neurons at the end)
 */

public class AgentMS {
	private NeuralNetwork brain;	//each agent has a brain (neural net)
	private Point2D position;		//where the agent is on the map
	private Point2D facing;			//which way they're facing (used as inputs) as an (x, y) pair
	public double rotation;		//the angle from which facing is calculated
	private double speed;			//the speed of the agent
	private double lTrack, rTrack;  //the influence rating toward turning left and turning right, used as outputs
	private double fitness;			//how well the agent is doing, quantified (for the genetic algorithm)
	private double scale; 			//the size of the agent
	private int closestGood;		//the index in the goods list of the good closest to the agent (used to determine inputs for the neural net)
	private int closestEvil;		//the index in the evils list of the evil closest to the agent (used to determine inputs for the neural net)
	
	public AgentMS() { //initialization
		Random rnd = new Random();
		brain = new NeuralNetwork(Params.INPUTS, Params.OUTPUTS, Params.HIDDEN, Params.NEURONS_PER_HIDDEN);
		rotation = rnd.nextDouble() * Math.PI * 2;
		lTrack = 0.16;
		rTrack = 0.16;
		fitness = 100;
		scale = ControllerMS.SCALE;
		closestGood = 0;
		closestEvil = 0;
		position = new Point2D.Double(rnd.nextDouble() * Params.WIN_WIDTH, rnd.nextDouble() * Params.WIN_HEIGHT);
		facing = new Point2D.Double(-Math.sin(rotation), Math.cos(rotation));
	}
	
	public boolean update(ArrayList<Point2D> goods, ArrayList<Point2D> evils) {		//updates all the parameters of the sweeper, sounds fairly important
		ArrayList<Double> inputs = new ArrayList<Double>();
		//find the closest mine, figure out the direction the mine is from the sweeper's perspective by creating a unit vector
		//your code goes here [OK?]
		getClosestGood(goods);
		getClosestEvil(evils);
		//create the inputs for the neural net
		//your code goes here [OK?]
		inputs.add(facing.getX()); inputs.add(facing.getY());

		Point2D rPos = new Point2D.Double(goods.get(closestGood).getX() - position.getX(),goods.get(closestGood).getY() - position.getY());
		double dist = Math.sqrt(Math.pow(goods.get(closestGood).getX() - position.getX(),2) + Math.pow(goods.get(closestGood).getY() - position.getY(),2));
		inputs.add(rPos.getX()/dist); inputs.add(rPos.getY()/dist);		// uses facing direction and nearest good vector as input

		rPos = new Point2D.Double(evils.get(closestEvil).getX() - position.getX(),evils.get(closestEvil).getY() - position.getY());
		dist = Math.sqrt(Math.pow(evils.get(closestEvil).getX() - position.getX(),2) + Math.pow(evils.get(closestEvil).getY() - position.getY(),2));
		inputs.add(rPos.getX()/dist); inputs.add(rPos.getY()/dist);		// uses facing direction and nearest evil vector as input
		// get outputs from the sweeper's brain
		ArrayList<Double> output = brain.Update(inputs);
		if (output.size() < Params.OUTPUTS) {
			System.out.println("Incorrect number of outputs.");
			return false; //something went really wrong if this happens
		}

		//turn left or turn right?
		lTrack = output.get(0);
		rTrack = output.get(1);
		/*System.out.print(lTrack);
		System.out.print(" ");
		System.out.println(rTrack);*/
		double rotationForce = lTrack - rTrack;
		rotationForce = Math.min(ControllerMS.MAX_TURN_RATE, Math.max(rotationForce,  -ControllerMS.MAX_TURN_RATE)); //clamp between lower and upper bounds
		rotation += rotationForce;

		//update the speed and direction of the sweeper
		speed = Math.min(ControllerMS.MAX_SPEED, lTrack + rTrack);
		facing.setLocation(-Math.sin(rotation), Math.cos(rotation));
		
		//then update the position, torus style
		double xPos = (Params.WIN_WIDTH + position.getX() + facing.getX() * speed) % Params.WIN_WIDTH;
		double yPos = (Params.WIN_HEIGHT + position.getY() + facing.getY() * speed) % Params.WIN_HEIGHT;
		position.setLocation(xPos, yPos);
		return true;
	}

	public Point2D getClosestGood(ArrayList<Point2D> goods) { //finds the good closest to the sweeper
		double closestSoFar = 99999999;
		Point2D closestObject = new Point2D.Double(0,0);
		double length;
		for (int i = 0; i < goods.size(); i++) {
			length = Point2D.distanceSq(goods.get(i).getX(), goods.get(i).getY(), position.getX(), position.getY());
			if (length < closestSoFar) {
				closestSoFar = length;
				closestObject = new Point2D.Double(goods.get(i).getX(), goods.get(i).getY());
				closestGood = i;
			}
		}
		return closestObject;
	}
	public Point2D getClosestEvil(ArrayList<Point2D> evils) { //finds the evil closest to the sweeper
		double closestSoFar = 99999999;
		Point2D closestObject = new Point2D.Double(0,0);
		double length;
		for (int i = 0; i < evils.size(); i++) {
			length = Point2D.distanceSq(evils.get(i).getX(), evils.get(i).getY(), position.getX(), position.getY());
			if (length < closestSoFar) {
				closestSoFar = length;
				closestObject = new Point2D.Double(position.getX() - evils.get(i).getX(), position.getY() - evils.get(i).getY());
				closestEvil = i;
			}
		}
		return closestObject;
	}

	public int checkForGood(ArrayList<Point2D> goods, double size) { //has the sweeper actually swept up the closest good to it this tick?
		if (Point2D.distance(position.getX(), position.getY(), goods.get(closestGood).getX(), goods.get(closestGood).getY()) < (size + scale / 2)) {
			return closestGood;
		}
		return -1;
	}
	public int checkForEvil(ArrayList<Point2D> evils, double size) { //has the sweeper actually swept up the closest evil to it this tick?
		if (Point2D.distance(position.getX(), position.getY(), evils.get(closestEvil).getX(), evils.get(closestEvil).getY()) < (size + scale / 2)) {
			return closestEvil;
		}
		return -1;
	}
	
	public void reset() {	//reinitialize this sweeper's position/direction values
		Random rnd = new Random();
		rotation = rnd.nextDouble() * Math.PI * 2;
		position = new Point2D.Double(rnd.nextDouble() * Params.WIN_WIDTH, rnd.nextDouble() * Params.WIN_HEIGHT);
		facing = new Point2D.Double(-Math.sin(rotation), Math.cos(rotation));
		fitness = 100;
	}
	
	public void draw(Graphics2D g) {	//draw the sweeper in its correct place
		AffineTransform at = g.getTransform(); //affine transforms are a neat application of matrix algebra
		g.rotate(rotation, position.getX(), position.getY()); //they allow you to rotate a g.draw kind of function's output
		//draw the sweeper using a fancy color scheme
		g.setColor(new Color(255, 255, 0));
		g.drawRect((int)(position.getX() - scale / 2), (int)(position.getY()-scale / 2), (int)scale,  (int)scale);
		g.setColor(new Color(0, Math.min(255, (int)fitness+(int)(fitness-100)*12), Math.min(255, (int)fitness+(int)(fitness-100)*12)));
		g.fillRect((int)(position.getX() - scale / 2)+1, (int)(position.getY()-scale / 2)+1, (int)scale-2,  (int)scale-2);


		//draw the direction it's facing
		g.setColor(new Color(255, 0, 255));		
		g.drawLine((int)(position.getX()), (int)(position.getY()), (int)(position.getX() - scale / 2 + facing.getX()*scale), (int)(position.getY() - scale / 2 + facing.getY()*scale));
		g.setTransform(at); //set the transform back to the normal transform
		//draw its fitness
		g.setColor(new Color(0, 255, 255));
		g.drawString("" + fitness, (int)position.getX() - (int)(scale / 2), (int)position.getY() +2*(int)scale);
		
		//you're welcome to alter the drawing, I just wanted something simple and quasi-functional
	}
	
	//simple functions 
	public Point2D getPos() { return position; }
	public void incrementFitness() { fitness++; } //this may need to get more elaborate pending what you would want sweepers to learn...
	public void decrementFitness() { fitness--; if(fitness < 0){fitness = 0;} } //this may need to get more elaborate pending what you would want sweepers to learn...
	public double getFitness() { return fitness; }
	public void putWeights(ArrayList<Double> w) { brain.replaceWeights(w); }
	public int getNumberOfWeights() { return brain.getNumberOfWeights(); }
}
