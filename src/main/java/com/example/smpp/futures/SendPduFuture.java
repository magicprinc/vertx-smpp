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

import com.example.smpp.types.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseImpl;

public interface SendPduFuture<T> extends Future<T>, Promise<T> {

  static <T> SendPduFuture<T> promise(ContextInternal contextInternal) {
    return new SendPduFutureImpl<>(new PromiseImpl<>(contextInternal));
  }

  static <T, E extends SendPduFailedException> SendPduFuture<T> failedFuture(E e) {
    var promise = new PromiseImpl<T>();
    promise.fail(e);
    return new SendPduFutureImpl<>(promise);
  }

  // TODO здесь надо перечислить все методы, которые возвращают Future<T> и заменить на этот интерфейс

  @Override
  SendPduFuture<T> onComplete(Handler<AsyncResult<T>> handler);

  @Override
  SendPduFuture<T> onSuccess(Handler<T> handler);

  @Override
  SendPduFuture<T> onFailure(Handler<Throwable> handler);

  SendPduFuture<T> onWindowTimeout(Handler<SendPduWindowTimeoutException> handler);

  SendPduFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler);

  SendPduFuture<T> onWrongOperation(Handler<SendPduWrongOperationException> handler);

  SendPduFuture<T> onDiscarded(Handler<SendPduDiscardedException> handler);

  SendPduFuture<T> onWriteFailed(Handler<SendPduWriteFailedException> handler);

  SendPduFuture<T> onNackked(Handler<SendPduNackkedException> handler);
}
