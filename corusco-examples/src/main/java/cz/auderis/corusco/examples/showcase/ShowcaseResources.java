package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.resource.MapResources;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.examples.generated.GeneratedCustomerRowTableResources;
import java.util.Map;

final class ShowcaseResources {

    private ShowcaseResources() {
    }

    static Resources create() {
        return MapResources.of(Map.ofEntries(
                Map.entry(ShowcasePresenterActions.SAVE_TEXT.id(), "Save"),
                Map.entry(ShowcasePresenterActions.SAVE_TOOLTIP.id(), "Commit current form values and accept a new baseline."),
                Map.entry(ShowcasePresenterActions.RESET_TEXT.id(), "Reset"),
                Map.entry(ShowcasePresenterActions.RESET_TOOLTIP.id(), "Restore the generated form model to its baseline."),
                Map.entry(ShowcasePresenterActions.RELOAD_TEXT.id(), "Reload H2"),
                Map.entry(ShowcasePresenterActions.RELOAD_TOOLTIP.id(), "Reload the 100K-row time-series table from H2."),
                Map.entry(ShowcasePresenterActions.ADD_EVENT_TEXT.id(), "Add Event"),
                Map.entry(ShowcasePresenterActions.ADD_EVENT_TOOLTIP.id(), "Append a synthetic event through the observable row source."),
                Map.entry(ShowcasePresenterActions.TOGGLE_OPTIMIZED_RENDERERS_TEXT.id(), "Optimized Renderers"),
                Map.entry(ShowcasePresenterActions.TOGGLE_OPTIMIZED_RENDERERS_TOOLTIP.id(), "Toggle cached timestamp and state renderers."),
                Map.entry(ShowcasePresenterActions.OPEN_DOCS_TEXT.id(), "Docs"),
                Map.entry(ShowcasePresenterActions.OPEN_DOCS_TOOLTIP.id(), "Open the Corusco project page."),

                Map.entry(ShowcaseCustomerEditResources.NAME_LABEL.id(), "Customer name"),
                Map.entry(ShowcaseCustomerEditResources.NAME_TOOLTIP.id(), "Required, letters and spaces only."),
                Map.entry(ShowcaseCustomerEditResources.CREDIT_LIMIT_LABEL.id(), "Credit limit"),
                Map.entry(ShowcaseCustomerEditResources.CREDIT_LIMIT_TOOLTIP.id(), "Allowed range: 0.00 to 500,000.00."),
                Map.entry(ShowcaseCustomerEditResources.AGE_LABEL.id(), "Age"),
                Map.entry(ShowcaseCustomerEditResources.VALID_FROM_LABEL.id(), "Valid from"),
                Map.entry(ShowcaseCustomerEditResources.TYPE_LABEL.id(), "Customer type"),
                Map.entry(ShowcaseCustomerEditResources.ACTIVE_LABEL.id(), "Active customer"),

                Map.entry(TimeseriesObservationTableResources.SEQUENCE_HEADER.id(), "Seq"),
                Map.entry(TimeseriesObservationTableResources.TIMESTAMP_MILLIS_HEADER.id(), "Timestamp"),
                Map.entry(TimeseriesObservationTableResources.TIMESTAMP_MILLIS_TOOLTIP.id(), "Epoch milliseconds formatted in the active zone."),
                Map.entry(TimeseriesObservationTableResources.SYMBOL_HEADER.id(), "Symbol"),
                Map.entry(TimeseriesObservationTableResources.VENUE_HEADER.id(), "Venue"),
                Map.entry(TimeseriesObservationTableResources.REGION_HEADER.id(), "Region"),
                Map.entry(TimeseriesObservationTableResources.CHANNEL_HEADER.id(), "Channel"),
                Map.entry(TimeseriesObservationTableResources.STATE_HEADER.id(), "State"),
                Map.entry(TimeseriesObservationTableResources.STATE_TOOLTIP.id(), "Finite state renderer with bounded visual cache."),
                Map.entry(TimeseriesObservationTableResources.BID_HEADER.id(), "Bid"),
                Map.entry(TimeseriesObservationTableResources.ASK_HEADER.id(), "Ask"),
                Map.entry(TimeseriesObservationTableResources.LAST_PRICE_HEADER.id(), "Last"),
                Map.entry(TimeseriesObservationTableResources.VOLUME_HEADER.id(), "Volume"),
                Map.entry(TimeseriesObservationTableResources.NOTIONAL_HEADER.id(), "Notional"),
                Map.entry(TimeseriesObservationTableResources.LATENCY_MILLIS_HEADER.id(), "Latency ms"),

                Map.entry(AuditEventTableResources.TIMESTAMP_MILLIS_HEADER.id(), "Time"),
                Map.entry(AuditEventTableResources.TIMESTAMP_MILLIS_TOOLTIP.id(), "Event epoch formatted by the timestamp renderer."),
                Map.entry(AuditEventTableResources.STATE_HEADER.id(), "State"),
                Map.entry(AuditEventTableResources.STATE_TOOLTIP.id(), "Current operational state."),
                Map.entry(AuditEventTableResources.MESSAGE_HEADER.id(), "Message"),

                Map.entry(GeneratedCustomerRowTableResources.NAME_HEADER.id(), "Customer"),
                Map.entry(GeneratedCustomerRowTableResources.NAME_TOOLTIP.id(), "Editable generated column backed by a record updater."),
                Map.entry(GeneratedCustomerRowTableResources.ORDERS_HEADER.id(), "Orders")
        ));
    }

    static String resolve(Resources resources, ResourceKey<String> key) {
        return resources.find(key).orElse(key.id());
    }
}
