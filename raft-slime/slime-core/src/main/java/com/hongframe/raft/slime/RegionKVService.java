package com.hongframe.raft.slime;

import com.hongframe.raft.slime.metadata.RegionEpoch;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-06-09 00:59
 */
public interface RegionKVService {

    long getRegionId();

    RegionEpoch getRegionEpoch();

}
