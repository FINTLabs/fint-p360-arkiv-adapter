package no.fint.p360.data.utilities;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public enum QueryUtils {
    ;

    public static MultiValueMap<String, String> getQueryParams(String query) {
        return UriComponentsBuilder.fromUriString(query).build().getQueryParams();
    }
}
