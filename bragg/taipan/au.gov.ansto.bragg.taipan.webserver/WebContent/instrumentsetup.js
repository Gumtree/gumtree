var title = "Taipan";
var batchEnabled = true;
var devices = [
               {"group":"BEAM STATUS", 
            	   "items":[{"classId":"monitor_time", "deviceId":"monitor_time", "title":"Time of Counting", "units":"s"}, 
            	            {"classId":"bm1_counts", "deviceId":"bm1_counts", "title":"Monitor", "units":"ct"},
            	            {"classId":"bm2_counts", "deviceId":"bm2_counts", "title":"Detector", "units":"ct"}
            	            ]
               },
               {"group":"MOTOR STATUS", 
            	   "items":[{"classId":"m1", "deviceId":"m1", "title":"m1", "units":"deg"}, 
            	            {"classId":"m2", "deviceId":"m2", "title":"m2", "units":"deg"}, 
            	            {"classId":"s1", "deviceId":"s1", "title":"s1", "units":"deg"}, 
            	            {"classId":"s2", "deviceId":"s2", "title":"s2", "units":"deg"}, 
            	            {"classId":"a1", "deviceId":"a1", "title":"a1", "units":"deg"}, 
            	            {"classId":"a2", "deviceId":"a2", "title":"a2", "units":"deg"}
            	            ]
               },
               {"group":"PARAMETERS", 
            	   "items":[{"classId":"ei", "deviceId":"ei", "title":"ei", "units":"meV"},
            		   		{"classId":"vei", "deviceId":"vei", "title":"vei", "units":"meV"},
            		   		{"classId":"vei_1", "deviceId":"vei_1", "title":"vei_1", "units":"meV"},
            	            {"classId":"ef", "deviceId":"ef", "title":"ef", "units":"meV"}, 
            	            {"classId":"en", "deviceId":"en", "title":"en", "units":"meV"}, 
            	            {"classId":"qh", "deviceId":"qh", "title":"qh", "units":""}, 
            	            {"classId":"qk", "deviceId":"qk", "title":"qk", "units":""}, 
            	            {"classId":"ql", "deviceId":"ql", "title":"ql", "units":""},
            	            {"classId":"qm", "deviceId":"qm", "title":"qm", "units":""}
            	            ]
               },
               {"group":"SCAN STATUS", 
            	   "items":[{"classId":"scan_variable", "deviceId":"/commands/scan/bmonscan/scan_variable", "title":"Scan Variable", "units":""},  
            	            {"classId":"scan_variable_value", "deviceId":"/commands/scan/bmonscan/feedback/scan_variable_value", "title":"Current Value", "units":""},
            	            {"classId":"currpoint", "deviceId":"currpoint", "title":"Scan Point", "units":""}
            	   ]
               },
               {"group":"TEMPERATURE CONTROLLER", 
            	   "items":[{"classId":"sensorValueA", "deviceId":"/sample/tc1/sensor/sensorValueA", "title":"Sensor A", "units":"K"},
            	            {"classId":"sensorValueB", "deviceId":"/sample/tc1/sensor/sensorValueB", "title":"Sensor B", "units":"K"},
            	            {"classId":"sensorValueC", "deviceId":"/sample/tc1/sensor/sensorValueC", "title":"Sensor C", "units":"K"},
            	            {"classId":"sensorValueD", "deviceId":"/sample/tc1/sensor/sensorValueD", "title":"Sensor D", "units":"K"}
            	   ]
               },
//               {"group":"TEMPERATURE CONTROLLER 1", 
//            	   "items":[
//            	            {"classId":"tc1SensorB", "deviceId":"/sample/tc1/sensor/sensorValueB", "title":"TC1 Sensor B", "units":"K"},
//            	            {"classId":"tc3Driveable", "deviceId":"/sample/tc3/sensor/setpoint1", "title":"TC3 Driveable", "units":"K"},
//            	            {"classId":"tc1Driveable", "deviceId":"/sample/tc1/sensor/setpoint1", "title":"TC1 Driveable", "units":"K"}
//							{"classId":"tc9Sensor1", "deviceId":"/sample/tc9/Loop1/sensor", "title":"TC9 Loop1 Sensor", "units":"K"},
//							{"classId":"tc9Sensor2", "deviceId":"/sample/tc9/Loop2/sensor", "title":"TC9 Loop2 Sensor", "units":"K"},
//							{"classId":"tc9Sensor3", "deviceId":"/sample/tc9/Loop3/sensor", "title":"TC9 Loop3 Sensor", "units":"K"},
//							{"classId":"tc9Sensor4", "deviceId":"/sample/tc9/Loop4/sensor", "title":"TC9 Loop4 Sensor", "units":"K"},
//							{"classId":"tc9Setpoint1", "deviceId":"/sample/tc9/Loop1/setpoint", "title":"TC9 Loop1 Setpoint", "units":"K"},
//							{"classId":"tc9Setpoint2", "deviceId":"/sample/tc9/Loop2/setpoint", "title":"TC9 Loop2 Setpoint", "units":"K"},
//							{"classId":"tc9Setpoint3", "deviceId":"/sample/tc9/Loop3/setpoint", "title":"TC9 Loop3 Setpoint", "units":"K"},
//							{"classId":"tc9Setpoint4", "deviceId":"/sample/tc9/Loop4/setpoint", "title":"TC9 Loop4 Setpoint", "units":"K"}
//            	   ]
//               },
//               {"group":"TEMPERATURE CONTROLLER 2", 
//            	   "items":[
//							{"classId":"tc2Sensor", "deviceId":"/sample/tc2/Sensor/value", "title":"TC2 Sensor", "units":"K"},
//							{"classId":"tc2Setpoint", "deviceId":"/sample/tc2/setpoint", "title":"TC2 Setpoint", "units":"K"},
//							{"classId":"DilutTemp", "deviceId":"/sample/magnetic/DilutTempReading", "title":"Dilut Temp", "units":"K"}
//            	   ]
//               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"}
               ];

var histmemUrl = "taipan/rest/plot?height=400&width=600;"
				+ "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=640&screen_size_y=600";