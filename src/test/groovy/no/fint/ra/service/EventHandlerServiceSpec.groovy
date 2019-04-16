package no.fint.ra.service

import no.fint.adapter.event.EventResponseService
import no.fint.adapter.event.EventStatusService
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event

import spock.lang.Specification

class EventHandlerServiceSpec extends Specification {
    private EventHandlerService eventHandlerService
    private EventStatusService eventStatusService
    private EventResponseService eventResponseService

    void setup() {
        eventStatusService = Mock(EventStatusService)
        eventResponseService = Mock(EventResponseService)
        p360UserService = Mock(P360UserService)
        p360ContactServiceP360 = Mock(P360ContactServiceP360)
        eventHandlerService = new EventHandlerService(eventStatusService: eventStatusService,
                eventResponseService: eventResponseService,
                p360ContactService: p360ContactServiceP360,
                p360UserService: p360UserService
        )
    }

    def "Post response on health check"() {
        given:
        def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH, 'test')

        when:
        eventHandlerService.handleEvent(event)

        then:
        1 * eventResponseService.postResponse(_ as Event)
    }
}
