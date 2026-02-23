package pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.*;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(PactConsumerTestExt.class)
public abstract class JsonApiResponsePactTest {

    private final String consumerName;
    private final String providerName;

    protected JsonApiResponsePactTest(String consumerName, String providerName) {
        this.consumerName = consumerName;
        this.providerName = providerName;
    }

    /** Each subclass defines the path */
    protected abstract String getPath();

    /** Each subclass defines the type */
    protected abstract String getType();

    /** Each subclass defines the method POST/PUT... */
    protected abstract String getMethod();

    /** Each subclass defines the Status */
    protected abstract int getStatus();

    protected Map<String, String> getQueryParams() {
        return Collections.emptyMap();
    }

    protected boolean hasQueryParams() {
        return false;
    }

    protected boolean hasRequestBody() {
        return false;
    }

    protected boolean isPaginated() {
        return false;
    }

    protected String getInteractionName(){
        return "";
    }

    protected boolean hasGivenState() {
        return false;
    }

    protected String getGivenState(){
        return "";
    }

    public V4Pact createPact(PactDslWithProvider builder) {

        String queryString = getQueryParams().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        PactDslJsonBody requestBody = (PactDslJsonBody) new PactDslJsonBody()
                .object("data")
                    .stringType("type", getType())
                    .object("attributes")
                .closeObject();

        PactDslJsonBody resourceBody = (PactDslJsonBody) new PactDslJsonBody()
                .stringType("id", "123")
                .stringType("type", getType())
                .object("attributes")
                .closeObject();

        PactDslJsonBody responseBody = (PactDslJsonBody) new PactDslJsonBody()
                .minArrayLike("data", 1, resourceBody);
                if (isPaginated()){
                    responseBody.object("meta")
                            .closeObject();
                }else{
                    responseBody.nullValue("meta");
                }


                responseBody.object("links")
                .eachKeyLike(
                        "self",
                        PactDslJsonRootValue.stringMatcher(".+")
                )
                .closeObject();

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type",  "application/json; charset=UTF-8");
        PactDslRequestWithPath pact;

        if (hasGivenState()) {
            pact = builder
                    .given(getGivenState())
                    .uponReceiving("JsonApiResponsePactTest " + getInteractionName() +" test interaction")
                    .path(getPath())
                    .method(getMethod());
        } else {
            pact = builder
                    .uponReceiving("JsonApiResponsePactTest " + getInteractionName() +" test interaction")
                    .path(getPath())
                    .method(getMethod());
        }

        if (hasQueryParams()) {
            pact = pact
                    .query(queryString);
        }

        if (hasRequestBody()) {
            pact = pact
                    .body(requestBody);
        }

        return pact
                .headers(requestHeaders)
                .willRespondWith()
                .status(getStatus())
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createPact")
    void testFetchingData(MockServer mockServer) throws IOException {
        String queryString = getQueryParams().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String url = mockServer.getUrl() + getPath();
        if (!queryString.isEmpty()) {
            url += "?" + queryString;
        }

        Request request = Request.create(getMethod(), url)
                .addHeader("Content-Type",  "application/json; charset=UTF-8");

        if (hasRequestBody()) {
            String jsonBody = "{\n" +
                    "  \"data\": {\n" +
                    "    \"type\": \"" + getType() + "\",\n" +
                    "    \"attributes\": {\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            request = request.bodyString(jsonBody, ContentType.APPLICATION_JSON);
        }

        ClassicHttpResponse httpResponse =
                (ClassicHttpResponse) request.execute().returnResponse();

        assertThat(httpResponse.getCode(), is(equalTo(getStatus())));
    }

    public static PactDslJsonBody resourceDsl() {
        return (PactDslJsonBody) new PactDslJsonBody()
                .object("data")
                .stringType("id")
                .stringType("type")
                .object("attributes")
                .closeObject();
    }
}
