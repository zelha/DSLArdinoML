package groovuinoml.dsl;

import java.util.*;


import groovy.lang.Binding;
import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.*;
import io.github.mosser.arduinoml.kernel.generator.ToWiring;
import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.*;

public class GroovuinoMLModel {
	private List<Brick> bricks;
	private List<State> states;
	private Mode initialMode;
	private State initialState;
	private ArrayList<Mode> modes;
	private List<AnalogSensor> analogSensors;



	private Binding binding;
	
	public GroovuinoMLModel(Binding binding) {
		this.bricks = new ArrayList<>();
		this.states = new ArrayList<>();
		this.modes = new ArrayList<>();
		this.analogSensors = new ArrayList<>(  );
		this.binding = binding;
	}
	
	public void createSensor(String name, Integer pinNumber) {
		Sensor sensor = new Sensor();
		sensor.setName(name);
		sensor.setPin(pinNumber);
		this.bricks.add(sensor);
		this.binding.setVariable(name, sensor);
	}
	
	public void createActuator(String name, Integer pinNumber) {
		Actuator actuator = new Actuator();
		actuator.setName(name);
		actuator.setPin(pinNumber);
		this.bricks.add(actuator);
		this.binding.setVariable(name, actuator);
	}
	
	public void createState(String name, List<Action> actions) {
			State state = new State();
			state.setName( name );
			state.setActions( actions );
			this.states.add(state);
			this.binding.setVariable( name, state );
	}


	public void createSignaling(State stateA, Actuator actuatorA, int numberbip, String lengthbip, String when){
		Signaling signaling = new Signaling();
		if(lengthbip.equals("short")){
			signaling.setBeepSize(BEEP.SHORT);
		}else{
			signaling.setBeepSize(BEEP.LONG);
		}
		signaling.setNumberOfBeeps( numberbip );
		signaling.setActuator( actuatorA );
		if(when.equals("start")){
			stateA.setSignaling( signaling );
		}else{
			stateA.getTransition().getNext().setSignaling(signaling);
		}
	}

	// List<Sensors>
	public void createTransition(String nameTransition, State from, State to, List<Sensor> sensors, List<SIGNAL> value,  List<LogicalOperator>  logicalOperator) {
		Transition transition = new Transition();
		transition.setName(nameTransition);
		transition.setNext(to);
		transition.setSensors(sensors); //for the list of sensors
		transition.setValue(value);
		transition.setLogicalOperator(logicalOperator);
		transition.setActualState( from );
		from.setTransition(transition);
		this.binding.setVariable( nameTransition, transition );
		//transitions.add(transition);
	}


	public void createTransitionMode(Mode mode1, Mode mode2, AnalogSensor analogSensor, String signe, double valeurThreshold) {
		analogSensor.setThreshold(valeurThreshold);
		TransitionMode transitionMode = new TransitionMode();
		transitionMode.setNext(mode2);
		transitionMode.setAnalogSensors( analogSensor ); //for the list of sensors
		transitionMode.setSigne( signe );
		transitionMode.setActual( mode1 );
		mode1.setTransitionMode( transitionMode );

		//this.transitionModes.add( transitionMode );

	}


	public void createMode(String modeName, ArrayList<State> states, ArrayList<Transition> transitions, State initState) {
		Mode mode = new Mode();
		mode.setName(modeName);

		//set states
		mode.setStates(states);

		// set transitions
		mode.setTransitions(transitions);

		mode.setInitState(initState);

		//associate each state with mode
		for (State state : states) {
			state.setMode( mode );
		}

		this.modes.add(mode);
		this.binding.setVariable(modeName,mode);
	}

	public void createAnalogSensor(String analogSensorname, int pin) {
		AnalogSensor analogSensor = new AnalogSensor();
		analogSensor.setName( analogSensorname );
		analogSensor.setPin(pin);
		this.bricks.add( analogSensor );
		this.binding.setVariable(analogSensorname,analogSensor);
	}


	public void setInitialMode(Mode initialMode) {
			this.initialMode = initialMode;
	}

	public void setInitialState(State initialState) {
		this.initialState = initialState;
	}

	public void showModes() {
		if (this.modes.size() != 0) {
			for (Mode mode : this.modes) {
				mode.setShow( true );
			}
		}
	}

	public void showStates() {
		System.out.println( "jjjjjjjjjjj" );
		if (this.states.size() != 0) {
			for (State state : this.states) {
				state.setShow( true );
			}
		} if (this.modes.size() != 0) {
			System.out.println( "modes states blabla" );
			for (Mode mode : this.modes) {
				if (mode.getStates().size() !=0) {
					System.out.println( "pppppppp" );
					for (State state : mode.getStates()) {
						System.out.println( "mmmmmmmmm" );
						state.setShow( true );
					}
				}
			}
		}
	}


	@SuppressWarnings("rawtypes")
	public Object generateCode(String appName) {
		App app = new App();
		app.setName(appName);
		app.setBricks(this.bricks);
		app.setInitialMode(this.initialMode);
		app.setInitialState( this.initialState );
		app.setMode( this.modes );
		app.setStates(this.states);
		Visitor codeGenerator = new ToWiring();
		app.accept(codeGenerator);
		
		return codeGenerator.getResult();
	}





}
