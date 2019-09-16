set -m
i=0
for x in {1..2}
do
    (java Client "b146-20" "1025" "s${i}") &
done  <<EOD
r${i=$(($i+1))}#hi
quit
EOD