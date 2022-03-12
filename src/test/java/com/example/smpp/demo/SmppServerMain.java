package com.example.smpp.demo;

import com.cloudhopper.smpp.pdu.BindTransceiver;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.example.smpp.Smpp;
import com.example.smpp.server.SmppServer;
import com.example.smpp.server.SmppServerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.JksOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class SmppServerMain extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(SmppServerMain.class);

  private static final int INSTANCES = 1;
  private static final int THREADS = 1;
  private static final boolean SSL = false;

  private SmppServer server;

  @Override
  public void start(Promise<Void> startPromise) {
    var clientName = new String[]{null};
    var opts = new SmppServerOptions();
    if (SSL) {
      opts.setSsl(true)
          .setKeyStoreOptions(
              new JksOptions()
                  .setPath("src/test/resources/keystore")
                  .setPassword("changeit"));
    }
    server = Smpp.server(vertx, opts);
    server
        .configure(cfg -> {
          log.info("user code: configuring new session#{}", cfg.getId());
          cfg.setSystemId("vertx-smpp-server");
          cfg.setWindowSize(600);
//          cfg.setWriteTimeout(2000);
          cfg.setRequestExpiryTimeout(1000); // Время на отправку запроса и получение ответа
          cfg.onCreated(sess -> {
            // FIXME сессия еще не связана, boundToSystemId == null
            log.info("user code: session#{} created, bound to {}", sess.getId(), sess.getBoundToSystemId());
          });
          cfg.onBindReceived(bind -> {
            var systemId = ((BindTransceiver) bind.getRequest()).getSystemId();
            log.info("user code: inbound bind from " + systemId);
            clientName[0] = systemId;
          });
          cfg.onRequest(reqCtx -> {
//            if (reqCtx.getRequest() instanceof SubmitSm) {
//              try {
//                Thread.sleep(10000);
//              } catch (InterruptedException e) {
//                e.printStackTrace();
//              }
//            }

            var sess = reqCtx.getSession();
            sess
                .reply(reqCtx.getRequest().createResponse())
                .onSuccess(nothing -> {
                  if (reqCtx.getRequest() instanceof SubmitSm) {
                    sess.send(new DeliverSm())
                        .onSuccess(resp -> {})
                        .onFailure(Throwable::printStackTrace);
                  }
                })
                .onFailure(Throwable::printStackTrace);
          });
          cfg.onClose(sess -> {
            log.info("user code: closed session#{}", sess.getId());
          });
          cfg.onForbiddenRequest(reqCtx -> {
            log.info("user code: forbidden req {}", reqCtx.getRequest().getName());
          });
          cfg.onForbiddenResponse(rspCtx -> {
            log.info("user code: forbidden rsp {}", rspCtx.getResponse().getName());
          });
          return true;
        })
        .start("localhost", SSL? 2777: 2776)
        .onSuccess(done -> {
          log.info("Server online");
          startPromise.complete();
        })
        .onFailure(startPromise::fail);

    onShutdown(vertx, server);
  }

  public static void main(String[] args) {
    var vertex = Vertx.vertx();
    var depOpts = new DeploymentOptions()
      .setInstances(INSTANCES) // TODO scale to 2 and more
      .setWorkerPoolSize(THREADS)
      ;
    vertex.deployVerticle(SmppServerMain.class.getCanonicalName(), depOpts);
  }

  private static void onShutdown(Vertx vertx, SmppServer server) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      var closePromise = Promise.<Void>promise();
      var latch = new CountDownLatch(1);
      if (server.isListening()) {
        server.close(closePromise);
        closePromise.future()
            .onComplete(ar -> {
              log.info("Server offline");
              vertx.close()
                  .onComplete(unused -> {
                    log.debug("vertx closed");
                    latch.countDown();
                  });
            });
      } else {
        log.info("Server was not listening");
        vertx.close()
            .onComplete(unused -> {
              log.debug("vertx closed");
              latch.countDown();
            });
      }
      try {
        log.debug("waiting for server and vertx to shutdown");
        latch.await();
      } catch (InterruptedException e) {
        log.error("shutdown interrupted", e);
      }
    }));
  }
}