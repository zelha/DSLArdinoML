package io.github.mosser.arduinoml.kernel.generator;

import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.*;
import io.github.mosser.arduinoml.kernel.structural.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick and dirty visitor to support the generation of Wiring code
 */
public class ToWiring extends Visitor<StringBuffer> {

	private final static String CURRENT_STATE = "current_state";

	boolean enableMode = true;

	public ToWiring() {
		this.result = new StringBuffer();
	}

	private void w(String s) {
		result.append(String.format("%s\n",s));
	}

	@Override
	public void visit(App app) {

		if(app.getModes().size() == 0){
			enableMode = false;
		}

		w("// Wiring code generated from an ArduinoML model");
		w(String.format("// Application name: %s\n", app.getName()));
		w("\n");


		//************************
		//signalling stuff
		for(Mode mode : app.getModes()) {
			for(State state : mode.getStates()) {
				try {
					state.getSignaling().accept( this );
				} catch (java.lang.NullPointerException e) {
				}
			}
		}
		//************************



		for(Mode mode : app.getModes()) {
			w(String.format("void mode_%s(String currentStateName);", mode.getName()));
			for(State state : mode.getStates()) {
				w( String.format( "void state_%s();", state.getName() ) );
			}
		}
		w("\n");



		w("void setup(){");
		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}
		w("  Serial.begin(9600);");
		w("}\n");


		w("long timerOn = 0;");
		w("long time = 0; long debounce = 200;");
		w("long sensorValue = 0;\n");
		//w("const long analogInPin = 11;");
		/*for(AnalogSensor analogSensor : app.getAnalogSensor()) {
			w(String.format("int analog_sensor_%s = %d;", analogSensor.getName(), analogSensor.getThreshold()));
		}*/
		w("\n");

		if(enableMode) {
			for (Mode mode : app.getModes()) {
				mode.accept(this);
			}


			for (Mode mode : app.getModes()) {

				for (State state : mode.getStates()) {
					state.accept(this);

				}
				w("\n\n\n");
				if (mode.getTransitionMode().size() != 0) {
					for (TransitionMode transitionMode : mode.getTransitionMode()) {
						transitionMode.accept(this);
					}
				}
			}

		}else{

			for (State state : app.getStates()) {
				state.accept(this);
			}
		}

		if (app.getInitialMode() != null) {
			w("void loop() {");
			w(String.format("  mode_%s(\"state_%s\");", app.getInitialMode().getName(), app.getInitialMode().getInitState().getName()));
			w("}");
		}
		else if (app.getInitialState() != null) {
			w("void loop() {");
			w(String.format("  state_%s();", app.getInitialState().getName()));
			w("}");
		}


	}

	@Override
	public void visit(Actuator actuator) {
	 	w(String.format("  pinMode(%d, OUTPUT); // %s [Actuator]", actuator.getPin(), actuator.getName()));
	}


	@Override
	public void visit(Sensor sensor) {
		w(String.format("  pinMode(%d, INPUT);  // %s [Sensor]", sensor.getPin(), sensor.getName()));
	}

	@Override
	public void visit(SignalStuff signalStuff) {
		w("// Signal Stuff");
	}

	@Override
	public void visit(State state) {
	w(String.format("  void state_%s() {",state.getName()));
	w("    Serial.print(\"sensor : \");     // if logging the sensors");
	w("    Serial.print(analogRead(2)); // 3 lines for each sensor");
	if (state.isShow()) {
		w(String.format("    Serial.print(\"state : state_%s ; \");  //if logging the state", state.getName()));
	}
	if(state.getSignaling() != null ) {
			int timer = state.getSignaling().getTimerBip();
			boolean isHigh = true;
			for (int i = 0; i < state.getSignaling().getNumberOfBeeps() * 2; i++) {
				if (i == 0) {
					w(String.format("	if(timerOn++ < %d){", timer));
					w("		digitalWrite(" + state.getSignaling().getActuator().getPin() + "," + SIGNAL.HIGH.name() + ");");
					w("	}");
				} else if (i < state.getSignaling().getNumberOfBeeps() * 2 - 1) {
					w(String.format("	else if(timerOn >= %d && timerOn < %d){", timer * i, timer * (i + 1)));
					if (isHigh) {
						w("		digitalWrite(" + state.getSignaling().getActuator().getPin() + "," + SIGNAL.LOW.name() + ");");
						w("		timerOn++;");
						w("	}");
						isHigh = false;
					} else {
						w("		digitalWrite(" + state.getSignaling().getActuator().getPin() + "," + SIGNAL.HIGH.name() + ");");
						w("		timerOn++;");
						w("	}");
						isHigh = true;
					}
				} else {
					w("	else{");
					w(String.format("		digitalWrite(%d,LOW);", state.getSignaling().getActuator().getPin()));
					w("	}");
				}
			}
	}else{
		w("    timerOn = 0;");
	}
		if (state.getMode() == null) {

			for (Action action : state.getActions()) {
				action.accept( this );
			}

			if (state.getTransition() != null) {
				w( "    boolean guard = millis() - time > debounce;" );
				context.put( CURRENT_STATE, state );
				state.getTransition().accept( this );
				w( "  }\n" );
			} else {
				w( "  }" );
			}
		}

		else {

			for (Action action : state.getActions()) {
				action.accept( this );
			}

			if (state.getTransition() != null) {
				context.put( CURRENT_STATE, state );
				state.getTransition().accept( this );
				w( "  }\n" );
			} else {
				w( "  }" );
			}


		}
	}


	@Override
	public void visit(Transition transition) {

		Mode mode = transition.getActualState().getMode();

		if (mode == null) {

			String multipleSensorsEquation = transition.getSensor().isEmpty() ? "    if(guard " : "    if( guard && (";
			int i = 0;
			for (Sensor sensor : transition.getSensor()) {//get Sensor-> liste des sensors
				multipleSensorsEquation += "digitalRead(" + sensor.getPin() + ") == ";
				SIGNAL signal = transition.getValue().get( i );
				multipleSensorsEquation += (signal.equals( SIGNAL.HIGH ) || signal.equals( SIGNAL.LOW )) ? signal : signal.getIntValue();
				if (transition.getLogicalOperator().size() > i) { // if there is another condition
					multipleSensorsEquation += (transition.getLogicalOperator().get( i++ ).equals( LogicalOperator.AND_LOG ) ? " && " : " || ");
				}
			}

			multipleSensorsEquation += transition.getSensor().isEmpty() ? "){" : " )) {";

			w( multipleSensorsEquation );
			/*w(String.format("  if( digitalRead(%d) == %s && guard ) {",
					 transition.getSensor().getPin(),transition.getValue()));*/

			/*if (((State) context.get( CURRENT_STATE )).getEmphasized()) {
				w( "     timerOn = 0;" );
			}*/
			w( "       time = millis();" );
			w( String.format( "      state_%s();", transition.getNext().getName() ) );
			w( "    } else {" );
			w( String.format( "      state_%s();", ((State) context.get( CURRENT_STATE )).getName() ) );
			w( "    }" );
		}


		else {
			String multipleSensorsEquation = transition.getSensor().isEmpty() ? "    if( " : "    if( (";
			for (Sensor sensor : transition.getSensor()) {//get Sensor-> liste des sensors
				multipleSensorsEquation += "digitalRead(" + sensor.getPin() + ") == ";
				int i = 0;
				SIGNAL signal = transition.getValue().get( i );
				multipleSensorsEquation += (signal.equals( SIGNAL.HIGH ) || signal.equals( SIGNAL.LOW )) ? signal : signal.getIntValue();
				if (!sensor.equals( transition.getSensor().get( transition.getSensor().size()-1 ) )) {
					if (transition.getLogicalOperator().size() > i) { // if there is another condition
						multipleSensorsEquation += (transition.getLogicalOperator().get( i ).equals( LogicalOperator.AND_LOG ) ? " && " : " || ");
						i++;
					}
				}
			}

			multipleSensorsEquation += transition.getSensor().isEmpty() ? "){" : " )) {";

			w( multipleSensorsEquation );

			w( String.format( "      mode_%s(\"state_%s\");", mode.getName(), transition.getNext().getName() ) );
			w( "    } else {" );
			w( String.format( "      mode_%s(\"state_%s\");", mode.getName(), transition.getActualState().getName() ) );
			w( "    }" );



		}
	}

	@Override
	public void visit(Action action) {
		w(String.format("    digitalWrite(%d,%s);",action.getActuator().getPin(),action.getValue()));
		/*w(String.format("  digitalWrite(%d,%s);",
				action.getActuator().getPin(),
				(action.getValue().equals(SIGNAL.HIGH) ||
				action.getValue().equals(SIGNAL.LOW))?
						action.getValue() : action.getValue().getIntValue()));*/

	}


	//************PARTIE MODE ************
	@Override
	public void visit(AnalogSensor analogSensor) {
		w(String.format("  pinMode(%d, INPUT); // %s [AnalogSensor]", analogSensor.getPin(), analogSensor.getName()));
	}


	@Override
	public void visit(Mode mode) {

		w(String.format("  void mode_%s(String currentStateName) {",mode.getName()));
		if (mode.isShow()) {
			w(String.format("    Serial.print(\"mode : mode_%s ; \");  //if logging the mode", mode.getName()));
		}

		int i =0;
		String str ="";
		String signe = "";
		for(TransitionMode transitionMode : mode.getTransitionMode()) {
			if (i == 0) { str = "if";}
			else {str = "else if";}
			if (transitionMode.getSigne().equals( "sup" )) { signe = ">";}
			else if(transitionMode.getSigne().equals( "inf" )) { signe = "<";}
			AnalogSensor analogSensor = transitionMode.getAnalogSensors();
			w(String.format("    %s(analogRead(%d) %s %f){", str, analogSensor.getPin(), signe,analogSensor.getThreshold()));
			w(String.format("      mode_%s(\"state_%s\");", transitionMode.getNext().getName(),  transitionMode.getNext().getInitState().getName()));
			w( "    }" );
			i++;
		}
		w("    else{");
		i =0;
		for(State state : mode.getStates()) {
			if (i == 0) {
				str = "if";
			} else {
				str = "} else if";
			}
			w( String.format( "      %s(strcmp(currentStateName.c_str(), \"state_%s\") == 0){", str, state.getName() ) );
			w( String.format( "        state_%s();", state.getName() ) );
			i++;
		}
		w("      }");
		w( "    }" );
		w( "  }\n" );


	}

	@Override
	public void visit(TransitionMode transitionMode) {
		//w("//transition mode");
	}


	@Override
	public void visit(Signaling signaling) {
		w("//******** signaling stuff *********");
	}
}
