package a4;

import java.util.Set;

import common.Util;
import common.types.Tuple1;

/** A instance represents a person and their health.
 *
 * @author Mshnik, revised by gries */
public class Person extends Tuple1<String> {

	/** The possible flu-related states of a person. */
	public enum State {  // The names indicate the state.
		HEALTHY,
		SICK,
		DEAD,
		IMMUNE
	}

	/** The network to this person belongs. */
	private final Network graph;

	/** Amount of health this person has. >= 0.<br>
	 * 0 means dead, >0 means alive */
	private int health;

	/** State of this person. */
	private State state;

	/** Time step in which this person became sick (-1 if never been sick). */
	private int stepGotSick= -1;

	/** Time step in which this person became immune (-1 if not immune). */
	private int stepGotImmune= -1;

	/** Time step in which this person died (-1 if not dead). */
	private int stepDied= -1;

	/** Constructor: a healthy Person with name n and health h, added to graph g. <br>
	 * Precondition: The new person is not in g, and their name is distinct from the name of <br>
	 * any other person in g. */
	public Person(String n, int h, Network g) {
		super(n);
		health= h;
		state= State.HEALTHY;
		graph= g;
		graph.addVertex(this);
	}

	/** Return a representation of this Person. */
	public @Override String toString() {
		return super.toString() + " - " + state;
	}

	/** Return the name of this person. */
	public String name() {
		return _1;
	}

	/** Make this person sick during step currentStep. <br>
	 * Throw a RuntimeException if this person is not HEALTHY. */
	public void getSick(int currentStep) {
		if (state != State.HEALTHY) {
			throw new RuntimeException(state + " person can't become sick");
		}
		state= State.SICK;
		stepGotSick= currentStep;
	}

	/** Make this person immune during step currentStep. <br>
	 * Throw a RuntimeException if this person is immune or dead. */
	public void getImmune(int currentStep) {
		if (state == State.IMMUNE || state == State.DEAD) {
			throw new RuntimeException(state + " person can't become immune");
		}
		state= State.IMMUNE;
		stepGotImmune= currentStep;
	}

	/** Decrement the health of this person in step currentStep. <br>
	 * If its health becomes 0, the person dies. <br>
	 * Throw a RuntimeException if this person is not sick. */
	public void reduceHealth(int currentStep) {
		if (state != State.SICK) {
			throw new RuntimeException(state + " person can't lose health");
		}
		health-- ;
		if (health == 0) {
			state= State.DEAD;
			stepDied= currentStep;
		}
	}

	/** = the state of this person. */
	public State state() {
		return state;
	}

	/** = "This person is alive". */
	public boolean isAlive() {
		return state != State.DEAD;
	}

	/** = "This person is dead". */
	public boolean isDead() {
		return !isAlive();
	}

	/** = "This person is healthy. */
	public boolean isHealthy() {
		return state == State.HEALTHY;
	}

	/** = "This person is immune". */
	public boolean isImmune() {
		return state == State.IMMUNE;
	}

	/** = "This person is sick". */
	public boolean isSick() {
		return state == State.SICK;
	}

	/** = the time step in which this person got sick" (-1 if never been sick). */
	public int frameGotSick() {
		return stepGotSick;
	}

	/** = the time step in which this person got immune" (-1 if not immune). */
	public int frameGotImmune() {
		return stepGotImmune;
	}

	/** = the time step in which this person died" (-1 if not dead). */
	public int frameDied() {
		return stepDied;
	}

	/** = the neighbors of this person. */
	public Set<Person> neighbors() {
		return graph.neighborsOf(this);
	}

	/** = a random neighbor of this person */
	public Person randomNeighbor() {
		return Util.randomElement(graph.neighborsOf(this));
	}
}
