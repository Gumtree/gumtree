for {set i 0} {$i < 40} {incr i} {

hmscan clear
hmscan add som [SplitReply [som]] 0
hmscan run 24 timer 300
 
}