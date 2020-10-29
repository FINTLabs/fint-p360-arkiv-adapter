package no.fint.p360.data.testutils;

import no.fint.arkiv.p360.caze.*;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.P360Utils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class P360ObjectFactory {

    private final DatatypeFactory datatypeFactory;

    public P360ObjectFactory() throws DatatypeConfigurationException {

        datatypeFactory = DatatypeFactory.newInstance();
    }

    public CaseResult newP360Case() {
        CaseResult caseResult = new CaseResult();
        caseResult.setCaseNumber("19/12345");
        caseResult.setRecno(12345);
        caseResult.setDate(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
        caseResult.setCreatedDate(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
        caseResult.setTitle("Tilskudd - AWQR - Ternen - 232291-0 - 35 - FINT Test #1");
        caseResult.setUnofficialTitle("Tilskudd - AWQR - Ternen - 232291-0 - 35 - FINT Test #1");
        caseResult.setNotes("notes");
        caseResult.setArchiveCodes(newArchiveCodes());
        caseResult.setExternalId(P360Utils.getExternalIdParameter(FintUtils.createIdentifikator("35")));
        caseResult.setDocuments(newP360ArrayOfCaseDocument());
        caseResult.setStatus("S");
        caseResult.setContacts(new ArrayOfCaseContactResult());
        caseResult.setResponsibleEnterprise(newResponsibleEnterprise());
        caseResult.setResponsiblePerson(newResponsiblePerson());

        return caseResult;
    }

    public ArrayOfArchiveCodeResult newArchiveCodes() {
        final ArrayOfArchiveCodeResult result = new ArrayOfArchiveCodeResult();
        result.getArchiveCodeResult().add(newArhiveCode("EMNE", "C99"));
        return result;
    }

    private ArchiveCodeResult newArhiveCode(String type, String code) {
        ArchiveCodeResult result = new ArchiveCodeResult();

        result.setArchiveType(type);
        result.setArchiveCode(code);

        return result;
    }

    private ResponsiblePerson newResponsiblePerson() {
        ResponsiblePerson responsiblePerson = new ResponsiblePerson();
        responsiblePerson.setRecno(23456);
        responsiblePerson.setName("Arkivaren");
        return responsiblePerson;
    }

    private ResponsibleEnterprise newResponsibleEnterprise() {
        ResponsibleEnterprise responsibleEnterprise = new ResponsibleEnterprise();
        responsibleEnterprise.setRecno(123456);
        responsibleEnterprise.setName("Arkivet");
        return responsibleEnterprise;
    }

    public List<CaseResult> newP360CaseList() {
        List<CaseResult> cases = new ArrayList<>();
        cases.add(newP360Case());
        cases.add(newP360Case());

        return cases;
    }

    public CaseDocumentResult newP360CaseDocument() {
        CaseDocumentResult caseDocument = new CaseDocumentResult();
        caseDocument.setRecno(123);

        return caseDocument;
    }

    public ArrayOfCaseDocumentResult newP360ArrayOfCaseDocument() {
        ArrayOfCaseDocumentResult arrayOfCaseDocumentResult = new ArrayOfCaseDocumentResult();

        arrayOfCaseDocumentResult.getCaseDocumentResult().add(newP360CaseDocument());

        return arrayOfCaseDocumentResult;
    }
}
