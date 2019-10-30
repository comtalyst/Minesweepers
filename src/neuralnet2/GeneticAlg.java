package neuralnet2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*
Notes
	pop is an array of genomes
	genome is an array of nn weights, which could be distributed into nn layers
	each agent carries a genome
 */

public class GeneticAlg {

	class Genome implements Comparable<Genome> {			//a simple class to handle a single genome
		private ArrayList<Double> weights;					//since no other class needs to know how Genome is implemented
		private double fitness;								//it is a subclass of the genetic algorithm class
		
		public Genome() {
			weights = new ArrayList<Double>();				//here the 'chromosomes' for a genetic alg influenced by a neural net are the weights of the neuron's inputs
			fitness = 0;									//fitness increases as the genome becomes more fit
		}
		
		public Genome(ArrayList<Double> w, double f) {
			weights = new ArrayList<Double>();
			for (Double d : w) {
				weights.add(d);
			}
			fitness = f;
		}
		
		public Genome clone() {								//mmm, cloning genomes
			return new Genome(weights, fitness);
		}
		
		public ArrayList<Double> getWeights() { return weights; }
		public double getFitness() { return fitness; }
		public void setFitness(double f) { fitness = f; }

		@Override
		public int compareTo(Genome o) {					//the comparable interface needs a definition of compareTo
			if (this.fitness > o.getFitness()) {			//the interface is being used so that the genomes can be sorted by fitness
				return 1;
			} else if (this.fitness < o.getFitness()) {
				return -1;
			}
			return 0;
		}
	}
		
	private ArrayList<Genome> pop;		//the genomes (weights for neural nets) who are the members of the genetic algorithm's gene pool					
	private int popSize;				//the pools' size
	private int chromosomeLength;		//the length of the weights list
	private double totalFitness;		//the summation of all the genomes' fitnesses
	private double bestFitness;			//the best fitness of all the genomes, then the average, then the worst
	private double avgFitness;			//could be used for plotting fitnesses
	private double worstFitness;
	private int fittestGenome;			//the index of the most fit genome in the population
	private int genCount;				//what generation the pool has made it to
	private double mutationRate;		//how often mutation (for each entry in a weight list) and crossover occurs
	private double crossoverRate;
	private ArrayList<Double> child1;
	private ArrayList<Double> child2;
	
	public GeneticAlg(int populationSize, double mutRate, double crossRate, int numWeights) {
		popSize = populationSize;
		mutationRate = mutRate;
		crossoverRate = crossRate;
		chromosomeLength = numWeights;
		totalFitness = 0;
		genCount = 0;
		fittestGenome = 0;
		bestFitness = 0;
		worstFitness = 99999999;
		avgFitness = 0;
		//initialize population with randomly generated weights
		pop = new ArrayList<Genome>();
		Random rnd = new Random();
		for (int i = 0; i < popSize; i++) {
			pop.add(new Genome());
			for (int j = 0; j < chromosomeLength; j++) {
				pop.get(i).weights.add(rnd.nextDouble()*2 - 1);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void crossover(ArrayList<Double> parent1, ArrayList<Double> parent2) {
		//implement crossover, similar to the previous project

		//your code goes here [Done?]
		int p = ThreadLocalRandom.current().nextInt(0,parent1.size());
		//System.out.println(Integer.toString(parent1.size()) + " == " + Integer.toString(parent2.size()));
        ArrayList<Double> s1x = new ArrayList<Double>(parent1.subList(0,p+1));
        ArrayList<Double> s1y = (p != parent1.size()-1)? new ArrayList<Double>(parent1.subList(p+1,parent1.size())):new ArrayList<Double>();
        ArrayList<Double> s2x = new ArrayList<Double>(parent2.subList(0,p+1));
        ArrayList<Double> s2y = (p != parent1.size()-1)? new ArrayList<Double>(parent2.subList(p+1,parent1.size())):new ArrayList<Double>();
		parent1 = s2x;								// reconstruct the strings
        parent1.addAll(s1y);
		parent2 = s1x;
		parent2.addAll(s2y);
	}
	
	public void mutate(ArrayList<Double> chromo) {
		//mutate each weight dependent upon the mutation rate
		//the weights are bounded by the maximum allowed perturbation

		//your code goes here [OK]
		for(int i = 0; i < chromo.size(); i++){
			if(ThreadLocalRandom.current().nextDouble(0,1) <= mutationRate){
				chromo.set(i,chromo.get(i)+ThreadLocalRandom.current().nextDouble(-Params.MAX_PERTURBATION,Params.MAX_PERTURBATION+0.000001));
			}
		}
		/*int x = ThreadLocalRandom.current().nextInt(0, chromo.size());
		int y = ThreadLocalRandom.current().nextInt(0, chromo.size());
		if(x > y){
			int tmp = x;
			x = y;
			y = tmp;
		}
		for(int i = x; i <= y; i++){
			chromo.set(i,chromo.get(i)+ThreadLocalRandom.current().nextDouble(-Params.MAX_PERTURBATION,Params.MAX_PERTURBATION+0.000001));
		}*/
		// ArrayList is pass by reference
	}
	
	public Genome getChromoByRoulette() {		//random parent selection using a roulette approach
		Random rnd = new Random();
		double stop = rnd.nextDouble() * totalFitness;	//pick a random fitness value at which to stop
		double fitnessSoFar = 0;
		Genome result = new Genome();			// Robin: this seems to makes the prob accords to the fitness. cool.
		for (int i = 0; i < popSize; i++) {
			fitnessSoFar += pop.get(i).fitness;
			if (fitnessSoFar >= stop) {
				result = pop.get(i).clone();
				break;							//I hate using break, maybe this should be a while loop, yes?
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Genome> epoch(ArrayList<Genome> oldpop) { //get the new generation from the old generation
		pop = (ArrayList<Genome>) oldpop.clone();			//the previous population is the current population
		reset();											//reinitialize fitness stats
		Collections.sort((List<Genome>) pop);				//sort them by fitness
		calculateBestWorstAvgTot();							//calculate the fitness stats
		ArrayList<Genome> newPop = new ArrayList<Genome>();
		if (Params.NUM_COPIES_ELITE * Params.NUM_ELITE % 2 == 0) {			//take the top NUM_ELITE performers and add them to the new population
			grabNBest(Params.NUM_ELITE, Params.NUM_COPIES_ELITE, newPop);
		}
		while (newPop.size() < popSize) {					//fill the rest of the new population by children from parents using the classic genetic algorithm
			//your 9-ish lines of code goes here [OK?]
			Genome x = getChromoByRoulette();
			//System.out.println("Roulette " + Integer.toString(x.weights.size()));
			if(ThreadLocalRandom.current().nextDouble(0,1) <= crossoverRate){
				Genome y = getChromoByRoulette();
				//System.out.println("Roulette " + Integer.toString(y.weights.size()));
				crossover(x.weights,y.weights);
				//System.out.println("Xovered " + Integer.toString(x.weights.size()));
			}
			//if(ThreadLocalRandom.current().nextDouble(0,1) <= mutationRate) {
				mutate(x.weights);
				//System.out.println("Mutated " + Integer.toString(x.weights.size()));
			//}

			newPop.add(x);
		}
		pop = (ArrayList<Genome>) newPop.clone();
		return pop;											//this probably could have been written better, why return a class variable?
	}
	
	public void grabNBest(int nBest, int numCopies, ArrayList<Genome> popList) { //hopefully the population is sorted correctly...
		while (nBest-->0) {
			for (int i = 0; i < numCopies; i++) {
				popList.add(pop.get(popSize - 1 - nBest));
			}
		}
	}
	
	public void calculateBestWorstAvgTot() { //fairly self-explanatory, try commenting it
		totalFitness = 0;
		double highestSoFar = 0;
		double lowestSoFar = 99999999;
		for (int i = 0; i < popSize; i++) {
			if (pop.get(i).fitness > highestSoFar) {
				highestSoFar = pop.get(i).fitness;
				fittestGenome = i;
				bestFitness = highestSoFar;
			}
			if (pop.get(i).fitness < lowestSoFar) {
				lowestSoFar = pop.get(i).fitness;
				worstFitness = lowestSoFar;
			}
			totalFitness += pop.get(i).fitness;
		}
		avgFitness = totalFitness / popSize;
	}
	
	public void reset() {		//reset fitness stats
		totalFitness = 0;
		bestFitness = 0;
		worstFitness = 99999999;
		avgFitness = 0;
	}

	//self-explanatory
	public ArrayList<Genome> getChromosomes() { return pop; }
	public double avgFitness() { return totalFitness / popSize; }
	public double bestFitness() { return bestFitness; }
	
}
