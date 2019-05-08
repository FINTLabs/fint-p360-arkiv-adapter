package no.fint.ra.data.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.document.*;
import no.fint.model.administrasjon.arkiv.*;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.felles.Person;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.ra.data.utilities.FintUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fint.ra.data.utilities.FintUtils.getSafeValue;

@Service
@Slf4j
public class P360DocumentService extends P360AbstractService {

    private IDocumentService documentService;
    private ObjectFactory objectFactory;

    public P360DocumentService() {
        super("http://software-innovation.com/SI.Data", "DocumentService");
    }

    @PostConstruct
    private void init() {

        documentService = new DocumentService(DocumentService.WSDL_LOCATION, serviceName).getBasicHttpBindingIDocumentService();
        super.addAuthentication(documentService);

        objectFactory = new ObjectFactory();
    }

    public JournalpostResource getJournalPost(String systemid) {
        JournalpostResource journalpost = new JournalpostResource();
        GetDocumentsQuery documentsQuery = new GetDocumentsQuery();
        documentsQuery.setRecno(objectFactory.createGetDocumentsQueryRecno(Integer.valueOf(systemid)));
        documentsQuery.setIncludeRemarks(Boolean.TRUE);
        documentsQuery.setIncludeCustomFields(Boolean.TRUE);

        GetDocumentsResult documentsResult = documentService.getDocuments(documentsQuery);

        log.info("DocumentsResult: {}", documentsResult);

        if (documentsResult.isSuccessful() && documentsResult.getTotalPageCount().getValue() == 1) {
            DocumentResult documentResult = documentsResult.getDocuments().getValue().getDocumentResult().get(0);

            getSafeValue(documentResult.getFiles())
                    .map(ArrayOfDocumentFileResult::getDocumentFileResult)
                    .map(List::size)
                    .map(Integer::longValue)
                    .ifPresent(journalpost::setAntallVedlegg);
            getSafeValue(documentResult.getTitle()).ifPresent(journalpost::setTittel);
            getSafeValue(documentResult.getOfficialTitle()).ifPresent(journalpost::setOffentligTittel);
            getSafeValue(documentResult.getDocumentDate())
                    .map(XMLGregorianCalendar::toGregorianCalendar)
                    .map(GregorianCalendar::getTime)
                    .ifPresent(journalpost::setDokumentetsDato);
            getSafeValue(documentResult.getJournalDate())
                    .map(XMLGregorianCalendar::toGregorianCalendar)
                    .map(GregorianCalendar::getTime)
                    .ifPresent(journalpost::setJournalDato);

            //journalpost.setOpprettetDato(documentResult.getCreatedDate().getValue());
            journalpost.setDokumentbeskrivelse(Collections.emptyList());
            journalpost.setForfatter(Collections.emptyList());
            journalpost.setNokkelord(Collections.emptyList());
            journalpost.setReferanseArkivDel(Collections.emptyList());

            journalpost.setBeskrivelse(String.format("%s - %s - %s", documentResult.getType().getValue().getDescription().getValue(), documentResult.getStatusDescription().getValue(), documentResult.getAccessCodeDescription().getValue()));

            journalpost.addJournalPostType(Link.with(JournalpostType.class, "systemid", documentResult.getType().getValue().getCode().getValue()));
            journalpost.addJournalStatus(Link.with(JournalStatus.class, "systemid", documentResult.getStatusCode().getValue()));

            journalpost.setForfatter(Collections.singletonList(documentResult.getResponsiblePersonName().getValue()));

            journalpost.setKorrespondansepart(
                    getSafeValue(documentResult.getContacts())
                            .map(ArrayOfDocumentContactResult::getDocumentContactResult)
                            .map(Collection::stream)
                            .orElse(Stream.empty())
                            .map(it -> {
                                KorrespondanseResource result = new KorrespondanseResource();
                                result.addKorrespondansepart(Link.with(Korrespondansepart.class, "systemid", it.getContactRecno().getValue()));
                                result.addKorrespondanseparttype(Link.with(KorrespondansepartType.class, "systemid", it.getRole().getValue()));
                                return result;
                            })
                            .collect(Collectors.toList()));


            String[] split = getSafeValue(documentResult.getDocumentNumber()).orElse("").split("-");
            if (split.length == 2 && StringUtils.isNumeric(split[1])) {
                journalpost.setJournalSekvensnummer(Long.parseLong(split[1]));
            }

            getSafeValue(documentResult.getResponsiblePerson())
                    .map(ResponsiblePerson::getExternalId)
                    .flatMap(FintUtils::getSafeValue)
                    .map(Link.apply(Person.class, "fodselsnummer"))
                    .ifPresent(journalpost::addSaksbehandler);
            getSafeValue(documentResult.getResponsibleEnterprise())
                    .map(ResponsibleEnterprise::getExternalId)
                    .flatMap(FintUtils::getSafeValue)
                    .map(Link.apply(Organisasjonselement.class, "organisasjonsnummer"))
                    .ifPresent(journalpost::addAdministrativEnhet);
            getSafeValue(documentResult.getCategory())
                    .map(DocumentCategoryResult::getRecno)
                    .map(String::valueOf)
                    .map(Link.apply(JournalpostType.class, "systemid"))
                    .ifPresent(journalpost::addJournalPostType);
            getSafeValue(documentResult.getStatusCode())
                    .map(Link.apply(JournalStatus.class, "systemid"))
                    .ifPresent(journalpost::addJournalStatus);

            List<DocumentFileResult> documentFileResult = documentResult.getFiles().getValue().getDocumentFileResult();
            List<DokumentbeskrivelseResource> dokumentbeskrivelseResourcesList = new ArrayList<>();
            documentFileResult.forEach(file -> {
                DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
                getSafeValue(file.getTitle()).ifPresent(dokumentbeskrivelseResource::setTittel);

                DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
                getSafeValue(file.getFormat()).ifPresent(dokumentobjektResource::setFormat);
                //dokumentobjektResource.setReferanseDokumentfil(file.getRecno().toString());
                dokumentobjektResource.addReferanseDokumentfil(Link.with(Dokumentfil.class, "systemid", file.getRecno().toString()));

                getSafeValue(file.getStatusCode()).map(Link.apply(DokumentStatus.class, "systemid")).ifPresent(dokumentbeskrivelseResource::addDokumentstatus);

                getSafeValue(file.getModifiedBy())
                        .map(Collections::singletonList)
                        .ifPresent(dokumentbeskrivelseResource::setForfatter);
                dokumentbeskrivelseResource.setBeskrivelse(String.format("%s - %s - %s - %s", file.getStatusDescription().getValue(), file.getRelationTypeDescription().getValue(), file.getAccessCodeDescription().getValue(), file.getVersionFormatDescription().getValue()));
                dokumentbeskrivelseResource.setDokumentobjekt(Collections.singletonList(dokumentobjektResource));

                getSafeValue(file.getRelationTypeCode())
                        .map(Link.apply(TilknyttetRegistreringSom.class, "systemid"))
                        .ifPresent(dokumentbeskrivelseResource::addTilknyttetRegistreringSom);

                dokumentbeskrivelseResourcesList.add(dokumentbeskrivelseResource);

            });
            journalpost.setDokumentbeskrivelse(dokumentbeskrivelseResourcesList);
            //journalpost.setJournalPostnummer(Long.valueOf(documentResult.getCaseNumber().getValue()));
        }

        return journalpost;
    }


    public void createJournalPost(SaksmappeResource sak) {

        List<JournalpostResource> journalpostResources = sak.getJournalpost();

        journalpostResources.forEach(journalpostResource -> {
            CreateDocumentParameter createDocumentParameter = new CreateDocumentParameter();

            createDocumentParameter.setTitle(objectFactory.createDocumentParameterBaseTitle(journalpostResource.getOffentligTittel()));
            createDocumentParameter.setUnofficialTitle(objectFactory.createDocumentParameterBaseTitle(journalpostResource.getTittel()));
            createDocumentParameter.setCaseNumber(objectFactory.createCreateDocumentParameterCaseNumber(sak.getMappeId().getIdentifikatorverdi()));

            List<Link> journalPostType = journalpostResource.getJournalPostType();
            String[] split = journalPostType.get(0).getHref().split("/");
            createDocumentParameter.setCategory(objectFactory.createDocumentParameterBaseCategory(split[split.length - 1]));

            List<Link> journalStatus = journalpostResource.getJournalStatus();
            String[] split1 = journalStatus.get(0).getHref().split("/");
            createDocumentParameter.setStatus(objectFactory.createDocumentParameterBaseStatus(split1[split1.length - 1]));

            ArrayOfDocumentContactParameter arrayOfDocumentContactParameter = objectFactory.createArrayOfDocumentContactParameter();
            journalpostResource.getKorrespondansepart().forEach(korrespodansepart -> {
                /*
                String[] split2 = korrespodansepart.getHref().split("/");
                DocumentContactParameter documentContactParameter = objectFactory.createDocumentContactParameter();
                documentContactParameter.setExternalId(objectFactory.createContactInfoExternalId(String.format("recno:%s", split2[split2.length - 1])));
                documentContactParameter.setRole(objectFactory.createDocumentContactParameterRole("Mottaker"));
                arrayOfDocumentContactParameter.getDocumentContactParameter().add(documentContactParameter);
                 */

            });
            createDocumentParameter.setContacts(objectFactory.createArrayOfDocumentContactParameter(arrayOfDocumentContactParameter));

            /*
            CreateFileParameter createFileParameter = new CreateFileParameter();
            createDocumentParameter.setFiles();
            journalpostResource.getDokumentbeskrivelse().forEach(dokumentbeskrivelseResource -> {
                dokumentbeskrivelseResource.getDokumentobjekt().forEach(dokumentobjektResource -> {

                });
            });
             */

            DocumentOperationResult documentOperationResult = documentService.createDocument(createDocumentParameter);
            if (documentOperationResult.isSuccessful()) {
                log.info("Documents successfully created");
            } else {
                log.info("Documents unsuccessfully created");

            }
        });
    }

    public boolean ping() {

        try {
            documentService.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }
}
