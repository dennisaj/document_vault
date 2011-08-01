#!/bin/bash

PDF=$1

if [ -z $PDF ] || [ ! -r $PDF ]; then
	echo "Usage: $0 pdfName"
	exit 1
fi

BASEDIR=$(dirname "$PDF")
BASENAME=$BASEDIR/$(basename "$PDF" .pdf)

/usr/local/bin/gs -q -sDEVICE=png16m -r300 -dNOPAUSE -dBATCH -dSAFER -sOutputFile="$BASENAME-%d.png" "$PDF"

if [[ $? != 0 ]]; then
	echo "Unable to convert file"
	exit 1
fi

for i in $(ls $BASENAME*.png)  ; do
	THUMB_NAME=$BASEDIR/$(basename "$i" .png)-thumbnail.png
	/usr/local/bin/convert $i -resize 50% $i
	if [[ $? != 0 ]]; then
		echo "Unable to resize file $i"
		exit 1
	fi
	/usr/local/bin/convert $i -resize 80 -black-threshold 75% $THUMB_NAME 
	if [[ $? != 0 ]]; then
		echo "Unable to create thumbnail for $i"
		exit 1
	fi
done

exit 0
