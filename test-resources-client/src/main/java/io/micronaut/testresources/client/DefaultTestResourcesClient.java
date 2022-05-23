/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.testresources.client;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.uri.UriBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

/**
 * A simple implementation of the test resources proxy client.
 */
@SuppressWarnings("unchecked")
public class DefaultTestResourcesClient implements TestResourcesClient {
    public static final String ACCESS_TOKEN = "Access-Token";

    private static final URI RESOLVABLE_PROPERTIES_URI = UriBuilder.of("/proxy").path("/list").build();
    private static final URI REQUIRED_PROPERTIES_URI = UriBuilder.of("/proxy").path("/requirements").build();
    private static final URI CLOSE_ALL_URI = UriBuilder.of("/proxy").path("/close/all").build();
    private static final URI RESOLVE_URI = UriBuilder.of("/proxy").path("/resolve").build();

    private final BlockingHttpClient client;

    private final String accessToken;

    public DefaultTestResourcesClient(HttpClient client, String accessToken) {
        this.client = client.toBlocking();
        this.accessToken = accessToken;
    }

    @Override
    public List<String> getResolvableProperties() {
        return doGet(RESOLVABLE_PROPERTIES_URI, List.class);
    }

    @Override
    public Optional<String> resolve(String name, Map<String, Object> properties) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("properties", properties);
        HttpRequest<?> req = configure(HttpRequest.POST(RESOLVE_URI, params));
        return Optional.ofNullable(client.retrieve(req));
    }

    @Override
    public List<String> getRequiredProperties() {
        return doGet(REQUIRED_PROPERTIES_URI, List.class);
    }

    @Override
    public void closeAll() {
        doGet(CLOSE_ALL_URI, String.class);
    }

    private <T> T doGet(URI uri, Class<T> clazz) {
        HttpRequest<?> req = configure(HttpRequest.GET(uri));
        return client.retrieve(req, clazz);
    }

    private <T> MutableHttpRequest<T> configure(MutableHttpRequest<T> request) {
        MutableHttpRequest<T> result = request.header(USER_AGENT, "Micronaut HTTP Client")
            .header(ACCEPT, "application/json");
        if (accessToken != null) {
            result = result.header(ACCESS_TOKEN, accessToken);
        }
        return result;
    }
}
