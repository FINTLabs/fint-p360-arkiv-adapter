package no.fint.ra.data.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.*;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.ra.data.exception.CodeTableNotFound;
import no.fint.ra.data.fint.KodeverkFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class P360SupportService extends P360AbstractService {


    private static final String CONTACT_ROLE_TABLE = "Activity - Contact role";
    private static final String DOCUMENT_CATEGORY_TABLE = "Document category";
    private static final String DOCUMENT_STATUS_TABLE = "Document status";
    private static final String CASE_STATUS_TABLE = "Case status";
    private static final String JOURNAL_STATUS_TABLE = "Journal status";


    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "SupportService");


    private ISupportService supportService;
    private ObjectFactory objectFactory;

    @Autowired
    private KodeverkFactory kodeverkFactory;


    public P360SupportService() {
        super("http://software-innovation.com/SI.Data", "SupportService");
    }

    @PostConstruct
    private void init() {

        supportService = new SupportService(SupportService.WSDL_LOCATION, SERVICE_NAME).getBasicHttpBindingISupportService();
        super.addAuthentication(supportService);

        objectFactory = new ObjectFactory();

    }

    public List<JournalStatusResource> getJournalStatusTable() {
        GetCodeTableRowsResult codeTable = getCodeTable(JOURNAL_STATUS_TABLE);
        return codeTable.getCodeTableRows().getValue().getCodeTableRowResult()
                .stream().map(kodeverkFactory::toJournalStatus).collect(Collectors.toList());
    }

    public List<SaksstatusResource> getCaseStatusTable() {
        GetCodeTableRowsResult codeTable = getCodeTable(CASE_STATUS_TABLE);
        return codeTable.getCodeTableRows().getValue().getCodeTableRowResult()
                .stream().map(kodeverkFactory::toSaksstatus).collect(Collectors.toList());
    }

    public List<DokumentStatusResource> getDocumentStatusTable() {
        GetCodeTableRowsResult codeTable = getCodeTable(DOCUMENT_STATUS_TABLE);
        return codeTable.getCodeTableRows().getValue().getCodeTableRowResult()
                .stream().map(kodeverkFactory::toDokumentstatus).collect(Collectors.toList());
    }

    public List<JournalpostTypeResource> getDocumentCategoryTable() {
        GetCodeTableRowsResult codeTable = getCodeTable(DOCUMENT_CATEGORY_TABLE);
        return codeTable.getCodeTableRows().getValue().getCodeTableRowResult()
                .stream().map(kodeverkFactory::toJournalpostType).collect(Collectors.toList());
    }

    public List<KorrespondansepartTypeResource> getDocumentContactRole() {
        GetCodeTableRowsResult codeTable = getCodeTable(CONTACT_ROLE_TABLE);
        return codeTable.getCodeTableRows().getValue().getCodeTableRowResult()
                .stream().map(kodeverkFactory::toKorrespondansepartType).collect(Collectors.toList());
    }

    public GetCodeTableRowsResult getCodeTable(String table) {
        GetCodeTableRowsQuery codeTableRowsQuery = new GetCodeTableRowsQuery();
        codeTableRowsQuery.setCodeTableName(objectFactory.createGetCodeTableRowsQueryCodeTableName(String.format("code table: %s", table)));
        GetCodeTableRowsResult codeTableRows = supportService.getCodeTableRows(codeTableRowsQuery);
        if (codeTableRows.isSuccessful()) {
            return codeTableRows;
        }

        throw new CodeTableNotFound(String.format("Could not find %s", table));
    }


    public boolean ping() {

        try {
            supportService.ping();
        } catch (WebServiceException e) {
            return false;
        }

        return true;
    }
}
