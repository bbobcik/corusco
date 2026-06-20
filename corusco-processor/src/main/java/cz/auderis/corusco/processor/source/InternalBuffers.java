package cz.auderis.corusco.processor.source;

import java.util.List;
import java.util.stream.Stream;

record InternalBuffers(
        List<StringBuilder> localBuffers,
        List<StringBuilder> allBuffers
) {

    Stream<StringBuilder> localBuffersStream() {
        return localBuffers.stream();
    }

    Stream<StringBuilder> allBuffersStream() {
        return allBuffers.stream();
    }
}
