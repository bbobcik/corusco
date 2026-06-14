package cz.auderis.corusco.examples.showcase;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoruscoShowcaseApplicationTest {

    @Test
    void showcaseScenarioExercisesGeneratedActionsAndH2Tables() {
        var result = CoruscoShowcaseApplication.runScenario();
        assertThat(result).contains(
                "generatedRows=3",
                "timeseriesRows=100000",
                "timeseriesColumns=13",
                "timeseriesHeader=Timestamp",
                "timeseriesRowHeight=22",
                "timeseriesStateRenderer=ShowcaseVisualRenderer",
                "timeseriesRegionRenderer=ShowcaseVisualRenderer",
                "saved=Alicia"
        );
        assertThat(result).anySatisfy(item ->
                assertThat(item)
                        .startsWith("timeseriesTimestamp=20")
                        .doesNotContain("1766000000000"));
    }
}
