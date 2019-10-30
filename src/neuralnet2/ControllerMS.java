package neuralnet2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import neuralnet2.GeneticAlg.Genome;

@SuppressWarnings("serial")
public class ControllerMS extends JPanel implements ActionListener {

	private Timer timer;					//timer runs the simulation
	private int ticks;						//tick counter for a run of a generation's agents
	private int numGoods;					//the number of goods to use in the simulator
	private int numEvils;					//the number of evils to use in the simulator
	private int generations;				//the counter for which generation the sim's on
	private int numAgents;					//how many agents
	private int numWeights;					//how many weights an agent has
//	private ArrayList<Double> avgFitness; 	//useful if you were plotting the progression of fitness
//	private ArrayList<Double> bestFitness;
	private GeneticAlg GA;					//the genetic algorithm that manages the genome weights
	private ArrayList<Genome> pop;			//the weights of the neural nets for each of the agents
	private ArrayList<AgentMS> agents;		//the agents themselves (the sweepers)
	private ArrayList<Point2D> goods;		//the goods
	private ArrayList<Point2D> evils;		//the evils
	private BufferedImage pic;				//the image in which things are drawn
	private JLabel picLabel;				//the label that holds the image
	private JLabel dataLabel;				//the label that holds the fitness information
	
	//these are specific to the mine sweeping scenario
	//for the controller to run the whole simulation
	public static final int GOODS = 15;
	public static final int EVILS = 15;
	public static final int SWEEPERS = 16;
	public static final int TICKS = 1200;				//how long agents have a chance to gain fitness
	public static final double GOOD_SIZE = 5;
	public static final double EVIL_SIZE = 5;

	//for the mine sweepers
	public static final double MAX_TURN_RATE = 0.5;		//how quickly they may turn
	public static final double MAX_SPEED = 8;			//how fast they can go
	public static final int SCALE = 16;					//the size of the sweepers

	public ControllerMS(int xDim, int yDim) {
        setBackground(Color.LIGHT_GRAY);
		//addMouseListener(new MAdapter());
		//addMouseMotionListener(new MAdapter());
		setFocusable(true);
		setDoubleBuffered(true);
		//create the things to display, then add them
		pic = new BufferedImage(xDim, yDim, BufferedImage.TYPE_INT_RGB);
		picLabel = new JLabel(new ImageIcon(pic));
		dataLabel = new JLabel("Info");
		dataLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		add(picLabel);
		add(dataLabel);
		//initialize all of the variables!
		numGoods = GOODS;
		numEvils = EVILS;
		numAgents = SWEEPERS;
		ticks = 0;
		generations = 0;
//		avgFitness = new ArrayList<Double>();
//		bestFitness = new ArrayList<Double>();

		//make up agents
		agents = new ArrayList<AgentMS>(numAgents);
		for (int i = 0; i < numAgents; i++) {
			agents.add(new AgentMS());
		}
		numWeights = agents.get(0).getNumberOfWeights();

		//give agent neural nets their weights
		GA = new GeneticAlg(numAgents, Params.MUTATION_RATE, Params.CROSSOVER_RATE, numWeights);
		pop = GA.getChromosomes();
		for (int i = 0; i < numAgents; i++) {
			agents.get(i).putWeights(pop.get(i).getWeights());
		}

		//set up the goods
		goods = new ArrayList<Point2D>(numGoods);
		Random rnd = new Random();
		for (int i = 0; i < numGoods; i++) {
			goods.add(new Point2D.Double(rnd.nextDouble() * xDim, rnd.nextDouble() * yDim));
		}
		//set up the evils
		evils = new ArrayList<Point2D>(numEvils);
		for (int i = 0; i < numEvils; i++) {
			evils.add(new Point2D.Double(rnd.nextDouble() * xDim, rnd.nextDouble() * yDim));
		}
		//start it up!
		timer = new Timer(1, this);
		timer.start();
	}
	
	public void drawThings(Graphics2D g) {
		//cover everything with a blank screen
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, pic.getWidth(), pic.getHeight());
		//draw agents
		for (AgentMS a : agents) {
			a.draw(g);
		}
		//draw goods
		g.setColor(Color.GREEN);
		for (Point2D m : goods) {
			g.fillRect((int)(m.getX()-GOOD_SIZE/2), (int)(m.getY()-GOOD_SIZE/2), (int)GOOD_SIZE, (int)GOOD_SIZE);
		}
		//draw evils
		g.setColor(Color.RED);
		for (Point2D m : evils) {
			g.fillRect((int)(m.getX()-EVIL_SIZE/2), (int)(m.getY()-EVIL_SIZE/2), (int)EVIL_SIZE, (int)EVIL_SIZE);
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
//		drawThings(pic.getGraphics());
	}
	
	public void updateAgents() {
		ticks = (ticks + 1);		//count ticks to set the length of the generation run
		if (ticks < TICKS) { //do another tick toward finishing a generation
			Random rnd = new Random();
			//update each agent by calling their update function and checking to see if they got a mine
			for (int i = 0; i < numAgents; i++) {
				/*if(i == 0){
					System.out.println(agents.get(i).rotation);
				}*/
				if (!agents.get(i).update(goods,evils)) {
					System.out.println("Error: Wrong amount of neural net inputs.");
					break;
				}
				//did it find a good
				int foundGood = agents.get(i).checkForGood(goods, GOOD_SIZE);
				//if it found a good, add to that agent's fitness and make a new good
				if (foundGood >= 0) {
					agents.get(i).incrementFitness();
					goods.set(foundGood, new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
				}
				//did it find a evil
				int foundEvil = agents.get(i).checkForEvil(evils, EVIL_SIZE);
				//if it found a evil, add to that agent's fitness and make a new evil
				if (foundEvil >= 0) {
					agents.get(i).decrementFitness();
					evils.set(foundEvil, new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
				}
				//keep track of that agent's fitness in the GA as well as the NN
				pop.get(i).setFitness(agents.get(i).getFitness());
			}
		} else { //a generation has completed, run the genetic algorithm and update the agents
//			avgFitness = GA.avgFitness();
//			bestFitness = GA.bestFitness();
			generations++;
			dataLabel.setText("Previous generation " + generations + ":  Avg. fitness of " + GA.avgFitness() + ".  Best fitness of " + GA.bestFitness() + ".");
			ticks = 0;
			pop = GA.epoch(pop); //the big genetic algorithm process line
			for (int i = 0; i < numAgents; i++) { //give the agents all the new weights information
				agents.get(i).putWeights(pop.get(i).getWeights());
				agents.get(i).reset();
			}
		}
	}
	
	//@Override
	public void actionPerformed(ActionEvent e) {
		updateAgents();
		drawThings((Graphics2D) pic.getGraphics());
		repaint();
	}

}
