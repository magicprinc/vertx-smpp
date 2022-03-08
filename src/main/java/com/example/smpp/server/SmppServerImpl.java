package com.example.smpp.server;

import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import com.example.smpp.util.vertx.CountDownLatch;
import io.netty.channel.Channel;
import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SmppServerImpl extends NetServerImpl implements Cloneable, SmppServer {
  private static final Logger log = LoggerFactory.getLogger(SmppServerImpl.class);

  private final SmppServerConnectionHandler handler = new SmppServerConnectionHandler(this);
  private final Pool pool = new Pool();

  public SmppServerImpl(VertxInternal vertx, SmppServerOptions options) {
    super(vertx, options);
    connectHandler(sock -> {
    });
  }

  @Override
  protected Handler<Channel> childHandler(ContextInternal context, SocketAddress socketAddress, SSLHelper sslHelper) {
//    EventLoopContext connContext;
//    if (context instanceof EventLoopContext) {
//      connContext = (EventLoopContext) context;
//    } else {
//      connContext = vertx.createEventLoopContext(context.nettyEventLoop(), context.workerPool(), context.classLoader());
//    }
    return new SmppServerWorker((EventLoopContext) context, context::duplicate, this, vertx, sslHelper, options, handler, pool);
  }

  @Override
  public Future<SmppServer> start(String host, int port) {
    return listen(port, host).map(this);
  }

  @Override
  public SmppServer onSessionCreated(Handler<SmppSession> sessionCreatedHandler) {
    handler.connectionHandler = sessionCreatedHandler;
    return this;
  }

  @Override
  public SmppServer onRequest(Handler<PduRequestContext<?>> pduRequestHandler) {
    handler.requestHandler = pduRequestHandler;
    return this;
  }

  @Override
  public void close(Promise<Void> completion) {
    log.debug("closing sessions {}", pool.size());
    var latch = new CountDownLatch(vertx, pool.size());
    pool.forEach(sess -> {
      var closeSessionPromise = vertx.<Void>promise();
      sess.close(closeSessionPromise);
      closeSessionPromise.future()
          .onComplete(ar -> {
            log.debug("close counted");
            latch.countDown(1);
          });
    });
    latch.await(10, TimeUnit.SECONDS)
        .onComplete(nothing -> {
          log.debug("closing NetServer");
          super.close(completion);
        });
  }
}
