#!/bin/bash

cat sheepdog/configs/final1.txt | while read conf
do
	#players=( g1 g2  g2prime g3 g4 g5 g6 g7 g8_final g9)
	players=( g2prime g2final g2oneonone)
	#players=( g2 g3 g4 )
	fname=${conf// /_}
	echo "Fname $fname"
	touch $fname
	for p in "${players[@]}"
	do 
		echo $p $conf
		echo $p >> $fname
		java sheepdog.sim.Sheepdog $p $conf false false 2>&1 1 >/dev/null | grep SUCCESS >> $fname
	done
done


cat sheepdog/configs/final2.txt | while read conf
do
	#players=( g1 g2  g2prime g3 g4 g5 g6 g7 g8_final g9)
	players=( g2prime g2final g2oneonone)
	#players=( g2 g3 g4 )
	fname=${conf// /_}
	echo "Fname $fname"
	touch $fname
	for p in "${players[@]}"
	do 
		echo $p $conf
		echo $p >> $fname
		java sheepdog.sim.Sheepdog $p $conf true false 2>&1 1 >/dev/null | grep SUCCESS >> $fname
	done
done


# 1. Limit the number of active dogs
# 2. Limit the number of adtive dogs by the end
# 3. Closest one on one (& reverse?)
#
