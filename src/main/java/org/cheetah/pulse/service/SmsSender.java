// service/SmsSender.java
package org.cheetah.pulse.service;

public interface SmsSender {
  void send(String phone, String message);
}

