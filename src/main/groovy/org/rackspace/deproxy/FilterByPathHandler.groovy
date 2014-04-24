package org.rackspace.deproxy

import com.sun.jdi.connect.IllegalConnectorArgumentsException

import java.util.regex.Pattern

class FilterByPathHandler {

    FilterByPathHandler(handlers=null, Closure<Response> fallbackHandler=null) {
        if (handlers != null) {
            if (handlers instanceof Map<?, Closure<Response>>) {
                for (entry in handlers.entrySet()) {
                    def (pattern, Closure<Response> handler) = [entry.key, entry.value]
                    if (!(pattern instanceof Pattern)) {
                        pattern = Pattern.compile(Pattern.quote(pattern.toString()))
                    }
                    this.handlers.add(new Entry(pattern: pattern, handler: entry.value))
                }
            } else if (handlers instanceof Iterable<List>) {
                for (item in handlers) {
                    def (pattern, Closure<Response> handler) = item
                    if (!(pattern instanceof Pattern)) {
                        pattern = Pattern.compile(Pattern.quote(pattern.toString()))
                    }

                    this.handlers.add(new Entry(pattern: pattern, handler: handler))
                }
            } else {
                throw new IllegalArgumentException("handlers")
            }
        }

        this.fallbackHandler = fallbackHandler
    }

    static FilterByPathHandler ByName(Map params) {
        def handlers = params?.handlers
        Closure<Response> fallbackHandler = params?.fallbackHandler as Closure<Response>

        return new FilterByPathHandler(handlers, fallbackHandler)
    }
    static FilterByPathHandler ByName(Closure<Response> fallbackHandler, handlers) {
        // this overload is solely for the purpose of providing parameter names in auto-complete
        return ByName(handlers: handlers, fallbackHandler: fallbackHandler)
    }

    class Entry {
        Pattern pattern
        Closure<Response> handler
    }

    List<Entry> handlers = [] as List<Entry>
    Closure<Response> fallbackHandler

    Response handleRequest(Request request) {

        for (entry in handlers) {
            if (request.path ==~ entry.pattern) {
                return entry.handler(request)
            }
        }

        if (fallbackHandler != null) {
            return fallbackHandler(request)
        }

        return new Response(404)
    }
}
