package no.fint.p360.data.testutils;

import no.fint.arkiv.p360.caze.ArrayOfCaseDocumentResult;
import no.fint.arkiv.p360.caze.CaseDocumentResult;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.arkiv.p360.caze.ObjectFactory;

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
        CaseResult caseResult = new CaseResult();
        caseResult.setCaseNumber(objectFactory.createString("19/12345"));
        caseResult.setRecno(12345);
        caseResult.setDate(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
        caseResult.setCreatedDate(objectFactory.createCaseResultCreatedDate(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar())));
        caseResult.setTitle(objectFactory.createString("title"));
        caseResult.setUnofficialTitle(objectFactory.createString("title"));
        caseResult.setNotes(objectFactory.createString("notes"));
        caseResult.setArchiveCodes(objectFactory.createArrayOfArchiveCodeResult(objectFactory.createArrayOfArchiveCodeResult()));


        caseResult.setDocuments(objectFactory.createArrayOfCaseDocumentResult(newP360ArrayOfCaseDocument()));
        caseResult.setStatus(objectFactory.createString("S"));

        return caseResult;
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
