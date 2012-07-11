set sample {Vn 2.41A calibration}
drive mom 53.34
hmscan clear
hmscan add som [SplitReply [som]] 0
hmscan run 9 timer 14400
set sample {Vn 1.53A calibration}
drive mom 43.9
hmscan clear
hmscan add som [SplitReply [som]] 0
hmscan run 3 timer 14400
set sample {Vn 1.22A calibration}
drive mom 68.42
hmscan clear
hmscan add som [SplitReply [som]] 0
hmscan run 3 timer 14400