package no.fint.ra.data.utilities;

import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.ra.data.exception.UnableToParseTitle;

import java.util.HashMap;
import java.util.Map;

public class TitleParser {

    //LM9544 - Ternen - Reketråler - Statsbudsjettet - Tilskudd

    public static final int TILSKUDDFARTOY_KALLESIGNAL = 0;
    public static final int TILSKUDDFARTOY_NAVN = 1;
    public static final int DIGISAK_NR = 2;
    public static final int MATRIKKELNUMMER = 1;
    public static final int KULTURMINNE_ID = 3;

    public static class Title {
        Map<Integer, String> titleMap;

        public Title() {
            titleMap = new HashMap<>();
        }

        public void setDimension(Integer dimension, String value) {
            titleMap.put(dimension, value);
        }

        public String getDimension(Integer dimension) {
            return titleMap.get(dimension);
        }
    }

    public static Title parseTitle(String caseTitle) {
        String[] titleArray = caseTitle.split("-");
        if (titleArray.length <= 1) {
            throw new UnableToParseTitle(String.format("Unable to parse title: %s", caseTitle));
        }

        Title title = new Title();

        for (int i = 0; i < titleArray.length; i++) {
            title.setDimension(i, titleArray[i].trim());
        }
        return title;
    }

    // TODO: 2019-05-09 Vi må finne ut hvordan denne skal være
    public static String getTitleString(TilskuddFartoyResource tilskuddFartoy) {
        return String.format("%s - %s - %s - Tilskudd", tilskuddFartoy.getKallesignal(), tilskuddFartoy.getKulturminneId(), tilskuddFartoy.getSoknadsnummer());
    }

}
