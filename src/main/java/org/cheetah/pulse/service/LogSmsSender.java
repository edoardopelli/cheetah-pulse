// Implementazione dev: log
package org.cheetah.pulse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile({"local","dev","test"})
class LogSmsSender implements SmsSender {
  @Override public void send(String phone, String message) {
    log.info("SMS to {} -> {}", phone, message);
  }
}