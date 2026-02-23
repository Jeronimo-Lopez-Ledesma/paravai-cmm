package pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(PactConsumerTestExt.class)
public abstract class CreateUpdateResourcePactTest {

    private final String consumerName;
    private final String providerName;

    protected CreateUpdateResourcePactTest(String consumerName, String providerName) {
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

    protected String getInteractionName(){
        return "";
    }

    public V4Pact createPact(PactDslWithProvider builder) {
        PactDslJsonBody requestBody = (PactDslJsonBody) new PactDslJsonBody()
                .object("data")
                    .stringType("type", getType())
                    .object("attributes")
                .closeObject();

        PactDslJsonBody responseBody = (PactDslJsonBody) new PactDslJsonBody()
                .object("data")            // <─ ahora es objeto, no array
                .stringType("id")
                .stringType("type")
                .object("attributes")  // contenido genérico
                .closeObject()         // cierra attributes
                .closeObject()                      // cierra data
                // --- meta: mapa <String, Object> -----------------------
                .object("meta")                     // meta es un objeto (puede ir vacío)
                .closeObject()
                // --- links: mapa <String, String> ----------------------
                .object("links")
                .eachKeyLike(
                        "self",                     // nombre clave ejemplo
                        PactDslJsonRootValue.stringMatcher(".+") // valor debe ser string NO vacío
                )
                .closeObject();

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        return builder
                .uponReceiving("CreateUpdateResourcePactTest " + getInteractionName() +" test interaction")
                .path(getPath())
                .method(getMethod())
                .body(requestBody)
                .headers(requestHeaders)
                .willRespondWith()
                .status(getStatus())
                .body(responseBody)
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createPact")
    void testCreateOrUpdateResource(MockServer mockServer) throws IOException {
        String jsonBody = "{\n" +
                "  \"data\": {\n" +
                "    \"type\": \"" + getType() + "\",\n" +
                "    \"attributes\": {\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Request request = Request.create(getMethod(), mockServer.getUrl() + getPath())
                .addHeader("Content-Type", "application/json")
                .bodyString(jsonBody, ContentType.APPLICATION_JSON);

        ClassicHttpResponse httpResponse =
                (ClassicHttpResponse) request.execute().returnResponse();

        assertThat(httpResponse.getCode(), is(equalTo(getStatus())));
    }

}
