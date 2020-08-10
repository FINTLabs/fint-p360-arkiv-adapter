package no.fint.p360.data.p360.rpc;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.document.CreateDocumentParameter;
import no.fint.arkiv.p360.document.DocumentResult;
import no.fint.p360.data.exception.CreateDocumentException;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.p360.P360DocumentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.p360.api", havingValue = "RPC")
public class P360DocumentServiceRPC extends P360AbstractRPCService implements P360DocumentService {
    @Override
    public void createDocument(CreateDocumentParameter createDocumentParameter) throws CreateDocumentException {

    }

    @Override
    public DocumentResult getDocumentBySystemId(String systemId) throws GetDocumentException {
        return null;
    }

    @Override
    public boolean ping() {
        return false;
    }
}
