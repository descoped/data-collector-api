#
# Build DC Cron image
#
FROM alpine:latest

RUN apk add --no-cache bash curl openssl

ENV DC_HOST=0.0.0.0
ENV DC_HOST=9990

#
# Cron daemon
#
ADD ./docker/crontab.txt /crontab.txt
ADD ./docker/cron-script.sh /cron-script.sh
RUN chmod +x /cron-script.sh
RUN /usr/bin/crontab /crontab.txt

ADD docker/start-crond.sh /run.sh
RUN chmod +x /run.sh

VOLUME ["/spec"]

ENTRYPOINT ["/run.sh"]
