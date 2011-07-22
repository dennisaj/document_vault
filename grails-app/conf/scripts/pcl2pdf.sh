#!/bin/bash

PCL=$1
STARTPAGE=$2
ENDPAGE=$3

if [ -z $PCL ] || [ ! -r $PCL ] || [ -z $STARTPAGE ] || [ -z $ENDPAGE ]; then
	echo "Usage: $0 pclName startPage endPage"
	exit
fi

BASEDIR=$(dirname "$PCL")
BASENAME=$BASEDIR/$(basename "$PCL" .pcl)

/usr/local/bin/pcl6 -dSAFER -dNOPAUSE -dBATCH -dFirstPage=$STARTPAGE -dLastPage=$ENDPAGE -sDEVICE=pdfwrite -sOutputFile=$BASENAME.pdf $PCL

exit $?
