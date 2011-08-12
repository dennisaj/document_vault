#!/bin/bash

STARTPAGE=$1
ENDPAGE=$2
PCL=$3
PDF=$4

if [ -z $PDF ] || [ -z $PCL ] || [ ! -r $PCL ] || [ -z $STARTPAGE ] || [ -z $ENDPAGE ]; then
	echo "Usage: $0 startPage endPage inputPcl outputPdf"
	exit 1
fi

/usr/local/bin/pcl6 -dSAFER -dNOPAUSE -dBATCH -dFirstPage=$STARTPAGE -dLastPage=$ENDPAGE -sDEVICE=pdfwrite -sOutputFile=$PDF $PCL

exit $?
