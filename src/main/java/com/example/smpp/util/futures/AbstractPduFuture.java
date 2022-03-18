package com.example.smpp.util.futures;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.future.PromiseInternal;

import java.util.function.Function;

// package-private
abstract class AbstractPduFuture<T> implements Future<T>, Promise<T> {

  protected final PromiseInternal<T> delegateAsPromise;
  protected final Future<T> delegateAsFuture;

  public AbstractPduFuture(PromiseInternal<T> delegateAsPromise) {
    this.delegateAsPromise = delegateAsPromise;
    this.delegateAsFuture = delegateAsPromise.future();
  }

  @Override
  public boolean tryComplete(T result) {
    return delegateAsPromise.tryComplete(result);
  }

  @Override
  public boolean isComplete() {
    return delegateAsFuture.isComplete();
  }

  @Override
  public Future<T> onComplete(Handler<AsyncResult<T>> handler) {
    return delegateAsFuture.onComplete(handler);
  }

  @Override
  public T result() {
    return delegateAsFuture.result();
  }

  @Override
  public Throwable cause() {
    return delegateAsFuture.cause();
  }

  @Override
  public boolean succeeded() {
    return delegateAsFuture.succeeded();
  }

  @Override
  public boolean failed() {
    return delegateAsFuture.failed();
  }

  @Override
  public <U> Future<U> compose(Function<T, Future<U>> successMapper, Function<Throwable, Future<U>> failureMapper) {
    return delegateAsFuture.compose(successMapper, failureMapper);
  }

  @Override
  public <U> Future<U> transform(Function<AsyncResult<T>, Future<U>> mapper) {
    return delegateAsFuture.transform(mapper);
  }

  @Override
  public <U> Future<T> eventually(Function<Void, Future<U>> mapper) {
    return delegateAsFuture.eventually(mapper);
  }

  @Override
  public <U> Future<U> map(Function<T, U> mapper) {
    return delegateAsFuture.map(mapper);
  }

  @Override
  public <V> Future<V> map(V value) {
    return delegateAsFuture.map(value);
  }

  @Override
  public Future<T> otherwise(Function<Throwable, T> mapper) {
    return delegateAsFuture.otherwise(mapper);
  }

  @Override
  public Future<T> otherwise(T value) {
    return delegateAsFuture.otherwise(value);
  }

  @Override
  public boolean tryFail(Throwable throwable) {
    return delegateAsPromise.tryFail(throwable);
  }

  @Override
  public boolean tryFail(String message) {
    return delegateAsPromise.tryFail(message);
  }
}