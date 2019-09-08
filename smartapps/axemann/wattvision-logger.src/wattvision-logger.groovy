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
	//iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision.png",
    iconUrl: "https://github.com/axemann/wattvision-logger/raw/master/smartapps/axemann/wattvision-logger.src/images/wattvision.png",
	// iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision%402x.png",
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
        input "power", "capability.powerMeter", title: "Power Meters", required: false, multiple: false
    }
    section ("Wattvision Sensor ID") {
        input "sensor_id", "text", title: "Wattvision Sensor ID"
    }
    // section ("Wattvision API ID") {
    //     input "api_id", "text", title: "Wattvision API ID"
    // }
    // section ("Wattvision API Key") {
    //     input "api_key", "text", title: "Wattvision API Key"
    // }
}
// preferences {
//     page(name: "landing_page", title: "App Status", nextPage: "sensor", uninstall: true) {
//         section("Tap Next to view or configure settings")
//     }
// }

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(power, "power", handlePowerEvent)
}

def handlePowerEvent(evt) {
    logField(evt,"power") { it.toString() }
}

private logField(evt, field, Closure c) {
    def value = c(evt.value)
    float watts = value.toFloat()
    def api_id = appSettings.api_id
    def api_key = appSettings.api_key
    def body = '{"sensor_id":"' + "${sensor_id}" + '","api_id":"' + "${api_id}" + '","api_key":"' + "${api_key}" + '","watts":"' + "${watts}" + '"}'
	def uri = "https://www.wattvision.com/api/v0.2/elec"
	
    def params = [
            uri: uri,
            body: [
            sensor_id: sensor_id,
            api_id: api_id,
            api_key: api_key,
            watts: watts
            ]
        ]
    
    try {
        httpPostJson(params) { resp ->
            resp.headers.each {
            }
            log.info "Sending data to Wattvision:  ${params.body.watts}W"
            log.info "Wattvision server response: ${resp.data}"
        }
    } catch (e) {
    	log.debug "Data sent: ${body} to ${uri}"
        log.debug "something went wrong: $e"
    }
}