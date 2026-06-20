package cz.auderis.corusco.processor.source;

import java.util.ArrayList;
import java.util.List;

record FragmentGroup (
        List<SourceFragment> fragments
) {

    FragmentGroup() {
        this(new ArrayList<>(1));
    }

}
