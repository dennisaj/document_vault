#!/bin/bash

PDF=$1
HOST=$2
PORT=$3
TYPE=$4

if [ -z $PDF ] || [ ! -r $PDF ] || [ -z $HOST ] || [ -z $PORT ] || [ -z $TYPE ]; then
	echo "Usage: $0 pdf printerHost printerPort printerType"
	exit 1
fi

/usr/local/bin/gs -q -dBATCH -dNOPAUSE -r300 -sDEVICE=$TYPE -sOutputFile=- $PDF | nc $HOST $PORT
exit $PIPESTATUS
