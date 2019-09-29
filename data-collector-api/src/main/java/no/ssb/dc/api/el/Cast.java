package no.ssb.dc.api.el;

class Cast {

    Float toFloat(String value) {
        return Float.valueOf(value);
    }

    Double toDouble(String value) {
        return Double.valueOf(value);
    }

    Integer toInteger(String value) {
        return Integer.valueOf(value);
    }

    Long toLong(String value) {
        return Long.valueOf(value);
    }
}
