package org.gumtree.data.util.performance;

import java.util.Map;
import java.util.TreeMap;

public class Benchmarker {
    public static Map<String, Long> timers   = new TreeMap<String, Long>();
    public static Map<String, Long> counters = new TreeMap<String, Long>();
    public static Map<String, Long> starters = new TreeMap<String, Long>();
    public static Map<String, Long> nbcalls  = new TreeMap<String, Long>();
    public static Map<String, Long> nbthread = new TreeMap<String, Long>();

    public static void start(String label) {
        Long currentTime = System.currentTimeMillis();
        synchronized (Benchmarker.class) {
            if (counters.containsKey(label)) {
                Long counter = counters.get(label);
                Long call = nbcalls.get(label);
                if (counter <= 0) {
                    counters.put(label, (long) 1);
                    starters.put(label, currentTime);
                } else {
                    counters.put(label, ++counter);
                    Long thread = nbthread.get(label);
                    if( thread < counter ) {
                        nbthread.put(label, thread);
                    }
                }
                nbcalls.put(label, ++call);
            } else {
                counters.put(label, (long) 1);
                starters.put(label, currentTime);
                timers.put(label, (long) 0);
                nbcalls.put(label, (long) 1);
                nbthread.put(label, (long) 1);
            }
        }
    }

    public static void stop(String label) {
        Long currentTime = System.currentTimeMillis();
        synchronized (Benchmarker.class) {
            if (counters.containsKey(label)) {
                Long counter = counters.get(label);
                if (counter > 1) {
                    counter--;
                    counters.put(label, counter);
                } else if (counter > 0) {
                    counter--;
                    Long starter = starters.get(label);
                    Long time = timers.get(label);
                    time += (currentTime - starter);
                    timers.put(label, time);
                    starters.put(label, (long) 0);
                    counters.put(label, counter);
                } else {
                    System.out
                    .println(">>>>>>>>>>>>>>>>>> Benchmark  <<<<<<<<<<<<<<<<<<<<<<");
                    System.out.println("To much stop for: " + label);
                    System.out
                    .println(">>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                }
            } else {
                System.out
                .println(">>>>>>>>>>>>>>>>>> Benchmark  <<<<<<<<<<<<<<<<<<<<<<");
                System.out.println("Stoping inexistent timer: " + label);
                System.out
                .println(">>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            }
        }
    }

    public static Map<String, Long> getTimers() {
        return timers;
    }

    public static String print() {
        String result = "";
        for (String label : timers.keySet()) {
            result += print( label
                    + ": "
                    + ((Benchmarker.getTimers().get(label)) / (float) 1000) + " s",
                    60)
                    + print( " nb calls: " + nbcalls.get(label), 20 )
                    + print( " max thread: " +  nbthread.get(label), 20)
                    + "\n";
        }
        return result;
    }

    public static String print(String value, int length ) {
        String result = value;

        if( value.length() < length ) {
            for( int i = 0; i < length - value.length(); i++ ) {
                result += " ";
            }
        }
        return result;
    }

}
