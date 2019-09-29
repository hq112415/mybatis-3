package org.apache.ibatis.autoconstructor;

public class BadSubject {
  private final int id;
  private final String name;
  private final int age;
  private final Height height;
  private final Double weight;

  public BadSubject(final int id, final String name, final int age, final Height height, final Double weight) {
    this.id = id;
    this.name = name;
    this.age = age;
    this.height = height;
    this.weight = weight == null ? 0 : weight;
  }

  private class Height {

  }
}
