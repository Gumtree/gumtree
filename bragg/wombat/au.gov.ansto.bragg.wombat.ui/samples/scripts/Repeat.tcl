hmscan clear
hmscan add som 0.016602 0
for {set i 0} {$i < 12} {incr i} {
hmscan run 1 timer 3600
}
 