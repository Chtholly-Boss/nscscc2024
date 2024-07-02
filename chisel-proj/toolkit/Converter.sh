#! /usr/bin/bash
little_endian=""
while read line
do
    little_endian="\
${line:6:2}\
${line:4:2}\
${line:2:2}\
${line:0:2}\
"
    echo $little_endian
done
