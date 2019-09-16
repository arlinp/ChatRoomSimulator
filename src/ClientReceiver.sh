set -m
for i in {1..2}
do
    (java Client "b146-20" "1025" "r${i}") &
done