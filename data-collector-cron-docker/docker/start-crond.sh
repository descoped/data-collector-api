#!/usr/bin/env sh

# Initialize Cron scheduler expression
echo "Cron expression: $CRON_EXPRESSION"
echo "$CRON_EXPRESSION /cron-script.sh" > /crontab.txt
/usr/bin/crontab /crontab.txt

# Run script first time
/cron-script.sh

# start cron
/usr/sbin/crond -f -l 8
