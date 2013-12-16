var title = "Taipan Status";
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
            	            {"classId":"ef", "deviceId":"ef", "title":"ef", "units":"meV"}, 
            	            {"classId":"en", "deviceId":"en", "title":"en", "units":"meV"}, 
            	            {"classId":"qh", "deviceId":"qh", "title":"qh", "units":""}, 
            	            {"classId":"qk", "deviceId":"qk", "title":"qk", "units":""}, 
            	            {"classId":"ql", "deviceId":"ql", "title":"ql", "units":""}
            	            ]
               },
               {"group":"SCAN STATUS", 
            	   "items":[{"classId":"scan_variable", "deviceId":"/commands/scan/bmonscan/scan_variable", "title":"Scan Variable", "units":""},  
            	            {"classId":"scan_variable_value", "deviceId":"/commands/scan/bmonscan/feedback/scan_variable_value", "title":"Current Value", "units":""},
            	            {"classId":"currpoint", "deviceId":"currpoint", "title":"Scan Point", "units":""}
            	   ]
               },
//               {"group":"TEMPERATURE CONTROLLER", 
//            	   "items":[{"classId":"sensorValueA", "deviceId":"/sample/tc1/sensor/sensorValueA", "title":"Sensor A", "units":"K"},
//            	            {"classId":"sensorValueB", "deviceId":"/sample/tc1/sensor/sensorValueB", "title":"Sensor B", "units":"K"},
//            	            {"classId":"sensorValueC", "deviceId":"/sample/tc1/sensor/sensorValueC", "title":"Sensor C", "units":"K"},
//            	            {"classId":"sensorValueD", "deviceId":"/sample/tc1/sensor/sensorValueD", "title":"Sensor D", "units":"K"}
//            	   ]
//               },
               {"group":"TEMPERATURE CONTROLLER", 
            	   "items":[{"classId":"tc1SensorB", "deviceId":"/sample/tc1/sensor/sensorValueB", "title":"TC1 Sensor B", "units":"K"}
            	   ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"}
               ];

var histmemUrl = "taipan/rest/plot?height=400&width=600";