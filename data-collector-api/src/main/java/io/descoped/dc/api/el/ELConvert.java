package io.descoped.dc.api.el;

import io.descoped.dc.api.util.CommonUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class ELConvert {

    public Long utcDateToEpoc(String value) {
        try {
            TemporalAccessor ta = DateTimeFormatter.ISO_DATE_TIME.parse("2020-10-05T00:00:00.000Z");
            long epochSecond = LocalDateTime.from(ta).toEpochSecond(ZoneOffset.UTC);
            return Long.valueOf(epochSecond);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException("Error convert utc date '" + value + "' to Epoc (long)!\n" + CommonUtils.captureStackTrace(e));
        }
    }

}
