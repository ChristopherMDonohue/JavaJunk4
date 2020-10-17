package a4;

import java.util.Scanner;
import java.util.Set;

import common.Util;
import common.types.Tuple;
import common.types.Tuple5;
import io.ScannerUtils;

/** An instance represents a flu spreading and ultimately dying out among<br>
 * a limited population (or killing everyone). <br>
 * <br>
 * Each Flu is created on a Network of people and with a chosen first patient. <br>
 * Flu is runnable, but for the purposes of this project, it does not need to <br>
 * be run on a separate thread.
 *
 * @author MPatashnik, revised by gries. */
public class Flu implements Runnable {
	/** The graph on which this Flu is running. */
	private Network network;

	/** The tree representing this Flu spreading. */
	private FluTree tree;

	/** Number of time steps this Flu took to create dt. */
	private int steps;

	/** The Flu model: Statistics that determine the spread of the disease. */
	private Statistics statistics;

	/** How many chars to print per line in the running section. */
	private static final int RUNNING_CHAR_COUNT_MAX= 50;

	/** Used in printing the run progress. */
	private int runningCharCount= 7;

	/** Constructor: a new Flu on network nw with first patient fp and disease model s. */
	public Flu(Network nw, Person fp, Statistics s) {
		steps= 0;
		network= nw;
		fp.getSick(0);
		tree= new FluTree(fp);
		statistics= s;
	}

	/** Run the disease until no sick people remain. Print out info about running. */
	public @Override void run() {
		System.out.print("Running");
		while (network.getPeopleOfType(Person.State.SICK).size() > 0) {
			step();
		}
		System.out.println("Done.\n");
	}

	/** Perform a single step on the disease, using disease model statistics. <br>
	 * First, sick people may become immune with a certain probability. <br>
	 * Second, sick people become less healthy by 1, and if their health reaches 0, they die.<br>
	 * Third, sick people may spread the disease to one neighbor, with a certain probability. */
	private void step() {
		Set<Person> people= network.vertexSet();
		System.out.print(".");
		runningCharCount++ ;
		if (runningCharCount > RUNNING_CHAR_COUNT_MAX) {
			System.out.print("\n");
		}

		// For each sick person, make them immune with a certain probability
		for (Person p : people) {
			if (p.isSick() && statistics.personBecomesImmune()) {
				p.getImmune(steps);
			}
		}

		// For each sick person, deduct 1 from health and make death if health becomes 0
		for (Person p : people) {
			if (p.isSick()) {
				p.reduceHealth(steps);
			}
		}

		// For each sick person, spread the disease to one random neighbor with a
		// certain probability.
		for (Person p : people) {
			if (p.isSick()) {
				Person n= p.randomNeighbor();
				if (n != null && n.isHealthy() && statistics.fluSpreadsToPerson()) {
					n.getSick(steps);
					tree.insert(n, p);
				}
			}
		}

		steps= steps + 1;
	}

	/** Read in the five statistic arguments from the console. <br>
	 * Return a Tuple5, with the following components: <br>
	 * - size: the number of people in the network <br>
	 * - maxHealth: how much health each person starts with <br>
	 * - connectionProbability: probability that two people are connected in the network <br>
	 * - sicknessProbability: probability that a sick person spreads the sickness <br>
	 *** to a neighbor in one time step <br>
	 * - immunizationProbability: probability that a sick person becomes immune in one time step */
	private static Tuple5<Integer, Integer, Double, Double, Double> readArgs() {
		Scanner scanner= ScannerUtils.defaultScanner();
		int size= ScannerUtils.get(Integer.class, scanner, "Enter the size of the population: ",
			"a positive non-zero integer", (i) -> i > 0);
		int maxHealth= ScannerUtils.get(Integer.class, scanner,
			"Enter the amount of health for each person: ",
			"a positive non-zero integer", (i) -> i > 0);
		double connectionProb= ScannerUtils.get(Double.class, scanner,
			"Enter the probability of a connection: ",
			"a double in the range [0,1]", (d) -> d >= 0 && d <= 1);
		double sicknessProb= ScannerUtils.get(Double.class, scanner,
			"Enter the probability of becoming sick: ",
			"a double in the range [0,1]", (d) -> d >= 0 && d <= 1);
		double immunizationProb= ScannerUtils.get(Double.class, scanner,
			"Enter the probability of becoming immune: ",
			"a double in the range [0,1]", (d) -> d >= 0 && d <= 1);
		scanner.close();
		return Tuple.of(size, maxHealth, connectionProb, sicknessProb, immunizationProb);
	}

	/** Run Flu on the arguments listed in args. <br>
	 * If args doesn't match the pattern below, read in arguments via the console by using
	 * readArgs().
	 *
	 * Then, call disease.run() and create a FluFrame showing the created FluTree.
	 *
	 * args should be an array of <br>
	 *
	 * [size, maxHealth, connection probability, sickness probability, immunization probability],
	 *
	 * or unused (any value). If not used, the user is prompted for input in the console. */
	public static void main(String[] args) {
		// Get arguments
		int size= 10;
		int maxHealth= 5;
		double connectionProbability= 0.7;
		double sicknessProbability= 0.5;
		double immunizationProbability= 0.1;

		try {
			// Attempt to read from args array passed in
			size= Integer.parseInt(args[0]);
			maxHealth= Integer.parseInt(args[1]);
			connectionProbability= Double.parseDouble(args[2]);
			sicknessProbability= Double.parseDouble(args[3]);
			immunizationProbability= Double.parseDouble(args[4]);
		} catch (Exception e) {
			// If too few or wrong type, read from scanner
			Tuple5<Integer, Integer, Double, Double, Double> args2= readArgs();
			size= args2._1;
			maxHealth= args2._2;
			connectionProbability= args2._3;
			sicknessProbability= args2._4;
			immunizationProbability= args2._5;
		}

		// Set defaults and create the Network, Statistics, and Flu objects
		System.out.print("\nSetting up ");
		System.out.print(".");
		Network n= new Network(size, maxHealth, connectionProbability);
		System.out.print(".");
		Statistics s= new Statistics(sicknessProbability, immunizationProbability);
		System.out.print(".");
		Flu d= new Flu(n, Util.randomElement(n.vertexSet()), s);
		System.out.println("Done.");

		d.run();
		System.out.println(d.tree.toStringVerbose() + "\n");
		for (Person p : d.network.getPeopleOfType(Person.State.HEALTHY)) {
			System.out.println(p);
		}
		FluJFrame.show(d.tree, d.steps);
	}
}
