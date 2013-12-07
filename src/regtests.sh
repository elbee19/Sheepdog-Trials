#!/bin/bash

echo $$ > /tmp/amit.id

#cat sheepdog/configs/normal.txt | while read conf
#do
#	players=( g1 g2 g3 g4 g5 g6 g7 g8_final g9)
#	#players=( g2 g3 g4 )
#	fname=${conf// /_}
#	echo "Fname $fname"
#	touch $fname
#	for p in "${players[@]}"
#	do 
#		echo $p $conf
#		echo $p >> $fname
#		java sheepdog.sim.Sheepdog $p $conf false false 2>&1 1 >/dev/null | grep SUCCESS >> $fname
#	done
#done

#players=( g2prime g2startmod10 g2startmod20 g2startmod30 g2startmod40 g2startmod50 g2startmod60 g2startmod70 g2startmod80 g2startmod90 g2startmod100 )
#fname="startmod"
#echo "Fname $fname"
#touch $fname
#for p in "${players[@]}"
#do 
	#echo $p
	#echo $p >> $fname
	#java sheepdog.sim.Sheepdog $p 100 700 0 false false 2>&1 1 >/dev/null | grep SUCCESS >> $fname
	##java sheepdog.sim.Sheepdog $p 100 100 0 false false 2>&1 1 >/dev/null | grep SUCCESS >> $fname
#done


#players=( g2prime g2endmod1 g2endmod2 g2endmod3 g2endmod4 g2endmod5 g2endmod6 g2endmod7 g2endmod8 g2endmod9 g2endmod10 )
#fname="endmod"
#echo "Fname $fname"
#touch $fname
#for p in "${players[@]}"
#do 
	#echo $p
	#echo $p >> $fname
	#java sheepdog.sim.Sheepdog $p 20 350 0 false false 2>&1 1 >/dev/null | grep SUCCESS >> $fname
	##java sheepdog.sim.Sheepdog $p 15 50 0 false false 2>&1 1 >/dev/null | grep SUCCESS >> $fname
#done


fname="endmod"
cat sheepdog/configs/advanced.txt | while read conf
do
	players=( g1 g2  g2prime g2oneonone g3 g4 g5 g6 g7 g8_final g9)
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

