import java.lang.reflect.Method;

// this is a DSLD file
// start off creating a custom DSL Descriptor for your Groovy DSL

// The following snippet adds the 'newProp' to all types that are a subtype of GroovyObjects
contribute(currentType(subType('groovy.lang.Script'))) {
    property name : 'high', type: Object, provider: 'GroovuinoML', doc: 'Stands for SIGNAL.HIGH'
    property name : 'low', type: Object, provider: 'GroovuinoML', doc: 'Stands for SIGNAL.LOW'

//	property name : 'led', type : Object, provider: 'GroovuinoML', doc: 'A variable'
//	property name : 'led1', type : Object, provider: 'GroovuinoML', doc: 'A variable'
//	property name : 'led2', type : Object, provider: 'GroovuinoML', doc: 'A variable'
//	property name : 'led3', type : Object, provider: 'GroovuinoML', doc: 'A variable'
//	property name : 'button', type : Object, provider: 'GroovuinoML', doc: 'A variable'
//	property name : 'on', type : Object, provider: 'GroovuinoML', doc: 'A variable'
//	property name : 'off', type : Object, provider: 'GroovuinoML', doc: 'A variable'

    method type: Script, name: 'actuator', params: [name:String], doc: 'Create a new Actuator'
    method type: void, name: 'pin', params: [pinNumber:Integer], doc: 'Associate Sensor or Actuator to a pin number'
    method type: void, name: 'onPin', params: [pinNumber:Integer], doc: 'Associate Sensor or Actuator to a pin number'

    method type: Script, name: 'state', params: [name:String], doc: 'Create a new State'
    method type: Script, name: 'means', params: [actuator:Object, actuator:Object], doc: 'Identify which Actuator is concerned by the state'
    method type: Script, name: 'means', params: [actuator:Object], doc: 'Identify which Actuator is concerned by the state'
    method type: Script, name: 'means', params: [actuatorName:String, actuatorName:String], doc: 'Identify which Actuator is concerned by the state'
    method type: Script, name: 'means', params: [actuatorName:String], doc: 'Identify which Actuator is concerned by the state'
    method type: Script, name: 'becomes', params: [signal:Object], doc: 'Define new Actuator value'
    method type: Script, name: 'becomes', params: [signalName:String], doc: 'Define new Actuator value'

    method type: void, name: 'initial', params: [state:Object], doc: 'Define the initial State'
    method type: void, name: 'initial', params: [stateName:String], doc: 'Define the initial State'

    method type: Script, name: 'from', params: [stateOrMode:Object], doc: 'Define first state of a Transition of state or mode'
    method type: Script, name: 'from', params: [stateOrModeName:String], doc: 'Define first state of a Transition of state or mode'


    method type: Script, name: 'to', params: [state:Object], doc: 'Define second state of a Transition'
    method type: Script, name: 'to', params: [stateName:String], doc: 'Define second state of a Transition'
    method type: Script, name: 'when', params: [sensor:Object], doc: 'Identify a Sensor that triggers the Transition'
    method type: Script, name: 'when', params: [sensorName:String], doc: 'Identify a Sensor that triggers the Transition'
    method type: Script, name: 'and', params: [sensor:Object], doc: 'Define another Sensor to triggers the Transition'
    method type: Script, name: 'and', params: [sensorName:String], doc: 'Define another Sensor to triggers the Transition'
    method type: Script, name: 'or', params: [sensor:Object], doc: 'Define another Sensor to triggers the Transition'
    method type: Script, name: 'or', params: [sensorName:String], doc: 'Define another Sensor to triggers the Transition'

    method type: void, name: 'export', params: [name:String], doc: 'Export the Arduino Script and define its name'


    //grammar for the realease 2

    method type: Script, name: 'mode', params: [modeName:String], doc: 'Define the object mode'
    method type: Script, name: 'states', params: [states:ArrayList<String>], doc: 'Define the states of the mode'
    method type: Script, name: 'transi', params: [transitions:ArrayList<Object>], doc: 'Define the transitions of the mode'



    method type: void, name: 'analogsensor', params: [analogsensorName:String], doc: 'Define the analog sensor'
    method type: void, name: 'threshold', params: [thresholdValue:Integer], doc: 'Define the value of the analog sensor '

    method type: Script, name: 'inside', params: [modeName:String], doc: 'Define states inside particular mode'



}
