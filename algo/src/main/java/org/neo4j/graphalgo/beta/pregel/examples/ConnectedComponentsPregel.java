/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.beta.pregel.examples;

import org.neo4j.graphalgo.beta.pregel.PregelComputation;
import org.neo4j.graphalgo.beta.pregel.PregelContext;

import java.util.Queue;

public class ConnectedComponentsPregel implements PregelComputation {

    @Override
    public void compute(PregelContext context, final long nodeId, Queue<Double> messages) {
        double oldComponentId = context.getNodeValue(nodeId);
        double newComponentId = oldComponentId;
        if (context.isInitialSuperStep()) {
            // In the first round, every node uses its own id as the component id
            newComponentId = nodeId;
        } else if (messages != null && !messages.isEmpty()){
                Double nextComponentId;
                while ((nextComponentId = messages.poll()) != null) {
                    if (nextComponentId.longValue() < newComponentId) {
                        newComponentId = nextComponentId.longValue();
                    }
                }
        }

        if (newComponentId != oldComponentId) {
            context.setNodeValue(nodeId, newComponentId);
            context.sendMessages(nodeId, newComponentId);
        }
    }
}
