package cz.auderis.corusco.core.lifecycle;

enum NoOperation implements Subscription, Detachable, Disposable {

    INSTANCE;

    @Override
    public void detach() {
        // no operation
    }

    @Override
    public void close() {
        // no operation
    }

}
