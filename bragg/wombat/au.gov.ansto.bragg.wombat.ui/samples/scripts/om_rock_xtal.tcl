# set the zero angle for the plate
set sample "ruby/alumina xtal omega rock 1.5A"
set omang 22.5
som softzero [expr {0 - $omang}]
set omlow [expr $omang - 16.0]
set omhi [expr $omang + 16.0]
set omst [expr $omang - 15.0]
som softlowerlim $omlow
som softupperlim $omhi
hmscan clear
hmscan add som $omst 0.1
hmscan run 225 timer 5