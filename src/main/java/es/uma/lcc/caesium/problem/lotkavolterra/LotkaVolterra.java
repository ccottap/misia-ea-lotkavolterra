package es.uma.lcc.caesium.problem.lotkavolterra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Scanner;

/**
 * Implementation of the Lotka-Volterra Model for multiple species.
 * @author ccottap
 * @version 1.0
 */
public class LotkaVolterra {
	/**
	 * Number of species in the model.
	 */
	private int n;
	/**
	 * Growth rates of the species.
	 */
	private double[] r;
	/**
	 * Predation loss matrix.
	 */
	private double[][] beta;
	/**
	 * self limitation of the species. It could be viewed as the 
	 * ration between the growth rate and the self-limitation.
	 */
	private double[] d;
	/**
	 * Predation gain matrix.
	 */
	private double[][] gamma;
	/**
	 * array of temporal populations for internal use.
	 */
	private double[] tempPopulations;
	/**
	 * a constant for the upper bound of population sizes. This is
	 * used to prevent overflow if pathological parameters are used.
	 */
	private static final double POPULATION_UPPER_BOUND = 1e6;
	

	/**
	 * Default constructor. No species are defined.
	 */
	public LotkaVolterra() {
		n = 0;
		r = null;
		d = null;
		beta = null;
		gamma = null;
	}
	
	/**
	 * Constructor with an initial number of species.
	 * @param n the number of species.
	 */
	public LotkaVolterra(int n) {
		this();
		setNumberOfSpecies(n);
	}
	
	/**
	 * Constructor from file.
	 * @param filename the file name.
	 * @throws FileNotFoundException if the file is not found.
	 */
	public LotkaVolterra(String filename) throws FileNotFoundException {
		Scanner inputFile = new Scanner (new File(filename));
		inputFile.useLocale(Locale.US);
		int numSpecies = inputFile.nextInt();
		setNumberOfSpecies(numSpecies);
		for (int i = 0; i < numSpecies; i++) {
			r[i] = inputFile.nextDouble();
		}
		for (int i = 0; i < numSpecies; i++) {
			d[i] = inputFile.nextDouble();
		}
		for (int i = 0; i < numSpecies; i++) {
        	for (int j = 0; j < numSpecies; j++) {
        		beta[i][j] = inputFile.nextDouble();
        	}
        }
		for (int i = 0; i < numSpecies; i++) {
			for (int j = 0; j < numSpecies; j++) {
				gamma[i][j] = inputFile.nextDouble();
			}
		}
		inputFile.close();
	}

	/**
	 * Sets the number of species in the model.
	 * @param n the number of species.
	 */
	public void setNumberOfSpecies(int n) {
		this.n = n;
        r = new double[n];
        d = new double[n];
        beta = new double[n][n];
        gamma = new double[n][n];
        tempPopulations = new double[n];
	}
	
	/**
	 * Gets the number of species in the model.
	 * @return the number of species.
	 */
	public int getNumberOfSpecies() {
		return n;
	}
	
	/**
	 * Sets the growth rate for a specific species.
	 * @param speciesIndex the index of the species.
	 * @param growthRate the growth rate to set.
	 */
	public void setGrowthRate(int speciesIndex, double growthRate) {
		assert speciesIndex >= 0 && speciesIndex < n : "Species index out of bounds.";
		r[speciesIndex] = growthRate;
	}
	
	/**
	 * Gets the growth rate for a specific species.
	 * 
	 * @param speciesIndex the index of the species.
	 * @return the growth rate of the species.
	 */
	public double getGrowthRate(int speciesIndex) {
		assert speciesIndex >= 0 && speciesIndex < n : "Species index out of bounds.";
		return r[speciesIndex];
	}
	
	/**
	 * Sets the predation loss coefficient between two species.
	 * @param predatorIndex the index of the predator species.
	 * @param preyIndex the index of the prey species.
	 * @param lossCoefficient the predation loss coefficient.
	 */
	public void setPredationLoss(int predatorIndex, int preyIndex, double lossCoefficient) {
		assert predatorIndex >= 0 && predatorIndex < n : "Predator index out of bounds.";
		assert preyIndex >= 0 && preyIndex < n : "Prey index out of bounds.";
		assert lossCoefficient >= 0.0 : "Loss coefficient must be non-negative.";
		beta[preyIndex][predatorIndex] = lossCoefficient;
	}
	
	/**
	 * Gets the predation loss coefficient between two species.
	 * @param predatorIndex the index of the predator species.
	 * @param preyIndex the index of the prey species.
	 * @return the predation loss coefficient.
	 */
	public double getPredationLoss(int predatorIndex, int preyIndex) {
		assert predatorIndex >= 0 && predatorIndex < n : "Predator index out of bounds.";
		assert preyIndex >= 0 && preyIndex < n : "Prey index out of bounds.";
		return beta[preyIndex][predatorIndex];
	}
	
	/**
	 * Sets the self-limitation for a specific species.
	 * @param speciesIndex the index of the species.
	 * @param selfLimitation the self-limitation.
	 */
	public void setSelfLimitation(int speciesIndex, double selfLimitation) {
		assert speciesIndex >= 0 && speciesIndex < n : "Species index out of bounds.";
		assert selfLimitation >= 0.0 : "Self-limitation must be non-negative.";
		d[speciesIndex] = selfLimitation;
	}
	
	/**
	 * Gets the self-limitation for a specific species.
	 * @param speciesIndex the index of the species.
	 * @return the self-limitation.
	 */
	public double getSelfLimitation(int speciesIndex) {
		assert speciesIndex >= 0 && speciesIndex < n : "Species index out of bounds.";
		return d[speciesIndex];
	}
	
	/**
	 * Sets the predation gain coefficient between two species.
	 * @param predatorIndex the index of the predator species.
	 * @param preyIndex the index of the prey species.
	 * @param gainCoefficient the predation gain coefficient.
	 */
	public void setPredationGain(int predatorIndex, int preyIndex, double gainCoefficient) {
		assert predatorIndex >= 0 && predatorIndex < n : "Predator index out of bounds.";
		assert preyIndex >= 0 && preyIndex < n : "Prey index out of bounds.";
		assert gainCoefficient >= 0.0 : "Gain coefficient must be non-negative.";
		gamma[preyIndex][predatorIndex] = gainCoefficient;
	}
	
	/**
	 * Gets the predation gain coefficient between two species.
	 * @param predatorIndex the index of the predator species.
	 * @param preyIndex the index of the prey species.
	 * @return the predation gain coefficient.
	 */
	public double getPredationGain(int predatorIndex, int preyIndex) {
		assert predatorIndex >= 0 && predatorIndex < n : "Predator index out of bounds.";
		assert preyIndex >= 0 && preyIndex < n : "Prey index out of bounds.";
		return gamma[preyIndex][predatorIndex];
	}
	
	/**
	 * Sets the parameters of the Lotka-Volterra model in bulk.
	 * @param r growth rates array.
	 * @param beta predation loss matrix.
	 * @param d self-limitation array.
	 * @param gamma predation gain matrix.
	 * @throws IllegalArgumentException if the dimensions do not match the number of species.
	 */
	public void setParameters(double[] r, double[][] beta, double[] d, double[][] gamma) {
		if (r.length != n || d.length != n || beta.length != n || gamma.length != n) {
			throw new IllegalArgumentException("Parameter dimensions do not match the number of species.");
		}
		for (int i = 0; i < n; i++) {
			if (beta[i].length != n || gamma[i].length != n) {
				throw new IllegalArgumentException("Parameter dimensions do not match the number of species.");
			}
		}
		for (int i = 0; i < n; i++) {
			this.r[i] = r[i];
			this.d[i] = d[i];
			for (int j = 0; j < n; j++) {
				this.beta[i][j] = beta[i][j];
				this.gamma[i][j] = gamma[i][j];
			}
		}
    }
	
	
	/**
	 * Computes the differentials for the Lotka-Volterra model given the current populations.
	 * @param populations current populations of the species.
	 * @return array of differentials for each species.
	 */
	public double[] computeDifferentials(double[] populations) {
		assert populations.length == n : "Population array length does not match number of species.";

		double[] differentials = new double[n];
		
		for (int i = 0; i < n; i++) {

			double growthTerm = r[i];
			double selfLimitationTerm = populations[i] * d[i];
			double interactionTerm = 0.0;

			for (int j = 0; j < n; j++) {
				interactionTerm += (gamma[j][i]-beta[i][j]) * populations[j];				
			}

			differentials[i] = populations[i] * 
								(growthTerm - selfLimitationTerm + interactionTerm);
		}

		return differentials;
	}
	
	/**
	 * Integration step using the Runge-Kutta 4th order method.
	 * @param populations current populations of the species.
	 * @param dt time step for integration.
	 * @return updated populations after the time step.
	 */
	public double[] integrationStepRK4(double[] populations, double dt) {
		assert populations.length == n : "Population array length does not match number of species.";

		double[] k1 = computeDifferentials(populations);
		double dt2 = 0.5*dt;
		double dt6 = dt/6.0;

		for (int i = 0; i < n; i++) {
			tempPopulations[i] = populations[i] + dt2 * k1[i];
		}
		double[] k2 = computeDifferentials(tempPopulations);

		for (int i = 0; i < n; i++) {
			tempPopulations[i] =  populations[i] + dt2 * k2[i];
		}
		double[] k3 = computeDifferentials(tempPopulations);

		for (int i = 0; i < n; i++) {
			tempPopulations[i] =  populations[i] + dt * k3[i];
		}
		double[] k4 = computeDifferentials(tempPopulations);

		double[] newPopulations = new double[n];
		for (int i = 0; i < n; i++) {
			newPopulations[i] = populations[i] + (dt6 * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]));
			if (newPopulations[i] < 0.0) {
				newPopulations[i] = 0.0;
			}
		}

		return newPopulations;
	}
	
	/**
	 * Integrates the populations over a specified time period using RK4 method.
	 * @param initialPopulations initial populations of the species.
	 * @param totalTime total time for integration.
	 * @param dt time step for integration.
	 * @return a trace of the population state.
	 */
	public LotkaVolterraTrace integrate(double[] initialPopulations, double totalTime, double dt) {
		assert initialPopulations.length == n : "Initial population array length does not match number of species.";
		boolean validPopulations = true;
		
		LotkaVolterraTrace trace = new LotkaVolterraTrace(n);
		double time = 0.0;
        double[] populations = initialPopulations.clone();	
		trace.addState(time, populations);

		while (time < totalTime) {
			if (validPopulations) {
				populations = integrationStepRK4(populations, dt);
				for (int i = 0; i < n; i++) {
					if (populations[i] > POPULATION_UPPER_BOUND) {
						validPopulations = false;
						break;
					}
				}
			}
			time += dt;
			trace.addState(time, populations);
		}

		return trace;
	}
	
	
	/**
	 * Saves the Lotka-Volterra model parameters to a file that can be later read.
	 * @param filename the file name.
	 * @throws FileNotFoundException if the file cannot be created.
	 */
	public void saveToFile(String filename) throws FileNotFoundException {
		PrintWriter outputFile = new PrintWriter(new File(filename));
		outputFile.println(n);
		for (int i = 0; i < n; i++) {
			outputFile.print(r[i] + " ");
		}
		outputFile.println();
		for (int i = 0; i < n; i++) {
			outputFile.print(d[i] + " ");
		}
		outputFile.println();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				outputFile.print(beta[i][j] + " ");
			}
			outputFile.println();
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				outputFile.print(gamma[i][j] + " ");
			}
			outputFile.println();
		}
		outputFile.close();
	}
	
	@Override
	public String toString() {
		String s = "Lotka-Volterra Model with "+ n +" species\n";
		s += "Growth rates (r):\n";
		for (double rate : r) {
			s += rate + " ";
		}
		s += "\nSelf-limitation (d):\n";
		for (double capacity : d) {
			s += capacity + " ";
		}
		s += "\nPredation loss matrix (beta):\n";
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				s += beta[i][j] + " ";
			}
			s += "\n";
		}
		s += "Predation gain matrix (gamma):\n";
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				s += gamma[i][j] + " ";
			}
			s += "\n";
		}
		return s;
	}
	
}
