/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy

import spock.lang.Specification

class EndpointShutdownTest extends Specification {

    Deproxy deproxy
    DeproxyEndpoint e1
    DeproxyEndpoint e2
    int port1
    int port2

    void testEndpointShutdown() {

        given:

        def pf = new PortFinder()
        port1 = pf.getNextOpenPort()
        port2 = pf.getNextOpenPort()

        deproxy = new Deproxy()
        e1 = deproxy.addEndpoint(port1)
        e2 = deproxy.addEndpoint(port2)


        when: "try to start another endpoint on the same port"
        def e3 = deproxy.addEndpoint(port1)

        then: "should throw an exception"
        thrown(IOException)


        when: "try to start another endpoint on the same port, after shutting down the first one"
        e1.shutdown()
        def e4 = deproxy.addEndpoint(port1)

        then: "should work fine"
        notThrown(IOException)

        cleanup:
        deproxy.shutdown()
    }

    void cleanup() {
        if (deproxy) {
            deproxy.shutdown()
        }
    }


}
