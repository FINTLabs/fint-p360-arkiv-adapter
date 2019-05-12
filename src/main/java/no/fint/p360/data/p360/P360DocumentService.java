package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.document.*;
import no.fint.p360.data.exception.CreateDocumentException;
import no.fint.p360.data.exception.GetDocumentException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;

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
        super.setup(documentService, "DocumentService");

        objectFactory = new ObjectFactory();
    }

    public void createDocument(CreateDocumentParameter createDocumentParameter) {
        DocumentOperationResult documentOperationResult = documentService.createDocument(createDocumentParameter);
        if (documentOperationResult.isSuccessful()) {
            log.info("Documents successfully created");
            return;
        }
        throw new CreateDocumentException();
    }

    public DocumentResult getDocumentBySystemId(String systemId) {
        GetDocumentsQuery documentsQuery = new GetDocumentsQuery();
        documentsQuery.setRecno(objectFactory.createGetDocumentsQueryRecno(Integer.valueOf(systemId)));
        documentsQuery.setIncludeRemarks(Boolean.TRUE);
        documentsQuery.setIncludeCustomFields(Boolean.TRUE);

        GetDocumentsResult documentsResult = documentService.getDocuments(documentsQuery);

        log.info("DocumentsResult: {}", documentsResult);

        if (documentsResult.isSuccessful() && documentsResult.getTotalPageCount().getValue() == 1) {
            return documentsResult.getDocuments().getValue().getDocumentResult().get(0);
        }
        if (documentsResult.getTotalPageCount().getValue() != 1) {
            throw new GetDocumentException("Document could not be found");
        }
        throw new GetDocumentException(documentsResult.getErrorDetails().getValue());
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
