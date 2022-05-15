package com.yejunyu.rapid.core.netty;

import com.yejunyu.rapid.common.utils.RemotingHelper;
import com.yejunyu.rapid.common.utils.RemotingUtil;
import com.yejunyu.rapid.core.LifeCycle;
import com.yejunyu.rapid.core.RapidConfig;
import com.yejunyu.rapid.core.netty.handlers.NettyHttpServerHandler;
import com.yejunyu.rapid.core.netty.processor.Processor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/9
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {

    private final RapidConfig rapidConfig;

    private int port = 8888;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossEventLoopGroup;
    private EventLoopGroup workEventLoopGroup;

    private Processor processor;

    public NettyHttpServer(RapidConfig rapidConfig, Processor processor) {
        this.rapidConfig = rapidConfig;
        this.processor = processor;
        if (rapidConfig.getPort() > 0 && rapidConfig.getPort() < 65535) {
            this.port = rapidConfig.getPort();
        }
        init();
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (useEpoll()) {
            this.bossEventLoopGroup = new EpollEventLoopGroup(rapidConfig.getEventLoopBossNum(),
                    new DefaultThreadFactory("NettyBossEpoll"));
            this.workEventLoopGroup = new EpollEventLoopGroup(rapidConfig.getEventLoopWorkNum(),
                    new DefaultThreadFactory("NettyWorkerEpoll"));
        } else {
            this.bossEventLoopGroup = new NioEventLoopGroup(rapidConfig.getEventLoopBossNum(),
                    new DefaultThreadFactory("NettyBossNIO"));
            this.workEventLoopGroup = new NioEventLoopGroup(rapidConfig.getEventLoopWorkNum(),
                    new DefaultThreadFactory("NettyWorkerNIO"));
        }
    }

    @Override
    public void start() {
        this.serverBootstrap
                .group(bossEventLoopGroup, workEventLoopGroup)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 就是keepalive, 对应用层没什么用
                .option(ChannelOption.SO_KEEPALIVE, false)
                // 让关闭连接释放的端口今早可使用
                .option(ChannelOption.SO_REUSEADDR, true)
                // 禁用 nagle 算法,
                .childOption(ChannelOption.TCP_NODELAY, true)
                // TCP接收缓冲区的容量上限
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                // TCP发送缓冲区的容量上限
                .childOption(ChannelOption.SO_SNDBUF, 65535)
                // 握手等待队列和 accept 队列之和
                .childOption(ChannelOption.SO_BACKLOG, 1024)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(
                                // http编解码
                                new HttpServerCodec(),
                                // http body 聚合
                                new HttpObjectAggregator(rapidConfig.getMaxContentLength()),
                                // http content 压缩
                                new HttpContentCompressor(),
                                new HttpServerExpectContinueHandler(),
                                new IdleStateHandler(60, 60, 60),
                                new NettyServerConnectManagerHandler(),
                                new NettyHttpServerHandler(processor)
                        );
                    }
                });
        if (rapidConfig.isNettyAllocator()) {
            this.serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        try {
            this.serverBootstrap.bind().sync();
            log.info("<========== Server Start Up on port " + this.port + "==========>");
        } catch (InterruptedException e) {
            throw new RuntimeException("this serverBootstrap#start bind fail!", e);
        }
    }

    @Override
    public void shutdown() {
        if (bossEventLoopGroup != null) {
            bossEventLoopGroup.shutdownGracefully();
        }
        if (workEventLoopGroup != null) {
            workEventLoopGroup.shutdownGracefully();
        }
    }

    public boolean useEpoll() {
        return rapidConfig.isUseEpoll() && RemotingUtil.isLinuxPlatform();
    }

    /**
     * 连接管理器
     *
     * @see io.netty.channel.ChannelDuplexHandler 也是继承了
     * @see io.netty.channel.ChannelInboundHandlerAdapter
     */
    static class NettyServerConnectManagerHandler extends ChannelDuplexHandler {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty server pipeline: channelRegistered {}", remoteAddr);
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty server pipeline: channelUnregistered {}", remoteAddr);
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty server pipeline: channelActive {}", remoteAddr);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("Netty server pipeline: channelInactive {}", remoteAddr);
            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                // 心跳断开
                if (event.state().equals(IdleState.ALL_IDLE)) {
                    final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    log.warn("Netty server pipeline: userEventTriggered {}", remoteAddr);
                    ctx.channel().close();
                }
            }
            super.userEventTriggered(ctx, evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.warn("Netty server pipeline: exceptionCaught {}", remoteAddr);
            ctx.channel().close();
        }
    }
}
