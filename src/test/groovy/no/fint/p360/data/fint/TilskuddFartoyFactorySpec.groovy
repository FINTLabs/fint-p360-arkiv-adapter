package no.fint.p360.data.fint

import no.fint.arkiv.p360.caze.ObjectFactory
import no.fint.arkiv.p360.document.DocumentResult
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource
import no.fint.p360.data.kulturminne.TilskuddFartoyFactory
import no.fint.p360.data.noark.common.NoarkFactory
import no.fint.p360.data.noark.journalpost.JournalpostFactory
import no.fint.p360.data.noark.part.PartFactory
import no.fint.p360.data.p360.P360DocumentService
import no.fint.p360.data.testutils.P360ObjectFactory
import no.fint.p360.repository.KodeverkRepository
import spock.lang.Specification

class TilskuddFartoyFactorySpec extends Specification {

    private ObjectFactory objectFactory
    private TilskuddFartoyFactory tilskuddFartoyFactory
    private JournalpostFactory journalpostFactory
    private P360DocumentService documentService
    private P360ObjectFactory p360ObjectFactory
    private NoarkFactory noarkFactory
    private KodeverkRepository kodeverkRepository
    private PartFactory partFactory

    void setup() {
        objectFactory = new ObjectFactory()
        documentService = Mock()
        kodeverkRepository = Mock()
        journalpostFactory = Mock()
        partFactory = Mock()
        noarkFactory = new NoarkFactory(
                documentService: documentService,
                journalpostFactory: journalpostFactory,
                partFactory:  partFactory,
                kodeverkRepository: kodeverkRepository
        )
        tilskuddFartoyFactory = new TilskuddFartoyFactory(
                noarkFactory: noarkFactory
        )
        p360ObjectFactory = new P360ObjectFactory()
    }

    def "Convert from P360 case to Tilskudd fartoy"() {
        given:
        def caseResult = p360ObjectFactory.newP360Case()


        when:
        def fint = tilskuddFartoyFactory.toFintResource(caseResult)

        then:
        1 * documentService.getDocumentBySystemId(_ as String) >> new DocumentResult()
        1 * journalpostFactory.toFintResource(_ as DocumentResult) >> new JournalpostResource()
        1 * kodeverkRepository.getSaksstatus() >> []
        fint
        fint.getMappeId().identifikatorverdi == "19/12345"
    }

    def "Convert list of P360 cases to Tilskudd fartoy"() {

        when:
        def fartoys = tilskuddFartoyFactory.toFintResourceList(p360ObjectFactory.newP360CaseList())
        def result = fartoys.size()

        then:
        result == 2
        2 * kodeverkRepository.getSaksstatus() >> []
    }
}
