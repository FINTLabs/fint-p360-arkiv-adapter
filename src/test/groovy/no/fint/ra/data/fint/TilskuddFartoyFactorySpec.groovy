package no.fint.ra.data.fint


import no.fint.arkiv.p360.caze.ObjectFactory
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource
import no.fint.ra.data.noark.NoarkFactory
import no.fint.ra.data.p360.P360DocumentService
import no.fint.ra.data.testutils.P360ObjectFactory
import spock.lang.Specification

class TilskuddFartoyFactorySpec extends Specification {

    private ObjectFactory objectFactory
    private TilskuddFartoyFactory tilskuddFartoyFactory
    private P360DocumentService documentService
    private P360ObjectFactory p360ObjectFactory
    private NoarkFactory noarkFactory
    private KodeverkService kodeverkService

    void setup() {
        objectFactory = new ObjectFactory()
        documentService = Mock(P360DocumentService)
        kodeverkService = Mock()
        noarkFactory = new NoarkFactory(documentService: documentService)
        tilskuddFartoyFactory = new TilskuddFartoyFactory(
                documentService: documentService,
                noarkFactory: noarkFactory,
                kodeverkService: kodeverkService
        )
        p360ObjectFactory = new P360ObjectFactory()
    }

    def "Convert from P360 case to Tilskudd fartoy"() {
        given:
        def caseResult = p360ObjectFactory.newP360Case()


        when:
        def fint = tilskuddFartoyFactory.toFintResource(caseResult)

        then:
        1 * documentService.getJournalPost(_ as String) >> new JournalpostResource()
        1 * kodeverkService.getSaksstatus() >> []
        fint
        fint.getMappeId().identifikatorverdi == "19/12345"
    }

    def "Convert list of P360 cases to Tilskudd fartoy"() {

        when:
        def fartoys = tilskuddFartoyFactory.toFintResourceList(p360ObjectFactory.newP360CaseList())
        def result = fartoys.count()

        then:
        result == 2
        2 * kodeverkService.getSaksstatus() >> []
    }
}
