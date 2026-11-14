package cz.auderis.corusco.core.form;

import cz.auderis.corusco.core.convert.Converters;
import cz.auderis.corusco.core.convert.EmptyTextPolicy;
import cz.auderis.corusco.core.convert.ParseResult;
import cz.auderis.corusco.core.convert.StringConverter;
import cz.auderis.corusco.core.key.TextFieldKey;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservableFormModelTest {
    private static final TextFieldKey<Example, BigDecimal> NUMBER =
            TextFieldKey.of("example/number", Example.class, BigDecimal.class);

    @Test void invalidEditPublishesCompletedDirtyStateAndRetainsSemanticValue() {
        Example form = new Example();
        var events = new ArrayList<FormState>();
        form.state().subscribe(event -> events.add(event.newValue()));
        form.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().dirty()).isTrue();
        assertThat(events.getFirst().committable()).isFalse();
        assertThat(form.number.isDirty()).isFalse();
        assertThat(form.number.value()).isEqualByComparingTo("10");
        form.acceptCurrentValues();
        assertThat(form.state().value().dirty()).isTrue();
    }

    @Test void rawTextReentryPublishesOnlyCompletedStates() {
        Example form = new Example();
        var observed = new ArrayList<String>();
        form.state().subscribe(event -> observed.add(form.number.rawText().value() + ":" + event.newValue().committable()));
        form.number.rawText().subscribe(event -> {
            if ("bad".equals(event.newValue())) form.number.setRawText("20", StandardChangeOrigin.USER);
        });
        form.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(observed).containsExactly("bad:false", "20:true");
    }

    @Test void firstStateAccessInsideRawListenerKeepsReentrantEditSerialized() {
        Example form = new Example();
        var observed = new ArrayList<String>();
        form.number.rawText().subscribe(event -> {
            if ("bad".equals(event.newValue())) {
                form.state().subscribe(change -> observed.add(form.number.rawText().value() + ":" + change.newValue().committable()));
                form.number.setRawText("20", StandardChangeOrigin.USER);
            }
        });
        form.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(observed).containsExactly("bad:false", "20:true");
        assertThat(form.number.parseState().value()).isInstanceOf(ParseState.Parsed.class);
    }

    @Test void firstCompositeStateAccessInsideChildListenerKeepsRoutingStable() {
        Example form = new Example();
        Composite parent = new Composite(form);
        form.number.rawText().subscribe(event -> {
            if ("bad".equals(event.newValue())) {
                parent.state();
                form.number.setRawText("20", StandardChangeOrigin.USER);
            }
        });
        form.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(form.number.parseState().value()).isInstanceOf(ParseState.Parsed.class);
        assertThat(parent.state().value().committable()).isTrue();
        parent.close();
    }

    @Test void constructionDuringChildMutationFailsWithoutClaimingChild() {
        Example form = new Example();
        form.number.rawText().subscribe(event -> assertThatThrownBy(() -> new Composite(form))
                .isInstanceOf(IllegalStateException.class));
        form.number.setRawText("bad", StandardChangeOrigin.USER);
        Composite parent = new Composite(form);
        parent.close();
    }

    @Test void repeatedTextPublishesWhenConverterResultChanges() {
        var converter = new MutableConverter();
        var form = new MutableForm(converter);
        var events = new ArrayList<FormState>();
        form.state().subscribe(event -> events.add(event.newValue()));
        form.number.setRawText("same", StandardChangeOrigin.USER);
        converter.fail = true;
        form.number.setRawText("same", StandardChangeOrigin.USER);
        assertThat(events).hasSize(2);
        assertThat(events.getLast().committable()).isFalse();
    }

    @Test void rejectsSharedOrDisposedChildrenAndNeverAttachesAfterClose() {
        Example child = new Example();
        Composite first = new Composite(child);
        first.state();
        assertThatThrownBy(() -> new Composite(child)).isInstanceOf(IllegalStateException.class);
        first.close();
        Composite second = new Composite(child);
        second.close();
        assertThatThrownBy(second::state).isInstanceOf(IllegalStateException.class);
        child.close();
        assertThatThrownBy(() -> new Composite(child)).isInstanceOf(IllegalStateException.class);
    }

    @Test void unrelatedFormEditRemainsSynchronousDuringListener() {
        Example first = new Example();
        Example second = new Example();
        first.state(); second.state();
        first.number.rawText().subscribe(event -> {
            second.number.setRawText("20", StandardChangeOrigin.USER);
            assertThat(second.number.value()).isEqualByComparingTo("20");
        });
        first.number.setRawText("11", StandardChangeOrigin.USER);
    }

    @Test void compositeCoalescesResetAndPreservesChildOverrides() {
        Example first = new Example();
        Example second = new Example();
        Composite parent = new Composite(first, second);
        var events = new ArrayList<FormState>();
        parent.state().subscribe(event -> events.add(event.newValue()));
        first.number.setRawText("11", StandardChangeOrigin.USER);
        second.number.setRawText("12", StandardChangeOrigin.USER);
        events.clear();
        parent.reset();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().dirty()).isFalse();
        assertThat(first.resetCalls).isEqualTo(1);
        assertThat(second.resetCalls).isEqualTo(1);
        parent.close();
        events.clear();
        first.number.setRawText("13", StandardChangeOrigin.USER);
        assertThat(events).isEmpty();
    }

    @Test void nestedCompositeSerializesAggregateReentryAndCoalescesReset() {
        Example first = new Example();
        Example second = new Example();
        Composite inner = new Composite(first, second);
        Composite outer = new Composite(inner);
        var states = new ArrayList<Boolean>();
        outer.state().subscribe(event -> {
            states.add(event.newValue().committable());
            if (!event.newValue().committable()) second.number.setRawText("22", StandardChangeOrigin.USER);
        });
        first.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(states).containsExactly(false, false);
        assertThat(second.number.value()).isEqualByComparingTo("22");
        states.clear();
        outer.reset();
        assertThat(states).containsExactly(true);
        outer.close(); inner.close();
    }

    @Test void typedProblemProjectionNotifiesOnlyWhenContentsChange() {
        Example form = new Example();
        var projection = form.fieldProblems(NUMBER.asFieldKey());
        var counts = new ArrayList<Integer>();
        var subscription = projection.subscribe(event -> counts.add(event.newValue().size()));
        form.number.setRawText("bad", StandardChangeOrigin.USER);
        form.number.setRawText("worse", StandardChangeOrigin.USER);
        form.number.setRawText("10", StandardChangeOrigin.USER);
        subscription.close();
        form.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(counts).containsExactly(1, 0);
    }

    @Test void closingCompositeRestoresIndependentChildRouting() {
        Example first = new Example();
        Example second = new Example();
        Composite parent = new Composite(first, second);
        parent.state();
        parent.close();
        first.number.rawText().subscribe(event -> {
            second.number.setRawText("30", StandardChangeOrigin.USER);
            assertThat(second.number.value()).isEqualByComparingTo("30");
        });
        first.number.setRawText("11", StandardChangeOrigin.USER);
    }

    @Test void closingParentDuringRawCallbackDefersDetachUntilReentrantEditCompletes() {
        Example child = new Example();
        Composite parent = new Composite(child);
        parent.state();
        child.number.rawText().subscribe(event -> {
            if ("bad".equals(event.newValue())) {
                parent.close();
                child.number.setRawText("20", StandardChangeOrigin.USER);
            }
        });
        child.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(child.number.value()).isEqualByComparingTo("20");
        assertThat(child.number.parseState().value()).isInstanceOf(ParseState.Parsed.class);
    }

    @Test void attachedChildCannotCloseBeforeItsParent() {
        Example leaf = new Example();
        Composite inner = new Composite(leaf);
        Composite outer = new Composite(inner);
        outer.state();
        assertThatThrownBy(inner::close).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(leaf::close).isInstanceOf(IllegalStateException.class);
        leaf.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(outer.state().value().committable()).isFalse();
        outer.close(); inner.close(); leaf.close();
    }

    @Test void parentThenChildCanCloseInsideEditWithoutReleasingRoutingEarly() {
        Example child = new Example();
        Composite parent = new Composite(child);
        parent.state();
        child.number.rawText().subscribe(event -> {
            parent.close();
            assertThatThrownBy(() -> new Composite(child)).isInstanceOf(IllegalStateException.class);
            child.close();
        });
        child.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(child.number.rawText().value()).isEqualTo("bad");
        assertThat(child.number.parseState().value()).isInstanceOf(ParseState.Failed.class);
    }

    @Test void nestedParentsCanCloseInOrderInsideLeafEdit() {
        Example leaf = new Example();
        Composite inner = new Composite(leaf);
        Composite outer = new Composite(inner);
        outer.state();
        leaf.number.rawText().subscribe(event -> {
            outer.close();
            inner.close();
            leaf.close();
        });
        leaf.number.setRawText("bad", StandardChangeOrigin.USER);
        assertThat(leaf.number.parseState().value()).isInstanceOf(ParseState.Failed.class);
    }

    private static final class Example extends AbstractObservableFormModel<BigDecimal> {
        int resetCalls;
        final TextFieldModel<Example, BigDecimal> number = register(new TextFieldModel<>(
                NUMBER, BigDecimal.TEN, Converters.bigDecimal(EmptyTextPolicy.REJECT)));
        @Override protected BigDecimal createResult() { return number.value(); }
        @Override public void reset() { resetCalls++; super.reset(); }
    }

    private static final class Composite extends AbstractObservableCompositeFormModel<String> {
        Composite(ObservableFormModel<?>... children) { super(children); }
        @Override protected String createResult() { return "ok"; }
    }

    private static final class MutableConverter implements StringConverter<BigDecimal> {
        boolean fail;
        public Class<BigDecimal> valueType() { return BigDecimal.class; }
        public ParseResult<BigDecimal> parse(String text) { return fail ? ParseResult.failure("unavailable") : ParseResult.success(BigDecimal.TEN); }
        public String format(BigDecimal value) { return value.toString(); }
    }
    private static final class MutableForm extends AbstractObservableFormModel<BigDecimal> {
        final TextFieldModel<Example, BigDecimal> number;
        MutableForm(MutableConverter converter) { number = register(new TextFieldModel<>(NUMBER, BigDecimal.TEN, converter)); }
        protected BigDecimal createResult() { return number.value(); }
    }
}
