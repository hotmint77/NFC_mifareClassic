package ir.bistcard.nfcconnector;

/*
  Created by hotmi on 2/23/2023.
 */
public class Utils {
  // Access Bit Calculator
  //http://calc.gmss.ru/Mifare1k/

  public static String byteArrayToHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b & 0xff)+" ");
    }
    return sb.toString();
  }
  public static byte[] hexStringToByteArray(String hexString) {
    int length = hexString.length();
    byte[] result = new byte[length / 2];
    for (int i = 0; i < length; i += 2) {
      result[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
        + Character.digit(hexString.charAt(i+1), 16));
    }
    return result;
  }
}
