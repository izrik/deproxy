package org.rackspace.deproxy

import java.nio.charset.Charset


class TcpForwarder {

    int port
    ServerSocket listener
    boolean _stop
    def threads = []
    final Object threadsLock = new Object();

    public TcpForwarder(int listenPort, String remoteHost, int remotePort) {

        this.port = listenPort
        listener = new ServerSocket(listenPort)
        def t = Thread.startDaemon {

            while (!_stop) {

                def source = listener.accept()
//                source.soTimeout = 1000
                Socket destination = new Socket(remoteHost, remotePort)
//                destination.soTimeout = 1000

                String line = "Creating a new connection to destination"
                log.debug(line)
                println(line)

                def t1 = Thread.startDaemon {
                    try {
                        pipe(source, destination, "s->d")
                    } finally {
                        source.close()
                        destination.close()
                    }
                }
                synchronized (threadsLock) {
                    threads.add(t1)
                }
                def t2 = Thread.startDaemon {
                    try {
                        pipe(destination, source, "s<-d")
                    } finally {
                        source.close()
                        destination.close()
                    }
                }
                synchronized (threadsLock) {
                    threads.add(t2)
                }
            }
        }

        synchronized (threadsLock) {
            threads.add(t)
        }
    }

    public void pipe(Socket from, Socket to, String label = "", boolean logTheData = true) {

        byte[] bytes = new byte[1024]
        while (!_stop) {
            try {
                int count = from.inputStream.read(bytes, 0, bytes.length)
                if (count > 0) {
                    to.outputStream.write(bytes, 0, count)
                    if (logTheData) {
                        def s = new String(bytes, 0, count, Charset.forName("US-ASCII"))
                        def line = "${label}: read ${count} bytes as string: ${s}"
                        log.debug(line)
                        println(line)
                    }
                }
            } catch (Exception ignored) {
                println("${label}: Caught an exception: ${ignored}")
                sleep(100)
            }
        }
    }

    public void stop() {
        _stop = true

        Thread[] threads2
        synchronized (threadsLock) {
            threads2 = threads.toArray() as Thread[]
            threads.clear()
        }

        try {
            for (Thread th in threads2) {
                th.interrupt()
                th.join(100)
            }
        } catch (Exception ignored) {}
    }
}
