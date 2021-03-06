package ca.uwaterloo.cs.streamingrpq.stree.data.arbitrary;

import ca.uwaterloo.cs.streamingrpq.stree.data.*;
import ca.uwaterloo.cs.streamingrpq.stree.util.Constants;
import ca.uwaterloo.cs.streamingrpq.stree.util.Hasher;

import java.util.*;

public class SpanningTreeRAPQ<V> extends AbstractSpanningTree<V, SpanningTreeRAPQ<V>, TreeNodeRAPQ<V>> {

    protected SpanningTreeRAPQ(Delta<V, SpanningTreeRAPQ<V>, TreeNodeRAPQ<V>> delta, V rootVertex, long timestamp) {
        super(timestamp, delta);

        TreeNodeRAPQ<V> root = new TreeNodeRAPQ<V>(rootVertex, 0, null, this, timestamp);
        this.rootNode = root;
        this.delta = delta;
        nodeIndex.put(Hasher.createTreeNodePairKey(rootVertex, 0), root);
        //保存因为过时即将被remove的节点
        candidates = new HashSet<>(Constants.EXPECTED_TREE_SIZE);
        candidateRemoval = new HashSet<>(Constants.EXPECTED_TREE_SIZE);
        visited = new HashSet<>(Constants.EXPECTED_TREE_SIZE);
    }

    @Override
    protected long populateCandidateRemovals(long minTimestamp) {
        // perform a bfs traversal on tree, no need for visited as it is a three
        LinkedList<TreeNodeRAPQ<V>> queue = new LinkedList<>();

        // minTimestamp of the tree should be updated, find the lowest timestamp in the tree higher than the minTimestmap
        // because after this maintenance, there is not going to be a node in the tree lower than the minTimestamp
        long minimumValidTimetamp = Long.MAX_VALUE;
        queue.addAll(rootNode.getChildren());
        //使用queue一层一层地遍历整棵树
        while(!queue.isEmpty()) {
            // populate the queue with children
            TreeNodeRAPQ<V> currentVertex = queue.remove();
            queue.addAll(currentVertex.getChildren());

            // check time timestamp to decide whether it is expired
            if(currentVertex.getTimestamp() <= minTimestamp) {
                candidates.add(currentVertex);
            }
            // find minValidTimestamp for filtering for the next maintenance window
            //找到下一次迭代时最小的timestamp
            if(currentVertex.getTimestamp() > minTimestamp && currentVertex.getTimestamp() < minimumValidTimetamp) {
                minimumValidTimetamp = currentVertex.getTimestamp();
            }
        }

        return minimumValidTimetamp;
    }

}
