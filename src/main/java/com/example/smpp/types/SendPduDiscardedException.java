package com.example.smpp.types;

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

import com.example.smpp.model.SendPduExceptionType;

public class SendPduDiscardedException extends SendPduFailedException {

  public SendPduDiscardedException(String message) {
    super(message);
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.REQUEST_DISCARDED_ON_CLOSE;
  }
}