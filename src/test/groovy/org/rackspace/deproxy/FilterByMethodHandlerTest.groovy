package org.rackspace.deproxy

import spock.lang.Specification

class FilterByMethodHandlerTest extends Specification {

    def "Single named handler should be triggered on its method"() {

        given:
        boolean handled = false
        FilterByMethodHandler handler = new FilterByMethodHandler(
                GETHandler: { request -> handled = true; return new Response(200) }
        )

        when:
        def response = handler.handleRequest(new Request('GET', '/'))

        then:
        handled == true
        response.code == 200
    }

    def "Single named handler should not be triggered on a different method"() {

        given:
        boolean handled = false
        FilterByMethodHandler handler = new FilterByMethodHandler(
                GETHandler: { request -> handled = true; return new Response(200) }
        )

        when:
        def response = handler.handleRequest(new Request('POST', '/'))

        then:
        handled == false
        response.code == 405
    }

    def "Multiple named handlers - only one should be triggered"() {

        given:
        boolean gotGet = false
        boolean gotPost = true
        FilterByMethodHandler handler = new FilterByMethodHandler(
                GETHandler: { request -> gotGet = true; return new Response(200) },
                POSTHandler: { request -> gotPost = true; return new Response(201) }
        )

        when:
        def response = handler.handleRequest(new Request('POST', '/'))

        then:
        gotGet == false
        gotPost == true
        response.code == 405
    }

    def "Define handlers for non-standard method names"() {

        given:
        boolean handled = true
        FilterByMethodHandler handler = new FilterByMethodHandler(
                extraHandlersByMethod: [
                    'QWERTY': { request -> handled = true; return new Response(200) }
                ]
        )

        when:
        def response = handler.handleRequest(new Request('QWERTY', '/'))

        then:
        handled == true
        response.code == 200
    }

    def "Handlers for non-standard methods are not triggered by standard methods"() {

        given:
        boolean handled = true
        FilterByMethodHandler handler = new FilterByMethodHandler(
                extraHandlersByMethod: [
                        'QWERTY': { request -> handled = true; return new Response(200) }
                ]
        )

        when:
        def response = handler.handleRequest(new Request('GET', '/'))

        then:
        handled == false
        response.code == 405
    }

    def "Standard method names in extraHandlersByMethod are ignored if the named handler is available"() {

        given:
        boolean handled = true
        FilterByMethodHandler handler = new FilterByMethodHandler(
                GETHandler: { request -> return new Response(404) },
                extraHandlersByMethod: [
                        'GET': { request -> handled = true; return new Response(200) }
                ]
        )

        when:
        def response = handler.handleRequest(new Request('GET', '/'))

        then:
        handled == false
        response.code == 404
    }

    def "If nothing matches, then the nextHandler is used."() {

        given:
        def customHandler = { request -> return new Response(606) }
        FilterByMethodHandler handler = new FilterByMethodHandler(
                nextHandler: customHandler
        )

        when:
        def response = handler.handleRequest(new Request('GET', '/'))

        then:
        response.code == 606
    }
}
