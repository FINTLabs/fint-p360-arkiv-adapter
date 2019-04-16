package no.fint.ra.data;

import no.fint.arkiv.p360.caze.ArrayOfstring;
import no.fint.arkiv.p360.caze.CreateCaseParameter;
import no.fint.arkiv.p360.caze.ObjectFactory;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.Props;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

@Component
public class P360CaseFactory {

    @Autowired
    private Props props;

    private ObjectFactory objectFactory;


    @PostConstruct
    private void init() {
        objectFactory = new ObjectFactory();
    }

    public CreateCaseParameter createTilskuddFartoy(TilskuddFartoyResource tilskuddFartoy) {
        CreateCaseParameter createCaseParameter = new CreateCaseParameter();

        createCaseParameter.setTitle(objectFactory.createCaseParameterBaseTitle(tilskuddFartoy.getTittel()));
        createCaseParameter.setStatus(objectFactory.createCaseParameterBaseStatus("B"));
        createCaseParameter.setFiledOnPaper(objectFactory.createCaseParameterBaseFiledOnPaper(false));

        createCaseParameter.setKeywords(objectFactory.createCaseParameterBaseKeywords(getKeywords()));

        // Set default to NOARK Sak
        createCaseParameter.setCaseType(objectFactory.createCreateCaseParameterCaseType("recno:2"));

        createCaseParameter.setResponsibleEnterpriseRecno(objectFactory.createCaseParameterBaseResponsibleEnterpriseRecno(Integer.valueOf(props.getResponsibleUnit())));
        createCaseParameter.setSubArchive(objectFactory.createCaseParameterBaseSubArchive(props.getSubArchive()));


        /*
        createCaseParameter.setResponsiblePersonIdNumber(
                objectFactory.createCaseParameterBaseResponsiblePersonIdNumber(
                        tilskuddFartoy.getSaksansvarlig().get(0).getHref()
                )
        );
        */

        /*
        createCaseParameter.setStartDate(
                objectFactory.createCaseParameterBaseStartDate(getXMLGregorianCalendar(tilskuddFartoy.getOpprettetDato())
                )
        );
        */




        return createCaseParameter;

    }

    private ArrayOfstring getKeywords() {

        ArrayOfstring keywordArray = objectFactory.createArrayOfstring();
        List<String> keywords = Arrays.asList(props.getKeywords());

        keywords.forEach(keywordArray.getString()::add);

        return keywordArray;
    }

    private XMLGregorianCalendar getXMLGregorianCalendar(Date date) {
        XMLGregorianCalendar xmlDate = null;
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);

        try {
            xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlDate;
    }

    public static Identifikator createIdentifikator(String value) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(value);
        return identifikator;
    }

}
