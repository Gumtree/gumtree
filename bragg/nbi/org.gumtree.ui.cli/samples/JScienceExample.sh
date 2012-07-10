import javax.units.*;
import org.jscience.physics.measures.*;
METER = new BaseUnit("m");
m0 = Measure.valueOf(100, METER);
m1 = m0.times(33).divide(2);
print(m1);