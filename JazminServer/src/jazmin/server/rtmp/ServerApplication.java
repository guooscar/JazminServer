/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package jazmin.server.rtmp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rtmp.io.f4v.F4vReader;
import jazmin.server.rtmp.io.flv.FlvReader;
import jazmin.server.rtmp.io.flv.FlvWriter;
import jazmin.server.rtmp.rtmp.RtmpConfig;
import jazmin.server.rtmp.rtmp.RtmpReader;
import jazmin.server.rtmp.rtmp.RtmpWriter;
import jazmin.server.rtmp.util.Utils;
/**
 * 
 * @author 
 *
 */
public class ServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

    private final String name;
    private final Map<String, ServerStream> streams;
    private Date createTime;
    public ServerApplication(final String rawName) {
        this.name = cleanName(rawName);        
        streams = new ConcurrentHashMap<String, ServerStream>();        
        createTime=new Date();
    }
    //
    public String getName() {
        return name;
    }
    public Date getCreateTime() {
		return createTime;
	}

	//
    public List<ServerStream>getStreams(){
    	return new ArrayList<ServerStream>(streams.values());
    }
    //
    public RtmpReader getReader(final String rawName) {
        final String streamName = Utils.trimSlashes(rawName);
        final String path = RtmpConfig.homeDir + "/apps/" + name + "/";
        final String readerPlayName;
        try {
            if(streamName.startsWith("mp4:")) {
                readerPlayName = streamName.substring(4);
                return new F4vReader(path + readerPlayName);
            } else {                
                if(streamName.lastIndexOf('.') < streamName.length() - 4) {
                    readerPlayName = streamName + ".flv";
                } else {
                    readerPlayName = streamName;
                }
                return new FlvReader(path + readerPlayName);
            }
        } catch(Exception e) {
            logger.info("reader creation failed: {}", e.getMessage());
            return null;
        }
    }
    //
    public RtmpWriter getWriter(final String rawName) {
        final String streamName = Utils.trimSlashes(rawName);
        final String path = RtmpConfig.homeDir + "/apps/" + name + "/";
        return new FlvWriter(path + streamName + ".flv");
    }
    //
    public ServerStream getStream(final String rawName) {        
        return getStream(rawName, null);
    }
    //
    public ServerStream getStream(final String rawName, final String type) {
        final String streamName = cleanName(rawName);
        ServerStream stream = streams.get(streamName);
        if(stream == null) {
            stream = new ServerStream(streamName, type);
            streams.put(streamName, stream);
        }
        return stream;
    }

    private static String cleanName(final String raw) {
        return Utils.trimSlashes(raw).toLowerCase();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[name: '").append(name);
        sb.append("' streams: ").append(streams);
        sb.append(']');
        return sb.toString();
    }

}
