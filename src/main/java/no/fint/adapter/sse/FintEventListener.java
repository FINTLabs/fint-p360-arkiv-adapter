package no.fint.adapter.sse;

import lombok.extern.slf4j.Slf4j;
import no.fint.ra.service.EventHandlerService;
import no.fint.event.model.Event;
import no.fint.sse.AbstractEventListener;
import org.glassfish.jersey.media.sse.InboundEvent;

/**
 * Event listener for the for the SSE client. When an inbound event is received the {@link #onEvent(InboundEvent)} method
 * calls {@link EventHandlerService} p360.
 */
@Slf4j
public class FintEventListener extends AbstractEventListener {

    private EventHandlerService eventHandler;

    public FintEventListener(EventHandlerService eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void onEvent(Event event) {
        log.info("EventListener for {}", event.getOrgId());
        log.info("Processing event: {}, for orgId: {}, for client: {}, action: {}",
                event.getCorrId(),
                event.getOrgId(),
                event.getClient(),
                event.getAction());

        eventHandler.handleEvent(event);
    }
}
