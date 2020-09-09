package com.xueersi.downloadtask.downloadpool;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件帮助类
 *
 * @author shixiaoqiang
 */
public class FileHelper {
    /**
     * 文件夹
     */
    private final static String sFolderPath = "XueErSi";
    /**
     * 图片文件夹
     */
    private final static String sImagePath = "Images";

    /**
     * SD卡是否存在
     *
     * @return
     */
    public static boolean isSDCardExist() {
        String SDCardState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(SDCardState);
    }

    /**
     * 文件或文件夹是否存在
     *
     * @param path
     * @return
     */
    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    /**
     * 创建文件夹
     *
     * @param folderPath 文件夹路径
     */
    public static boolean createFolder(String folderPath) {
        if (TextUtils.isEmpty(folderPath)) {
            return false;
        }

        File folder = new File(folderPath);
        if (folder.exists()) {
            if (isFolderReadable(folderPath)) {
                // 路径存在并可以读取，不用重新创建
                return true;
            }
            // 路径存在但是无法读取，删除路径
            folder.delete();
        }
        return folder.mkdirs();
    }

    /**
     * 文件夹是否可以读取
     *
     * @param folderPath
     * @return
     */
    private static boolean isFolderReadable(String folderPath) {
        File tempFile = new File(folderPath + "temp.txt");
        FileOutputStream tempStream = null;
        try {
            tempStream = new FileOutputStream(tempFile);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                tempStream.close();
            } catch (Exception e) {
            }
            try {
                tempFile.delete();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 在指定路径下创建文件 若路径不存在，先创建路径
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @return 文件
     */
    public static File createFile(String path, String fileName) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(fileName)) {
            return null;
        }
        if (!createFolder(path)) {
            return null;
        }
        File file = new File(path + File.separator + fileName);
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 复制Asset中文件到指定地址
     *
     * @param context
     * @param assetFile asset文件夹下文件全路径
     * @param destPath  目标文件路径
     * @param destFile  目标文件文件名
     * @return
     */
    public static String copyAssestFile(Context context, String assetFile,
                                        String destPath, String destFile) {
        if (context == null || TextUtils.isEmpty(assetFile)
                || TextUtils.isEmpty(destPath) || TextUtils.isEmpty(destFile)) {
            return null;
        }
        boolean result = false;
        InputStream asset = null;
        File dest = null;
        OutputStream output = null;
        try {
            if (isExist(destFile)) {
                result = true;
            } else {
                asset = context.getAssets().open(assetFile);
                if (asset != null) {
                    dest = createFile(destPath, destFile);
                    if (dest != null) {
                        output = new FileOutputStream(dest);
                        if (output != null) {
                            int readLen = 0;
                            byte[] buf = new byte[1024];
                            while ((readLen = asset.read(buf)) != -1) {
                                output.write(buf, 0, readLen);
                            }
                            result = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
            }
            try {
                if (asset != null) {
                    asset.close();
                }
            } catch (Exception e) {
            }
            if (!result && dest != null) {
                dest.delete();
            }
        }
        return (result && dest != null) ? dest.getPath() : null;
    }

    /**
     * 拷贝一个文件 srcFile源文件 destFile目标文件
     *
     * @param srcFileString
     * @param destFileString
     * @return
     * @throws IOException
     */
    public static boolean copyFileTo(String srcFileString, String destFileString)
            throws IOException {
        if (TextUtils.isEmpty(srcFileString)
                || TextUtils.isEmpty(destFileString)) {
            return false;
        }
        File srcFile = new File(srcFileString);
        File destFile = new File(destFileString);
        if (srcFile.isDirectory() || destFile.isDirectory())
            return false;// 判断是否是文件
        FileInputStream fis = new FileInputStream(srcFile);
        FileOutputStream fos = new FileOutputStream(destFile);
        int readLen = 0;
        byte[] buf = new byte[1024];
        while ((readLen = fis.read(buf)) != -1) {
            fos.write(buf, 0, readLen);
        }
        fos.flush();
        fos.close();
        fos = null;
        fis.close();
        fis = null;
        return true;
    }

    /**
     * 获取手机内存可用空间
     */
    @SuppressWarnings("deprecation")
    public static long getFreeSpace(Context context) {
        if (context == null) {
            return 0;
        }
        File filesDir = context.getFilesDir();
        if (filesDir == null) {
            return 0;
        }
        StatFs statfs = new StatFs(filesDir.getPath());
        return (long) (statfs.getBlockSize()) * statfs.getAvailableBlocks();
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    public boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        return file.delete();
    }

    /**
     * 清空目录下所有文件和文件夹
     */
    public static void deleteDir(String rootPath) {
        if (TextUtils.isEmpty(rootPath)) {
            return;
        }
        File file = new File(rootPath);
        if (file == null || !file.exists()) {
            return;
        }
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                deleteFolderIncludeSelf(files[i]);
            }
        }
    }

    /**
     * 删除文件和文件夹
     *
     * @param dir
     */
    private static void deleteFolderIncludeSelf(File dir) {
        if (dir == null || !dir.exists())
            return;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files)
                    if (file.isDirectory())
                        deleteFolderIncludeSelf(file);
                    else
                        file.delete();
            }
            dir.delete();
        } else
            dir.delete();
    }


}
