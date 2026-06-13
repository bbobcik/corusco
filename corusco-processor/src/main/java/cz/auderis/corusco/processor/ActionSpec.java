package cz.auderis.corusco.processor;

final class ActionSpec {

    final String constantName;
    final String id;
    final String textId;
    final String tooltipId;
    final int mnemonic;
    final int acceleratorKey;
    final int acceleratorModifiers;
    final boolean selectable;

    ActionSpec(
            String constantName,
            String id,
            String textId,
            String tooltipId,
            int mnemonic,
            int acceleratorKey,
            int acceleratorModifiers,
            boolean selectable
    ) {
        this.constantName = constantName;
        this.id = id;
        this.textId = textId;
        this.tooltipId = tooltipId;
        this.mnemonic = mnemonic;
        this.acceleratorKey = acceleratorKey;
        this.acceleratorModifiers = acceleratorModifiers;
        this.selectable = selectable;
    }
}
