package es.uma.lcc.caesium.problem.lotkavolterra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Population evolution trace for the Lotka-Volterra Model.
 * @author ccottap
 * @version 1.0
 */
public class LotkaVolterraTrace implements Iterable<double[]> {
	/**
	 * Number of species in the model.
	 */
	private int n;
	/**
	 * list of states (time and populations
	 */
	private List<double[]> states;

	/**
	 * Constructs an empty Lotka-Volterra trace for a given number of species.
	 * @param n the number of species.
	 */
	public LotkaVolterraTrace(int n) {
		this.n = n;
		states = new ArrayList<double[]>();
	}
	
	/**
	 * Constructs a Lotka-Volterra trace by reading it from a file.
	 * @param filename the name of the file.
	 * @throws FileNotFoundException if the file cannot be found.
	 */
	public LotkaVolterraTrace(String filename) throws FileNotFoundException {
		states = new ArrayList<double[]>();
		readFromFile(filename);
	}
	
	/**
	 * Gets the number of species in the model.
	 * @return the number of species.
	 */
	public int getNumberOfSpecies() {
		return n;
	}
	
	/**
	 * Gets the maximum time recorded in the trace.
	 * @return the maximum time.
	 */
	public double getMaxTime() {
		if (states.isEmpty()) {
			return Double.NaN;
		}
		return states.get(states.size() - 1)[0];
	}
	
	/**
	 * Gets the size of the trace (number of recorded states).
	 * @return the size of the trace.
	 */
	public int size() {
		return states.size();
	}
	
	/**
	 * Adds a new state to the trace. Assumes that each state corresponds to a later time
	 * than the previously added state.
	 * @param time the time of the state.
	 * @param state the populations at said time.
	 */
	public void addState(double time, double[] state) {
		assert state.length == n : "State length does not match number of species.";
		assert states.isEmpty() || time > states.get(states.size() - 1)[0] : "State time must be greater than the last state time.";
		double[] fullState = new double[n + 1];
		fullState[0] = time;
		System.arraycopy(state, 0, fullState, 1, n);
		states.add(fullState);
	}

	/**
	 * Gets the population state at a certain time. If the time does not
	 * exactly match a recorded state, the populations are linearly interpolated.
	 * @param time the time to get the state for.
	 * @return the population state at the given time.
	 */
	public double[] getStateAtTime(double time) {
		if (states.isEmpty()) {
			return null;
		}
		double[] result = new double[n];

		
		if (time <= states.get(0)[0]) {
			System.arraycopy(states.get(0), 1, result, 0, n);
		}
		if (time >= states.get(states.size() - 1)[0]) {
			System.arraycopy(states.get(states.size() - 1), 1, result, 0, n);
		}
		// binary search for the first entry with time larger than 
		// (or equal to) the given time
		int left = 0;
		int right = states.size() - 1;
		while (left < right) {
			int mid = (left + right) / 2;
			if (states.get(mid)[0] < time) {
				left = mid + 1;
			} else {
				right = mid;
			}
		}
		// left is now the index of the first entry with time larger than 
		// (or equal to) the given time
		if (states.get(left)[0] == time) {
			System.arraycopy(states.get(left), 1, result, 0, n);
		}
		else {
			double[] prevState = states.get(left - 1);
			double[] nextState = states.get(left);
			double t1 = prevState[0];
			double t2 = nextState[0];
			for (int j = 1; j <= n; j++) {
				double p1 = prevState[j];
				double p2 = nextState[j];
				result[j-1] = p1 + (p2 - p1) * (time - t1) / (t2 - t1);
			}
		}		
		return result;
	}
	
	/**
	 * Saves the trace to a file.
	 * @param filename the name of the file.	
	 * @throws FileNotFoundException if the file cannot be created.
	 */
	public void saveToFile(String filename) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File(filename));
		writer.println(n);
		for (double[] state : states) {
			writer.print(state[0]);
			for (int j = 1; j < state.length; j++) {
				writer.print(" " + state[j]);
			}
			writer.println();
		}
		writer.close();
	}
	
	/**
	 * Saves the trace to a file sampling it at regular intervals.
	 * @param filename the name of the file.
	 * @param initTime the initial time.
	 * @param endTime the end time.
	 * @param step the time step.
	 * @throws FileNotFoundException if the file cannot be created.
	 */
	public void saveToFile(String filename, double initTime, double endTime, double step) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new File(filename));
		writer.println(n);
		for (double t = initTime; t <= endTime; t += step) {
			double[] state = getStateAtTime(t);
			writer.print(t);
			for (int i = 0; i < state.length; i++) {
				writer.print(" " + state[i]);
			}
			writer.println();
		}
		writer.close();
	}
	
	
	/**
	 * Reads a Lotka-Volterra trace from a file.
	 * @param filename the name of the file.
	 * @throws FileNotFoundException if the file cannot be found.
	 */
	public void readFromFile(String filename) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filename));
		scanner.useLocale(Locale.US);
		n = scanner.nextInt();
		states.clear();
		while (scanner.hasNext()) {
			double time = scanner.nextDouble();
			double[] state = new double[n];
			for (int j = 0; j < n; j++) {
				state[j] = scanner.nextDouble();
			}
			addState(time, state);
		}
		scanner.close();
	}
	
	@Override
	public String toString() {
		String s = "Lotka-Volterra Trace:\n";
		s += "Time\tPopulations\n";
		for (double[] state : states) {
			s += state[0] + "\t";
			for (int j = 1; j < state.length; j++) {
				s += state[j] + "\t";
			}
			s+= "\n";
		}
		return s;
	}

	@Override
	public Iterator<double[]> iterator() {
		return states.iterator();
	}
}
