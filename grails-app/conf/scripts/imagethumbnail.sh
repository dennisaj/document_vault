#!/bin/bash

IMAGE=$1
SIZE=${2:-240}

if [ -z $IMAGE ] || [ ! -r $IMAGE ]; then
	echo "Usage: $0 imagePath [size]"
	exit
fi

BASEDIR=$(dirname "$IMAGE")
EXTENSION=${IMAGE##*.}
BASENAME=$BASEDIR/$(basename "$IMAGE" ".$EXTENSION")

THUMB_NAME=$BASENAME-thumbnail.png

/usr/local/bin/convert $IMAGE -resize $SIZE $THUMB_NAME 
if [[ $? != 0 ]]; then
	echo "Unable to create thumbnail for $IMAGE"
	exit 1
fi
