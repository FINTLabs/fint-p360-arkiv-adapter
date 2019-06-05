package no.fint.p360.data.noark.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.document.*;
import no.fint.model.administrasjon.arkiv.*;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.felles.Person;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.p360.AdapterProps;
import no.fint.p360.KulturminneProps;
import no.fint.p360.data.FileRepository;
import no.fint.p360.data.KodeverkRepository;
import no.fint.p360.data.exception.FileNotFound;
import no.fint.p360.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Slf4j
@Service
public class JournalpostFactory {

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AdapterProps adapterProps;

    @Autowired
    private KulturminneProps kulturminneProps;

    private ObjectFactory objectFactory;


    @PostConstruct
    public void init() {
        objectFactory = new ObjectFactory();
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
        journalpost.setBeskrivelse(String.format("%s - %s - %s", documentResult.getType().getValue().getDescription().getValue(), documentResult.getStatusDescription().getValue(), documentResult.getAccessCodeDescription().getValue()));

        // TODO: 2019-05-08 Check noark if this is correct
        journalpost.setForfatter(Collections.singletonList(documentResult.getResponsiblePersonName().getValue()));

        journalpost.setKorrespondansepart(
                optionalValue(documentResult.getContacts())
                        .map(ArrayOfDocumentContactResult::getDocumentContactResult)
                        .map(Collection::stream)
                        .orElse(Stream.empty())
                        .map(it -> {
                            KorrespondanseResource result = new KorrespondanseResource();
                            result.addKorrespondansepart(Link.with(Korrespondansepart.class, "systemid", it.getContactRecno().getValue()));
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
                .map(ResponsiblePerson::getExternalId)
                .flatMap(FintUtils::optionalValue)
                .map(Link.apply(Person.class, "fodselsnummer"))
                .ifPresent(journalpost::addSaksbehandler);
        optionalValue(documentResult.getResponsibleEnterprise())
                .map(ResponsibleEnterprise::getExternalId)
                .flatMap(FintUtils::optionalValue)
                .map(Link.apply(Organisasjonselement.class, "organisasjonsnummer"))
                .ifPresent(journalpost::addAdministrativEnhet);
        optionalValue(documentResult.getCategory())
                .map(DocumentCategoryResult::getRecno)
                .map(String::valueOf)
                .map(Link.apply(JournalpostType.class, "systemid"))
                .ifPresent(journalpost::addJournalPostType);
        optionalValue(documentResult.getStatusCode())
                .flatMap(code -> kodeverkRepository
                        .getJournalStatus()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(code, it.getKode()))
                        .findAny())
                .map(JournalStatusResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(JournalStatus.class, "systemid"))
                .ifPresent(journalpost::addJournalStatus);

        Optional.ofNullable(
                optionalValue(documentResult.getRemarks())
                        .map(ArrayOfRemarkInfo::getRemarkInfo)
                        .map(List::stream)
                        .orElse(Stream.empty())
                        .map(r -> String.format("%s: %s\n%s\n%s",
                                r.getTypeCode().getValue(),
                                r.getTypeDescription().getValue(),
                                r.getTitle().getValue(),
                                r.getContent().getValue()))
                        .peek(log::info)
                        .collect(Collectors.joining("\n\n")))
                .filter(StringUtils::isNotBlank)
                .ifPresent(journalpost::setBeskrivelse);

        List<DocumentFileResult> documentFileResult = documentResult.getFiles().getValue().getDocumentFileResult();
        List<DokumentbeskrivelseResource> dokumentbeskrivelseResourcesList = new ArrayList<>();
        documentFileResult.forEach(file -> {
            DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
            optionalValue(file.getTitle()).ifPresent(dokumentbeskrivelseResource::setTittel);

            DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
            optionalValue(file.getFormat()).ifPresent(dokumentobjektResource::setFormat);
            dokumentobjektResource.addReferanseDokumentfil(Link.with(Dokumentfil.class, "systemid", file.getRecno().toString()));

            optionalValue(file.getStatusCode())
                    .flatMap(kode -> kodeverkRepository
                            .getDokumentStatus()
                            .stream()
                            .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getKode()))
                            .findAny())
                    .map(DokumentStatusResource::getSystemId)
                    .map(Identifikator::getIdentifikatorverdi)
                    .map(Link.apply(DokumentStatus.class, "systemid"))
                    .ifPresent(dokumentbeskrivelseResource::addDokumentstatus);

            optionalValue(file.getModifiedBy())
                    .map(Collections::singletonList)
                    .ifPresent(dokumentbeskrivelseResource::setForfatter);
            dokumentbeskrivelseResource.setBeskrivelse(String.format("%s - %s - %s - %s", file.getStatusDescription().getValue(), file.getRelationTypeDescription().getValue(), file.getAccessCodeDescription().getValue(), file.getVersionFormatDescription().getValue()));

            optionalValue(file.getNote())
                    .filter(StringUtils::isNotBlank)
                    .ifPresent(dokumentbeskrivelseResource::setBeskrivelse);

            dokumentbeskrivelseResource.setDokumentobjekt(Collections.singletonList(dokumentobjektResource));

            optionalValue(file.getRelationTypeCode())
                    .flatMap(kode -> kodeverkRepository
                            .getTilknyttetRegistreringSom()
                            .stream()
                            .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getKode()))
                            .findAny())
                    .map(TilknyttetRegistreringSomResource::getSystemId)
                    .map(Identifikator::getIdentifikatorverdi)
                    .map(Link.apply(TilknyttetRegistreringSom.class, "systemid"))
                    .ifPresent(dokumentbeskrivelseResource::addTilknyttetRegistreringSom);

            optionalValue(file.getCategoryCode())
                    .flatMap(kode -> kodeverkRepository
                            .getDokumentType()
                            .stream()
                            .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getKode()))
                            .findAny())
                    .map(DokumentTypeResource::getSystemId)
                    .map(Identifikator::getIdentifikatorverdi)
                    .map(Link.apply(DokumentType.class, "systemid"))
                    .ifPresent(dokumentbeskrivelseResource::addDokumentType);

            dokumentbeskrivelseResourcesList.add(dokumentbeskrivelseResource);

        });
        journalpost.setDokumentbeskrivelse(dokumentbeskrivelseResourcesList);

        return journalpost;
    }

    public CreateDocumentParameter toP360(JournalpostResource journalpostResource, String caseNumber) {

        CreateDocumentParameter createDocumentParameter = objectFactory.createCreateDocumentParameter();

//        createDocumentParameter.setADContextUser(objectFactory.createDocumentParameterBaseADContextUser(adapterProps.getP360User()));

        createDocumentParameter.setTitle(objectFactory.createDocumentParameterBaseTitle(journalpostResource.getOffentligTittel()));
        createDocumentParameter.setUnofficialTitle(objectFactory.createDocumentParameterBaseUnofficialTitle(journalpostResource.getTittel()));
        createDocumentParameter.setCaseNumber(objectFactory.createCreateDocumentParameterCaseNumber(caseNumber));
        // TODO Set from incoming fields
//        createDocumentParameter.setAccessCode(objectFactory.createDocumentParameterBaseAccessCode("U"));
//        createDocumentParameter.setAccessGroup(objectFactory.createDocumentParameterBaseAccessGroup("Public"));

        journalpostResource
                .getJournalPostType()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createDocumentParameterBaseCategory)
                .findFirst()
                .ifPresent(createDocumentParameter::setCategory);

        journalpostResource
                .getJournalStatus()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createDocumentParameterBaseStatus)
                .findFirst()
                .ifPresent(createDocumentParameter::setStatus);

        ArrayOfDocumentContactParameter arrayOfDocumentContactParameter = objectFactory.createArrayOfDocumentContactParameter();
        journalpostResource
                .getKorrespondansepart()
                .stream()
                .map(this::createDocumentContact)
                .forEach(arrayOfDocumentContactParameter.getDocumentContactParameter()::add);
        createDocumentParameter.setContacts(objectFactory.createDocumentParameterBaseContacts(arrayOfDocumentContactParameter));

        ArrayOfCreateFileParameter arrayOfCreateFileParameter = objectFactory.createArrayOfCreateFileParameter();
        journalpostResource
                .getDokumentbeskrivelse()
                .stream()
                .flatMap(this::createFiles)
                .forEach(arrayOfCreateFileParameter.getCreateFileParameter()::add);
        createDocumentParameter.setFiles(objectFactory.createDocumentParameterBaseFiles(arrayOfCreateFileParameter));
        return createDocumentParameter;
    }


    private DocumentContactParameter createDocumentContact(KorrespondanseResource korrespondansepart) {
        DocumentContactParameter documentContactParameter = objectFactory.createDocumentContactParameter();

        korrespondansepart
                .getKorrespondansepart()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createDocumentContactParameterReferenceNumber)
                .findFirst()
                .ifPresent(documentContactParameter::setReferenceNumber);

        korrespondansepart
                .getKorrespondanseparttype()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createDocumentContactParameterRole)
                .findFirst()
                .ifPresent(documentContactParameter::setRole);

        return documentContactParameter;
    }

    private Stream<CreateFileParameter> createFiles(DokumentbeskrivelseResource dokumentbeskrivelse) {
        return dokumentbeskrivelse
                .getDokumentobjekt()
                .stream()
                .map(dokumentobjekt -> createFile(dokumentbeskrivelse, dokumentobjekt));
    }

    private CreateFileParameter createFile(DokumentbeskrivelseResource dokumentbeskrivelse, DokumentobjektResource dokumentobjekt) {
        CreateFileParameter createFileParameter = objectFactory.createCreateFileParameter();

        createFileParameter.setTitle(objectFactory.createCreateFileParameterTitle(dokumentbeskrivelse.getTittel()));
        createFileParameter.setFormat(objectFactory.createCreateFileParameterFormat(dokumentobjekt.getFormat()));

        dokumentbeskrivelse
                .getTilknyttetRegistreringSom()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createCreateFileParameterRelationType)
                .findFirst()
                .ifPresent(createFileParameter::setRelationType);

        dokumentbeskrivelse
                .getDokumentType()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createCreateFileParameterCategory)
                .findFirst()
                .ifPresent(createFileParameter::setCategory);

        dokumentbeskrivelse
                .getDokumentstatus()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(objectFactory::createCreateFileParameterStatus)
                .findFirst()
                .ifPresent(createFileParameter::setStatus);

        // TODO Map from incoming fields
        //createFileParameter.setNote(objectFactory.createCreateFileParameterNote(dokumentbeskrivelse.getBeskrivelse()));
//        createFileParameter.setAccessCode(objectFactory.createCreateFileParameterAccessCode("U"));
//        createFileParameter.setVersionFormat(objectFactory.createCreateFileParameterVersionFormat("A"));

        createFileParameter.setData(
                dokumentobjekt
                        .getReferanseDokumentfil()
                        .stream()
                        .map(Link::getHref)
                        .filter(StringUtils::isNotBlank)
                        .map(s -> StringUtils.substringAfterLast(s, "/"))
                        .map(fileRepository::getFile)
                        .map(DokumentfilResource::getData)
                        .map(Base64.getDecoder()::decode)
                        .map(objectFactory::createCreateFileParameterData)
                        .findAny()
                        .orElseThrow(() -> new FileNotFound("File not found for " + dokumentbeskrivelse.getTittel())));

        return createFileParameter;
    }

}
