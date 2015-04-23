/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;

/**
 * @author jonas
 *
 */
public class SipMessageEncoder extends MessageToByteEncoder<SipMessage> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final SipMessage msg, final ByteBuf out) {
        try {
            final Buffer b = msg.toBuffer();
            for (int i = 0; i < b.getReadableBytes(); ++i) {
                out.writeByte(b.getByte(i));
            }
            out.writeByte(SipParser.CR);
            out.writeByte(SipParser.LF);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
