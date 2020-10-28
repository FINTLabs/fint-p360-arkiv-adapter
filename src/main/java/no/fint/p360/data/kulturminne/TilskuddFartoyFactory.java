package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.arkiv.p360.caze.CreateCaseParameter;
import no.fint.arkiv.p360.caze.ObjectFactory;
import no.fint.arkiv.p360.document.CreateDocumentParameter;
import no.fint.model.arkiv.kulturminnevern.TilskuddFartoy;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartFactory;
import no.fint.p360.data.utilities.NOARKUtils;
import no.fint.p360.data.utilities.P360Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TilskuddFartoyFactory {

    @Autowired
    private NoarkFactory noarkFactory;

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    private TilskuddFartoyDefaults tilskuddFartoyDefaults;

    private ObjectFactory objectFactory;


    @PostConstruct
    private void init() {
        objectFactory = new ObjectFactory();
    }

    public TilskuddFartoyResource toFintResource(CaseResult caseResult) throws GetDocumentException, IllegalCaseNumberFormat {

        TilskuddFartoyResource tilskuddFartoy = new TilskuddFartoyResource();
        tilskuddFartoy.setSoknadsnummer(new Identifikator());
        tilskuddFartoy.setMappeId(new Identifikator());
        tilskuddFartoy.setSystemId(new Identifikator());
        String caseNumber = caseResult.getCaseNumber();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        noarkFactory.getSaksmappe(caseResult, tilskuddFartoy);

        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "soknadsnummer", caseResult.getExternalId().getId()));

        return tilskuddFartoy;
    }


    public List<TilskuddFartoyResource> toFintResourceList(List<CaseResult> caseResults) throws GetDocumentException, IllegalCaseNumberFormat {
        List<TilskuddFartoyResource> result = new ArrayList<>(caseResults.size());
        for (CaseResult caseResult : caseResults) {
            result.add(toFintResource(caseResult));
        }
        return result;
    }

    public CreateCaseParameter convertToCreateCase(TilskuddFartoyResource tilskuddFartoy) {
        CreateCaseParameter createCaseParameter = objectFactory.createCreateCaseParameter();

        tilskuddFartoyDefaults.applyDefaultsToCreateCase(tilskuddFartoy, createCaseParameter);

        createCaseParameter.setExternalId(P360Utils.getExternalIdParameter(tilskuddFartoy.getSoknadsnummer()));

        noarkFactory.applyCaseParameters(tilskuddFartoy, createCaseParameter);

        return createCaseParameter;
    }


    public CreateDocumentParameter convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        CreateDocumentParameter createDocumentParameter = journalpostFactory.toP360(journalpostResource, caseNumber);
        return createDocumentParameter;
    }
}
