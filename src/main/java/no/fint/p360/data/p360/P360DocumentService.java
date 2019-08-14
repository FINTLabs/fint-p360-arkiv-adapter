package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.document.*;
import no.fint.p360.data.exception.CreateDocumentException;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.utilities.P360Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
@Slf4j
public class P360DocumentService extends P360AbstractService {

    private IDocumentService documentService;
    private ObjectFactory objectFactory;

    @Value("${fint.p360.wsdl-location:./src/main/resources/wsdl}/DocumentService.wsdl")
    private String wsdlLocation;

    public P360DocumentService() {
        super("http://software-innovation.com/SI.Data", "DocumentService");
    }

    @PostConstruct
    private void init() throws MalformedURLException {
        URL wsdlLocationUrl = P360Utils.getURL(wsdlLocation);
        log.info("WSDL location: {}", wsdlLocationUrl);
        documentService = new DocumentService(wsdlLocationUrl, serviceName).getBasicHttpBindingIDocumentService();
        super.setup(documentService, "DocumentService");

        objectFactory = new ObjectFactory();
    }

    public void createDocument(CreateDocumentParameter createDocumentParameter) {
        log.info("Create Document: {}", createDocumentParameter);
        DocumentOperationResult documentOperationResult = documentService.createDocument(createDocumentParameter);
        log.info("Create Document Result: {}", documentOperationResult);
        if (documentOperationResult.isSuccessful()) {
            log.info("Documents successfully created");
            return;
        }
        throw new CreateDocumentException(documentOperationResult.getErrorMessage().getValue());
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
