set -m
for (( i=1; i<=$1; i++ ))
do
    (java Client "b146-35" "1025" "r${i}") &
done
