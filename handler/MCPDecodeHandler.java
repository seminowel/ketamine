package viamcp.handler;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Sharable
public class MCPDecodeHandler extends MessageToMessageDecoder {
   private final UserConnection info;
   private boolean handledCompression;
   private boolean skipDoubleTransform;

   public MCPDecodeHandler(UserConnection info) {
      this.info = info;
   }

   public UserConnection getInfo() {
      return this.info;
   }

   protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List out) throws Exception {
      if (this.skipDoubleTransform) {
         this.skipDoubleTransform = false;
         out.add(bytebuf.retain());
      } else if (!this.info.checkIncomingPacket()) {
         throw CancelDecoderException.generate((Throwable)null);
      } else if (!this.info.shouldTransformPacket()) {
         out.add(bytebuf.retain());
      } else {
         ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);

         try {
            boolean needsCompress = this.handleCompressionOrder(ctx, transformedBuf);
            this.info.transformIncoming(transformedBuf, CancelDecoderException::generate);
            if (needsCompress) {
               CommonTransformer.compress(ctx, transformedBuf);
               this.skipDoubleTransform = true;
            }

            out.add(transformedBuf.retain());
         } finally {
            transformedBuf.release();
         }

      }
   }

   private boolean handleCompressionOrder(ChannelHandlerContext ctx, ByteBuf buf) throws InvocationTargetException {
      if (this.handledCompression) {
         return false;
      } else {
         int decoderIndex = ctx.pipeline().names().indexOf("decompress");
         if (decoderIndex == -1) {
            return false;
         } else {
            this.handledCompression = true;
            if (decoderIndex > ctx.pipeline().names().indexOf("via-decoder")) {
               CommonTransformer.decompress(ctx, buf);
               ChannelHandler encoder = ctx.pipeline().get("via-encoder");
               ChannelHandler decoder = ctx.pipeline().get("via-decoder");
               ctx.pipeline().remove(encoder);
               ctx.pipeline().remove(decoder);
               ctx.pipeline().addAfter("compress", "via-encoder", encoder);
               ctx.pipeline().addAfter("decompress", "via-decoder", decoder);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      if (!PipelineUtil.containsCause(cause, CancelCodecException.class)) {
         super.exceptionCaught(ctx, cause);
      }
   }
}
