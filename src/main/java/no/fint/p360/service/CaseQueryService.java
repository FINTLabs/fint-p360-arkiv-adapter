package no.fint.p360.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.p360.data.p360.P360CaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.QueryUtils.getQueryParams;

@Service
@Slf4j
public class CaseQueryService {

    private final ImmutableMap<String, Function<String, Stream<CaseResult>>> queryMap;
    private final P360CaseService caseService;
    private final String[] validQueries;

    public CaseQueryService(P360CaseService caseService) {
        queryMap = new ImmutableMap.Builder<String, Function<String, Stream<CaseResult>>>()
                .put("soknadsnummer/", this::getCaseByExternalId)
                .put("mappeid/", this::getCaseByCaseNumber)
                .put("systemid/", this::getCaseBySystemId)
                .put("?", this::getGetCasesQueryByTitle)
                .build();
        this.caseService = caseService;
        validQueries = queryMap.keySet().toArray(new String[0]);
    }

    public boolean isValidQuery(String query) {
        return StringUtils.startsWithAny(StringUtils.lowerCase(query), validQueries);
    }

    public Stream<CaseResult> query(String query) {
        for (String prefix : validQueries) {
            if (StringUtils.startsWithIgnoreCase(query, prefix)) {
                return queryMap.get(prefix).apply(StringUtils.removeStartIgnoreCase(query, prefix));
            }
        }
        throw new IllegalArgumentException("Invalid query: " + query);
    }

    private Stream<CaseResult> getCaseByCaseNumber(String caseNumber) {
        return Stream.of(caseService.getCaseByCaseNumber(caseNumber));
    }

    private Stream<CaseResult> getCaseBySystemId(String systemId) {
        return Stream.of(caseService.getCaseBySystemId(systemId));
    }

    private Stream<CaseResult> getCaseByExternalId(String externalId) {
        return Stream.of(caseService.getCaseByExternalId(externalId));
    }

    private Stream<CaseResult> getGetCasesQueryByTitle(String query) {
        return caseService.getGetCasesQueryByTitle(getQueryParams(query)).stream();
    }
}
