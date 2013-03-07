#!/bin/bash

for i in 1 2 3 4 5 6 
do
	echo "{ \"location\" : [ 40.33392${i} , -111.67997${i} ] }" > location${i}.json
done


