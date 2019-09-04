package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.noark.journalpost.JournalpostService;
import no.fint.p360.data.p360.P360CaseService;
import no.fint.p360.data.utilities.Constants;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TilskuddfartoyService {

    @Autowired
    private P360CaseService caseService;

    @Autowired
    private TilskuddFartoyFactory tilskuddFartoyFactory;

    @Autowired
    private JournalpostService journalpostService;


    public TilskuddFartoyResource createTilskuddFartoyCase(TilskuddFartoyResource tilskuddFartoy) throws NotTilskuddfartoyException, CreateCaseException, GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, CreateDocumentException, GetDocumentException, IllegalCaseNumberFormat {
        String caseNumber = caseService.createCase(tilskuddFartoyFactory.toP360(tilskuddFartoy));
        for (JournalpostResource journalpostResource : tilskuddFartoy.getJournalpost()) {
            journalpostService.createJournalPost(caseNumber, journalpostResource);
        }
        return getTilskuddFartoyCaseByCaseNumber(caseNumber);
    }

    public TilskuddFartoyResource updateTilskuddFartoyCase(String caseNumber, TilskuddFartoyResource tilskuddFartoyResource) throws NotTilskuddfartoyException, GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, CreateDocumentException, GetDocumentException, IllegalCaseNumberFormat {
        CaseResult sakByCaseNumber = caseService.getSakByCaseNumber(caseNumber);
        if (!isTilskuddFartoy(sakByCaseNumber)) {
            throw new NotTilskuddfartoyException("Ikke en Tilskuddfartøy sak: " + caseNumber);
        }
        for (JournalpostResource journalpostResource : tilskuddFartoyResource.getJournalpost()) {
            journalpostService.createJournalPost(caseNumber, journalpostResource);
        }
        return getTilskuddFartoyCaseByCaseNumber(caseNumber);
    }

    public TilskuddFartoyResource getTilskuddFartoyCaseByCaseNumber(String caseNumber) throws NotTilskuddfartoyException, GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, GetDocumentException, IllegalCaseNumberFormat {
        CaseResult sakByCaseNumber = caseService.getSakByCaseNumber(caseNumber);

        if (isTilskuddFartoy(sakByCaseNumber)) {
            return tilskuddFartoyFactory.toFintResource(sakByCaseNumber);
        }

        throw new NotTilskuddfartoyException(String.format("MappeId %s er ikke en Tilskuddfartøy sak", caseNumber));
    }

    public TilskuddFartoyResource getTilskuddFartoyCaseByExternalId(String externalId) throws NotTilskuddfartoyException, GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, GetDocumentException, IllegalCaseNumberFormat {
        CaseResult caseResult = caseService.getSakByExternalId(externalId);

        if (isTilskuddFartoy(caseResult)) {
            return tilskuddFartoyFactory.toFintResource(caseResult);
        }

        throw new NotTilskuddfartoyException("Søknadsnummer " + externalId + " er ikke en Tilskuddfartøy sak");
    }

    public TilskuddFartoyResource getTilskuddFartoyCaseBySystemId(String systemId) throws NotTilskuddfartoyException, GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, GetDocumentException, IllegalCaseNumberFormat {
        CaseResult sakBySystemId = caseService.getSakBySystemId(systemId);

        if (isTilskuddFartoy(sakBySystemId)) {
            return tilskuddFartoyFactory.toFintResource(sakBySystemId);
        }
        throw new NotTilskuddfartoyException(String.format("SystemId %s er ikke en Tilskuddfartøy sak", systemId));
    }

    public List<TilskuddFartoyResource> searchTilskuddFartoyCaseByTitle(Map<String, String> query) throws GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, GetDocumentException, IllegalCaseNumberFormat {
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
