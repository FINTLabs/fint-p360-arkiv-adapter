package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.data.exception.NotTilskuddfartoyException;
import no.fint.p360.data.fint.TilskuddFartoyFactory;
import no.fint.p360.data.p360.P360CaseService;
import no.fint.p360.data.p360.P360DocumentService;
import no.fint.p360.data.utilities.Constants;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TilskuddfartoyService {

    @Autowired
    private P360CaseService caseService;

    @Autowired
    private TilskuddFartoyFactory tilskuddFartoyFactory;

    @Autowired
    private P360DocumentService documentService;


    public TilskuddFartoyResource createTilskuddFartoyCase(TilskuddFartoyResource tilskuddFartoy) {
        String caseNumber = caseService.createCase(tilskuddFartoyFactory.toP360(tilskuddFartoy));

        TilskuddFartoyResource tilskuddFartoyNew = getTilskuddFartoyCaseByCaseNumber(caseNumber);
        tilskuddFartoyNew.setJournalpost(tilskuddFartoy.getJournalpost());
        documentService.createJournalPost(tilskuddFartoyNew);
        return tilskuddFartoyNew;
    }

    public TilskuddFartoyResource getTilskuddFartoyCaseByCaseNumber(String caseNumber) {
        CaseResult sakByCaseNumber = caseService.getSakByCaseNumber(caseNumber);

        if (isTilskuddFartoy(sakByCaseNumber)) {
            return tilskuddFartoyFactory.toFintResource(sakByCaseNumber);
        }

        throw new NotTilskuddfartoyException(String.format("MappeId %s er ikke en Tilskuddfartøy sak", caseNumber));
    }

    public TilskuddFartoyResource getTilskuddFartoyCaseBySystemId(String systemId) {
        CaseResult sakBySystemId = caseService.getSakBySystemId(systemId);

        if (isTilskuddFartoy(sakBySystemId)) {
            return tilskuddFartoyFactory.toFintResource(sakBySystemId);
        }
        throw new NotTilskuddfartoyException(String.format("SystemId %s er ikke en Tilskuddfartøy sak", systemId));
    }

    public Stream<TilskuddFartoyResource> searchTilskuddFartoyCaseByTitle(MultiValueMap<String, String> query) {

        return tilskuddFartoyFactory.toFintResourceList(
                caseService.getGetCasesQueryByTitle(query)
                        .stream()
                        .filter(this::isTilskuddFartoy)
                        .collect(Collectors.toList())
        );
    }

    // TODO: 2019-05-11 Should we check for both archive classification and external id (is it a digisak)
    private boolean isTilskuddFartoy(CaseResult caseResult) {

        if (FintUtils.optionalValue(caseResult.getExternalId()).isPresent() && FintUtils.optionalValue(caseResult.getArchiveCodes()).isPresent()) {
            return caseResult.getExternalId().getValue().getType().getValue().equals(Constants.EXTERNAL_ID_TYPE)
                    && caseResult.getArchiveCodes().getValue().getArchiveCodeResult().stream().anyMatch(code -> code.getArchiveType().getValue().equals("Fartøy"));
        }

        return false;

    }
}
