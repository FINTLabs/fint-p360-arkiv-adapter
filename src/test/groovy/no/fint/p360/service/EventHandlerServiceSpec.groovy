package no.fint.p360.service

import no.fint.adapter.event.EventResponseService
import no.fint.adapter.event.EventStatusService
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.p360.data.p360.P360CaseService
import no.fint.p360.data.p360.P360DocumentService
import spock.lang.Specification

import java.util.concurrent.Executor

class EventHandlerServiceSpec extends Specification {
    private EventHandlerService eventHandlerService
    private EventStatusService eventStatusService
    private EventResponseService eventResponseService
    private P360DocumentService documentService
    private P360CaseService caseService
    private Executor executor

    void setup() {
        executor = new Executor() {
            @Override
            void execute(Runnable command) {
                command.run()
            }
        }
        eventStatusService = Mock(EventStatusService)
        eventResponseService = Mock(EventResponseService)
        caseService = Mock(P360CaseService)
        documentService = Mock(P360DocumentService)
        eventHandlerService = new EventHandlerService(
                executor: executor,
                eventStatusService: eventStatusService,
                eventResponseService: eventResponseService
        )
    }

    def "Post response on health check"() {
        given:
        def component = 'test'
        def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH, 'test')

        when:
        eventHandlerService.handleEvent(component, event)

        then:
        1 * eventResponseService.postResponse(component, _ as Event)
    }
}
