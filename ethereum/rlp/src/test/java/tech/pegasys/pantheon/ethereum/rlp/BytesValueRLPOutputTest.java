/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.rlp;

import static org.junit.Assert.assertEquals;

import tech.pegasys.pantheon.util.bytes.BytesValue;

import org.junit.Test;

public class BytesValueRLPOutputTest {

  private static BytesValue h(final String hex) {
    return BytesValue.fromHexString(hex);
  }

  private static String times(final String base, final int times) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < times; i++) sb.append(base);
    return sb.toString();
  }

  @Test
  public void empty() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    assertEquals(BytesValue.EMPTY, out.encoded());
  }

  @Test
  public void singleByte() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeByte((byte) 1);

    // Single byte should be encoded as itself
    assertEquals(h("0x01"), out.encoded());
  }

  @Test
  public void singleByteLowerBoundary() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeByte((byte) 0);
    assertEquals(h("0x00"), out.encoded());
  }

  @Test
  public void singleByteUpperBoundary() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeByte((byte) 0x7f);
    assertEquals(h("0x7f"), out.encoded());
  }

  @Test
  public void singleShortElement() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeByte((byte) 0xFF);

    // Bigger than single byte: 0x80 + length then value, where length is 1.
    assertEquals(h("0x81FF"), out.encoded());
  }

  @Test
  public void singleBarelyShortElement() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("2b", 55)));

    // 55 bytes, so still short: 0x80 + length then value, where length is 55.
    assertEquals(h("0xb7" + times("2b", 55)), out.encoded());
  }

  @Test
  public void singleBarelyLongElement() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("2b", 56)));

    // 56 bytes, so long element: 0xb7 + length of value size + value, where the value size is 56.
    // 56 is 0x38 so its size is 1 byte.
    assertEquals(h("0xb838" + times("2b", 56)), out.encoded());
  }

  @Test
  public void singleLongElement() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("3c", 2241)));

    // 2241 bytes, so long element: 0xb7 + length of value size + value, where the value size is
    // 2241,
    // 2241 is 0x8c1 so its size is 2 bytes.
    assertEquals(h("0xb908c1" + times("3c", 2241)), out.encoded());
  }

  @Test
  public void singleLongElementBoundaryCase_1() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("3c", 255)));
    assertEquals(h("0xb8ff" + times("3c", 255)), out.encoded());
  }

  @Test
  public void singleLongElementBoundaryCase_2() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("3c", 256)));
    assertEquals(h("0xb90100" + times("3c", 256)), out.encoded());
  }

  @Test
  public void singleLongElementBoundaryCase_3() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("3c", 65535)));
    assertEquals(h("0xb9ffff" + times("3c", 65535)), out.encoded());
  }

  @Test
  public void singleLongElementBoundaryCase_4() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("3c", 65536)));
    assertEquals(h("0xba010000" + times("3c", 65536)), out.encoded());
  }

  @Test
  public void singleLongElementBoundaryCase_5() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("3c", 16777215)));
    assertEquals(h("0xbaffffff" + times("3c", 16777215)), out.encoded());
  }

  @Test
  public void singleLongElementBoundaryCase_6() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeBytesValue(h(times("3c", 16777216)));
    assertEquals(h("0xbb01000000" + times("3c", 16777216)), out.encoded());
  }

  @Test(expected = IllegalStateException.class)
  public void multipleElementAddedWithoutList() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeByte((byte) 0);
    out.writeByte((byte) 1);
  }

  @Test
  public void longScalar() {
    // Scalar should be encoded as the minimal byte array representing the number. For 0, that means
    // the empty byte array, which is a short element of zero-length, so 0x80.
    assertLongScalar(h("0x80"), 0);

    assertLongScalar(h("0x01"), 1);
    assertLongScalar(h("0x0F"), 15);
    assertLongScalar(h("0x820400"), 1024);
  }

  private void assertLongScalar(final BytesValue expected, final long toTest) {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.writeLongScalar(toTest);
    assertEquals(expected, out.encoded());
  }

  @Test
  public void emptyList() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    out.endList();

    assertEquals(h("0xc0"), out.encoded());
  }

  @Test(expected = IllegalStateException.class)
  public void unclosedList() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    out.encoded();
  }

  @Test(expected = IllegalStateException.class)
  public void closeUnopenedList() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.endList();
  }

  @Test
  public void simpleShortList() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    out.writeByte((byte) 0x2c);
    out.writeByte((byte) 0x3b);
    out.endList();

    // List with payload size = 2 (both element are single bytes)
    // so 0xc0 + size then payloads
    assertEquals(h("0xc22c3b"), out.encoded());
  }

  @Test
  public void simpleShortListUpperBoundary() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    for (int i = 0; i < 55; i++) {
      out.writeByte((byte) 0x3c);
    }
    out.endList();
    assertEquals(h("0xf7" + times("3c", 55)), out.encoded());
  }

  @Test
  public void simpleLongListLowerBoundary() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    for (int i = 0; i < 56; i++) {
      out.writeByte((byte) 0x3c);
    }
    out.endList();
    assertEquals(h("0xf838" + times("3c", 56)), out.encoded());
  }

  @Test
  public void simpleLongListBoundaryCase_1() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    for (int i = 0; i < 255; i++) {
      out.writeByte((byte) 0x3c);
    }
    out.endList();
    assertEquals(h("0xf8ff" + times("3c", 255)), out.encoded());
  }

  @Test
  public void simpleLongListBoundaryCase_2() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    for (int i = 0; i < 256; i++) {
      out.writeByte((byte) 0x3c);
    }
    out.endList();
    assertEquals(h("0xf90100" + times("3c", 256)), out.encoded());
  }

  @Test
  public void simpleLongListBoundaryCase_3() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    for (int i = 0; i < 65535; i++) {
      out.writeByte((byte) 0x3c);
    }
    out.endList();
    assertEquals(h("0xf9ffff" + times("3c", 65535)), out.encoded());
  }

  @Test
  public void simpleLongListBoundaryCase_4() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    for (int i = 0; i < 65536; i++) {
      out.writeByte((byte) 0x3c);
    }
    out.endList();
    assertEquals(h("0xfa010000" + times("3c", 65536)), out.encoded());
  }

  @Test
  public void simpleLongListBoundaryCase_5() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    for (int i = 0; i < 16777215; i++) {
      out.writeByte((byte) 0x3c);
    }
    out.endList();
    assertEquals(h("0xfaffffff" + times("3c", 16777215)), out.encoded());
  }

  @Test
  public void simpleLongListBoundaryCase_6() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    for (int i = 0; i < 16777216; i++) {
      out.writeByte((byte) 0x3c);
    }
    out.endList();
    assertEquals(h("0xfb01000000" + times("3c", 16777216)), out.encoded());
  }

  @Test
  public void simpleNestedList() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();
    out.writeByte((byte) 0x2c);
    // Nested list has 2 simple elements, so will be 0xc20312
    out.startList();
    out.writeByte((byte) 0x03);
    out.writeByte((byte) 0x12);
    out.endList();
    out.writeByte((byte) 0x3b);
    out.endList();

    // List payload size = 5 (2 single bytes element + nested list of size 3)
    // so 0xc0 + size then payloads
    assertEquals(h("0xc52cc203123b"), out.encoded());
  }
}
