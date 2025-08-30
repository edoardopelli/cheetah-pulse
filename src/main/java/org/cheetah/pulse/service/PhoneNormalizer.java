// service/PhoneNormalizer.java
package org.cheetah.pulse.service;

import org.springframework.stereotype.Component;

@Component
public class PhoneNormalizer {
  public String normalize(String phone) {
    return phone.replaceAll("\\s+", "");
  }
}