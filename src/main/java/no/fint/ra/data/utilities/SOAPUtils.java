package no.fint.ra.data.utilities;

import javax.xml.bind.JAXBElement;
import java.util.Optional;

public enum SOAPUtils {
    ;

    public static <T> Optional<T> getSafeValue(JAXBElement<T> element) {
        if (!element.isNil()) {
            return Optional.of(element.getValue());
        }
        return Optional.empty();
    }

}
