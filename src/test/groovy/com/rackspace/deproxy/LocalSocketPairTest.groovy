
package com.rackspace.deproxy

import org.junit.Test

import static junit.framework.Assert.assertEquals

class LocalSocketPairTest {

    @Test
    void testLocalSocketPairCreation() {

        def (Socket client, Socket server) = LocalSocketPair.createLocalSocketPair()



        PrintWriter writer = new PrintWriter(client.outputStream)
        UnbufferedStreamReader reader = new UnbufferedStreamReader(server.inputStream)

        writer.println("asdf")
        writer.flush()
        def asdf = LineReader.readLine(reader)

        assertEquals(asdf, "asdf")



        writer = new PrintWriter(server.outputStream)
        reader = new UnbufferedStreamReader(client.inputStream)

        writer.println("qwerty")
        writer.flush()
        def qwerty = LineReader.readLine(reader)

        assertEquals(qwerty, "qwerty")

        client.close()
        server.close()
    }

    @Test
    void testLocalSocketPairCreationWithPort() {

        def (Socket client, Socket server) = LocalSocketPair.createLocalSocketPair(12345)

        assertEquals(12345, client.port)
        assertEquals(12345, server.localPort)



        PrintWriter writer = new PrintWriter(client.outputStream)
        UnbufferedStreamReader reader = new UnbufferedStreamReader(server.inputStream)

        writer.println("asdf")
        writer.flush()
        def asdf = LineReader.readLine(reader)

        assertEquals(asdf, "asdf")



        writer = new PrintWriter(server.outputStream)
        reader = new UnbufferedStreamReader(client.inputStream)

        writer.println("qwerty")
        writer.flush()
        def qwerty = LineReader.readLine(reader)

        assertEquals(qwerty, "qwerty")

        client.close()
        server.close()
    }

    @Test
    void testLocalSocketPairCreationWithPortFinder() {

        PortFinder pf = new PortFinder()

        pf.getNextOpenPort(12346)
        pf.getNextOpenPort()
        pf.getNextOpenPort()

        def (Socket client, Socket server) = LocalSocketPair.createLocalSocketPair(pf)

        //Since the port finder is deprecated, and unreliable, this still uses the built in java socket stuff.
        //The acceptance of a PortFinder just ensures that use of it doesn't break just yet.

        PrintWriter writer = new PrintWriter(client.outputStream)
        UnbufferedStreamReader reader = new UnbufferedStreamReader(server.inputStream)

        writer.println("asdf")
        writer.flush()
        def asdf = LineReader.readLine(reader)

        assertEquals(asdf, "asdf")



        writer = new PrintWriter(server.outputStream)
        reader = new UnbufferedStreamReader(client.inputStream)

        writer.println("qwerty")
        writer.flush()
        def qwerty = LineReader.readLine(reader)

        assertEquals(qwerty, "qwerty")

        client.close()
        server.close()
    }
}
