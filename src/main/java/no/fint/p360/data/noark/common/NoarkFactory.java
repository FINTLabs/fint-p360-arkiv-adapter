package no.fint.p360.data.noark.common;

import no.fint.arkiv.p360.caze.*;
import no.fint.model.administrasjon.arkiv.Part;
import no.fint.model.administrasjon.arkiv.PartRolle;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.PartsinformasjonResource;
import no.fint.model.resource.administrasjon.arkiv.SaksmappeResource;
import no.fint.p360.data.noark.journalpost.JournalpostService;
import no.fint.p360.data.noark.part.PartFactory;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.NOARKUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Service
public class NoarkFactory {

    @Autowired
    private JournalpostService journalpostService;

    @Autowired
    private PartFactory partFactory;

    public void getSaksmappe(CaseResult caseResult, SaksmappeResource saksmappeResource) {
        String caseNumber = caseResult.getCaseNumber().getValue();
        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        optionalValue(caseResult.getNotes())
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

        saksmappeResource.setPart(
                optionalValue(caseResult.getContacts())
                        .map(ArrayOfCaseContactResult::getCaseContactResult)
                        .map(List::stream)
                        .orElseGet(Stream::empty)
                        .map(this::createPartsinformasjon)
                        .collect(Collectors.toList()));

        saksmappeResource.setJournalpost(
                optionalValue(caseResult.getDocuments())
                        .map(ArrayOfCaseDocumentResult::getCaseDocumentResult)
                        .map(List::parallelStream)
                        .orElse(Stream.empty())
                        .map(CaseDocumentResult::getRecno)
                        .map(String::valueOf)
                        .map(journalpostService::getJournalPost)
                        .collect(Collectors.toList()));
    }

    private PartsinformasjonResource createPartsinformasjon(CaseContactResult caseContactResult) {
        PartsinformasjonResource partsinformasjon = new PartsinformasjonResource();

        optionalValue(caseContactResult.getRecno())
                .map(String::valueOf)
                .map(Link.apply(Part.class, "partid"))
                .ifPresent(partsinformasjon::addPart);

        optionalValue(caseContactResult.getRole())
                .map(Link.apply(PartRolle.class, "systemid"))
                .ifPresent(partsinformasjon::addPartRolle);

        return partsinformasjon;
    }


}
