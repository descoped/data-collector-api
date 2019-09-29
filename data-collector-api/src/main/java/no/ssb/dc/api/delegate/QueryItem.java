package no.ssb.dc.api.delegate;

// simple xpath and return string value
public interface QueryItem {
    Tuple<String, String> item();
}
