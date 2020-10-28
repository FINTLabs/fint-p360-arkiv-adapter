package no.fint.p360.data.utilities;

import no.fint.arkiv.p360.caze.*;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public enum P360Utils {
    ;

    public static ArrayOfstring getKeywords(List<String> keywords) {
        ArrayOfstring keywordArray = new ArrayOfstring();
        keywords.forEach(keywordArray.getString()::add);

        return keywordArray;
    }

    public static URL getURL(String location) throws MalformedURLException {
        if (StringUtils.startsWithAny(location, "file:", "http:", "https:")) {
            return new URL(location);
        }
        return new URL("file:" + location);
    }

    public static ExternalIdParameter getExternalIdParameter(Identifikator id) {

        ExternalIdParameter externalIdParameter = new ExternalIdParameter();
        externalIdParameter.setId(id.getIdentifikatorverdi());
        externalIdParameter.setType(Constants.EXTERNAL_ID_TYPE);

        return externalIdParameter;
    }

    public static ArrayOfClassCodeParameter getArchiveCodes(String type, String code) {
        ArrayOfClassCodeParameter arrayOfClassCodeParameter = new ArrayOfClassCodeParameter();
        ClassCodeParameter classCodeParameter = new ClassCodeParameter();

        classCodeParameter.setSort(1);
        classCodeParameter.setIsManualText(Boolean.FALSE);
        classCodeParameter.setArchiveCode(code);
        classCodeParameter.setArchiveType(type);
        arrayOfClassCodeParameter.getClassCodeParameter().add(classCodeParameter);

        return arrayOfClassCodeParameter;
    }

    public static <T> void applyParameterFromLink(List<Link> links, Function<String, T> mapper, Consumer<T> consumer) {
        links.stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .map(mapper)
                .findFirst()
                .ifPresent(consumer);
    }

    public static void applyParameterFromLink(List<Link> links, Consumer<String> consumer) {
        links.stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .findFirst()
                .ifPresent(consumer);
    }
}
