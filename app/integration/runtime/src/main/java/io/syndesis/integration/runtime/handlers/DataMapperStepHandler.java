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
package io.syndesis.integration.runtime.handlers;

import java.util.Optional;

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.model.integration.Step;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;

public class DataMapperStepHandler implements IntegrationStepHandler{
    @Override
    public boolean canHandle(Step step) {
        return "mapper".equals(step.getStepKind());
    }

    @Override
    public Optional<ProcessorDefinition> handle(Step step, ProcessorDefinition route, IntegrationRouteBuilder builder, int stepIndex) {
        ObjectHelper.notNull(route, "route");

        final String index = Integer.toString(stepIndex);

        return Optional.of(
            route.toF("atlas:mapping-step-%s.json", index)
        );
    }
}
