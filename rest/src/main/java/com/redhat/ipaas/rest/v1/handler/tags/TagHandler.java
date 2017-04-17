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
package com.redhat.ipaas.rest.v1.handler.tags;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.model.ListResult;
import com.redhat.ipaas.model.TagFinder;
import com.redhat.ipaas.model.connection.Connection;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.rest.v1.handler.BaseHandler;

import io.swagger.annotations.Api;

@Path("/tags")
@Api(value = "tags")
@Component
public class TagHandler extends BaseHandler {

    public TagHandler(DataManager dataMgr) {
        super(dataMgr);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ListResult<String> listTags() {
        
        return new TagFinder()
                .add(getDataManager().fetchAll(Integration.class))
                .add(getDataManager().fetchAll(Connection.class))
                .getResult();
    }

}
