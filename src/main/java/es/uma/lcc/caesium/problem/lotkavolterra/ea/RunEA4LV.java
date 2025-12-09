package es.uma.lcc.caesium.problem.lotkavolterra.ea;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Locale;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.base.EvolutionaryAlgorithm;
import es.uma.lcc.caesium.ea.config.EAConfiguration;
import es.uma.lcc.caesium.ea.statistics.VarianceDiversity;
import es.uma.lcc.caesium.problem.lotkavolterra.LotkaVolterra;
import es.uma.lcc.caesium.problem.lotkavolterra.LotkaVolterraTrace;

/**
 * Class for testing the evolutionary algorithm for parameter estimation in the
 * Lotka-Volterra model.
 * @author ccottap
 * @version 1.0
 */
public class RunEA4LV {

	/**
	 * Main method
	 * @param args command-line arguments
	 * @throws FileNotFoundException if configuration file cannot be read 
	 * @throws JsonException if the configuration file is not correctly formatted
	 */
	public static void main(String[] args) throws FileNotFoundException, JsonException {
		if (args.length < 2) {
			System.out.println("Parameters: <algorithm-configuration> <problem-data>");
			System.exit(1);
		}
		
		FileReader reader = new FileReader(args[0] + ".json");
		EAConfiguration conf = new EAConfiguration((JsonObject) Jsoner.deserialize(reader));
		
		int numruns = conf.getNumRuns();
		System.out.println(conf);
		EvolutionaryAlgorithm myEA = new EvolutionaryAlgorithm(conf);
		
		LotkaVolterraTrace lvt = new LotkaVolterraTrace(args[1] + ".txt");
		LotkaVolterraObjectiveFunction obj = new LotkaVolterraObjectiveFunction(lvt);
		myEA.setObjectiveFunction(obj);
		myEA.getStatistics().setDiversityMeasure(new VarianceDiversity());

		double best = Double.POSITIVE_INFINITY;
		LotkaVolterra bestModel = null;
		int bestRun = 0;
		for (int i=0; i<numruns; i++) {
			myEA.run();
			double currbest = myEA.getStatistics().getBest(i).getFitness();
			if (currbest < best) {
				best = currbest;
				bestRun = i;
				bestModel = obj.getLotkaVolterraModel(myEA.getStatistics().getBest(i).getGenome());
			}
			System.out.println ("Run " + i + ": " + 
								String.format(Locale.US, "%.2f", myEA.getStatistics().getTime(i)) + "s\t" +
								currbest);
		}
		PrintWriter file = new PrintWriter(args[0] + "-stats-" + args[1] + ".json");
		file.print(myEA.getStatistics().toJSON().toJson());
		file.close();
		
		System.out.println("Best model found in run #" + bestRun + " (error = " + best + "):");
		System.out.println(bestModel);
		LotkaVolterraTrace bestTrace = bestModel.integrate(lvt.getStateAtTime(0.0), 
														   lvt.getMaxTime(), 
														   1);
		bestTrace.saveToFile(args[0] + "-fit-" + args[1] + ".txt", 0, lvt.getMaxTime(), 1);
		bestModel.saveToFile(args[0] + "-model-" + args[1] + ".txt");
	}
	
	
	
	

}
