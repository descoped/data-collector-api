#!/usr/bin/env sh

echo "Print specification"

# Initialize Cron scheduler expression
echo "Cron expression: $CRON_EXPRESSION"
echo "$CRON_EXPRESSION /cron-script.sh" > /crontab.txt
/usr/bin/crontab /crontab.txt

# Run script first time
echo "Wait for 15 seconds @ $(date)"
sleep 15
/cron-script.sh

# start cron
/usr/sbin/crond -f -l 8
