# set the zero angle for the plate
set omang 157.5
som softzero [expr {0 - $omang}]
set omlow [expr $omang - 16.0]
set omhi [expr $omang + 16.0]
set omst [expr $omang - 2.5]
som softlowerlim $omlow
som softupperlim $omhi
hmscan clear
hmscan add som $omst 5
hmscan run 2 timer 60