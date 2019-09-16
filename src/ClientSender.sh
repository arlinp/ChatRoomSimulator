set -m
for i in {1..500}
do
    (java Client "b146-33" "1025" "s${i}" <<< "r${i}#hi") &
done
