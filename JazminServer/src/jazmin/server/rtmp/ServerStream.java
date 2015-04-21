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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rtmp.rtmp.RtmpMessage;
import jazmin.server.rtmp.rtmp.message.Metadata;
import jazmin.server.rtmp.util.Utils;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
/**
 * 
 */
public class ServerStream {
    private static final Logger logger = LoggerFactory.getLogger(ServerStream.class);
	//
    public static enum PublishType {
        LIVE,
        APPEND,
        RECORD;
        public String asString() {
            return this.name().toLowerCase();
        }
        public static PublishType parse(final String raw) {
            return PublishType.valueOf(raw.toUpperCase());
        }
    }
    //	
    private final String name;
    private final PublishType publishType;
    private final ChannelGroup subscribers;
    private final List<RtmpMessage> configMessages;
    private final Date createTime;
    private Channel publisher;
    private Map<String, String> metadata;
    //
    
    //
    public ServerStream(final String rawName, final String typeString) {        
        this.name = Utils.trimSlashes(rawName).toLowerCase();
        createTime=new Date();
        if(typeString != null) {
            this.publishType = PublishType.parse(typeString); // TODO record, append
            subscribers = new DefaultChannelGroup(name);
            configMessages = new ArrayList<RtmpMessage>();
        } else {
            this.publishType = null;
            subscribers = null;
            configMessages = null;
        }
        metadata=new HashMap<String, String>();
        logger.info("Created ServerStream {}", this);
    }

    public boolean isLive() {
        return publishType != null && publishType == PublishType.LIVE;
    }

    public PublishType getPublishType() {
        return publishType;
    }

    public ChannelGroup getSubscribers() {
        return subscribers;
    }

    public String getName() {
        return name;
    }
    
    public Date getCreateTime(){
    	return createTime;
    }

    public List<RtmpMessage> getConfigMessages() {
        return configMessages;
    }

    public void addConfigMessage(final RtmpMessage message) {
        configMessages.add(message);
    }

    public void setPublisher(Channel publisher) {
        this.publisher = publisher;
        configMessages.clear();
    }

    public Channel getPublisher() {
        return publisher;
    }

    /**
	 * @return the metadata
	 */
	public Map<String, String> getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(Metadata metadata) {
		this.metadata.put("duration", metadata.getInnerValue("duration"));
		this.metadata.put("width", metadata.getInnerValue("width"));
		this.metadata.put("height", metadata.getInnerValue("height"));
		this.metadata.put("videodatarate", metadata.getInnerValue("videodatarate"));
		this.metadata.put("framerate", metadata.getInnerValue("framerate"));
		this.metadata.put("videocodecid", metadata.getInnerValue("videocodecid"));
		this.metadata.put("encoder", metadata.getInnerValue("encoder"));
		this.metadata.put("filesize", metadata.getInnerValue("filesize"));
		
	}

	@Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();        
        sb.append("[name: '").append(name);
        sb.append("' type: ").append(publishType);
        sb.append(" publisher: ").append(publisher);
        sb.append(" subscribers: ").append(subscribers);
        sb.append(" config: ").append(configMessages);
        sb.append(']');
        return sb.toString();
    }

}
