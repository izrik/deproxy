package org.rackspace.deproxy

import spock.lang.Specification

class FilterByPathHandlerTest extends Specification {

    def "A single handler defined for a simple path should be triggered when that path is requested"() {

        given:
        boolean handled = false
        FilterByPathHandler handler = new FilterByPathHandler([
                '/path/to/resource' : { request -> handled = true; return new Response(200) }
        ])

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to/resource'))

        then:
        handled == true
        response.code == "200"
    }

    def "When a different path is requested, the handler shouldn't be triggered"() {

        given:
        boolean handled = false
        FilterByPathHandler handler = new FilterByPathHandler([
                '/path/to/resource' : { request -> handled = true; return new Response(200) }
        ])

        when:
        def response = handler.handleRequest(new Request('GET', '/something/else'))

        then:
        handled == false
        response.code == "404"
    }

    def "Slashes matter"() {

        given:
        boolean handled = false
        FilterByPathHandler handler = new FilterByPathHandler([
                '/path/to/resource' : { request -> handled = true; return new Response(200) }
        ])

        when:
        def response = handler.handleRequest(new Request('GET', 'path/to/resource'))

        then:
        handled == false
        response.code == "404"

        when:
        response = handler.handleRequest(new Request('GET', '/path/to/resource/'))

        then:
        handled == false
        response.code == "404"
    }

    def "The path should match exactly"() {

        given:
        boolean handled = false
        FilterByPathHandler handler = new FilterByPathHandler([
                '/path/to/resource' : { request -> handled = true; return new Response(200) }
        ])

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to/resourcestuff'))

        then:
        handled == false
        response.code == "404"

        when:
        response = handler.handleRequest(new Request('GET', '/path/to/resource/stuff'))

        then:
        handled == false
        response.code == "404"

        when:
        response = handler.handleRequest(new Request('GET', '/path/to'))

        then:
        handled == false
        response.code == "404"
    }

    def "Regex is good, too"() {

        given:
        boolean handled = false
        FilterByPathHandler handler = new FilterByPathHandler([
                (~/\/path\/\w+\/resource/) : { request -> handled = true; return new Response(200) }
        ])

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to/resource'))

        then:
        handled == true
        response.code == "200"

        when:
        handled = false
        response = handler.handleRequest(new Request('GET', '/path/for/resource'))

        then:
        handled == true
        response.code == "200"
    }

    def "Two separate handlers get triggered separately"() {

        given:
        boolean gotResource1 = false
        boolean gotResource2 = false
        FilterByPathHandler handler = new FilterByPathHandler([
                '/resource1' : { request -> gotResource1 = true; return new Response(200) },
                '/resource2' : { request -> gotResource2 = true; return new Response(200) }
        ])

        when:
        def response = handler.handleRequest(new Request('GET', '/resource1'))

        then:
        gotResource1 == true
        gotResource2 == false
        response.code == "200"

        when:
        gotResource1 = false
        response = handler.handleRequest(new Request('GET', '/resource2'))

        then:
        gotResource1 == false
        gotResource2 == true
        response.code == "200"
    }

    def "List syntax"() {

        given:
        boolean handled = false
        FilterByPathHandler handler = new FilterByPathHandler([
                ['/path/to/resource', { request -> handled = true; return new Response(200) }]
        ])

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to/resource'))

        then:
        handled == true
        response.code == "200"
    }

    def "With list syntax, the first handler that matches gets triggered, and no others"() {

        given:
        boolean handled1 = false
        boolean handled2 = false
        FilterByPathHandler handler = new FilterByPathHandler([
                ['/path/to/resource', { request -> handled1 = true; return new Response(200) }],
                ['/path/to/resource', { request -> handled2 = true; return new Response(200) }]
        ])

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to/resource'))

        then:
        handled1 == true
        handled2 == false
        response.code == "200"
    }

    def "If nothing matches, then the fallbackHandler is used."() {

        given:
        FilterByPathHandler handler = new FilterByPathHandler(
                null,
                { request -> return new Response(606) }
        )

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to/resource'))

        then:
        response.code == "606"
    }

    def "If exactMatch is false, then a handler should be triggered when its string/pattern matches any part of the incoming request path"() {

        given:
        boolean handled = false
        FilterByPathHandler handler = FilterByPathHandler.ByName(
                handlers: [ '/path/to/resource' : { request -> handled = true; return new Response(200) } ],
                exactMatch: false
        )

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to/resource/stuff'))

        then:
        handled == true
        response.code == "200"

        when:
        handled = false
        response = handler.handleRequest(new Request('GET', '/some/path/to/resource'))

        then:
        handled == true
        response.code == "200"

        when:
        handled = false
        response = handler.handleRequest(new Request('GET', '/some/path/to/resource/stuff'))

        then:
        handled == true
        response.code == "200"

        when:
        handled = false
        response = handler.handleRequest(new Request('GET', '/path/to'))

        then:
        handled == false
        response.code == "404"
    }

    def "If exactMatch is false, then a handler should not be triggered when its string/pattern is larger than the incoming request path"() {

        given:
        boolean handled = false
        FilterByPathHandler handler = FilterByPathHandler.ByName(
                handlers: [ '/path/to/resource' : { request -> handled = true; return new Response(200) } ],
                exactMatch: false
        )

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to'))

        then:
        handled == false
        response.code == "404"
    }

    def "Multiple handlers can be chained"() {

        given:
        boolean handled = false
        FilterByPathHandler childHandler = new FilterByPathHandler([
                '/path/to/resource' : { request -> handled = true; return new Response(200) }
        ])
        FilterByPathHandler handler = FilterByPathHandler.ByName(
                handlers: [ (~/^\/path\b/): childHandler.&handleRequest ],
                exactMatch: false
        )

        when:
        def response = handler.handleRequest(new Request('GET', '/path/to/resource'))

        then:
        handled == true
        response.code == "200"
    }

}
