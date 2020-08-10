package no.fint.p360.data.p360;

import no.fint.arkiv.p360.document.CreateDocumentParameter;
import no.fint.arkiv.p360.document.DocumentResult;
import no.fint.p360.data.exception.CreateDocumentException;
import no.fint.p360.data.exception.GetDocumentException;

public interface P360DocumentService {
    void createDocument(CreateDocumentParameter createDocumentParameter) throws CreateDocumentException;

    DocumentResult getDocumentBySystemId(String systemId) throws GetDocumentException;

    boolean ping();
}
