package no.fint.ra.data.utilities;

import no.fint.arkiv.p360.caze.*;

import javax.xml.bind.JAXBElement;
import java.util.List;

public enum P360Utils {
    ;

    public static JAXBElement<ArrayOfstring> getKeywords(List<String> keywords) {
        ObjectFactory objectFactory = new ObjectFactory();

        ArrayOfstring keywordArray = objectFactory.createArrayOfstring();
        keywords.forEach(keywordArray.getString()::add);

        return objectFactory.createCaseParameterBaseKeywords(keywordArray);
    }

    public static JAXBElement<ExternalIdParameter> getExternalIdParameter(String id) {
        ObjectFactory objectFactory = new ObjectFactory();

        ExternalIdParameter externalIdParameter = objectFactory.createExternalIdParameter();
        externalIdParameter.setId(objectFactory.createExternalIdParameterId(id));
        externalIdParameter.setType(objectFactory.createExternalIdParameterType(Constants.EXTERNAL_ID_TYPE));

        return objectFactory.createCaseParameterBaseExternalId(externalIdParameter);
    }

    public static JAXBElement<ArrayOfClassCodeParameter> getArchiveCodes(String code, String type) {
        ObjectFactory objectFactory = new ObjectFactory();

        ArrayOfClassCodeParameter arrayOfClassCodeParameter = objectFactory.createArrayOfClassCodeParameter();
        ClassCodeParameter classCodeParameter = objectFactory.createClassCodeParameter();

        classCodeParameter.setSort(1);
        classCodeParameter.setIsManualText(Boolean.FALSE);
        classCodeParameter.setArchiveCode(objectFactory.createClassCodeParameterArchiveCode(code));
        classCodeParameter.setArchiveType(objectFactory.createClassCodeParameterArchiveType(type));
        arrayOfClassCodeParameter.getClassCodeParameter().add(classCodeParameter);

        return objectFactory.createCaseParameterBaseArchiveCodes(arrayOfClassCodeParameter);
    }
}
