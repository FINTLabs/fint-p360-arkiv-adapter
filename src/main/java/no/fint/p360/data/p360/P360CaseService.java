package no.fint.p360.data.p360;

import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.arkiv.p360.caze.CreateCaseParameter;
import no.fint.p360.data.exception.CreateCaseException;
import no.fint.p360.data.exception.GetTilskuddFartoyException;
import no.fint.p360.data.exception.GetTilskuddFartoyNotFoundException;

import java.util.List;
import java.util.Map;

public interface P360CaseService {
    boolean ping();

    String createCase(CreateCaseParameter createCaseParameter) throws CreateCaseException;

    CaseResult getSakByCaseNumber(String caseNumber) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException;

    CaseResult getSakBySystemId(String systemId) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException;

    CaseResult getSakByExternalId(String externalId) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException;

    List<CaseResult> getGetCasesQueryByTitle(Map<String, String> params) throws GetTilskuddFartoyException;
}
