package no.fint.ra.data.utilities;

import no.fint.model.felles.kompleksedatatyper.Identifikator;

public enum FintUtils {
    ;

    public static Identifikator createIdentifikator(String value) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(value);
        return identifikator;
    }
}
