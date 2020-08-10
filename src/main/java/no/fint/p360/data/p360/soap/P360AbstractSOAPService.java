package no.fint.p360.data.p360.soap;

import no.fint.p360.data.utilities.RequestUtilities;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

public abstract class P360AbstractSOAPService {

    @Autowired
    protected RequestUtilities requestUtilities;

    final QName serviceName;

    public P360AbstractSOAPService(String namespaceURI, String localPart) {
        serviceName = new QName(namespaceURI, localPart);
    }

    void setup(Object port, String service) {
        BindingProvider bp = (BindingProvider) port;
        requestUtilities.addAuthentication(bp.getRequestContext());
        requestUtilities.setEndpointAddress(bp.getRequestContext(), service);
    }
}