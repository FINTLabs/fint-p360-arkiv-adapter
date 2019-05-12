package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.p360.support.*;
import no.fint.model.resource.administrasjon.arkiv.*;
import no.fint.p360.data.exception.CodeTableNotFound;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class P360SupportService extends P360AbstractService {

    private static final QName SERVICE_NAME = new QName("http://software-innovation.com/SI.Data", "SupportService");

    private ISupportService supportService;
    private ObjectFactory objectFactory;

    public P360SupportService() {
        super("http://software-innovation.com/SI.Data", "SupportService");
    }

    @Value("${fint.p360.tables.contact-role:Activity - Contact role}")
    private String contactRoleTable;

    @Value("${fint.p360.tables.document-category:Document category}")
    private String documentCategoryTable;

    @Value("${fint.p360.tables.document-status:Document status}")
    private String documentStatusTable;

    @Value("${fint.p360.tables.case-status:Case status}")
    private String caseStatusTable;

    @Value("${fint.p360.tables.journal-status:Journal status}")
    private String journalStatusTable;

    @PostConstruct
    private void init() {
        supportService = new SupportService(SupportService.WSDL_LOCATION, SERVICE_NAME).getBasicHttpBindingISupportService();
        super.addAuthentication(supportService);
        objectFactory = new ObjectFactory();
    }

    public Stream<JournalStatusResource> getJournalStatusTable() {
        return getCodeTableRowResultStream(journalStatusTable)
                .map(this::toJournalStatus);
    }


    public Stream<SaksstatusResource> getCaseStatusTable() {
        return getCodeTableRowResultStream(caseStatusTable)
                .map(this::toSaksstatus);
    }

    public Stream<DokumentStatusResource> getDocumentStatusTable() {
        return getCodeTableRowResultStream(documentStatusTable)
                .map(this::toDokumentstatus);
    }

    public Stream<JournalpostTypeResource> getDocumentCategoryTable() {
        return getCodeTableRowResultStream(documentCategoryTable)
                .map(this::toJournalpostType);
    }

    public Stream<KorrespondansepartTypeResource> getDocumentContactRole() {
        return getCodeTableRowResultStream(contactRoleTable)
                .map(this::toKorrespondansepartType);
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

    public String getSIFVersion() {
        return supportService.getSIFVersion();
    }

    private Stream<CodeTableRowResult> getCodeTableRowResultStream(String table) {
        GetCodeTableRowsResult codeTable = getCodeTable(table);
        return FintUtils.optionalValue(codeTable.getCodeTableRows())
                .map(ArrayOfCodeTableRowResult::getCodeTableRowResult)
                .map(List::stream)
                .orElseThrow(() -> new CodeTableNotFound(table));
    }

    private SaksstatusResource toSaksstatus(CodeTableRowResult codeTableRow) {
        SaksstatusResource saksstatusResource = new SaksstatusResource();

        saksstatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        saksstatusResource.setKode(codeTableRow.getCode().getValue());
        saksstatusResource.setNavn(codeTableRow.getDescription().getValue());

        return saksstatusResource;
    }

    private DokumentStatusResource toDokumentstatus(CodeTableRowResult codeTableRow) {
        DokumentStatusResource dokumentStatusResource = new DokumentStatusResource();

        dokumentStatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        dokumentStatusResource.setKode(codeTableRow.getCode().getValue());
        dokumentStatusResource.setNavn(codeTableRow.getDescription().getValue());
        return dokumentStatusResource;
    }

    private JournalpostTypeResource toJournalpostType(CodeTableRowResult codeTableRow) {
        JournalpostTypeResource journalpostTypeResource = new JournalpostTypeResource();

        journalpostTypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        journalpostTypeResource.setKode(codeTableRow.getCode().getValue());
        journalpostTypeResource.setNavn(codeTableRow.getDescription().getValue());

        return journalpostTypeResource;
    }

    private KorrespondansepartTypeResource toKorrespondansepartType(CodeTableRowResult codeTableRow) {
        KorrespondansepartTypeResource korrespondansepartTypeResource = new KorrespondansepartTypeResource();

        korrespondansepartTypeResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        korrespondansepartTypeResource.setKode(codeTableRow.getCode().getValue());
        korrespondansepartTypeResource.setNavn(codeTableRow.getDescription().getValue());

        return korrespondansepartTypeResource;

    }

    private JournalStatusResource toJournalStatus(CodeTableRowResult codeTableRow) {
        JournalStatusResource journalStatusResource = new JournalStatusResource();

        journalStatusResource.setSystemId(FintUtils.createIdentifikator(codeTableRow.getRecno().toString()));
        journalStatusResource.setKode(codeTableRow.getCode().getValue());
        journalStatusResource.setNavn(codeTableRow.getDescription().getValue());

        return journalStatusResource;
    }

}
