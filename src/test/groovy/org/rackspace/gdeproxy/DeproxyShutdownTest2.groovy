package org.rackspace.gdeproxy

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DeproxyShutdownTest2 extends Specification {

    Deproxy deproxy
    int port
    ServerSocket serverSocket1
    ServerSocket serverSocket2

    void testEndpointShutdown() {

        given:

        def pf = new PortFinder()
        port = pf.getNextOpenPort()
        deproxy = new Deproxy()
        def e = deproxy.addEndpoint(port)

        // start making a request to the endpoint in another thread, so we can
        // try to shutdown while the request is in progress.
        MessageChain mc
        CountDownLatch clientStarted = new CountDownLatch(1)
        CountDownLatch clientFinished = new CountDownLatch(1)
        Exception caughtException = null

        Thread th = Thread.startDaemon {

            clientStarted.countDown()

            try {
                mc = deproxy.makeRequest(url: "http://localhost:${port}",
                        defaultHandler: Handlers.Delay(2000))
            } catch (Exception ex) {
                caughtException = ex
            }

            clientFinished.countDown()
        }



        when: "shutting down the deproxy"

        // give the client request time to start
        clientStarted.await(500, TimeUnit.MILLISECONDS)

        deproxy.shutdown()

        // the client request should have completed by now, but we still need
        // to wait for makeRequest to return and the mc variable to be set.

        clientFinished.await(500, TimeUnit.MILLISECONDS)

        then: "client request should have completed"
        caughtException == null
        mc != null
        mc.handlings.size() == 1
        mc.handlings[0].endpoint == e



        when: "we try to re-use the port"
        serverSocket2 = new ServerSocket(port)

        then: "it should be available, and not throw any exception"
        notThrown(IOException)


        cleanup:
        th.join()
    }

    void cleanup() {
        if (deproxy) {
            deproxy.shutdown()
        }
        if (serverSocket1) {
            serverSocket1.close()
        }
        if (serverSocket2) {
            serverSocket2.close()
        }
    }
}
