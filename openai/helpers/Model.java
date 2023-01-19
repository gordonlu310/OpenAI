package com.gordonlu.openai.helpers;

import com.google.appinventor.components.common.OptionList;
import com.google.appinventor.components.common.Default;

import java.util.*;
import java.io.*;
import java.lang.*;

public enum Model implements OptionList<String> {
  Ada("text-ada-001"),
  Babbage("text-babbage-001"),
  Curie("text-curie-001"),
  Davinci("text-davinci-003");

  private String model;

  Model(String m) {
    this.model = m;
  }

  public String toUnderlyingValue() {
    return model;
  }

  private static final Map<String, Model> lookup = new HashMap<>();

  static {
    for(Model m : Model.values()) {
      lookup.put(m.toUnderlyingValue(), m);
    }
  }

  public static Model fromUnderlyingValue(String m) {
    return lookup.get(m);
  }
}

