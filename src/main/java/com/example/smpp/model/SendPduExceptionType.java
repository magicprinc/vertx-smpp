package com.example.smpp.model;

public enum SendPduExceptionType {
  WINDOW_TIMEOUT,
  CHANNEL_CLOSED,
  WRONG_OPERATION,
  BIND_REFUSED,
  REQUEST_DISCARDED_ON_CLOSE,
  WRITE_TO_CHANNEL_FAILED,
  GENERIC_NACK_RECEIVED,
}
