package no.fint.p360.data.noark.journalpost;


import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.p360.data.exception.CreateDocumentException;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.p360.P360DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JournalpostService {

    @Autowired
    private P360DocumentService documentService;

    @Autowired
    private JournalpostFactory factory;

    public void createJournalPost(String caseNumber, JournalpostResource journalpostResource) throws CreateDocumentException {
        documentService.createDocument(factory.toP360(journalpostResource, caseNumber));
    }

    public JournalpostResource getJournalPost(String systemId) throws GetDocumentException {
        return factory.toFintResource(documentService.getDocumentBySystemId(systemId));
    }


}
