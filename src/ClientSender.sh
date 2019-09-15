set -m
for i in {1..50}
do
    (java Client "b146-35" "1025" "s${i}" <<< "r${i}#hi") &
done
