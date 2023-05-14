package com.gordonlu.openai.helpers;

import com.google.appinventor.components.common.OptionList;
import com.google.appinventor.components.common.Default;

import java.util.*;
import java.io.*;
import java.lang.*;

public enum Role implements OptionList<String> {
  User("user"),
  System("system"),
  Assistant("assistant");

  private String role;

  Role(String r) {
    this.role = r;
  }

  public String toUnderlyingValue() {
    return role;
  }

  private static final Map<String, Role> lookup = new HashMap<>();

  static {
    for(Role r : Role.values()) {
      lookup.put(r.toUnderlyingValue(), r);
    }
  }

  public static Role fromUnderlyingValue(String r) {
    return lookup.get(r);
  }
}

