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
package org.neo4j.graphalgo.core.utils.partition;

import org.neo4j.graphalgo.core.utils.collection.primitive.PrimitiveLongIterator;
import org.neo4j.graphalgo.api.Degrees;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.graphalgo.core.utils.partition.Partition.MAX_NODE_COUNT;

public final class PartitionUtils {

    private PartitionUtils() {}

    public static List<Partition> numberAlignedPartitioning(
        int concurrency,
        long nodeCount,
        long alignTo
    ) {
        final long initialBatchSize = ParallelUtil.adjustedBatchSize(nodeCount, concurrency, alignTo);
        final long remainder = initialBatchSize % alignTo;
        final long adjustedBatchSize = remainder == 0 ? initialBatchSize : initialBatchSize + (alignTo - remainder);

        List<Partition> partitions = new ArrayList<>(concurrency);
        for (long i = 0; i < nodeCount; i += adjustedBatchSize) {
            long actualBatchSize = i + adjustedBatchSize < nodeCount ? adjustedBatchSize : nodeCount - i;
            partitions.add(new Partition(i, actualBatchSize));
        }

        return partitions;
    }

    public static List<Partition> degreePartition(Graph graph, long batchSize) {
        return degreePartition(graph.nodeIterator(), graph, batchSize);
    }

    public static List<Partition> degreePartition(
        PrimitiveLongIterator nodes,
        Degrees degrees,
        long batchSize
    ) {
        List<Partition> partitions = new ArrayList<>();
        long start = 0L;
        while (nodes.hasNext()) {
            assert batchSize > 0L;
            long partitionSize = 0L;
            long nodeId = 0L;
            while (nodes.hasNext() && partitionSize <= batchSize && nodeId - start < MAX_NODE_COUNT) {
                nodeId = nodes.next();
                partitionSize += degrees.degree(nodeId);
            }

            long end = nodeId + 1;
            partitions.add(new Partition(start, end - start));
            start = end;
        }
        return partitions;
    }

}
