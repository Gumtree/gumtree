# simple script to run a beam monitor scan 
bmonscan clear
# set channel variable chooses which counter channel
# currently: wombat beam monitor = 0
#            shielded 3He        = 1
#            old hifar U235      = 2
# NOTE THIS CURRENTLY DOESN'T WORK
bmonscan setchannel 0
# after the "bmonscan add" put the motor name,
# the start position and the step size
bmonscan add ss1vo -10 1
# after "bmonscan run " place the number of steps
# then the mode (timer or monitor) and the 
# time or monitor count for each step
bmonscan run 21 timer 15