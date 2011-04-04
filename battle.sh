#!/bin/bash

mvn clean install
if [ "$?" -ne "0" ]; then
	echo "Unable to start Nets Robocode Battle because of a build error."
	exit 1
fi

mvn -Pbattle -N

