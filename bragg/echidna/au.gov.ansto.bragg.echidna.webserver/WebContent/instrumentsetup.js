var title = "Echidna";
var useNewProxy = true;
var batchEnabled = true;
var devices = [
               {"group":"BEAM STATUS", 
            	   "items":[{"classId":"plc_tertiary", "deviceId":"plc_tertiary", "title":"Sample Shutter", "units":""},
            	            {"classId":"bm1_event_rate", "deviceId":"bm1_event_rate", "title":"Monitor 1 Rate", "units":"c/s"},
            	            {"classId":"bm2_event_rate", "deviceId":"bm2_event_rate", "title":"Monitor 2 Rate", "units":"c/s"},
            	            {"classId":"bm3_event_rate", "deviceId":"bm3_event_rate", "title":"Monitor 3 Rate", "units":"c/s"}
            	            ]
               },
               {"group":"EXPERIMENT STATUS", 
            	   "items":[{"classId":"stth", "deviceId":"stth", "title":"stth", "units":"deg"}, 
            	            {"classId":"currpoint", "deviceId":"currpoint", "title":"Current Point", "units":""},
            	            {"classId":"hmm_preset", "deviceId":"hmm_preset", "title":"HMM Preset", "units":"s"}
            	            ]
               },
               {"group":"EXPERIMENT INFO", 
            	   "items":[{"classId":"title", "deviceId":"title", "title":"Proposal", "units":""},
            	            {"classId":"user", "deviceId":"user", "title":"User", "units":""}
            	            ]
               },
               {"group":"ROBOT CHANGER", 
            	   "items":[{"classId":"Pallet_Nam", "deviceId":"/sample/robby/Control/Pallet_Nam", "title":"Pallet Name", "units":""},  
            	            {"classId":"Pallet_Idx", "deviceId":"/sample/robby/Control/Pallet_Idx", "title":"Sample Position", "units":""},
            	            {"classId":"setpoint", "deviceId":"/sample/robby/setpoint", "title":"Robot Status", "units":""}
            	   ]
               },
//               ,
//               {"group":"TEMPERATURE CONTROLLER 1", 
//            	   "items":[{"classId":"sensor1ValueA", "deviceId":"/sample/tc1/sensor/sensorValueA", "title":"TC1 Sensor A", "units":"K"},
//            	            {"classId":"sensor1ValueB", "deviceId":"/sample/tc1/sensor/sensorValueB", "title":"TC1 Sensor B", "units":"K"},
//            	            {"classId":"sensor1ValueC", "deviceId":"/sample/tc1/sensor/sensorValueC", "title":"TC1 Sensor C", "units":"K"},
//            	            {"classId":"sensor1ValueD", "deviceId":"/sample/tc1/sensor/sensorValueD", "title":"TC1 Sensor D", "units":"K"}
//            	   ]
//               },
//               {"group":"TEMPERATURE CONTROLLER 2", 
//            	   "items":[{"classId":"sensor2ValueA", "deviceId":"/sample/tc2/sensor/sensorValueA", "title":"TC2 Sensor A", "units":"K"},
//            	            {"classId":"sensor2ValueB", "deviceId":"/sample/tc2/sensor/sensorValueB", "title":"TC2 Sensor B", "units":"K"},
//            	            {"classId":"sensor2ValueC", "deviceId":"/sample/tc2/sensor/sensorValueC", "title":"TC2 Sensor C", "units":"K"},
//            	            {"classId":"sensor2ValueD", "deviceId":"/sample/tc2/sensor/sensorValueD", "title":"TC2 Sensor D", "units":"K"}
//            	   ]
//               },
               {"group":"FURNACE", 
            	   "items":[{"classId":"furnaceTemp", "deviceId":"/sample/tc1/sensor", "title":"Temperature", "units":"K"},
            	            {"classId":"furnaceSetpoint", "deviceId":"/sample/tc1/setpoint", "title":"Set Point", "units":"K"}
            	   ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"}
               ];

var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_X&scaling_type=LIN&screen_size_x=900&screen_size_y=500";