package no.fint.p360.data.p360.rpc;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.arkiv.p360.caze.CreateCaseParameter;
import no.fint.p360.data.exception.CreateCaseException;
import no.fint.p360.data.exception.GetTilskuddFartoyException;
import no.fint.p360.data.exception.GetTilskuddFartoyNotFoundException;
import no.fint.p360.data.p360.P360CaseService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.p360.api", havingValue = "RPC")
public class P360CaseServiceRPC extends P360AbstractRPCService implements P360CaseService {
    @Override
    public boolean ping() {
        return false;
    }

    @Override
    public String createCase(CreateCaseParameter createCaseParameter) throws CreateCaseException {
        return null;
    }

    @Override
    public CaseResult getSakByCaseNumber(String caseNumber) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException {
        return null;
    }

    @Override
    public CaseResult getSakBySystemId(String systemId) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException {
        return null;
    }

    @Override
    public CaseResult getSakByExternalId(String externalId) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException {
        return null;
    }

    @Override
    public List<CaseResult> getGetCasesQueryByTitle(Map<String, String> params) throws GetTilskuddFartoyException {
        return null;
    }
}
