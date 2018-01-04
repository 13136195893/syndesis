/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.model.connection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.Violation;
import io.syndesis.model.WithConfigurationProperties;
import io.syndesis.model.WithName;
import io.syndesis.model.WithProperties;
import io.syndesis.model.action.ActionsSummary;
import org.immutables.value.Value;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value.Immutable
@JsonDeserialize(builder = ConnectorSummary.Builder.class)
@SuppressWarnings("immutables")
public interface ConnectorSummary extends WithName, WithConfigurationProperties {

    final class Builder extends ImmutableConnectorSummary.Builder {
        // make ImmutableConnectorSummary.Builder accessible

        public Builder createFrom(final Connector connector) {
            final ActionsSummary actionsSummary = new ActionsSummary.Builder()//
                .totalActions(connector.getActions().size())//
                .actionCountByTags(
                    connector.getActions().stream()
                        .flatMap(s -> s.getTags().stream().distinct())
                        .collect(
                            Collectors.groupingBy(
                                Function.identity(),
                                Collectors.reducing(0, (e) -> 1, Integer::sum)
                            )
                        )
                )
                .build();

            return new Builder().createFrom((WithProperties) connector)//
                .name(connector.getName())//
                .description(connector.getDescription())//
                .icon(connector.getIcon())
                .actionsSummary(actionsSummary);
        }

    }

    ActionsSummary getActionsSummary();

    String getDescription();

    List<Violation> getErrors();

    String getIcon();

    List<Violation> getWarnings();
}
