set -m
for i in {1..5}
do
    (java Client "b146-35" "1025" "u${i}") &
done

(java Client b146-35 1025 Sender <<< "all#hi") &