# simple script to run a beam monitor scan 

for {set i 0} {$i < 11} {incr i} {
drive mf2 [expr $i]

bmonscan clear
# set channel variable chooses which counter channel
# currently: wombat beam monitor = 0
#            shielded 3He        = 1
#            old hifar U235      = 2
bmonscan setchannel 0
# after the "bmonscan add" put the motor name,
# the start position and the step size
bmonscan add mchi 90 0.05
# after "bmonscan run " place the number of steps
# then the mode (timer or monitor) and the 
# time or monitor count for each step
bmonscan run 41 timer 10

}