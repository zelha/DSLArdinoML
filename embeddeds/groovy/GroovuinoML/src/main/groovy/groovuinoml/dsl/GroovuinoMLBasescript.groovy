package main.groovy.groovuinoml.dsl

import groovuinoml.GrammarException
import io.github.mosser.arduinoml.kernel.behavioral.LogicalOperator
import io.github.mosser.arduinoml.kernel.behavioral.Mode
import io.github.mosser.arduinoml.kernel.structural.AnalogSensor
import main.groovy.groovuinoml.dsl.GroovuinoMLBinding
import sun.misc.Signal

import java.rmi.server.ExportException
import java.util.List;

import io.github.mosser.arduinoml.kernel.behavioral.Action
import io.github.mosser.arduinoml.kernel.behavioral.State
import io.github.mosser.arduinoml.kernel.structural.Actuator
import io.github.mosser.arduinoml.kernel.structural.Sensor
import io.github.mosser.arduinoml.kernel.structural.SIGNAL

abstract class GroovuinoMLBasescript extends Script {

	// sensor "name" pin n
	def sensor(String name) {
		[pin: { n -> ((GroovuinoMLBinding)this.getBinding()).getGroovuinoMLModel().createSensor(name, n) },
		 onPin: { n -> ((GroovuinoMLBinding)this.getBinding()).getGroovuinoMLModel().createSensor(name, n)}]
	}


	// actuator "name" pin n
	def actuator(String name) {
		[pin: { n ->
			[echantillonage: { timestamp ->
				[by: { unit ->
				((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createActuator(name, n)
				}]
			}]
		}]
	}

	// state "name" means actuator becomes signal [and actuator becomes signal]*n
	def state(String name) {
		List<Action> actions = new ArrayList<Action>()
		((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createState(name, actions)
		// recursive closure to allow multiple and statements
		def closure
		closure = { actuator ->
			[becomes: { signal ->
				Action action = new Action()
				action.setActuator(actuator instanceof String ? (Actuator)((GroovuinoMLBinding)this.getBinding()).getVariable(actuator) : (Actuator)actuator)
				action.setValue(signal instanceof String ? (SIGNAL)((GroovuinoMLBinding)this.getBinding()).getVariable(signal) : (SIGNAL)signal)

				actions.add(action)
				[and: closure, or : closure]
			}]
		}
		[means: closure]
	}


	// initial state
	def initial(state) {
		((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().setInitialState(state instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state) : (State)state)
	}


// from "jour" to "nuit" when "analogsensor" threshold "sup" at 3
	def from(mode1) {
		List<Sensor> sensors = new ArrayList<Sensor>();
		List<SIGNAL> signales = new ArrayList<SIGNAL>();
		[to: mode = { mode2 ->
			[when:  { sensor -> //analogique sensor
				[threshold: signe = { signe ->
					[at: val = { val ->
						Sensor sensorA = sensor instanceof String ? (Sensor) ((GroovuinoMLBinding) this.getBinding()).getVariable(sensor) : (Sensor) sensor
						((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createTransitionMode( mode1, mode2, sensorA,signe,val)
					}]
				}]
			}]
		}]
	}


	//transision "t1" from state1 to state2 when sensor1 becomes signal1 [ and / or  sensor2 becomes signal2 ]
	def transition(nameTransition) {
		List<Sensor> sensors = new ArrayList<Sensor>();
		List<SIGNAL> signales = new ArrayList<SIGNAL>();
		List<LogicalOperator> logicalOperator = new ArrayList<LogicalOperator>();
		int i = 0;
		[from: state = { state1 ->
			[to: state = { state2 ->
				[when:  { sensor -> //boutton
					[becomes: signal = { signal ->
						sensorA = sensor instanceof String ? (Sensor) ((GroovuinoMLBinding) this.getBinding()).getVariable(sensor) : (Sensor) sensor
						signalA = signal instanceof String ? (SIGNAL) ((GroovuinoMLBinding) this.getBinding()).getVariable(signal) : (SIGNAL) signal
						signales.add(signalA)
						sensors.add(sensorA)
						((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createTransition(
								nameTransition,
								state1 instanceof String ? (State) ((GroovuinoMLBinding) this.getBinding()).getVariable(state1) : (State) state1,
								state2 instanceof String ? (State) ((GroovuinoMLBinding) this.getBinding()).getVariable(state2) : (State) state2,
								sensors,
								signales,
								logicalOperator
						)
						[and : { sensor2 ->
							[becomes: signal2 = { signal2 ->
								signalB = signal2 instanceof String ? (SIGNAL) ((GroovuinoMLBinding) this.getBinding()).getVariable(signal2) : (SIGNAL) signal2
								sensorB = sensor2 instanceof String ? (Sensor) ((GroovuinoMLBinding) this.getBinding()).getVariable(sensor2) : (Sensor) sensor2
								signales.add(signalB)
								sensors.add(sensorB)
								logicalOperator.add(LogicalOperator.AND_LOG);
							}]
						}, or : { sensor3 ->
							[becomes: signal2 = { signal3 ->
								signalC = signal3 instanceof String ? (SIGNAL) ((GroovuinoMLBinding) this.getBinding()).getVariable(signal3) : (SIGNAL) signal3
								sensorB = sensor3 instanceof String ? (Sensor) ((GroovuinoMLBinding) this.getBinding()).getVariable(sensor3) : (Sensor) sensor3
								signales.add(signalC)
								sensors.add(sensorB)
								logicalOperator.add(LogicalOperator.OR_LOG);
							}]
						}]
					}]
				}]
			}]
		}]
	}



	//signalstuff state1
	//signalstuff "on" when "start" with 1 bip "long" on "led" onlymode "jour"
	//signalstuff "on" when "stop" with 3 bip "short" on "led"
	def signalstuff(state1){
		[when: { when ->
			[with: { numberbip ->
				[bip: { longerbip ->
					[on: { actuator ->
						((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().makeEmphasized(state1,actuator);
					},
					 on: { actuator ->
						 [onlymode: { mode ->
							 ((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().makeEmphasized(state1,actuator);
						 }]
					 }
					]
				}]
			}]
		}]
	}

	def analogsensor(analogSensorName) {
		[onPin: { n ->
			[echantillonage: { timestamp ->
				[by: { unit ->
					((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createAnalogSensor( analogSensorName, n);
				}]
			}]
		}]
	}



	// export name
	def export(String name) {
		println(((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().generateCode(name).toString())
	}

	// disable run method while running
	int count = 0
	abstract void scriptBody()
	def run() {
		if(count == 0) {
			count++
			scriptBody()
		} else {
			println "Run method is disabled"
		}
	}

	def mode(String name) {
		[states: { states ->
			[transi: { map ->
				[init: { initstate ->
					try {
						((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createMode(name, states, map, initstate)
					}
					catch (Exception e) {}
				}]
			}]
		}]
	}



}