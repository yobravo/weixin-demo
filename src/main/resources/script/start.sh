#!/bin/bash
app_path=$(dirname $0)

app_full_path=`cd $(dirname $0); pwd`;
APP_NAME=`basename ${app_full_path}`;

JAR=$1
jvm_opt=$2;

nohup java ${jvm_opt} -jar ${JAR} & > /dev/null 2>&1