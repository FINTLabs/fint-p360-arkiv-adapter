package no.fint.ra.data.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.document.*;
import no.fint.model.administrasjon.arkiv.*;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

            journalpost.setAntallVedlegg((long) documentResult.getFiles().getValue().getDocumentFileResult().size());
            journalpost.setTittel(documentResult.getTitle().getValue());
            journalpost.setOffentligTittel(documentResult.getOfficialTitle().getValue());
            journalpost.setDokumentetsDato(documentResult.getDocumentDate().getValue().toGregorianCalendar().getTime());
            journalpost.setJournalDato(documentResult.getJournalDate().getValue().toGregorianCalendar().getTime());
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
            documentResult
                    .getContacts()
                    .getValue()
                    .getDocumentContactResult()
                    .stream()
                    .map(it -> {
                        KorrespondanseResource result = new KorrespondanseResource();
                        result.addKorrespondansepart(Link.with(Korrespondansepart.class, "systemid", it.getContactRecno().getValue()));
                        result.addKorrespondanseparttype(Link.with(KorrespondansepartType.class, "systemid", it.getRole().getValue()));
                        return result;
                    })
                    .collect(Collectors.toList()));

            String[] split = documentResult.getDocumentNumber().getValue().split("-");
            if (split.length == 2) {
                journalpost.setJournalSekvensnummer(Long.parseLong(split[1]));
            }

            journalpost.addSaksbehandler(Link.with(Personalressurs.class, "ansattnummer", String.valueOf(documentResult.getResponsiblePerson().getValue().getRecno())));
            journalpost.addAdministrativEnhet(Link.with(Organisasjonselement.class, "organisasjonsummer", String.valueOf(documentResult.getResponsibleEnterprise().getValue().getRecno())));

            journalpost.addJournalPostType(Link.with(JournalpostTypeResource.class, "systemid", documentResult.getCategory().getValue().getRecno().toString()));
            journalpost.addJournalStatus(Link.with(JournalStatusResource.class, "systemid", documentResult.getStatusCode().getValue()));

            List<DocumentFileResult> documentFileResult = documentResult.getFiles().getValue().getDocumentFileResult();
            List<DokumentbeskrivelseResource> dokumentbeskrivelseResourcesList = new ArrayList<>();
            documentFileResult.forEach(file -> {
                DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
                dokumentbeskrivelseResource.setTittel(file.getTitle().getValue());

                DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
                dokumentobjektResource.setFormat(file.getFormat().getValue());
                //dokumentobjektResource.setReferanseDokumentfil(file.getRecno().toString());
                dokumentobjektResource.addReferanseDokumentfil(Link.with(Dokumentfil.class, "systemid", file.getRecno().toString()));

                dokumentbeskrivelseResource.addDokumentstatus(Link.with(DokumentStatus.class, "systemid", file.getStatusCode().getValue()));

                dokumentbeskrivelseResource.setForfatter(Collections.singletonList(file.getModifiedBy().getValue()));
                dokumentbeskrivelseResource.setBeskrivelse(String.format("%s - %s - %s - %s", file.getStatusDescription().getValue(), file.getRelationTypeDescription().getValue(), file.getAccessCodeDescription().getValue(), file.getVersionFormatDescription().getValue()));
                dokumentbeskrivelseResource.setDokumentobjekt(Collections.singletonList(dokumentobjektResource));
                dokumentbeskrivelseResource.addTilknyttetRegistreringSom(Link.with(TilknyttetRegistreringSom.class, "systemid", file.getRelationTypeCode().getValue()));


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
            createDocumentParameter.setCategory(objectFactory.createDocumentParameterBaseCategory(split[split.length -1]));

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
