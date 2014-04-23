package org.rackspace.deproxy

class FilterByMethodHandler {

    public FilterByMethodHandler(Map params) {
        this()
    }
    public FilterByMethodHandler(Closure<Response> GETHandler=null,
                                 Closure<Response> HEADHandler=null,
                                 Closure<Response> POSTHandler=null,
                                 Closure<Response> PUTHandler=null,
                                 Closure<Response> DELETEHandler=null,
                                 Closure<Response> TRACEHandler=null,
                                 Closure<Response> OPTIONSHandler=null,
                                 Closure<Response> CONNECTHandler=null,
                                 Closure<Response> PATCHHandler=null,
                                 Map<String, Closure<Response>> extraHandlersByMethod=null,
                                 Closure<Response> fallbackHandler=null) {

        this.GETHandler = GETHandler
        this.HEADHandler = HEADHandler
        this.POSTHandler = POSTHandler
        this.PUTHandler = PUTHandler
        this.DELETEHandler = DELETEHandler
        this.TRACEHandler = TRACEHandler
        this.OPTIONSHandler = OPTIONSHandler
        this.CONNECTHandler = CONNECTHandler
        this.PATCHHandler = PATCHHandler

        this.extraHandlersByMethod = [:]
        if (extraHandlersByMethod != null) {
            for (key in extraHandlersByMethod.keySet()) {
                this.extraHandlersByMethod[key] = extraHandlersByMethod[key]
            }
        }

        this.fallbackHandler = fallbackHandler
    }

    Closure<Response> GETHandler;
    Closure<Response> HEADHandler;
    Closure<Response> POSTHandler;
    Closure<Response> PUTHandler;
    Closure<Response> DELETEHandler;
    Closure<Response> TRACEHandler;
    Closure<Response> OPTIONSHandler;
    Closure<Response> CONNECTHandler;
    Closure<Response> PATCHHandler;

    Map<String, Closure<Response>> extraHandlersByMethod
    Closure<Response> fallbackHandler

    public Response handleRequest(Request request) {

        if (request.method == 'GET' && GETHandler != null) {
            return GETHandler(request)
        }

        if (request.method == 'HEAD' && HEADHandler != null) {
            return HEADHandler(request)
        }

        if (request.method == 'POST' && POSTHandler != null) {
            return POSTHandler(request)
        }

        if (request.method == 'PUT' && PUTHandler != null) {
            return PUTHandler(request)
        }

        if (request.method == 'DELETE' && DELETEHandler != null) {
            return DELETEHandler(request)
        }

        if (request.method == 'TRACE' && TRACEHandler != null) {
            return TRACEHandler(request)
        }

        if (request.method == 'OPTIONS' && OPTIONSHandler != null) {
            return OPTIONSHandler(request)
        }

        if (request.method == 'CONNECT' && CONNECTHandler != null) {
            return CONNECTHandler(request)
        }

        if (request.method == 'PATCH' && PATCHHandler != null) {
            return PATCHHandler(request)
        }

        if (extraHandlersByMethod.containsKey(request.method)) {
            return extraHandlersByMethod[request.method](request)
        }

        if (fallbackHandler != null) {
            return fallbackHandler(request)
        }

        return new Response(405);
    }
}
