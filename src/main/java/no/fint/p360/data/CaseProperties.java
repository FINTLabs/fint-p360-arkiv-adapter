package no.fint.p360.data;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CaseProperties {
    private String administrativEnhet;
    private String arkivdel;
    private String[] noekkelord;
    private String klassifikasjon;
    private String saksstatus;
    private String korrespondansepartType;
    private String journalpostType;
    private String journalstatus;
    private String dokumentstatus;
    private String dokumentType;
    private String tilknyttetRegistreringSom;
}