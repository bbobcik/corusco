package cz.auderis.corusco.processor.source;

public interface StructuredSourceFragment extends SourceFragment {

    SectionId getDefaultSection();
    SectionId addSection(String name);
    void clearSection(SectionId section);

    void addFragment(SourceFragment fragment);
    void addFragment(SectionId section, SourceFragment fragment);

    default void addFragment(CharSequence text) {
        addFragment(new SimpleMutableFragment().append(text));
    }

    default void addFragment(SectionId section, CharSequence text) {
        addFragment(section, new SimpleMutableFragment().append(text));
    }

    String headerPart();
    void headerPart(CharSequence headerPart);

    String trailerPart();
    void trailerPart(CharSequence trailerPart);

}
