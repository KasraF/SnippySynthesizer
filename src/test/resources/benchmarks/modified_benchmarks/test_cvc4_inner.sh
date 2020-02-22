#!/bin/bash

filename=`echo $1 | cut -d'/' -f 2`
echo "$filename"
base_filename="${filename%.*}"
out=`timeout 20 /c/utils/sygus-solvers/cvc4/cvc4-1.7-win64-opt.exe $1`
retval=$?
if [ $retval = 0 ]; then
	echo $out | tr '\r' '\n' | awk 'END{print}'
fi
for cvc4_file in for_cvc4/$base_filename*.sl; do
	out=`timeout 20 /c/utils/sygus-solvers/cvc4/cvc4-1.7-win64-opt.exe $cvc4_file`
	retval=$?
	if [ $retval = 0 ]; then
		echo $out | tr '\r' '\n' | awk 'END{print}'
	fi
done