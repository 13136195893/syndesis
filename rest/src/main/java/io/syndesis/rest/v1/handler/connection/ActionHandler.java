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
package io.syndesis.rest.v1.handler.connection;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.Action;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.swagger.annotations.Api;

import javax.ws.rs.Path;

@Path("/actions")
@Api(value = "actions")
@org.springframework.stereotype.Component
public class ActionHandler extends BaseHandler implements Lister<Action>, Getter<Action> {

    public ActionHandler(DataManager dataMgr) {
        super(dataMgr);
    }

    @Override
    public Kind resourceKind() {
        return Kind.Action;
    }

}
