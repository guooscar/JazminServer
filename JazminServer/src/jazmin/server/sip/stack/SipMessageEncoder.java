/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;

/**
 * @author jonas
 *
 */
public class SipMessageEncoder extends MessageToByteEncoder<SipMessage> {
	private static Logger logger=LoggerFactory.get(SipMessageEncoder.class);
    @Override
    protected void encode(final ChannelHandlerContext ctx, final SipMessage msg, final ByteBuf out) {
    	try {
    		if(logger.isDebugEnabled()){
    			logger.debug(">>>>>>>>>>\n{}",msg);
    		}
            final Buffer b = msg.toBuffer();
            for (int i = 0; i < b.getReadableBytes(); ++i) {
                out.writeByte(b.getByte(i));
            }
            out.writeByte(SipParser.CR);
            out.writeByte(SipParser.LF);
        } catch (final IOException e) {
            logger.catching(e);
        }
    }

}
