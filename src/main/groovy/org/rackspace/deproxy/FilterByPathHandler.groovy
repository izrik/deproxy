package org.rackspace.deproxy

import com.sun.jdi.connect.IllegalConnectorArgumentsException

import java.util.regex.Pattern

class FilterByPathHandler {

    FilterByPathHandler(handlers=null, Closure<Response> fallbackHandler=null, exactMatch=true) {
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
        this.exactMatch = exactMatch
    }

    static FilterByPathHandler ByName(Map params) {
        def handlers = params?.handlers
        Closure<Response> fallbackHandler = params?.fallbackHandler as Closure<Response>
        boolean exactMatch = (params?.exactMatch == true)

        return new FilterByPathHandler(handlers, fallbackHandler, exactMatch)
    }
    static FilterByPathHandler ByName(Closure<Response> fallbackHandler, handlers, boolean exactMatch) {
        // this overload is solely for the purpose of providing parameter names in auto-complete
        return ByName(handlers: handlers, fallbackHandler: fallbackHandler, exactMatch: exactMatch)
    }

    class Entry {
        Pattern pattern
        Closure<Response> handler
    }

    List<Entry> handlers = [] as List<Entry>
    Closure<Response> fallbackHandler
    boolean exactMatch

    Response handleRequest(Request request) {

        for (entry in handlers) {
            boolean trigger
            if (exactMatch) {
                trigger = (request.path ==~ entry.pattern)
            } else {
                trigger = entry.pattern.matcher(request.path).find()
            }

            if (trigger) {
                return entry.handler(request)
            }
        }

        if (fallbackHandler != null) {
            return fallbackHandler(request)
        }

        return new Response(404)
    }
}
