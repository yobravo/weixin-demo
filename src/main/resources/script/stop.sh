#!/bin/bash
app_path=$(dirname $0)

app_full_path=`cd $(dirname $0); pwd`;
APP_NAME=$(basename ${app_full_path});

pid=$(ps aux | grep ${APP_NAME} | grep -v "grep\|bin/bash" | awk '{print $2}')
if [[ ! -z $pid ]]; then
        kill -9 $pid >/dev/null 2>&1 ; echo "stopped..."
else
        echo "No process currently running..."
fi