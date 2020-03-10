#!/bin/bash -xe


## Parse arguments
print_usage() {
  printf "
Tests concurrency handling for the service.

Usage: ./%s [-h <HOST:PORT>]
Options:
    -h      Specify host of the started app, for example: -h http://localhost:3000
" "$(basename "$0")"
}

HOST='http://localhost:3000'

while getopts 'h:' flag; do
  case "${flag}" in
  h) HOST="$OPTARG" ;;
  *)
    print_usage
    exit 1
    ;;
  esac
done


## Set Current Dir to the script's dir
cd "${0%/*}"


## Warm-up
ab -n 30 -c 30 -s 60 $HOST/status
for i in {1..30}
do
   ab -n 1 -c 1 -p ./post_data.txt -H "X-Api-Key:key$i" -T "multipart/form-data; boundary=1234567890" "$HOST/faces/MaryMaree?retrain=no"
done


## Run test
ab -n 10000 -c 100 -s 60 $HOST/status
ab -n 100 -c 50 -p ./post_data.txt -H "X-Api-Key:key1" -T "multipart/form-data; boundary=1234567890" "$HOST/faces/JohnJohnson?retrain=no"
for i in {1..30}
do
   ab -n 10 -c 1 -p ./post_data.txt -H "X-Api-Key:key$i" -T "multipart/form-data; boundary=1234567890" "$HOST/faces/JohnJohnson?retrain=yes" &
done

