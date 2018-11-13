#!/bin/bash
app_path=$(dirname $0)

app_full_path=`cd $(dirname $0); pwd`;
APP_NAME=$(basename ${app_full_path});

ps aux | grep ${APP_NAME} | grep -v "grep\|bin/bash"

pid=$(ps aux | grep ${APP_NAME} | grep -v "grep\|bin/bash" | awk '{print $2}')

if [[ ! -z $pid ]]; then
        echo "PID = ${pid}"
else
        echo "No process currently running"
fi;