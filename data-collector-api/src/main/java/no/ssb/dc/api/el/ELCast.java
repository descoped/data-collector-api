package no.ssb.dc.api.el;

import no.ssb.dc.api.util.CommonUtils;

public class ELCast {

    public Float toFloat(String value) {
        try {
            return Float.valueOf(value);
        } catch (RuntimeException | Error e) {
            throw new EvaluationException("Error casting '" + value + "' to Float!\n" + CommonUtils.captureStackTrace(e));
        } catch (Exception e) {
            throw new EvaluationException("Error casting '" + value + "' to Float!\n" + CommonUtils.captureStackTrace(e));
        }
    }

    public Double toDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (RuntimeException | Error e) {
            throw new EvaluationException("Error casting '" + value + "' to Double!\n" + CommonUtils.captureStackTrace(e));
        } catch (Exception e) {
            throw new EvaluationException("Error casting '" + value + "' to Double!\n" + CommonUtils.captureStackTrace(e));
        }
    }

    public Integer toInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (RuntimeException | Error e) {
            throw new EvaluationException("Error casting '" + value + "' to Integer!\n" + CommonUtils.captureStackTrace(e));
        } catch (Exception e) {
            throw new EvaluationException("Error casting '" + value + "' to Integer!\n" + CommonUtils.captureStackTrace(e));
        }
    }

    public Long toLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (RuntimeException | Error e) {
            throw new EvaluationException("Error casting '" + value + "' to Long!\n" + CommonUtils.captureStackTrace(e));
        } catch (Exception e) {
            throw new EvaluationException("Error casting '" + value + "' to Long!\n" + CommonUtils.captureStackTrace(e));
        }
    }
}
