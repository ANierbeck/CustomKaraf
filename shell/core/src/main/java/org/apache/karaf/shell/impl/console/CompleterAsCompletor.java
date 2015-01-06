/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.karaf.shell.impl.console;

import java.util.List;

import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.impl.console.parsing.CommandLineImpl;

public class CompleterAsCompletor implements jline.console.completer.Completer {

    private final Session session;
    private final Completer completer;

    public CompleterAsCompletor(Session session, Completer completer) {
        this.session = session;
        this.completer = completer;
    }

    @SuppressWarnings("unchecked")
	public int complete(String buffer, int cursor, @SuppressWarnings("rawtypes") List candidates) {
        return completer.complete(session, CommandLineImpl.build(buffer, cursor, isExpansionEnabled()), candidates); //CQL-Handling
    }

    //special handling for CQL-Shell, disables the brackets from completion
    private boolean isExpansionEnabled() {
        Object v = session.get("org.apache.felix.gogo.expansion");
        if (v != null) {
            return Boolean.parseBoolean(v.toString());
        }
        return true;
    }
}
