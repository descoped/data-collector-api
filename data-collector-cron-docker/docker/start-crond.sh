#!/usr/bin/env sh

# Run script first time
/cron-script.sh

# start cron
/usr/sbin/crond -f -l 8
