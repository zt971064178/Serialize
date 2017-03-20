package com.redissdk.redis.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import cn.okline.SevenZip.Compression.LZMA.Decoder;
import cn.okline.SevenZip.Compression.LZMA.Encoder;




public class ByteCompressUtils
{
    
    /***
     * 压缩GZip
     * 
     * @param data
     * @return
     */
    public static byte[] gZip(byte[] data)
    {
        byte[] b = null;
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            b = bos.toByteArray();
            bos.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return b;
    }

    /***
     * 解压GZip
     * 
     * @param data
     * @return
     */
    public static byte[] unGZip(byte[] data)
    {
        byte[] b = null;
        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1)
            {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return b;
    }
    
    public static byte[] compressByteLZMA(byte[] bytesOut)
    {
        byte[] outbytes = null;
        try
        {
            Encoder coder = new Encoder();
            ByteArrayInputStream input = new ByteArrayInputStream(bytesOut);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] header = ".lza".getBytes();
            
            //Write file flag
            out.write(header, 0, 4);
            
            // Write the encoder properties
            coder.WriteCoderProperties(out);

            // Write the decompressed byte size.
            out.write(ByteUtils.longToByte(bytesOut.length), 0, 8);

            // Encode the byte.
            coder.Code(input, out, bytesOut.length, -1, null);
            out.flush();
            outbytes = out.toByteArray();
            out.close();
            input.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return outbytes;
    }

    public static byte[] decompressByteLZMA(byte[] bytes)
    {
        byte[] outbytes = null;
        try
        {

            Decoder coder = new Decoder();
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] header = new byte[4];
            
            //Read file flag
            input.read(header, 0, 4);
            
            //Determine whether the file is normal lzma.
            if(!".lza".equals(new String(header)))
            {
                throw new NonLZMAFileException("This bytes is not the correct LZMA compression format");
            }
            
            // Read the decoder properties
            byte[] properties = new byte[5];
            input.read(properties, 0, 5);

            // Read in the decompress byte size.
            byte[] byteLengthOfBytes = new byte[8];
            input.read(byteLengthOfBytes, 0, 8);
            long fileLength = ByteUtils.byteToLong(byteLengthOfBytes);

            // Decompress the byte.
            coder.SetDecoderProperties(properties);

            coder.Code(input, out, fileLength);

            out.flush();
            outbytes = out.toByteArray();
            out.close();
            input.close();
        }
        catch (IOException | NonLZMAFileException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return outbytes;
    }

}
