# kotlin-querybean-generator
Annotation processor for generating Kotlin query beans for type safe query construction.

For Kotlin users these are preferred over Java query beans in that they use Kotlin properties 
rather than Java public fields. This limits the query bean enhancement to just the beans themselves
where as with java query beans we need to enhance callers (as we are effectively simulating 'properties'
via java public fields and enhancement).

Refer to the documentation at: https://ebean.io/docs/query/query-beans
