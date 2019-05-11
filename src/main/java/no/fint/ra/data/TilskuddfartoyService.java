package no.fint.ra.data;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.fint.TilskuddFartoyFactory;
import no.fint.ra.data.p360.service.P360CaseService;
import no.fint.ra.data.p360.service.P360DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

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
        return tilskuddFartoyFactory.toFintResource(caseService.getSakByCaseNumber(caseNumber));

    }

    public TilskuddFartoyResource getTilskuddFartoyCaseBySystemId(String systemId) {
        return tilskuddFartoyFactory.toFintResource(caseService.getSakBySystemId(systemId));

    }

    public Stream<TilskuddFartoyResource> searchTilskuddFartoyCaseByTitle(MultiValueMap<String, String> query) {
        return tilskuddFartoyFactory.toFintResourceList(caseService.getGetCasesQueryByTitle(query));
    }
}
