package cz.auderis.corusco.core.help;

import cz.auderis.corusco.core.key.HelpTopic;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultHelpServiceTest {

    private static final HelpTopic NAME_TOPIC = HelpTopic.of("customer/name");

    @Test
    void dispatchesTopicRequestsToHandler() {
        List<HelpRequest> requests = new ArrayList<>();
        HelpService service = new DefaultHelpService(requests::add);

        service.open(NAME_TOPIC);

        assertThat(requests).containsExactly(HelpRequest.of(NAME_TOPIC));
        assertThat(service.lastRequest()).contains(HelpRequest.of(NAME_TOPIC));
    }

    @Test
    void requestCarriesOptionalSourceAndContext() {
        List<HelpRequest> requests = new ArrayList<>();
        HelpService service = new DefaultHelpService(requests::add);
        Object source = new Object();

        service.open(NAME_TOPIC, source, "field-focus");

        HelpRequest request = requests.getFirst();
        assertThat(request.topic()).isEqualTo(NAME_TOPIC);
        assertThat(request.sourceOptional()).containsSame(source);
        assertThat(request.contextOptional()).contains("field-focus");
    }

    @Test
    void openingWithoutHandlerFailsClearlyButRecordsRequest() {
        DefaultHelpService service = new DefaultHelpService();

        assertThatThrownBy(() -> service.open(NAME_TOPIC))
                .isInstanceOf(HelpServiceException.class)
                .hasMessageContaining(NAME_TOPIC.id());
        assertThat(service.lastRequest()).contains(HelpRequest.of(NAME_TOPIC));
    }

    @Test
    void handlerCanBeReplaced() {
        List<String> events = new ArrayList<>();
        DefaultHelpService service = new DefaultHelpService(request -> events.add("first:" + request.topic().id()));

        service.open(NAME_TOPIC);
        service.setHandler(request -> events.add("second:" + request.topic().id()));
        service.open(HelpTopic.of("customer/orders"));

        assertThat(events).containsExactly("first:customer/name", "second:customer/orders");
    }

    @Test
    void requestRejectsNullTopic() {
        assertThatThrownBy(() -> HelpRequest.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("topic");
    }
}
