package neuralnet2;

import java.util.ArrayList;
import java.util.Random;

/*
Notes
	Complete Dec 13
 */

public class NeuralNetwork {								//as general a description of a neural network as possible so that it can be used in any NN scenario

	//a subclass defining a neuron
	class Neuron {
		private int numInputs;								//each neuron takes in inputs
		ArrayList<Double> weights;							//whose significance is modified by a weight
		
		public Neuron(int inputs) {
			numInputs = inputs + 1;							// one extra for the bias node
			weights = new ArrayList<Double>(numInputs);
			Random rnd = new Random();
			for (int i = 0; i < numInputs; i++) {			//randomized weight initialization from -1 to 1
				weights.add(rnd.nextDouble()*2.0 - 1);		// theta (from the previous layer to this neuron)
			}
		}
		public int getNumInputs(){
			return numInputs;
		}
	}
	
	//a subclass defining a layer of neurons in a network
	class NeuronLayer {
		private int numNeurons;								//a layer consists of at least one neuron
		ArrayList<Neuron> neurons;							//the neurons of the layer
		
		public NeuronLayer(int neuronCount, int inputsPerNeuron) {
			numNeurons = neuronCount;
			neurons = new ArrayList<Neuron>(numNeurons);
			for (int i = 0; i < neuronCount; i++) {			//randomized neuron initialization
				neurons.add(new Neuron(inputsPerNeuron));	// create neuronCount neurons with inputsPerNeuron neurons on the previous layer
			}
		}
		public int getNumNeurons(){
			return numNeurons;
		}
	}
	
	private int numInputs;									//a neural net takes in a set of inputs - first layer size
	private int numOutputs;									//and delivers a set of outputs - last layer size
	private int numHiddenLayers;							//between these inputs and outputs are 'hidden' layers of neurons
	private int numNeuronsPerHiddenLayer;					//which may have many neurons to create the many synaptic connections
	private ArrayList<NeuronLayer> layers;
	
	//initialization/creation of a network given the parameters defining the size of the network
	public NeuralNetwork(int numIn, int numOut, int numHidden, int numNeuronPerHidden) {
		numInputs = numIn;
		numOutputs = numOut;
		numHiddenLayers = numHidden;
		numNeuronsPerHiddenLayer = numNeuronPerHidden;
		layers = new ArrayList<NeuronLayer>();
		createNet();
	}
	
	public void createNet() {
		//create layers of the network
		if (numHiddenLayers > 0) {
			//add a new layer to Layers connecting the inputs to the first hidden network if one exists
			//your code goes here [OK]
			layers.add(new NeuronLayer(numNeuronsPerHiddenLayer, numInputs));
			for (int i = 0; i < numHiddenLayers - 1; i++) {						//for the hidden middle layers, one hidden layer to the next
				//more code here [OK]
				layers.add(new NeuronLayer(numNeuronsPerHiddenLayer, numNeuronsPerHiddenLayer));
			}
			//one last layer to connect the last hidden layer to the outputs [OK]
			layers.add(new NeuronLayer(numOutputs, numNeuronsPerHiddenLayer));
		} else {
			layers.add(new NeuronLayer(numOutputs, numInputs));					//if there's no hidden layers, just one layer with inputs and outputs
		}
	}

	//idea for these methods: read through the neural net layer by layer and append all of them into one long weights ArrayList
	public ArrayList<Double> getWeights() { //gets the weights from the network and turns it into a simple list
		ArrayList<Double> weights = new ArrayList<Double>();
		//for each weight in each neuron in each layer
		for (NeuronLayer l : layers) {
			int sz = l.getNumNeurons();
			for (int j = 0; j < sz; j++) {
				for (int k = 0; k < l.neurons.get(j).numInputs; k++) {
					//one line goes here [OK]
					weights.add(l.neurons.get(j).weights.get(k));
				}
			}
		}
		return weights;
	}
	
	public int getNumberOfWeights() { //returns total number of weights in the whole network
		int weights = 0;
		//your code goes here [OK]
		weights = (numInputs+1)*numNeuronsPerHiddenLayer + (numNeuronsPerHiddenLayer+1)*numNeuronsPerHiddenLayer*(numHiddenLayers-1) + (numNeuronsPerHiddenLayer+1)*numOutputs;
		//System.out.println(weights);
		return weights;
	}
	
	public void replaceWeights(ArrayList<Double> newWeights) { //...replaces weights given an input ArrayList
		int cWeight = 0; //index to walk through newWeights
		//your code goes here [OK]

		int lc = 0;
		for (NeuronLayer l : layers){
			int sz = l.getNumNeurons();
			for (int j = 0; j < sz; j++) {
				l.neurons.get(j).weights.clear();
				for(int k = 0; k < l.neurons.get(j).numInputs; k++){
					//System.out.println(lc + " " + j + " " + k + " ::: " + cWeight);
					l.neurons.get(j).weights.add(newWeights.get(cWeight++));
				}
			}
			lc++;
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Double> Update(ArrayList<Double> inputs) { //takes the inputs and computes the outputs having run through the neural net layer
		ArrayList<Double> outputs = new ArrayList<Double>(numOutputs);
		int weight = 0;
		double netInput = 0;
		if (inputs.size() != numInputs) {
			System.out.println("*explodes*");
			return outputs;		//empty outputs if incorrect number of inputs
		}
		for (int i = 0; i < numHiddenLayers + 1; i++) { //for each layer
			if (i > 0) {
				inputs = (ArrayList<Double>) outputs.clone(); //make the new inputs be the outputs from the previous iteration of the loop
			}
			outputs.clear();
			weight = 0; //an indexing variable
			//for each neuron in that layer [OK]
			int sz = layers.get(i).getNumNeurons();
			for(int j = 0; j < sz; j++){
				//for each input-weight combo in that neuron [OK]
				netInput = 0;
				int neuronInputs = layers.get(i).neurons.get(j).numInputs;
				if(neuronInputs-1 != inputs.size()){							// ok cuz inputs keeps updating when passing each layer
					System.out.println("*explodes loudly*");
					System.out.println("neuronInputs-1 != inputs.size()");
				}
				if(neuronInputs != layers.get(i).neurons.get(j).weights.size()){
					System.out.println("*roasted*");
					System.out.println("neuronInputs != weights.size(): " + Integer.toString(neuronInputs) + " != " + Integer.toString(layers.get(i).neurons.get(j).weights.size()));
				}
				for(int k = 0; k < neuronInputs-1; k++){
					//do the summation of input*weight (called the activation value) [OK]
					netInput += layers.get(i).neurons.get(j).weights.get(k)*inputs.get(k);
				}
				//the output of the neuron is then dependent upon the activation exceeding a threshold value stored as the bias
				//the bias is stored as the last, extra 'weight' at the end of the weights ArrayList
				netInput += layers.get(i).neurons.get(j).weights.get(neuronInputs - 1)*Params.BIAS;  //uncomment this line, it's a hint [OK]
				outputs.add(sigmoid(netInput, Params.ACT_RESPONSE)); //scale the activation using a sigmoid function
				weight = 0; //reset the indexing to
			}
		}
		return outputs;
	}
	
	public double sigmoid(double activation, double response) { //the sigmoid function returns a value between 0 and 1, <0.5 for negative inputs, >0.5 for positive inputs
		return 1.0 / (1.0 + Math.exp(-activation / response));
	}
}
