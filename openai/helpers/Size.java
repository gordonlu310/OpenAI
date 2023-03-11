package com.gordonlu.openai.helpers;

import com.google.appinventor.components.common.OptionList;
import com.google.appinventor.components.common.Default;

import java.util.*;
import java.io.*;
import java.lang.*;

public enum Size implements OptionList<String> {
  Square256("256x256"),
  Square512("512x512"),
  Square1024("1024x1024");

  private String size;

  Size(String s) {
    this.size = s;
  }

  public String toUnderlyingValue() {
    return size;
  }

  private static final Map<String, Size> lookup = new HashMap<>();

  static {
    for(Size s : Size.values()) {
      lookup.put(s.toUnderlyingValue(), s);
    }
  }

  public static Size fromUnderlyingValue(String s) {
    return lookup.get(s);
  }
}

