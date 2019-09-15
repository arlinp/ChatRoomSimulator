for i in {1..5}
do
    (java ClientReceiver "b146-36" "1025" "u${i}") & (java ClientSender "b146-36" "1025" "c${i}")
done