package org.rackspace.deproxy

class AcceptHeader extends Header {

    // http://tools.ietf.org/html/rfc2616#section-14.1
    //
    //    Accept         = "Accept" ":"
    //    #( media-range [ accept-params ] )
    //
    //    media-range    = ( "*/*"
    //                     | ( type "/" "*" )
    //                     | ( type "/" subtype )
    //                     ) *( ";" parameter )
    //    accept-params  = ";" "q" "=" qvalue *( accept-extension )
    //    accept-extension = ";" token [ "=" ( token | quoted-string ) ]

    // #section-3.6
    //    parameter               = attribute "=" value
    //    attribute               = token
    //    value                   = token | quoted-string

    public AcceptHeader(MediaType... mediaRanges) {
        super("Accept", mediaRanges*.toString().join(', '))

        this.mediaRanges = mediaRanges
    }

    final MediaType[] mediaRanges
}
