package no.fint.p360.data.testutils;

import no.fint.arkiv.p360.caze.*;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.P360Utils;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class P360ObjectFactory {

    private ObjectFactory objectFactory;
    private DatatypeFactory datatypeFactory;

    public P360ObjectFactory() throws DatatypeConfigurationException {
        objectFactory = new ObjectFactory();
        datatypeFactory = DatatypeFactory.newInstance();
    }

    public CaseResult newP360Case() {
        CaseResult caseResult = objectFactory.createCaseResult();
        caseResult.setCaseNumber("19/12345");
        caseResult.setRecno(12345);
        caseResult.setDate(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
        caseResult.setCreatedDate(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
        caseResult.setTitle("Tilskudd - AWQR - Ternen - 232291-0 - 35 - FINT Test #1");
        caseResult.setUnofficialTitle("Tilskudd - AWQR - Ternen - 232291-0 - 35 - FINT Test #1");
        caseResult.setNotes("notes");
        caseResult.setArchiveCodes(objectFactory.createArrayOfArchiveCodeResult());
        caseResult.setExternalId(P360Utils.getExternalIdParameter(FintUtils.createIdentifikator("35")));
        caseResult.setDocuments(newP360ArrayOfCaseDocument());
        caseResult.setStatus("S");
        caseResult.setContacts(objectFactory.createArrayOfCaseContactResult());
        caseResult.setResponsibleEnterprise(newResponsibleEnterprise());
        caseResult.setResponsiblePerson(newResponsiblePerson());

        return caseResult;
    }

    private ResponsiblePerson newResponsiblePerson() {
        ResponsiblePerson responsiblePerson = objectFactory.createResponsiblePerson();
        responsiblePerson.setRecno(23456);
        responsiblePerson.setName("Arkivaren");
        return responsiblePerson;
    }

    private ResponsibleEnterprise newResponsibleEnterprise() {
        ResponsibleEnterprise responsibleEnterprise = objectFactory.createResponsibleEnterprise();
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
        ArrayOfCaseDocumentResult arrayOfCaseDocumentResult = objectFactory.createArrayOfCaseDocumentResult();

        arrayOfCaseDocumentResult.getCaseDocumentResult().add(newP360CaseDocument());

        return arrayOfCaseDocumentResult;
    }
}
