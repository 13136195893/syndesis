/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.runtime;

import java.util.List;

import io.syndesis.model.connection.Connection;
import io.syndesis.rest.v1.operations.Violation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.CloseResource")
public class ConnectionsITCase extends BaseITCase {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final static Class<List<Violation>> RESPONSE_TYPE = (Class) List.class;

    @Test
    public void emptyNamesShouldNotBeAllowed() {
        final Connection connection = new Connection.Builder().name(" ").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections/validation", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(got.getBody()).hasSize(1);
    }

    @Test
    public void emptyTagsShouldBeIgnored() {
        final Connection connection = new Connection.Builder().id("tags-connection-test").name("tags-connection-test")
            .addTag("", " ", "taggy").build();

        final ResponseEntity<Connection> got = post("/api/v1/connections", connection, Connection.class,
            tokenRule.validToken(), HttpStatus.OK);

        final Connection created = got.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getTags()).containsExactly("taggy");
    }

    @Test
    public void nullNamesShouldNotBeAllowed() {
        final Connection connection = new Connection.Builder().build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections/validation", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(got.getBody()).hasSize(1);
    }

    @Before
    public void preexistingConnection() {
        final Connection connection = new Connection.Builder().name("Existing connection").build();

        dataManager.create(connection);
    }

    @Test
    public void shouldDetermineValidityForInvalidConnections() {
        final Connection connection = new Connection.Builder().name("Existing connection").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections/validation", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(got.getBody()).hasSize(1);
    }

    @Test
    public void shouldDetermineValidityForValidConnections() {
        final Connection connection = new Connection.Builder().name("Test connection").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/connections/validation", connection, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        assertThat(got.getBody()).isNull();
    }

}
