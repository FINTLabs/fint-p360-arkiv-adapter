package no.fint.ra.data.p360;

import no.fint.arkiv.p360.caze.*;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.KulturminneProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class P360CaseFactory {

    @Autowired
    private KulturminneProps kulturminneProps;

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

        // TODO: 2019-04-30 Denne bør vel egentlig komme fra journalEnhet
        createCaseParameter.setResponsibleEnterpriseRecno(objectFactory.createCaseParameterBaseResponsibleEnterpriseRecno(Integer.valueOf(kulturminneProps.getResponsibleUnit())));
        createCaseParameter.setSubArchive(objectFactory.createCaseParameterBaseSubArchive(kulturminneProps.getSubArchive()));

        ArrayOfClassCodeParameter arrayOfClassCodeParameter = objectFactory.createArrayOfClassCodeParameter();
        ClassCodeParameter classCodeParameter = objectFactory.createClassCodeParameter();

        classCodeParameter.setArchiveCode(objectFactory.createString("Henko"));
        classCodeParameter.setArchiveType(objectFactory.createString("Fartøy"));
        arrayOfClassCodeParameter.getClassCodeParameter().add(classCodeParameter);

        //createCaseParameter.setArchiveCodes(objectFactory.createCaseParameterBaseArchiveCodes(arrayOfClassCodeParameter));


        /*
        createCaseParameter.setResponsiblePersonIdNumber(
                objectFactory.createCaseParameterBaseResponsiblePersonIdNumber(
                        tilskuddFartoy.getSaksansvarlig().get(0).getHref()
                )
        );
        */




        return createCaseParameter;

    }

    private ArrayOfstring getKeywords() {

        ArrayOfstring keywordArray = objectFactory.createArrayOfstring();
        List<String> keywords = Arrays.asList(kulturminneProps.getKeywords());

        keywords.forEach(keywordArray.getString()::add);

        return keywordArray;
    }



}
