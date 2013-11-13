package org.rackspace.deproxy

class MediaType {

    public static final MediaType StarStar = new MediaType("*", "*")

    final String type
    final String subtype
//    final Parameter[] parameters

    public MediaType(String type, String subtype="*") {
        this.type = type
        this.subtype = subtype
    }

    String toString() {
        return "${type}/${subtype}"
    }
}
