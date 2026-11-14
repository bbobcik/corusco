package cz.auderis.corusco.core.convert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConvertersTest {

    @Test
    void localizedNumbersAreStrictAndExact() {
        var us = Converters.bigDecimal(EmptyTextPolicy.REJECT, Locale.US);
        var cz = Converters.bigDecimal(EmptyTextPolicy.REJECT, Locale.forLanguageTag("cs-CZ"));
        assertThat(us.parse("1,234.56789")).isEqualTo(ParseResult.success(new BigDecimal("1234.56789")));
        assertThat(cz.parse("1\u00a0234,56789")).isEqualTo(ParseResult.success(new BigDecimal("1234.56789")));
        assertThat(cz.parse("1 234,5")).isInstanceOf(ParseResult.Failure.class);
        assertThat(cz.parse("12\u00a034,5")).isInstanceOf(ParseResult.Failure.class);
        for (String invalid : new String[]{"1,23", "1.2x", "NaN", "∞", " 1", "1 "}) {
            assertThat(us.parse(invalid)).as(invalid).isInstanceOf(ParseResult.Failure.class);
        }
        var precise = new BigDecimal("123456789012345678901234567890.12345678901234567890");
        assertThat(us.parse(us.format(precise))).isEqualTo(ParseResult.success(precise));
        var ints = Converters.integer(EmptyTextPolicy.REJECT, Locale.US);
        var longs = Converters.longValue(EmptyTextPolicy.REJECT, Locale.US);
        assertThat(ints.parse("2,147,483,647")).isEqualTo(ParseResult.success(Integer.MAX_VALUE));
        assertThat(ints.parse("2,147,483,648")).isInstanceOf(ParseResult.Failure.class);
        assertThat(ints.parse("-2,147,483,648")).isEqualTo(ParseResult.success(Integer.MIN_VALUE));
        assertThat(ints.parse("-2,147,483,649")).isInstanceOf(ParseResult.Failure.class);
        assertThat(ints.parse("1.1")).isInstanceOf(ParseResult.Failure.class);
        assertThat(ints.parse("1.0")).isEqualTo(ParseResult.success(1));
        assertThat(longs.parse("-9,223,372,036,854,775,808")).isEqualTo(ParseResult.success(Long.MIN_VALUE));
        assertThat(longs.parse("9,223,372,036,854,775,808")).isInstanceOf(ParseResult.Failure.class);
        assertThat(longs.parse("9,223,372,036,854,775,807")).isEqualTo(ParseResult.success(Long.MAX_VALUE));
        assertThat(longs.parse("-9,223,372,036,854,775,809")).isInstanceOf(ParseResult.Failure.class);
    }

    @Test
    void localizedDatesAreStrictAndRoundTripYears() {
        for (Locale locale : new Locale[]{Locale.US, Locale.forLanguageTag("cs-CZ")}) {
            var converter = Converters.localDate(EmptyTextPolicy.NULL_VALUE, locale);
            for (LocalDate date : new LocalDate[]{LocalDate.of(2024, 2, 29), LocalDate.of(99, 1, 2), LocalDate.of(2099, 12, 31)}) {
                assertThat(converter.parse(converter.format(date))).as(locale + " " + date).isEqualTo(ParseResult.success(date));
            }
            assertThat(converter.parse(converter.format(LocalDate.of(2024, 2, 29)) + "x")).isInstanceOf(ParseResult.Failure.class);
            assertThat(converter.parse("")).isEqualTo(ParseResult.success(null));
        }
        assertThat(Converters.localDate(EmptyTextPolicy.REJECT, Locale.US).parse("2/29/2023")).isInstanceOf(ParseResult.Failure.class);
        assertThat(Converters.localDate(EmptyTextPolicy.REJECT, Locale.US).parse("2/29/24")).isInstanceOf(ParseResult.Failure.class);
    }

    @Test
    void localizedDateUsesLocaleDigits() {
        var converter = Converters.localDate(EmptyTextPolicy.REJECT, Locale.forLanguageTag("ar-EG"));
        LocalDate date = LocalDate.of(2024, 2, 29);
        assertThat(converter.format(date)).contains("٢٠٢٤");
        assertThat(converter.parse(converter.format(date))).isEqualTo(ParseResult.success(date));
    }

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
