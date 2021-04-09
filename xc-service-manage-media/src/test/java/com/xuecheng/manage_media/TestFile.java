package com.xuecheng.manage_media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TestFile {

//    测试文件分块
    @Test
    public void testChunk() throws IOException {
        //源文件
        File courseFile = new File("D:\\upload\\lucene.avi");
        //块文件目录
        String fileChunk = "D:\\upload\\number\\";

        //先定义块文件大小
        long chunkFileSize = 1 * 1024 * 1024;

        //块数 源文件大小 *1 /块文件大小1M 向上转型
        long chunkFileNumber = (long) Math.ceil(courseFile.length() * 1.0 / chunkFileSize);

        //创建读文件对象
        RandomAccessFile randomAccessFile = new RandomAccessFile(courseFile,"r");

        //缓冲区
        byte[] bytes = new byte[1024];
        //分块
        for (int i = 0; i < chunkFileNumber; i++) {
            //块文件
            File file = new File(fileChunk+i);
            int len = -1;
            //创建向块文件的写对象
            RandomAccessFile raf_write = new RandomAccessFile(file,"rw");
            while ((len = randomAccessFile.read(bytes))!=-1){
                raf_write.write(bytes,0,len);
                //如果块文件的大小达到1M ，就开始下一次
                if (file.length() >= chunkFileSize){
                    break;
                }
            }
            raf_write.close();
        }
        randomAccessFile.close();
    }

    @Test
    public void testMerge() throws IOException {
        //块文件目录
        String fileChunk = "D:\\upload\\number\\";
        //块文件列表
        File file = new File(fileChunk);
        File[] files = file.listFiles();
        //将块文件排序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });
        //合并文件
        File courseFile = new File("D:\\upload\\lucene.avi");
        //创建新的文件
        boolean newFile = courseFile.createNewFile();

        //创建写的对象
        RandomAccessFile randomAccessFile = new RandomAccessFile(courseFile,"rw");

        //缓存区
        byte[] bytes =new byte[1024];
        for (File chunkFile : fileList) {
            //创建一个读块文件的对象
            RandomAccessFile randomAccess = new RandomAccessFile(chunkFile,"r");
            int let = -1;
            while ((let = randomAccess.read(bytes)) !=-1){
                randomAccessFile.write(bytes,0,let);
            }
            randomAccess.close();
        }
        randomAccessFile.close();
    }

    @Test
    public void testMd5() throws IOException {
        File file = new File("D:\\xc\\video\\c\\5\\ c5c75d70f382e6016d2f506d134eee11\\c5c75d70f382e6016d2f506d134eee11.avi");
        FileInputStream fileInputStream = new FileInputStream(file);
        String md5Hex = DigestUtils.md5Hex(fileInputStream);

        System.out.println(md5Hex);

    }
}
