package no.fint.p360.data.noark.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.document.*;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.arkiv.kodeverk.JournalStatus;
import no.fint.model.arkiv.kodeverk.JournalpostType;
import no.fint.model.arkiv.kodeverk.KorrespondansepartType;
import no.fint.model.arkiv.kodeverk.Merknadstype;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource;
import no.fint.model.resource.arkiv.kodeverk.KorrespondansepartTypeResource;
import no.fint.model.resource.arkiv.kodeverk.MerknadstypeResource;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.arkiv.noark.MerknadResource;
import no.fint.p360.data.noark.dokument.DokumentbeskrivelseFactory;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.repository.KodeverkRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static no.fint.p360.data.utilities.FintUtils.optionalValue;
import static no.fint.p360.data.utilities.P360Utils.applyParameterFromLink;

@Slf4j
@Service
public class JournalpostFactory {

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Autowired
    private DokumentbeskrivelseFactory dokumentbeskrivelseFactory;



    @PostConstruct
    public void init() {

    }


    public JournalpostResource toFintResource(DocumentResult documentResult) {
        JournalpostResource journalpost = new JournalpostResource();


        optionalValue(documentResult.getFiles())
                .map(ArrayOfDocumentFileResult::getDocumentFileResult)
                .map(List::size)
                .map(Integer::longValue)
                .ifPresent(journalpost::setAntallVedlegg);
        optionalValue(documentResult.getTitle()).ifPresent(journalpost::setTittel);
        optionalValue(documentResult.getOfficialTitle()).ifPresent(journalpost::setOffentligTittel);
        optionalValue(documentResult.getDocumentDate())
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(GregorianCalendar::getTime)
                .ifPresent(journalpost::setDokumentetsDato);
        optionalValue(documentResult.getJournalDate())
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(GregorianCalendar::getTime)
                .ifPresent(journalpost::setJournalDato);
        optionalValue(documentResult.getCreatedDate())
                .map(FintUtils::parseDate)
                .ifPresent(journalpost::setOpprettetDato);

        // FIXME: 2019-05-08 check for empty
        journalpost.setDokumentbeskrivelse(Collections.emptyList());
        // FIXME: 2019-05-08 check for empty
        journalpost.setForfatter(Collections.emptyList());
        // FIXME: 2019-05-08 check for empty keywords
        journalpost.setNokkelord(Collections.emptyList());
        // FIXME: 2019-05-08 check for empty
        journalpost.setReferanseArkivDel(Collections.emptyList());

        // FIXME: 2019-05-08 Figure out which is already rep and if some of them should be code lists (noark) + skjerming
        journalpost.setBeskrivelse(String.format("%s - %s - %s", documentResult.getType().getDescription(), documentResult.getStatusDescription(), documentResult.getAccessCodeDescription()));

        // TODO: 2019-05-08 Check noark if this is correct
        journalpost.setForfatter(Collections.singletonList(documentResult.getResponsiblePersonName()));

        journalpost.setKorrespondansepart(
                optionalValue(documentResult.getContacts())
                        .map(ArrayOfDocumentContactResult::getDocumentContactResult)
                        .map(Collection::stream)
                        .orElse(Stream.empty())
                        .map(it -> {
                            KorrespondansepartResource result = new KorrespondansepartResource();
                            // TODO result.addKorrespondansepart(Link.with(Korrespondansepart.class, "systemid", it.getContactRecno()));
                            optionalValue(it.getRole())
                                    .flatMap(role ->
                                            kodeverkRepository
                                                    .getKorrespondansepartType()
                                                    .stream()
                                                    .filter(v -> StringUtils.equalsIgnoreCase(role, v.getKode()))
                                                    .findAny())
                                    .map(KorrespondansepartTypeResource::getSystemId)
                                    .map(Identifikator::getIdentifikatorverdi)
                                    .map(Link.apply(KorrespondansepartType.class, "systemid"))
                                    .ifPresent(result::addKorrespondanseparttype);
                            return result;
                        })
                        .collect(Collectors.toList()));


        String[] split = optionalValue(documentResult.getDocumentNumber()).orElse("").split("-");
        if (split.length == 2 && StringUtils.isNumeric(split[1])) {
            journalpost.setJournalSekvensnummer(Long.parseLong(split[1]));
        }

        optionalValue(documentResult.getResponsiblePerson())
                .map(ResponsiblePerson::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Personalressurs.class, "ansattnummer"))
                .ifPresent(journalpost::addSaksbehandler);
        optionalValue(documentResult.getResponsibleEnterprise())
                .map(ResponsibleEnterprise::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Organisasjonselement.class, "organisasjonsid"))
                .ifPresent(journalpost::addAdministrativEnhet);
        optionalValue(documentResult.getCategory())
                .map(DocumentCategoryResult::getRecno)
                .map(String::valueOf)
                .map(Link.apply(JournalpostType.class, "systemid"))
                .ifPresent(journalpost::addJournalposttype);
        optionalValue(documentResult.getStatusCode())
                .flatMap(code -> kodeverkRepository
                        .getJournalStatus()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(code, it.getKode()))
                        .findAny())
                .map(JournalStatusResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(JournalStatus.class, "systemid"))
                .ifPresent(journalpost::addJournalstatus);

        journalpost.setMerknad(
                optionalValue(documentResult.getRemarks())
                        .map(ArrayOfRemarkInfo::getRemarkInfo)
                        .map(List::stream)
                        .orElse(Stream.empty())
                        .map(this::createMerknad)
                        .collect(Collectors.toList()));

        List<DocumentFileResult> documentFileResult = documentResult.getFiles().getDocumentFileResult();

        journalpost.setDokumentbeskrivelse(documentFileResult
                .stream()
                .map(dokumentbeskrivelseFactory::toFintResource)
                .collect(Collectors.toList()));

        return journalpost;
    }


    private MerknadResource createMerknad(RemarkInfo remarkInfo) {
        MerknadResource merknad = new MerknadResource();

        optionalValue(remarkInfo.getTypeCode())
                .flatMap(type ->
                        kodeverkRepository
                                .getMerknadstype()
                                .stream()
                                .filter(v -> StringUtils.equalsIgnoreCase(type, v.getKode()))
                                .findAny())
                .map(MerknadstypeResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(Merknadstype.class, "systemid"))
                .ifPresent(merknad::addMerknadstype);

        merknad.setMerknadstekst(
                Stream.of(optionalValue(remarkInfo.getTitle()), optionalValue(remarkInfo.getContent()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.joining(" - ")));

        optionalValue(remarkInfo.getModifiedDate())
                .map(FintUtils::parseDate)
                .ifPresent(merknad::setMerknadsdato);

        // TODO merknad.addMerknadRegistrertAv();

        return merknad;
    }

    public CreateDocumentParameter toP360(JournalpostResource journalpostResource, String caseNumber) {

        CreateDocumentParameter createDocumentParameter = new CreateDocumentParameter();

//        createDocumentParameter.setADContextUser(objectFactory.createDocumentParameterBaseADContextUser(adapterProps.getP360User()));

        createDocumentParameter.setTitle(journalpostResource.getOffentligTittel());
        createDocumentParameter.setUnofficialTitle(journalpostResource.getTittel());
        createDocumentParameter.setCaseNumber(caseNumber);

        if (journalpostResource.getSkjerming() != null) {
            applyParameterFromLink(
                    journalpostResource.getSkjerming().getTilgangsrestriksjon(),
                    createDocumentParameter::setAccessCode);

            applyParameterFromLink(
                    journalpostResource.getSkjerming().getSkjermingshjemmel(),
                    createDocumentParameter::setParagraph);

            // TODO createDocumentParameter.setAccessGroup();
        }

        // TODO Set from incoming fields
        //createDocumentParameter.setDocumentDate();

        applyParameterFromLink(
                journalpostResource.getJournalposttype(),
                createDocumentParameter::setCategory);

        applyParameterFromLink(
                journalpostResource.getJournalstatus(),
                createDocumentParameter::setStatus);

        ofNullable(journalpostResource.getKorrespondansepart()).ifPresent(korrespondanseResources -> {
            ArrayOfDocumentContactParameter arrayOfDocumentContactParameter = new ArrayOfDocumentContactParameter();
            korrespondanseResources
                    .stream()
                    .map(this::createDocumentContact)
                    .forEach(arrayOfDocumentContactParameter.getDocumentContactParameter()::add);
            createDocumentParameter.setContacts(arrayOfDocumentContactParameter);
        });

        ofNullable(journalpostResource.getDokumentbeskrivelse()).ifPresent(dokumentbeskrivelseResources -> {
            ArrayOfCreateFileParameter arrayOfCreateFileParameter = new ArrayOfCreateFileParameter();
            dokumentbeskrivelseResources
                    .stream()
                    .peek(r -> log.info("Handling Dokumentbeskrivelse: {}", r))
                    .flatMap(this::createFiles)
                    .forEach(arrayOfCreateFileParameter.getCreateFileParameter()::add);
            createDocumentParameter.setFiles(arrayOfCreateFileParameter);
        });

        ofNullable(journalpostResource.getMerknad()).ifPresent(merknadResources -> {
            ArrayOfRemark arrayOfRemark = new ArrayOfRemark();
            merknadResources
                    .stream()
                    .map(this::createDocumentRemarkParameter)
                    .forEach(arrayOfRemark.getRemark()::add);
            createDocumentParameter.setRemarks(arrayOfRemark);
        });

        return createDocumentParameter;
    }

    private Remark createDocumentRemarkParameter(MerknadResource merknadResource) {
        Remark remark = new Remark();
        remark.setContent(merknadResource.getMerknadstekst());

        merknadResource
                .getMerknadstype()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))

                .findFirst()
                .ifPresent(remark::setRemarkType);

        return remark;
    }


    private DocumentContactParameter createDocumentContact(KorrespondansepartResource korrespondansepart) {
        DocumentContactParameter documentContactParameter = new DocumentContactParameter();

        /* TODO applyParameterFromLink(
                korrespondansepart.getKorrespondansepart(),
                documentContactParameter::setReferenceNumber
        );
         */

        applyParameterFromLink(
                korrespondansepart.getKorrespondanseparttype(),
                documentContactParameter::setRole);

        return documentContactParameter;
    }

    private Stream<CreateFileParameter> createFiles(DokumentbeskrivelseResource dokumentbeskrivelse) {
        return dokumentbeskrivelse
                .getDokumentobjekt()
                .stream()
                .map(dokumentobjekt -> dokumentbeskrivelseFactory.toP360(dokumentbeskrivelse, dokumentobjekt));
    }

}
