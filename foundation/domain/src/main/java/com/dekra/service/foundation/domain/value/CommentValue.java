package com.dekra.service.foundation.domain.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@SuppressWarnings("ClassCanBeRecord")
public final class CommentValue {
  private final String value;

  private CommentValue(String value) {
    if (value == null) {
      value = "";
    }
    this.value = value;
  }

  public static CommentValue of(String value) {
    return new CommentValue(value);
  }
}
