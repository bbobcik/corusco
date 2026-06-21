package cz.auderis.corusco.core.value;

public enum StandardChangeOrigin implements ChangeOrigin {

    USER,

    MODEL,

    BINDING,

    SYSTEM,

    GENERATED,

    ;

    @Override
    public String id() {
        return name();
    }

}
