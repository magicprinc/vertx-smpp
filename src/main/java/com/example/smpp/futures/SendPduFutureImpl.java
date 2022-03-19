package com.example.smpp.futures;

//   Copyright 2022 Artem Ayrapetov
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

import com.example.smpp.types.SendPduDiscardedException;
import com.example.smpp.types.SendPduFailedException;
import com.example.smpp.types.SendPduNackkedException;
import com.example.smpp.types.SendPduWindowTimeoutException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.future.PromiseInternal;

import static com.example.smpp.model.SendPduExceptionType.*;

class SendPduFutureImpl<T> extends AbstractPduFuture<T, SendPduFuture<T>> implements SendPduFuture<T> {

  public SendPduFutureImpl(PromiseInternal<T> promise) {
    super(promise);
  }

  @Override
  public SendPduFuture<T> future() {
    return this;
  }

  @Override
  public SendPduFuture<T> onComplete(Handler<AsyncResult<T>> handler) {
    delegateAsPromise.onComplete(handler);
    return this;
  }

  @Override
  public SendPduFuture<T> onSuccess(Handler<T> handler) {
    delegateAsPromise.onSuccess(handler);
    return this;
  }

  @Override
  public SendPduFuture<T> onFailure(Handler<Throwable> handler) {
    delegateAsPromise.onFailure(handler);
    return this;
  }

  @Override
  public SendPduFuture<T> onWindowTimeout(Handler<SendPduWindowTimeoutException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduFailedException) {
        var error = (SendPduFailedException) e;
        if (error.getType() == WINDOW_TIMEOUT) {
          handler.handle((SendPduWindowTimeoutException)e);
        }
      }
    });
    return this;
  }

  @Override
  public SendPduFuture<T> onDiscarded(Handler<SendPduDiscardedException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduFailedException) {
        var error = (SendPduFailedException) e;
        if (error.getType() == REQUEST_DISCARDED_ON_CLOSE) {
          handler.handle((SendPduDiscardedException)e);
        }
      }
    });
    return this;
  }

  @Override
  public SendPduFuture<T> onNackked(Handler<SendPduNackkedException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduFailedException) {
        var error = (SendPduFailedException) e;
        if (error.getType() == GENERIC_NACK_RECEIVED) {
          handler.handle((SendPduNackkedException)e);
        }
      }
    });
    return this;
  }
}
