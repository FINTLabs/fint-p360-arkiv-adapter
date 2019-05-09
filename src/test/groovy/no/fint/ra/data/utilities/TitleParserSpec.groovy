package no.fint.ra.data.utilities


import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource
import no.fint.ra.data.exception.UnableToParseTitle
import spock.lang.Specification

class TitleParserSpec extends Specification {

    def "Parse title"() {
        when:
        def title = TitleParser.parseTitle("LM9544 - Ternen - Reketråler - Statsbudsjettet - Tilskudd")

        then:
        title.getDimension(TitleParser.TILSKUDDFARTOY_KALLESIGNAL) == "LM9544"
        title.getDimension(TitleParser.TILSKUDDFARTOY_NAVN) == "Ternen"
        noExceptionThrown()
    }

    def "when unable to parse title exception is thrown"() {
        when:
        def title = TitleParser.parseTitle("LM9544, Ternen, Reketråler, Statsbudsjettet, Tilskudd")

        then:
        thrown UnableToParseTitle
    }

    def "Get title from TilskuddFartoy"() {
        when:
        def title = TitleParser.getTitleString(new TilskuddFartoyResource(kallesignal: "LM9544", kulturminneId: "123", soknadsnummer: FintUtils.createIdentifikator("321")))

        then:
        title != null

    }
}

