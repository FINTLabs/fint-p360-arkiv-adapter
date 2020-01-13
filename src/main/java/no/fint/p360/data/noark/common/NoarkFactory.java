package no.fint.p360.data.noark.common;

import no.fint.arkiv.p360.caze.*;
import no.fint.arkiv.p360.document.DocumentResult;
import no.fint.model.administrasjon.arkiv.Part;
import no.fint.model.administrasjon.arkiv.PartRolle;
import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.noark.part.PartFactory;
import no.fint.p360.data.p360.P360DocumentService;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.NOARKUtils;
import no.fint.p360.repository.KodeverkRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Service
public class NoarkFactory {

    @Autowired
    private P360DocumentService documentService;

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    private PartFactory partFactory;

    public void getSaksmappe(CaseResult caseResult, SaksmappeResource saksmappeResource) throws GetDocumentException, IllegalCaseNumberFormat {
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
                        .map(partFactory::getPartsinformasjon)
                        .collect(Collectors.toList()));

        List<String> journalpostIds = optionalValue(caseResult.getDocuments())
                .map(ArrayOfCaseDocumentResult::getCaseDocumentResult)
                .map(List::stream)
                .orElse(Stream.empty())
                .map(CaseDocumentResult::getRecno)
                .map(String::valueOf)
                .collect(Collectors.toList());
        List<JournalpostResource> journalpostList = new ArrayList<>(journalpostIds.size());
        for (String journalpostRecord : journalpostIds) {
            DocumentResult documentResult = documentService.getDocumentBySystemId(journalpostRecord);
            JournalpostResource journalpostResource = journalpostFactory.toFintResource(documentResult);
            journalpostList.add(journalpostResource);
        }
        saksmappeResource.setJournalpost(journalpostList);

        optionalValue(caseResult.getStatus())
                .flatMap(kode -> kodeverkRepository
                        .getSaksstatus()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getNavn()))
                        .findAny())
                .map(SaksstatusResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(Saksstatus.class, "systemid"))
                .ifPresent(saksmappeResource::addSaksstatus);

        optionalValue(caseResult.getResponsibleEnterprise())
                .map(ResponsibleEnterprise::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Organisasjonselement.class, "organisasjonsid"))
                .ifPresent(saksmappeResource::addAdministrativEnhet);

        optionalValue(caseResult.getResponsiblePerson())
                .map(ResponsiblePerson::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Personalressurs.class, "ansattnummer"))
                .ifPresent(saksmappeResource::addSaksansvarlig);

        caseResult
                .getArchiveCodes()
                .getValue()
                .getArchiveCodeResult()
                .stream()
                .map(ArchiveCodeResult::getArchiveCode)
                .map(JAXBElement::getValue)
                .flatMap(code -> kodeverkRepository
                        .getKlasse()
                        .stream()
                        .filter(it -> StringUtils.equals(code, it.getTittel())))
                .map(KlasseResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(KlasseResource.class, "systemid"))
                .forEach(saksmappeResource::addKlasse);
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


    public boolean health() {
        return documentService.ping();
    }
}
