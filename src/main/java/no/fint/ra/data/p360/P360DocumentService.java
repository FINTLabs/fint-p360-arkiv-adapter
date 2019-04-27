package no.fint.ra.data.p360;

import no.fint.arkiv.p360.document.*;
import no.fint.model.administrasjon.arkiv.Dokumentfil;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
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

        GetDocumentsResult documentsResult = documentService.getDocuments(documentsQuery);

        if (documentsResult.isSuccessful() && documentsResult.getTotalPageCount().getValue() == 1) {
            DocumentResult documentResult = documentsResult.getDocuments().getValue().getDocumentResult().get(0);

            journalpost.setAntallVedlegg((long) documentResult.getFiles().getValue().getDocumentFileResult().size());
            journalpost.setTittel(documentResult.getTitle().getValue());
            journalpost.setOffentligTittel(documentResult.getOfficialTitle().getValue());
            journalpost.setDokumentetsDato(documentResult.getDocumentDate().getValue().toGregorianCalendar().getTime());
            journalpost.setDokumentbeskrivelse(Collections.emptyList());
            journalpost.setForfatter(Collections.emptyList());
            journalpost.setNokkelord(Collections.emptyList());
            journalpost.setReferanseArkivDel(Collections.emptyList());

            String[] split = documentResult.getDocumentNumber().getValue().split("-");
            if (split.length == 2) {
                journalpost.setJournalSekvensnummer(Long.parseLong(split[1]));
            }

            List<DocumentFileResult> documentFileResult = documentResult.getFiles().getValue().getDocumentFileResult();
            List<DokumentbeskrivelseResource> dokumentbeskrivelseResourcesList = new ArrayList<>();
            documentFileResult.forEach(file -> {
                DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
                dokumentbeskrivelseResource.setTittel(file.getTitle().getValue());

                DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
                dokumentobjektResource.setFormat(file.getFormat().getValue());
                //dokumentobjektResource.setReferanseDokumentfil(file.getRecno().toString());
                dokumentobjektResource.addReferanseDokumentfil(Link.with(Dokumentfil.class, "systemid", file.getRecno().toString()));

                dokumentbeskrivelseResource.addDokumentstatus(Link.with(DokumentStatusResources.class, "systemid", file.getStatusCode().getValue()));

                dokumentbeskrivelseResource.setForfatter(Collections.singletonList(file.getModifiedBy().getValue()));
                dokumentbeskrivelseResource.setDokumentobjekt(Collections.singletonList(dokumentobjektResource));
                dokumentbeskrivelseResource.addTilknyttetRegistreringSom(Link.with(TilknyttetRegistreringSomResource.class, "systemid", file.getRelationTypeCode().getValue()));


                dokumentbeskrivelseResourcesList.add(dokumentbeskrivelseResource);

            });
            journalpost.setDokumentbeskrivelse(dokumentbeskrivelseResourcesList);
            //journalpost.setJournalPostnummer(Long.valueOf(documentResult.getCaseNumber().getValue()));
        }

        return journalpost;
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
