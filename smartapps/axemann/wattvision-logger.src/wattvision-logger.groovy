/**
 *  Copyright 2019 SmartThings
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
 *  Wattvision Logger
 *
 *  Author: Axemann
 *  Creation Date: 2019-09-07
 *
 *  Revision history:
 *      2019-09-17 - Fix energy reporting units (kWh -> Wh), code cleanup
 *      2019-09-08 - Add energy (kWh) reporting, update config page, moved api_id and api_key to IDE Settings, fix smartphone icon issues, fix device requirements, code cleanup
 *      2019-09-07 - Initial commit and bugfixes
 */
 
import java.time.*
import java.net.*

definition(
	name: "Wattvision Logger",
	namespace: "axemann",
	author: "Axemann",
	description: "Pushes data from an existing energy meter to your Wattvision account",
    category: "Green Living",
    iconUrl: "https://github.com/axemann/wattvision-logger/raw/master/smartapps/axemann/wattvision-logger.src/images/wattvision.png",
    iconX2Url: "https://github.com/axemann/wattvision-logger/raw/master/smartapps/axemann/wattvision-logger.src/images/wattvision@2x.png",
    iconX3Url: "https://github.com/axemann/wattvision-logger/raw/master/smartapps/axemann/wattvision-logger.src/images/wattvision@2x.png",
) {
    appSetting "api_id"
    appSetting "api_key"
}

preferences {
    section () {
        paragraph title: "Note", "Log into your Wattvision account at https://www.wattvision.com/usr/api to retrieve your API ID and API Key, then enter those in the App Settings page under Settings in the SmartThings IDE after completing the SmartApp installation."
    }
	section ("Log devices") {
        input "power", "capability.powerMeter", title: "Power Meter", required: true, multiple: false
        input "energy", "capability.energyMeter", title: "Energy Meter", required: false, multiple: false
    }
    section ("Wattvision Sensor ID") {
        input "sensor_id", "text", title: "Wattvision Sensor ID"
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(power, "power", handlePowerEvent)
    subscribe(energy, "energy", handleEnergyEvent)
}

def handlePowerEvent(evt) {
    logPowerField(evt,"power") { it.toString() }
}

def handleEnergyEvent(evt) {
    logEnergyField(evt,"energy") { it.toString() }
}

private logPowerField(evt, field, Closure c) {
    def value = c(evt.value)
    float watts = value.toFloat()
    def api_id = appSettings.api_id
    def api_key = appSettings.api_key
    def now = new Date().format("yyyy-MM-dd'T'HH:mm:ss")
	def uri = "https://www.wattvision.com/api/v0.2/elec"
	
    def powerparams = [
            uri: uri,
            body: [
            sensor_id: sensor_id,
            api_id: api_id,
            api_key: api_key,
            time: now,
            watts: watts
            ]
        ]
    
    try {
        httpPostJson(powerparams) { resp ->
            resp.headers.each {
            }
            log.info "Sending power data to Wattvision:  ${powerparams.body.watts}W"
            log.info "Wattvision server response: ${resp.data}"
        }
    } catch (e) {
    	log.debug "Data sent: ${powerparams.body} to ${powerparams.uri}"
        log.debug "something went wrong: $e"
    }
}

private logEnergyField(evt, field, Closure c) {
    def value = c(evt.value)
    float watthours = (value.toFloat() * 1000)
    def api_id = appSettings.api_id
    def api_key = appSettings.api_key
    def now = new Date().format("yyyy-MM-dd'T'HH:mm:ss")
	def uri = "https://www.wattvision.com/api/v0.2/elec"
	
    def params = [
            uri: uri,
            body: [
            sensor_id: sensor_id,
            api_id: api_id,
            api_key: api_key,
            time: now,
            watthours: watthours
            ]
        ]
    
    try {
        httpPostJson(params) { resp ->
            resp.headers.each {
            }
            log.info "Sending energy data to Wattvision:  ${params.body.watthours}Wh"
            log.info "Wattvision server response: ${resp.data}"
        }
    } catch (e) {
    	log.debug "Data sent: ${params.body} to ${params.uri}"
        log.debug "something went wrong: $e"
    }
}