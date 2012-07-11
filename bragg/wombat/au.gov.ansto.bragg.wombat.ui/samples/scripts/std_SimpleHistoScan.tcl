# simple batch that takes one static acquisition
# just change the number at the end
# also you can change the mode (monitor or timer)
# uncomment the loop as required
#for {set i 0} {$i < 306} {incr i} {

::histogram_memory::setmode normal
::histogram_memory::count_bm_controlled timer 60
::histogram_memory::save 0

#}