package no.ssb.dc.api.health;

public class HealthResourceUtils {

    public static float divide(long numerator, long denominator) {
        return denominator == 0 ? 0 : (float) (((double) numerator) / ((double) denominator));
    }

    public static String durationAsString(long since) {
        long elapsedTime = System.currentTimeMillis() - since;

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = elapsedTime / daysInMilli;
        elapsedTime = elapsedTime % daysInMilli;

        long elapsedHours = elapsedTime / hoursInMilli;
        elapsedTime = elapsedTime % hoursInMilli;

        long elapsedMinutes = elapsedTime / minutesInMilli;
        elapsedTime = elapsedTime % minutesInMilli;

        long elapsedSeconds = elapsedTime / secondsInMilli;
        elapsedTime = elapsedTime % secondsInMilli;

        long elapsedMillisSeconds = elapsedTime;

        return String.format(
                "%d days, %d hours, %d minutes, %d seconds, %d millis",
                elapsedDays,
                elapsedHours,
                elapsedMinutes,
                elapsedSeconds,
                elapsedMillisSeconds
        );
    }
}
