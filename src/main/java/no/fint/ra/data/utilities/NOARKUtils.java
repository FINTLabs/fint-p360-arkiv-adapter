package no.fint.ra.data.utilities;

import no.fint.ra.data.exception.IllegalCaseNumberFormat;
import no.fint.ra.data.exception.IllegalDocumentNumberFormat;

public enum NOARKUtils {
    ;

    public static String getCaseYear(String caseNumber) {
        String[] split = caseNumber.split("/");
        if (split.length != 2) {
            throw new IllegalCaseNumberFormat(String.format("Case number %s is illegal", caseNumber));
        }
        return split[0];
    }

    public static String getCaseSequenceNumber(String caseNumber) {
        String[] split = caseNumber.split("/");
        if (split.length != 2) {
            throw new IllegalCaseNumberFormat(String.format("Case number %s is illegal", caseNumber));
        }
        return split[1];
    }

    public static String getDocumentSequenceNumber(String documentNumber) {
        String[] split = documentNumber.split("-");
        if (split.length != 2) {
            throw new IllegalDocumentNumberFormat(String.format("Document number %s is illegal", documentNumber));
        }
        return split[1];
    }
}
