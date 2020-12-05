/**
 *  Aeon HEMv2+
 *
 *  Copyright 2014 Barry A. Burke
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *  Aeon Home Energy Meter v2 (US)
 *
 *  Author: Barry A. Burke
 *  Contributors: Brock Haymond: UI updates
 *
 *  Genesys: Based off of Aeon Smart Meter Code sample provided by SmartThings (2013-05-30). Built on US model
 *			 may also work on international versions (currently reports total values only)
 *
 *  History:
 * 		
 *	2014-06-13: Massive OverHaul
 *				- Fixed Configuration (original had byte order of bitstrings backwards
 *				- Increased reporting frequency to 10s - note that values won't report unless they change
 *				  (they will also report if they exceed limits defined in the settings - currently just using
 *				  the defaults).
 *				- Added support for Volts & Amps monitoring (was only Power and Energy)
 *				- Added flexible tile display. Currently only used to show High and Low values since last
 *				  reset (with time stamps). 
 *				- All tiles are attributes, so that their values are preserved when you're not 'watching' the
 *				  meter display
 *				- Values are formatted to Strings in zwaveEvent parser so that we don't lose decimal values 
 *				  in the tile label display conversion
 *				- Updated fingerprint to match Aeon Home Energy Monitor v2 deviceId & clusters
 *				- Added colors for Watts and Amps display
 * 				- Changed time format to 24 hour
 *	2014-06-17: Tile Tweaks
 *				- Reworked "decorations:" - current values are no longer "flat"
 *				- Added colors to current Watts (0-18000) & Amps (0-150)
 *				- Changed all colors to use same blue-green-orange-red as standard ST temperature guages
 *	2014-06-18: Cost calculations
 *				- Added $/kWh preference
 *	2014-09-07:	Bug fix & Cleanup
 *				- Fixed "Unexpected Error" on Refresh tile - (added Refresh Capability)
 *				- Cleaned up low values - reset to ridiculously high value instead of null
 *				- Added poll() command/capability (just does a refresh)
 * 	2014-09-19: GUI Tweaks, HEM v1 alterations (from Brock Haymond)
 *				- Reworked all tiles for look, color, text formatting, & readability
 *	2014-09-20: Added HEMv1 Battery reporting (from Brock Haymond)
 *	2014-11-06: Added alternate display of L2 and L2 values instead of Low/High, based on version by Jayant Jhamb
 *  2014-11-11: Massive overhaul completed (see GitHub comments for specifics)
 * 	
 *  2017-05-14: Start working on enegry monitor on HVAC 
 */
metadata {
	// Automatically generated. Make future change here.
	definition (
		name: 		"Aeon HEMv1 based HVAC monitor 2", 
		namespace: 	"Green Living",
		category: 	"Green Living",
		author: 	"Leihai You"
	) 
	{
    	capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Sensor"
        capability "Refresh"
        capability "Polling"
        
        attribute "energy", "string"
        attribute "power", "string"
        
        attribute "energyDisp", "string"
        attribute "energyOne", "string"
        attribute "energyTwo", "string"
        
        attribute "powerDisp", "string"
        attribute "powerOne", "string"
        attribute "powerTwo", "string"
        
        attribute "minutesOne", "string"
        attribute "minutesTwo", "string"
        
        attribute "lastPollOne", "number"
        attribute "lastPollTwo", "number"

        attribute "oneDayDisp11", "string"
        attribute "oneDayDisp12", "string"
        attribute "oneDayDisp21", "string"
        attribute "oneDayDisp22", "string"

		attribute "lastDetailInfoTime", "number"
        attribute "lastTotalInfoTime", "number"
        
		command "reset"
        command "configure"
        command "refresh"
        command "poll"
        command "toggleDisplay"
        
		fingerprint deviceId: "0x2101", inClusters: " 0x70,0x31,0x72,0x86,0x32,0x85,0x60" //v1
//		fingerprint deviceId: "0x3101", inClusters: "0x70,0x32,0x60,0x85,0x56,0x72,0x86"  // v2
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 33, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 33, scale: 0, size: 4).incomingMessage()
		}
        // TODO: Add data feeds for Volts and Amps
	}

	// tile definitions
	tiles {
    
    // Watts row
		valueTile("powerDisp", "device.powerDisp") {
			state (
				"default", 
				label:'${currentValue}', 
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
                backgroundColors:[
					[value: "0 Watts", 		color: "#153591"],
					[value: "3000 Watts", 	color: "#1e9cbb"],
					[value: "6000 Watts", 	color: "#90d2a7"],
					[value: "9000 Watts", 	color: "#44b621"],
					[value: "12000 Watts", 	color: "#f1d801"],
					[value: "15000 Watts", 	color: "#d04e00"], 
					[value: "18000 Watts", 	color: "#bc2323"]
					
				/* For low-wattage homes, use these values
					[value: "0 Watts", color: "#153591"],
					[value: "500 Watts", color: "#1e9cbb"],
					[value: "1000 Watts", color: "#90d2a7"],
					[value: "1500 Watts", color: "#44b621"],
					[value: "2000 Watts", color: "#f1d801"],
					[value: "2500 Watts", color: "#d04e00"],
					[value: "3000 Watts", color: "#bc2323"]
				*/
				]
			)
		}
        valueTile("powerOne", "device.powerOne") {
        	state(
        		"default", 
        		label:'${currentValue}', 
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
                backgroundColors:[
					[value: "0 Watts", 		color: "#153591"],
					[value: "3000 Watts", 	color: "#1e9cbb"],
					[value: "6000 Watts", 	color: "#90d2a7"],
					[value: "9000 Watts", 	color: "#44b621"],
					[value: "12000 Watts", 	color: "#f1d801"],
					[value: "15000 Watts", 	color: "#d04e00"], 
					[value: "18000 Watts", 	color: "#bc2323"]
					
				/* For low-wattage homes, use these values
					[value: "0 Watts", color: "#153591"],
					[value: "500 Watts", color: "#1e9cbb"],
					[value: "1000 Watts", color: "#90d2a7"],
					[value: "1500 Watts", color: "#44b621"],
					[value: "2000 Watts", color: "#f1d801"],
					[value: "2500 Watts", color: "#d04e00"],
					[value: "3000 Watts", color: "#bc2323"]
				*/
				]
			)
        }
        valueTile("powerTwo", "device.powerTwo") {
        	state(
        		"default", 
        		label:'${currentValue}', 
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
                backgroundColors:[
					[value: "0 Watts", 		color: "#153591"],
					[value: "3000 Watts", 	color: "#1e9cbb"],
					[value: "6000 Watts", 	color: "#90d2a7"],
					[value: "9000 Watts", 	color: "#44b621"],
					[value: "12000 Watts", 	color: "#f1d801"],
					[value: "15000 Watts", 	color: "#d04e00"], 
					[value: "18000 Watts", 	color: "#bc2323"]
					
				/* For low-wattage homes, use these values
					[value: "0 Watts", color: "#153591"],
					[value: "500 Watts", color: "#1e9cbb"],
					[value: "1000 Watts", color: "#90d2a7"],
					[value: "1500 Watts", color: "#44b621"],
					[value: "2000 Watts", color: "#f1d801"],
					[value: "2500 Watts", color: "#d04e00"],
					[value: "3000 Watts", color: "#bc2323"]
				*/
				]
			)
        }

	// Power row
		valueTile("energyDisp", "device.energyDisp") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
        valueTile("energyOne", "device.energyOne") {
        	state(
        		"default", 
        		label: '${currentValue}', 
        		foregroundColor: "#000000", 
        		backgroundColor: "#ffffff")
        }        
        valueTile("energyTwo", "device.energyTwo") {
        	state(
        		"default", 
        		label: '${currentValue}', 
        		foregroundColor: "#000000", 
        		backgroundColor: "#ffffff")
        }
        
	// Power comsumption row
		valueTile("minutesOne", "device.minutesOne") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
		valueTile("minutesTwo", "device.minutesTwo") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
        
        valueTile("oneOn", "device.oneOn") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
        }
		// history row
		valueTile("oneDayDisp11", "device.oneDayDisp11") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
		valueTile("oneDayDisp12", "device.oneDayDisp12") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
        valueTile("twoOn", "device.twoOn") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
        }
		valueTile("oneDayDisp21", "device.oneDayDisp21") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
		valueTile("oneDayDisp22", "device.oneDayDisp22") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
        
    // Controls row
		standardTile("reset", "command.reset", inactiveLabel: false) {
        	state "default", label:'reset', action:"reset", icon: "st.Health & Wellness.health7"
		}
        standardTile("refresh", "command.refresh", inactiveLabel: false) {
			state "default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-con"
		}
        standardTile("configure", "command.configure", inactiveLabel: false) {
			state "configure", label:'', action: "configure", icon:"st.secondary.configure"
        }
		standardTile("toggle", "command.toggleDisplay", inactiveLabel: false) {
        	state "default", label: "toggle", action: "toggleDisplay", icon: "st.motion.motion.inactive"
		}

// HEM Version Configuration only needs to be done here - comments to choose what gets displayed

		main (["energyDisp","energyOne","energyTwo",
			"powerDisp","powerOne","powerTwo"
			])

		details([
			"energyOne","energyDisp","energyTwo",
			"powerOne","powerDisp","powerTwo",
            "minutesOne", "minutesTwo","toggle",
            "oneOn", "oneDayDisp11", "oneDayDisp12",
            "twoOn", "oneDayDisp21", "oneDayDisp22",
			"reset","refresh",
			"configure"
		])
	}
    preferences {
    	input "kWhCost", "string", title: "\$/kWh (0.16)", description: "0.16", defaultValue: "0.16" as String
    	input "kWhDelay", "number", title: "total report seconds (30)", /* description: "30", */ defaultValue: 30
    	input "detailDelay", "number", title: "Detail report seconds (120)", /* description: "120", */ defaultValue: 120
    	input "resetGuard", "number", title: "resetGuard (set to 666 to reset)", /* description: "120", */ defaultValue: 120
    }
}

def initHistory() {
	def h = 4
	state._history = [new double[366], new double[366], new double[366], new double[366]];
	for (int j = 0; j < h; j++) {
    	for (int i = 0; i < 366; i++) {
     		state._history[j][i] = 0.0;
    	}
    }
    
    log.debug "Init history with h=${h}, v[3][365]=${state._history[3][365]}"
}

def installed() {
	state.display = 1
    state.lastDetailInfoTime = 100
    state.lastTotalInfoTime = 100
	reset()						// The order here is important
	configure()					// Since reports can start coming in even before we finish configure()
	refresh()
}

def updated() {
	configure()
	resetDisplay()
	refresh()
}

def getHMS(int id, int day) {
	try {
 	def cal = Calendar.getInstance();
    def dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    def idxDay = dayOfYear - day;
    def idxId  = id - 1 + totalHistory();
	// log.debug "${idxDay} ${idxId}"
    
	def vInSec = state._history[idxId][idxDay]
    def vInHour = (int) Math.floor(vInSec/3600);
    vInSec = vInSec - vInHour * 3600;
    def vInMin = (int) Math.floor(vInSec/60);
    vInSec = vInSec - vInMin * 60;
    vInSec = (int) Math.round(vInSec);
    // log.debug "${vInHour} ${vInMin} ${vInSec}"
    
    return "" + vInHour + "h " + vInMin + "m\n" + vInSec + "sec";
    } catch (Exception e) {
    	log.error e
    	return "ERR"
    }
}

def displayHistory(int id) {
	if (id == 1) {
    	sendEvent(name: "oneDayDisp11", value: getHMS(1, 1) as String, unit: "", descriptionText: "one day energy update 0", displayed: false)
    	sendEvent(name: "oneDayDisp12", value: getHMS(1, 2) as String, unit: "", descriptionText: "one day energy update 1", displayed: false)
    }
    
    if (id == 2) {
    	sendEvent(name: "oneDayDisp21", value: getHMS(2, 1) as String, unit: "", descriptionText: "one day energy update 2", displayed: false)
    	sendEvent(name: "oneDayDisp22", value: getHMS(2, 2) as String, unit: "", descriptionText: "one day energy update 3", displayed: false)
    }
}

def parse(String description) {
	// log.debug "Parse received ${description}"
	def result = null
	def cmd = zwave.parse(description, [0x31: 1, 0x32: 1, 0x60: 3])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	} else {
        log.debug "cannot parse [${description}]"
        return
    }
    
	if (result) { 
		// log.debug "Parse ${description} returned ${result?.descriptionText}"
		return result
	} else {
        log.debug "cannot handle Event [${cmd}]"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    [:]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd)
{
    log.debug "handling Event [${cmd}]"
    [:]
}

def totalHistory() {
	return 2
}

def updateToday(int dim) {
    try {
	if (dim >= totalHistory()) {
    	log.error "dim error"
    	return
    }

	if (!state._history) {
    	initHistory();
    }
    
   	def cal = Calendar.getInstance();
    def day0 = cal.get(Calendar.DAY_OF_YEAR) - 1
    def day1 = day0 - 1;
    if (day1 < 0) day1 += 366;
    if (state._history[dim][day0] < state._history[dim][day1]) {
    	state._history[dim][day0] = state._history[dim][day1];
    	// a dummy event to print information into msg
        sendEvent(name: "powerDisp", value: state.powerValue + 1, unit: "", descriptionText: "M1: ${state.minutesOne}, M2: ${state.minutesTwo}", displayed: true)
	    sendEvent(name: "powerDisp", value: state.powerValue, unit: "", descriptionText: "reset accu for ${dim} as ${state._history[dim][day1]}, day0=${day0}, day1=${day1}", displayed: true)
    }
    } catch (Exception e) {
    	log.error e
    }
}

def setData(int dim, double v) {
    try {
	if (dim >= totalHistory()) {
    	log.error "dim error"
    	return
    }

	if (!state._history) {
    	initHistory();
    }
    
   	def cal = Calendar.getInstance();
    def day0 = cal.get(Calendar.DAY_OF_YEAR) - 1

    log.debug "history[${dim}][${day0}] = ${v}"
	
	state._history[dim][day0] = v        
    def day1 = day0 - 1;
    if (day1 < 0) day1 += 366;
    state._history[dim + totalHistory()][day0] = state._history[dim][day0] - state._history[dim][day1];
    } catch (Exception e) {
    	log.error e
    }
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    def dispValue
    def newValue
    def formattedValue
    def MAX_AMPS = 220
    def MAX_WATTS = 24000
    
    def now = new Date();
	def timeString = now.format("h:mm a", location.timeZone)
    def timeInMillis = now.getTime()

	Long kDelay = settings.kWhDelay as Long
    if (kDelay == null) {
    	kDelay = 900
    }
    
    if (state.lastTotalInfoTime == null) {
        state.lastTotalInfoTime = 0
    }
    
    boolean displayIt = Math.round((state.lastTotalInfoTime + kDelay * 1000) / kDelay / 1000) * kDelay * 1000 <= timeInMillis
    
    if (displayIt) {
    	state.lastTotalInfoTime = timeInMillis;
        updateToday(0);
        updateToday(1);
    }
    
    
    if (cmd.meterType == 33) {
	    log.debug "Received meterreport type ${cmd.meterType}, scale ${cmd.scale}, value ${cmd.scaledMeterValue}"
		if (cmd.scale == 0) {
        	newValue = Math.round(cmd.scaledMeterValue * 100) / 100
        	if (newValue != state.energyValue) {
        		formattedValue = String.format("%5.2f", newValue)
    			dispValue = "${formattedValue}\nkWh"
                sendEvent(name: "energyDisp", value: dispValue as String, unit: "", descriptionText: "Display Energy: ${newValue} kWh", displayed: false)
                state.energyValue = newValue
                BigDecimal costDecimal = newValue * 0.16 // ( kWhCost as BigDecimal )
                def costDisplay = String.format("%5.2f",costDecimal)
                state.costDisp = "Cost\n\$"+costDisplay
                if (state.display == 1) { sendEvent(name: "energyTwo", value: state.costDisp, unit: "", descriptionText: "Display Cost: \$${costDisp}", displayed: false) }
                [name: "energy", value: newValue, unit: "kWh", descriptionText: "Total Energy: ${formattedValue} kWh", displayed: displayIt]
            } else {
            	[:]
            }
		} 
		else if (cmd.scale == 1) {
            newValue = Math.round( cmd.scaledMeterValue * 100) / 100
            if (newValue != state.energyValue) {
            	formattedValue = String.format("%5.2f", newValue)
    			dispValue = "${formattedValue}\nkVAh"
                sendEvent(name: "energyDisp", value: dispValue as String, unit: "", descriptionText: "Display Energy: ${formattedValue} kVAh", displayed: false)
                state.energyValue = newValue
				[name: "energy", value: newValue, unit: "kVAh", descriptionText: "Total Energy: ${formattedValue} kVAh", displayed: displayIt]
            }
		}
		else if (cmd.scale==2) {				
        	newValue = Math.round(cmd.scaledMeterValue)		// really not worth the hassle to show decimals for Watts
            if (newValue > MAX_WATTS) { return [:] }				// Ignore ridiculous values (a 200Amp supply @ 120volts is roughly 24000 watts)
        	if (newValue != state.powerValue) {
    			dispValue = newValue+"\nWatts"
                sendEvent(name: "powerDisp", value: dispValue as String, unit: "", descriptionText: "Display Power: ${newValue} Watts", displayed: false)
                
                if (newValue < state.powerLow) {
                	dispValue = newValue+"\n"+timeString
                	if (state.display == 1) { sendEvent(name: "powerOne", value: dispValue as String, unit: "", descriptionText: "Lowest Power: ${newValue} Watts")	}
                    state.powerLow = newValue
                    state.powerLowDisp = dispValue
                }
                if (newValue > state.powerHigh) {
                	dispValue = newValue+"\n"+timeString
                	if (state.display == 1) { sendEvent(name: "powerTwo", value: dispValue as String, unit: "", descriptionText: "Highest Power: ${newValue} Watts")	}
                    state.powerHigh = newValue
                    state.powerHighDisp = dispValue
                }
                state.powerValue = newValue
                [name: "power", value: newValue, unit: "W", descriptionText: "Total Power: ${newValue} Watts", displayed: displayIt]
            } else {
                [:]
            }
		} else {

        }
 	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    log.debug "handling Event [${cmd}]"

    def dispValue
	def newValue
	def formattedValue
    def MAX_WATTS = 24000

    def now = new Date();
	def timeString = now.format("h:mm a", location.timeZone)
    def timeInMillis = now.getTime()

	Long dDelay = settings.detailDelay as Long
    if (dDelay == null) {
    	dDelay = 900
    }

	if (state.lastDetailInfoTime == null) {
        state.lastDetailInfoTime = 0
    }

	if (state.lastPollTwo == null) {
    	state.lastPollTwo = timeInMillis
    }

    if (state.lastPollOne == null) {
    	state.lastPollOne = timeInMillis
    }
    
   	log.debug "state.lastPollOne=${state.lastPollOne} state.lastPollTwo=${state.lastPollTwo}"

	boolean displayIt = Math.round((state.lastDetailInfoTime + dDelay * 1000) / dDelay / 1000) * dDelay * 1000 <= timeInMillis
    // log.debug "dDelay=${dDelay} lastTime=${state.lastDetailInfoTime} timeInMillis=${timeInMillis} displayIt=${displayIt}"
    if (displayIt) {
    	state.lastDetailInfoTime = timeInMillis;
    }
    
   	if (cmd.commandClass == 50) {    
   		def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1]) // can specify command class versions here like in zwave.parse
		if (encapsulatedCommand) {
			if (cmd.sourceEndPoint == 1) {
            	log.debug "ed 1 scale = ${encapsulatedCommand.scale}"
				if (encapsulatedCommand.scale == 2 ) {
					newValue = Math.round(encapsulatedCommand.scaledMeterValue)
                    
                    if (newValue > MAX_WATTS) { return }
                    if (newValue >= 300) { // fan1 is on, the HVAC is working
                    	state.secondsOne = state.secondsOne +(timeInMillis - state.lastPollOne) / 1000.0 // assume all time since last poll are on
//                        log.debug("HVAC 1 worked for accumulated ${state.secondsOne} seconds now.")
	                    state.minutesOne = String.format("%7.1f", state.secondsOne / 60)
                        if (!state.is1ON) {
                        	log.debug "turn on is1ON"
                        	state.is1ON = true
			                sendEvent(name: "oneOn", value: "1 on", unit: "", descriptionText: "AC1 is ON", displayed: true)
							log.debug "is1ON=${state.is1ON}"                        
                        }
		                sendEvent(name: "minutesOne", value: state.minutesOne as String, unit: "min", displayed: false)
                       	setData(0, state.secondsOne)
                        try {
                        displayHistory(1)
                        } catch (Exception e) {
                        	log.error e;
                        }
                    } else {
                    	// log.debug "${cmd} here, state.is1ON=${state.is1ON}"
                    	if (state.is1ON) {
                        	log.debug "turn off is1ON"
			                sendEvent(name: "oneOn", value: "1 off", unit: "", descriptionText: "AC1 is OFF", displayed: true)
                        	state.is1ON = false
                            log.debug "is1ON=${state.is1ON}"
                        }
                    }
                    
                    state.lastPollOne = timeInMillis
                    
					formattedValue = newValue as String
					dispValue = "${formattedValue}\nWatts"
					if (dispValue != state.powerL1Disp) {
						state.powerL1Disp = dispValue
						if (state.display == 2) {
							[name: "powerOne", value: dispValue, unit: "", descriptionText: "L1 Power: ${formattedValue} Watts", displayed: displayIt]
						}
						else {
                        	[:]
						}
					} else {
                        [:]
                    }
				} 
				else if (encapsulatedCommand.scale == 0 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nkWh"
					if (dispValue != state.energyL1Disp) {
						state.energyL1Disp = dispValue
						if (state.display == 2) {
							[name: "energyOne", value: dispValue, unit: "", descriptionText: "L1 Energy: ${formattedValue} kWh", displayed: displayIt]
						}
						else {
                        	[:]
						}
					}
                    else {
                        [:]
                    }
				}
				else if (encapsulatedCommand.scale == 1 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nkVAh"
					if (dispValue != state.energyL1Disp) {
						state.energyL1Disp = dispValue
						if (state.display == 2) {
							[name: "energyOne", value: dispValue, unit: "", descriptionText: "L1 Energy: ${formattedValue} kVAh", displayed: displayIt]
						}
						else {
                        	[:]
						}
					}
                    else {
                  		[:]
                    }
				}
                else {
                	[:]
                }
			} 
			else if (cmd.sourceEndPoint == 2) {
            	log.debug "ed 2 scale = ${encapsulatedCommand.scale}"
				if (encapsulatedCommand.scale == 2 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue)
                    if (newValue > MAX_WATTS ) { return }

					if (newValue >= 300) { // fan2 is on, the HVAC is working
                    	def diff = (timeInMillis - state.lastPollTwo) / 1000.0
                    	state.secondsTwo = state.secondsTwo + diff // assume all time since last poll are on
                        setData(1, state.secondsTwo)
//                        log.debug("HVAC 2 worked for accumulated ${state.secondsTwo} seconds now. state.lastPollTwo=${state.lastPollTwo} timeInMillis=${timeInMillis} diff=${diff}")
	                    state.minutesTwo = String.format("%7.1f", state.secondsTwo / 60)
                        if (!state.is2ON) {
                        	log.debug "turn on is2ON"
                        	state.is2ON = true
			                sendEvent(name: "twoOn", value: "2 on", unit: "", descriptionText: "AC2 is ON", displayed: true)
							log.debug "is2ON=${state.is2ON}"                        
                        }
		                sendEvent(name: "minutesTwo", value: state.minutesTwo as String, unit: "min", displayed: false)
                        try {
                        displayHistory(2)
                        } catch (Exception e) {
                        	log.error e;
                        }
                    } else {
                    	// log.debug "${cmd} here, state.is1ON=${state.is1ON}"
                    	if (state.is2ON) {
                        	log.debug "turn off is2ON"
			                sendEvent(name: "twoOn", value: "2 off", unit: "", descriptionText: "AC2 is OFF", displayed: true)
                        	state.is2ON = false
                            log.debug "is2ON=${state.is2ON}"
                        }
                    }
                    state.lastPollTwo = timeInMillis

					formattedValue = newValue as String
					dispValue = "${formattedValue}\nWatts"
					if (dispValue != state.powerL2Disp) {
						state.powerL2Disp = dispValue
						if (state.display == 2) {
							[name: "powerTwo", value: dispValue, unit: "", descriptionText: "L2 Power: ${formattedValue} Watts", displayed: displayIt]
						}
						else {
                        	[:]
						}
					} else {
                    	[:]
                    }
				} 
				else if (encapsulatedCommand.scale == 0 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nkWh"
					if (dispValue != state.energyL2Disp) {
						state.energyL2Disp = dispValue
						if (state.display == 2) {
							[name: "energyTwo", value: dispValue, unit: "", descriptionText: "L2 Energy: ${formattedValue} kWh", displayed: displayIt]
						}
						else {
                        	[:]
						}
					} else {
                    	[:]
                    }
				} 
				else if (encapsulatedCommand.scale == 1 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nkVAh"
					if (dispValue != state.energyL2Disp) {
						state.energyL2Disp = dispValue
						if (state.display == 2) {
							[name: "energyTwo", value: dispValue, unit: "", descriptionText: "L2 Energy: ${formattedValue} kVAh", displayed: displayIt]
						}
						else {
                        	[:]
						}
					} else {
                        [:]
                    }
				}
                else {
                	[:]
                }
            }
		}
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log.debug "Unhandled event ${cmd} type=" + cmd.getClass()
	[:]
}

def refresh() {			// Request HEMv2 to send us the latest values for the 4 we are tracking
	log.debug "refresh()"
    
	delayBetween([
		zwave.meterV2.meterGet(scale: 0).format(),		// Change 0 to 1 if international version
		zwave.meterV2.meterGet(scale: 2).format()
	])
    resetDisplay()
}

def poll() {
	log.debug "poll()"
	refresh()
}

def toggleDisplay() {
	log.debug "toggleDisplay()"
    
	if (state.display == 1) { 
		state.display = 2 
	}
	else { 
		state.display = 1
	}
	resetDisplay()
}

def resetDisplay() {
	log.debug "resetDisplay() - energyL1Disp: ${state.energyL1Disp}"
	
    sendEvent(name: "powerDisp", value: state.powerValue, unit: "", displayed: false)
    sendEvent(name: "energyDisp", value: state.energyValue, unit: "", displayed: false)
    
	if ( state.display == 1 ) {
		sendEvent(name: "powerOne", value: state.powerLowDisp, unit: "", displayed: false)     
    	sendEvent(name: "energyOne", value: state.lastResetTime, unit: "", displayed: false)
    	sendEvent(name: "powerTwo", value: state.powerHighDisp, unit: "", displayed: false)
    	sendEvent(name: "energyTwo", value: state.costDisp, unit: "", displayed: false)    	
	}
	else {
		sendEvent(name: "powerOne", value: state.powerL1Disp, unit: "", displayed: false)     
    	sendEvent(name: "energyOne", value: state.energyL1Disp, unit: "", displayed: false)	
    	sendEvent(name: "powerTwo", value: state.powerL2Disp, unit: "", displayed: false)
    	sendEvent(name: "energyTwo", value: state.energyL2Disp, unit: "", displayed: false)
        sendEvent(name: "minutesOne", value: state.minutesOne, unit: "", displayed: false)
        sendEvent(name: "minutesTwo", value: state.minutesTwo, unit: "", displayed: false)
	}
}

def reset() {
	Long resetGuard = settings.resetGuard as Long
    if (resetGuard != 666) {
    	// a dummy event to print information into msg
        sendEvent(name: "powerDisp", value: state.powerValue + 1, unit: "", descriptionText: "M1: ${state.minutesOne}, M2: ${state.minutesTwo}", displayed: true)
	    sendEvent(name: "powerDisp", value: state.powerValue, unit: "", descriptionText: "set resetGuard to 666 to do reset!", displayed: true)
        return
 	}

	log.debug "reset()"
    
	state.lastDetailInfoTime = 0
    state.lastTotalInfoTime = 0
    
	state.energyValue = -1
	state.powerValue = -1
	state.ampsValue = -1
	state.voltsValue = -1
	
    state.powerHigh = 0
    state.powerHighDisp = ""
    state.powerLow = 99999
    state.powerLowDisp = ""
    
    state.energyL1Disp = ""
    state.energyL2Disp = ""
    state.powerL1Disp = ""
    state.powerL2Disp = ""
    
    state.secondsOne = 0.0
    state.secondsTwo = 0.0
    
    state.lastPollOne = new Date().getTime()
    state.lastPollTwo = new Date().getTime()
    
    state.minutesOne = "0"
    state.minutesTwo = "0"
    
    if (!state.display) { state.display = 1 }	// Sometimes it appears that installed() isn't called

    def dateString = new Date().format("M/d/YY", location.timeZone)
    def timeString = new Date().format("h:mm a", location.timeZone)    
	state.lastResetTime = "Since\n"+dateString+"\n"+timeString
	state.costDisp = "Cost\n--"
	
    resetDisplay()
    sendEvent(name: "energyDisp", value: "", unit: "")
    sendEvent(name: "powerDisp", value: "", unit: "")	

// No V1 available
	def cmd = delayBetween( [
		zwave.meterV2.meterReset().format(),			// Reset all values
		zwave.meterV2.meterGet(scale: 0).format(),		// Request the values we are interested in (0-->1 for kVAh)
		zwave.meterV2.meterGet(scale: 2).format()
	], 1000)
    cmd
}

def configure() {
	log.debug "configure()"
    
	Long kDelay = settings.kWhDelay as Long
    Long dDelay = settings.detailDelay as Long
    
    if (kDelay == null) {		// Shouldn't have to do this, but there seem to be initialization errors
		kDelay = 60
	}

	if (dDelay == null) {
		dDelay = 300
	}
    
    sendEvent(name: "powerDisp", value: state.powerValue - 1, unit: "", descriptionText: "M1: ${state.minutesOne}, M2: ${state.minutesTwo}", displayed: true)
    
	def cmd = delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: 1).format(),			// Disable (=0) selective reporting
		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: 2).format(),			// Don't send L1 Data unless watts have changed by 15
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 2, scaledConfigurationValue: 5).format(),			// Don't send L1 Data unless watts have changed by 15
		zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: 5).format(),			// Don't send L2 Data unless watts have changed by 15
		zwave.configurationV1.configurationSet(parameterNumber: 100, size: 1, scaledConfigurationValue: 0).format(),		// reset to defaults
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 6152).format(),   	// All L1/L2 kWh
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 30).format(), 		// Every 30 seconds
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 774).format(),		// Power (Watts) L1, L2, Total
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 12).format() 		// every 10 seconds
	], 2000)
	log.debug cmd

	cmd
}