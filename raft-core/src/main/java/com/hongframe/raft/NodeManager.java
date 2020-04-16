package com.hongframe.raft;

import com.hongframe.raft.entity.NodeId;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.util.Endpoint;
import com.hongframe.raft.util.Utils;
import org.apache.dubbo.common.utils.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-16 18:02
 */
public class NodeManager {

    private static final NodeManager INSTANCE = new NodeManager();

    private final ConcurrentMap<NodeId, Node> nodeMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<Node>> groupMap = new ConcurrentHashMap<>();
    private final ConcurrentHashSet<Endpoint> addrSet = new ConcurrentHashSet<>();

    public static NodeManager getInstance() {
        return INSTANCE;
    }

    public boolean nodeExists(Endpoint endpoint) {
        if(endpoint.getIp().equals(Utils.IP_ANY)) {
            return this.addrSet.contains(new Endpoint(Utils.IP_ANY, endpoint.getPort()));
        }
        return addrSet.contains(endpoint);
    }

    public void addAddress(final Endpoint addr) {
        this.addrSet.add(addr);
    }

    public boolean add(Node node) {
        NodeId nodeId = node.getNodeId();
        if(!nodeExists(nodeId.getPeerId().getEndpoint())) {
            return false;
        }
        if(this.nodeMap.putIfAbsent(nodeId, node) == null) {
            String gourp = nodeId.getGroupId();
            List<Node> nodes = groupMap.get(gourp);
            if(nodes == null) {
                nodes = Collections.synchronizedList(new ArrayList<>());
                List<Node> existsNode = this.groupMap.putIfAbsent(gourp, nodes);
                nodes = existsNode;
            }
            nodes.add(node);
            return true;
        }
        return false;
    }

    public Node get(final String groupId, final PeerId peerId) {
        return this.nodeMap.get(new NodeId(groupId, peerId));
    }

}
