package no.fint.ra.data.p360

import no.fint.arkiv.p360.caze.CreateCaseParameter
import no.fint.arkiv.p360.caze.ObjectFactory
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource
import no.fint.ra.Props
import spock.lang.Specification

class P360CaseFactorySpec extends Specification {

    def "FINT TilskuddFartoy to P360 CreateCaseParameter"() {

        given:
        def tilskuddFartoyResource = new TilskuddFartoyResource()
        def objectFactory = new ObjectFactory()
        def props = new Props(responsibleUnit: "123", subArchive: "456", keywords: ["test1", "test2"])
        def factory = new P360CaseFactory(objectFactory: objectFactory, props: props)

        tilskuddFartoyResource.setTittel("Test")

        when:
        def p360Case = factory.createTilskuddFartoy(tilskuddFartoyResource)

        then:
        p360Case
        p360Case.getTitle().getValue() == "Test"
        p360Case.getResponsibleEnterpriseRecno().getValue().toString() == props.getResponsibleUnit()
        p360Case.getSubArchive().getValue().toString() == props.getSubArchive()
        p360Case.getKeywords().getValue().getString().size() == 2
    }
}
