package com.hongframe.raft.rpc.impl;

import com.hongframe.raft.Node;
import com.hongframe.raft.NodeManager;
import com.hongframe.raft.Status;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.rpc.ClientRequests.*;
import com.hongframe.raft.rpc.RpcRequests.*;
import com.hongframe.raft.rpc.core.ClientRequestRpc;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ClientRequestRpcImpl implements ClientRequestRpc {
    @Override
    public Message getLeader(GetLeaderRequest request) {
        List<Node> nodes = new ArrayList<>();
        final String groupId = request.getGroupId();
        if (StringUtils.isNotBlank(request.getPeerId())) {
            PeerId peerId = new PeerId();
            if (peerId.parse(request.getPeerId())) {
                final  Status status = new Status();
                nodes.add(getNode(groupId,peerId, status));
                if(!status.isOk()) {
                    return new ErrorResponse(10001, status.getErrorMsg());
                }
            } else {
                return new ErrorResponse(10001, "Fail to parse peer");
            }
        } else {
            nodes = NodeManager.getInstance().getNodesByGroupId(groupId);
        }
        if(nodes == null || nodes.isEmpty()) {
            return new ErrorResponse(10001, "nonthing");
        }

        for(Node node : nodes) {
            PeerId leader = node.getLeaderId();
            if (leader != null && !leader.isEmpty()) {
                GetLeaderResponse response = new GetLeaderResponse();
                response.setLeaderId(leader.toString());
                return response;
            }
        }

        return new ErrorResponse(10001, "nonthing");
    }

    protected Node getNode(String groupId, PeerId peerId, Status st) {
        Node node = null;

        if (peerId != null) {
            node = NodeManager.getInstance().get(groupId, peerId);
            if (node == null) {
                st.setError(10001, "Fail to find node %s in group %s", peerId, groupId);
            }
        } else {
            List<Node> nodes = NodeManager.getInstance().getNodesByGroupId(groupId);
            if (nodes == null || nodes.isEmpty()) {
                st.setError(10001, "Empty nodes in group %s", groupId);
            } else if (nodes.size() > 1) {
                st.setError(10001, "Peer must be specified since there're %d nodes in group %s",
                        nodes.size(), groupId);

            } else {
                node = nodes.get(0);
            }
        }
        return node;
    }
}
