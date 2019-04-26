package no.fint.ra.data.fint


import no.fint.arkiv.p360.caze.ObjectFactory
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource
import no.fint.ra.data.p360.P360DocumentService
import no.fint.ra.data.testutils.P360ObjectFactory
import spock.lang.Specification

class TilskuddFartoyFactorySpec extends Specification {

    private ObjectFactory objectFactory
    private TilskuddFartoyFactory tilskuddFartoyFactory
    private P360DocumentService documentService
    private P360ObjectFactory p360ObjectFactory

    void setup() {
        objectFactory = new ObjectFactory()
        documentService = Mock(P360DocumentService)
        tilskuddFartoyFactory = new TilskuddFartoyFactory(documentService: documentService)
        p360ObjectFactory = new P360ObjectFactory()
    }

    def "Convert from P360 case to Tilskudd fartoy"() {
        given:
        def caseResult = p360ObjectFactory.newP360Case()


        when:
        def fint = tilskuddFartoyFactory.toFint(caseResult)

        then:
        1 * documentService.getJournalPost(_ as String) >> new JournalpostResource()
        fint
        fint.getMappeId().identifikatorverdi == "19/12345"
    }

    def "Convert list of P360 cases to Tilskudd fartoy"() {

        when:
        def fartoys = tilskuddFartoyFactory.p360ToFintTilskuddFartoys(p360ObjectFactory.newP360CaseList())

        then:
        fartoys.size() == 2
    }
}
