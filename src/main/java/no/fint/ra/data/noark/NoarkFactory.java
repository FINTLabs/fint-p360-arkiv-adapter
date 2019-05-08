package no.fint.ra.data.noark;

import no.fint.arkiv.p360.caze.CaseDocumentResult;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.administrasjon.arkiv.SaksmappeResource;
import no.fint.ra.data.p360.service.P360DocumentService;
import no.fint.ra.data.utilities.FintUtils;
import no.fint.ra.data.utilities.NOARKUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fint.ra.data.utilities.FintUtils.getSafeValue;

@Service
public class NoarkFactory {

    @Autowired
    private P360DocumentService documentService;

    public  void getSaksmappe(CaseResult caseResult, SaksmappeResource saksmappeResource) {
        String caseNumber = caseResult.getCaseNumber().getValue();
        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        getSafeValue(caseResult.getNotes())
                .filter(StringUtils::isNotBlank)
                .ifPresent(saksmappeResource::setBeskrivelse);
        saksmappeResource.setMappeId(FintUtils.createIdentifikator(caseNumber));
        saksmappeResource.setSystemId(FintUtils.createIdentifikator(caseResult.getRecno().toString()));
        saksmappeResource.setSakssekvensnummer(sequenceNumber);
        saksmappeResource.setSaksaar(caseYear);
        saksmappeResource.setSaksdato(caseResult.getDate().toGregorianCalendar().getTime());
        saksmappeResource.setOpprettetDato(caseResult.getCreatedDate().getValue().toGregorianCalendar().getTime());
        saksmappeResource.setTittel(caseResult.getUnofficialTitle().getValue());
        saksmappeResource.setOffentligTittel(caseResult.getTitle().getValue());
        saksmappeResource.setNoekkelord(caseResult
                .getArchiveCodes()
                .getValue()
                .getArchiveCodeResult()
                .stream()
                .flatMap(it -> Stream.of(it.getArchiveType().getValue(), it.getArchiveCode().getValue()))
                .collect(Collectors.toList()));

        List<JournalpostResource> journalpostResourceList = new ArrayList<>();
        List<CaseDocumentResult> caseDocumentResult = caseResult.getDocuments().getValue().getCaseDocumentResult();
        caseDocumentResult.forEach(doc ->
                journalpostResourceList.add(documentService.getJournalPost(doc.getRecno().toString()))
        );
        saksmappeResource.setJournalpost(journalpostResourceList);

    }
}
