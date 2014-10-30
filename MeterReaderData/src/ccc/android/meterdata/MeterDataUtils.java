package ccc.android.meterdata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MeterDataUtils 
{
    /**
     * Decode string to image
     * @param imageString The string to decode
     * @return decoded image
     */
    public static byte[] decodeToImage(String imageString) {

        byte[] imageByte = null;
        try {
        	imageByte = decode(imageString);
        	//imageByte = ConvertBinaryToByteArray(imageByte);
//            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
//            image = ImageIO.read(bis);
//            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageByte;
    }

    /**
     * Encode image to string
     * @param image The image to encode
     * @param type jpeg, bmp, ...
     * @return encoded string
     */
    public static String encodeToBase64String(byte[] imageBytes) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            imageString = encode(imageBytes);//ConvertByteToBitArray(imageBytes));

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }
    
    public static byte[] ConvertByteToBitArray(byte[] byteArray)
    {
    	byte[] bitArray = new byte[(int)Math.ceil(byteArray.length/8d)];
        for (int i = 0; i < byteArray.length; i++)
        {
            if (byteArray[i] < 0)
            	bitArray = SetBit(bitArray, i, 1);
            else
            	bitArray = SetBit(bitArray, i, 0);
        }
        return bitArray;
    }
    
    public static int GetBit(byte b, int bitNumber)
    {
        return (b & (1 << bitNumber)) != 0 ? 1 : 0;
    }

    public static byte[] SetBit(byte[] byteArray, int bitIndex, int bit)
    {
        if (bit > 1 || bit < 0)
            throw new IllegalArgumentException("bit has to be 0 or 1");
        int byteIndex = bitIndex / 8;
        int bitInByteIndex = bitIndex % 8;
        byte mask = (byte)(1 << bitInByteIndex);

        if (bit == 1)
            byteArray[byteIndex] |= mask;
        else
            byteArray[byteIndex] &= (byte)~mask;
        return byteArray;
    }
    
    public static byte[] ConvertBinaryToByteArray(byte[] bitArray)
    {
        byte[] output = new byte[(int)Math.ceil((double)(bitArray.length) * 8)];
        for (int i = 0; i < bitArray.length; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                if (GetBit(bitArray[i], j) == 1)
                    output[i * 8 + j] = -1;
                else
                    output[i * 8 + j] = 0;
            }
        }
        return output;
    }
    
    public final static String encode(byte[] d)
    {
      if (d == null) return null;
      byte data[] = new byte[d.length+2];
      System.arraycopy(d, 0, data, 0, d.length);
      byte dest[] = new byte[(data.length/3)*4];

      // 3-byte to 4-byte conversion
      for (int sidx = 0, didx=0; sidx < d.length; sidx += 3, didx += 4)
      {
        dest[didx]   = (byte) ((data[sidx] >>> 2) & 077);
        dest[didx+1] = (byte) ((data[sidx+1] >>> 4) & 017 |
                    (data[sidx] << 4) & 077);
        dest[didx+2] = (byte) ((data[sidx+2] >>> 6) & 003 |
                    (data[sidx+1] << 2) & 077);
        dest[didx+3] = (byte) (data[sidx+2] & 077);
      }

      // 0-63 to ascii printable conversion
      for (int idx = 0; idx <dest.length; idx++)
      {
        if (dest[idx] < 26)     dest[idx] = (byte)(dest[idx] + 'A');
        else if (dest[idx] < 52)  dest[idx] = (byte)(dest[idx] + 'a' - 26);
        else if (dest[idx] < 62)  dest[idx] = (byte)(dest[idx] + '0' - 52);
        else if (dest[idx] < 63)  dest[idx] = (byte)'+';
        else            dest[idx] = (byte)'/';
      }

      // add padding
      for (int idx = dest.length-1; idx > (d.length*4)/3; idx--)
      {
        dest[idx] = (byte)'=';
      }
      return new String(dest);
    }

    /**
     * Encode a String using Base64 using the default platform encoding
     **/
    public final static String encode(String s) {
      return encode(s.getBytes());
    }

    /**
     *  Decode data and return bytes.
     */
    public final static byte[] decode(String str)
    {
    	byte[] data = null;
      if (str == null)  return  null;
      data = decode(str.getBytes());
      return data;
    }

    /**
     *  Decode data and return bytes.  Assumes that the data passed
     *  in is ASCII text.
     */
    public final static byte[] decode(byte[] data)
    {

      int tail = data.length;
      while (data[tail-1] == '=')  tail--;
      byte dest[] = new byte[tail - data.length/4];

      // ascii printable to 0-63 conversion
      for (int idx = 0; idx <data.length; idx++)
      {
    	char chr = (char) data[idx];
        if (data[idx] == '=')    data[idx] = 0;
        else if (data[idx] == '/') data[idx] = 63;
        else if (data[idx] == '+') data[idx] = 62;
        else if (data[idx] >= '0'  &&  data[idx] <= '9')
          data[idx] = (byte)(data[idx] - ('0' - 52));
        else if (data[idx] >= 'a'  &&  data[idx] <= 'z')
          data[idx] = (byte)(data[idx] - ('a' - 26));
        else if (data[idx] >= 'A'  &&  data[idx] <= 'Z')
          data[idx] = (byte)(data[idx] - 'A');
        	
      }

      // 4-byte to 3-byte conversion
      int sidx, didx;
      for (sidx = 0, didx=0; didx < dest.length-2; sidx += 4, didx += 3)
      {
        dest[didx]   = (byte) ( ((data[sidx] << 2) & 255) |
                ((data[sidx+1] >>> 4) & 3) );
        dest[didx+1] = (byte) ( ((data[sidx+1] << 4) & 255) |
                ((data[sidx+2] >>> 2) & 017) );
        dest[didx+2] = (byte) ( ((data[sidx+2] << 6) & 255) |
                (data[sidx+3] & 077) );
      }
      if (didx < dest.length)
      {
        dest[didx]   = (byte) ( ((data[sidx] << 2) & 255) |
                ((data[sidx+1] >>> 4) & 3) );
      }
      if (++didx < dest.length)
      {
        dest[didx]   = (byte) ( ((data[sidx+1] << 4) & 255) |
                ((data[sidx+2] >>> 2) & 017) );
      }
      return dest;
    }
}
