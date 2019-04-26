package no.fint.ra.data.testutils;

import no.fint.arkiv.p360.caze.ArrayOfCaseDocumentResult;
import no.fint.arkiv.p360.caze.CaseDocumentResult;
import no.fint.arkiv.p360.caze.CaseResult;
import no.fint.arkiv.p360.caze.ObjectFactory;
import no.fint.ra.data.utilities.SOAPUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class P360ObjectFactory {

    private ObjectFactory objectFactory;

    public P360ObjectFactory() {
        objectFactory = new ObjectFactory();
    }

    public CaseResult newP360Case() {
        CaseResult caseResult = new CaseResult();
        caseResult.setCaseNumber(objectFactory.createString("19/12345"));
        caseResult.setRecno(12345);
        caseResult.setDate(SOAPUtils.getXMLGregorianCalendar(new Date()));
        caseResult.setCreatedDate(objectFactory.createCaseResultCreatedDate(SOAPUtils.getXMLGregorianCalendar(new Date())));
        caseResult.setTitle(objectFactory.createString("title"));
        caseResult.setUnofficialTitle(objectFactory.createString("title"));
        caseResult.setNotes(objectFactory.createString("notes"));



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
