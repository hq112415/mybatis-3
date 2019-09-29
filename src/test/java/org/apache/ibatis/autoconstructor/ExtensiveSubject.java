package org.apache.ibatis.autoconstructor;

public class ExtensiveSubject {
  private final byte aByte;
  private final short aShort;
  private final char aChar;
  private final int anInt;
  private final long aLong;
  private final float aFloat;
  private final double aDouble;
  private final boolean aBoolean;
  private final String aString;

  // enum types
  private final TestEnum anEnum;

  // array types

  // string to lob types:
  private final String aClob;
  private final String aBlob;

  public ExtensiveSubject(final byte aByte,
                          final short aShort,
                          final char aChar,
                          final int anInt,
                          final long aLong,
                          final float aFloat,
                          final double aDouble,
                          final boolean aBoolean,
                          final String aString,
                          final TestEnum anEnum,
                          final String aClob,
                          final String aBlob) {
    this.aByte = aByte;
    this.aShort = aShort;
    this.aChar = aChar;
    this.anInt = anInt;
    this.aLong = aLong;
    this.aFloat = aFloat;
    this.aDouble = aDouble;
    this.aBoolean = aBoolean;
    this.aString = aString;
    this.anEnum = anEnum;
    this.aClob = aClob;
    this.aBlob = aBlob;
  }

  public enum TestEnum {
    AVALUE, BVALUE, CVALUE;
  }
}
