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

package io.syndesis.controllers.integration;

import java.util.List;
import java.util.Set;

import io.syndesis.model.integration.Integration;

public interface StatusChangeHandlerProvider {

    List<StatusChangeHandler> getStatusChangeHandlers();

    interface StatusChangeHandler {

        Set<Integration.Status> getTriggerStatuses();

        StatusUpdate execute(Integration model);

        class StatusUpdate {
            private Integration.Status status;
            private String statusMessage;
            private List<String> stepsPerformed;

            public StatusUpdate(Integration.Status status, String statusMessage, List<String> stepsPerformed) {
                this.status = status;
                this.statusMessage = statusMessage;
                this.stepsPerformed = stepsPerformed;
            }

            public StatusUpdate(Integration.Status status, String statusMessage) {
                this(status, statusMessage, null);
            }

            public StatusUpdate(Integration.Status status) {
                this(status, null, null);
            }

            public StatusUpdate(Integration.Status status, List<String> stepsPerformed) {
                this(status, null, stepsPerformed != null && !stepsPerformed.isEmpty() ? stepsPerformed : null);
            }

            public Integration.Status getStatus() {
                return status;
            }

            public String getStatusMessage() {
                return statusMessage;
            }

            public List<String> getStepsPerformed() {
                return stepsPerformed;
            }
        }
    }
}
