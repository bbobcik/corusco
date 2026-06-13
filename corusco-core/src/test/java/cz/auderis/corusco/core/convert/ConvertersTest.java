package cz.auderis.corusco.core.convert;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConvertersTest {

    @Test
    void stringConverterPreservesEmptyText() {
        StringConverter<String> converter = Converters.string();

        assertThat(converter.parse("")).isEqualTo(ParseResult.success(""));
        assertThat(converter.format(null)).isEmpty();
    }

    @Test
    void integerConverterParsesAndRejectsInvalidText() {
        StringConverter<Integer> converter = Converters.integer();

        assertThat(converter.parse("42")).isEqualTo(ParseResult.success(42));
        assertThat(converter.parse("x")).isInstanceOf(ParseResult.Failure.class);
        assertThat(converter.parse("")).isEqualTo(ParseResult.failure("Value is required"));
    }

    @Test
    void emptyTextCanParseToNull() {
        StringConverter<BigDecimal> converter = Converters.bigDecimal(EmptyTextPolicy.NULL_VALUE);

        assertThat(converter.parse("")).isEqualTo(ParseResult.success(null));
        assertThat(converter.parse("12.50")).isEqualTo(ParseResult.success(new BigDecimal("12.50")));
    }

    @Test
    void localDateConverterUsesIsoDates() {
        StringConverter<LocalDate> converter = Converters.localDate(EmptyTextPolicy.REJECT);

        assertThat(converter.parse("2026-06-13")).isEqualTo(ParseResult.success(LocalDate.of(2026, 6, 13)));
        assertThat(converter.parse("13.06.2026")).isInstanceOf(ParseResult.Failure.class);
    }

    @Test
    void enumConverterUsesConstantNames() {
        StringConverter<CustomerState> converter = Converters.enumValue(CustomerState.class, EmptyTextPolicy.REJECT);

        assertThat(converter.parse("ACTIVE")).isEqualTo(ParseResult.success(CustomerState.ACTIVE));
        assertThat(converter.parse("active")).isInstanceOf(ParseResult.Failure.class);
    }

    private enum CustomerState {
        ACTIVE,
        BLOCKED
    }
}
