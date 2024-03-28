package com.gen;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class GenDist {
    // 文件夹路径
    static String configPath = "D:\\Develop\\PayCompany\\SourceCode\\Aisapay-plus\\mchList.json";

    static String releasePath = "D:\\Develop\\PayCompany\\SourceCode\\Aisapay-plus\\dist-release";

    public static void main(String[] args) throws Exception {


        String agentPath = "D:\\Develop\\PayCompany\\SourceCode\\Aisapay-plus\\jeepay-ui-main\\jeepay-ui-agent\\dist";
        String mchPath = "D:\\Develop\\PayCompany\\SourceCode\\Aisapay-plus\\jeepay-ui-main\\jeepay-ui-merchant\\dist";
        String mgrPath = "D:\\Develop\\PayCompany\\SourceCode\\Aisapay-plus\\jeepay-ui-main\\jeepay-ui-manager\\dist";

        // 要替换的字符串
        String agentString = "http://agent-api.ausapay.com";
        String mchString = "http://mch-api.ausapay.com";
        String mgrString = "http://mgr-api.ausapay.com";


        //读取配置文件
        BufferedReader readerConfig = new BufferedReader(new FileReader(configPath));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = readerConfig.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        readerConfig.close();
        JSONArray configs = JSONArray.parseArray(stringBuilder.toString());

//        {
//            "name": "bailian-pay",
//                "agentUrl": "http://agent-api.bailian-pay.com",
//                "mchUrl": "http://mch-api.bailian-pay.com",
//                "mgrUrl": "http://mgr-api.bailian-pay.com"
//        },
        //清空原来的，新建文件夹

        deleteFolder(releasePath);
        File fileRelease = new File(releasePath);
        fileRelease.mkdir();

        for (int i = 0; i < configs.size(); i++) {
            JSONObject configItem = configs.getJSONObject(i);
            String name = configItem.getString("name");

//            String agentPrefix = "agent-dist";
//            String agentTargetPath = releasePath + "\\" + name + "\\" + agentPrefix;
//            String agentNewStr = configItem.getString("agentUrl");
//            execReplaceAndCopy(agentPath, agentTargetPath, agentString, agentNewStr, name, agentPrefix);
//
//            String mchPrefix = "mch-dist";
//            String mchTargetPath = releasePath + "\\" + name + "\\" + mchPrefix;
//            String mchNewStr = configItem.getString("mchUrl");
//            execReplaceAndCopy(mchPath, mchTargetPath, mchString, mchNewStr, name, mchPrefix);

            String mgrPrefix = "mgr-dist";
            String mgrTargetPath = releasePath + "\\" + name + "\\" + mgrPrefix;
            String mgrNewStr = configItem.getString("mgrUrl");
            execReplaceAndCopy(mgrPath, mgrTargetPath, mgrString, mgrNewStr, name, mgrPrefix);
        }


    }

    private static void execReplaceAndCopy(String sourcePath, String targetPath, String oldSrc, String newSrc, String name, String prefix) throws Exception {
        // xxx/xxpay/agent-dist
        System.out.println(name + " " + prefix + " start");
        //复制到新地址
        copyDir(sourcePath, targetPath);
        // 替换后的字符串
        // 递归遍历文件夹中的所有文件
        File folder = new File(targetPath);
        replaceInFiles(folder, oldSrc, newSrc);
        //压缩zip
        zipFolderContents(targetPath, releasePath + "\\" + name + "\\" + prefix + ".zip");
        System.out.println(name + " " + prefix + " end");
        deleteFolder(targetPath);
    }

    private static void replaceInFiles(File folder, String searchString, String replaceString) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    replaceInFiles(file, searchString, replaceString);
                }
            }
        } else if (folder.isFile()) {
            replaceInFile(folder, searchString, replaceString);
        }
    }

    private static void replaceInFile(File file, String searchString, String replaceString) {
        try {
            if(!file.getName().contains(".js")){
                return;
            }
            // 读取文件内容
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();

            // 写入替换后的内容
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(stringBuilder.toString().replaceAll(searchString, replaceString));
            writer.close();

            log.info("替换完成：" + file.getAbsolutePath());
        } catch (Exception e) {
            log.error("替换失败：" + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * 复制文件夹
     *
     * @param oldDir 原来的目录
     * @param newDir 复制到哪个目录
     */
    public static void copyDir(String oldDir, String newDir) {
        File srcDir = new File(oldDir);
        // 判断文件是否不存在或是否不是文件夹
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            throw new IllegalArgumentException("参数错误");
        }
        File destDir = new File(newDir);
        if (!destDir.exists()) {
            // 不存在就创建目录
            if (destDir.mkdirs()) {
                // 列出目录中的文件
                File[] files = srcDir.listFiles();
                for (File f : files) {
                    // 是文件就调用复制文件方法 是目录就继续调用复制目录方法
                    if (f.isFile()) {
                        copyFile(f, new File(newDir, f.getName()));
                    } else if (f.isDirectory()) {
                        copyDir(oldDir + File.separator + f.getName(),
                                newDir + File.separator + f.getName());
                    }
                }
            }
        }
    }


    /**
     * 复制文件
     *
     * @param oldDir 原来的文件
     * @param newDir 复制到的文件
     */
    public static void copyFile(File oldDir, File newDir) {
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        byte[] b = new byte[1024];
        try {
            // 将要复制文件输入到缓冲输入流
            bufferedInputStream = new BufferedInputStream(new FileInputStream(oldDir));
            // 将复制的文件定义为缓冲输出流
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(newDir));
            // 定义字节数
            int len;
            while ((len = bufferedInputStream.read(b)) > -1) {
                // 写入文件
                bufferedOutputStream.write(b, 0, len);
            }
            //刷新此缓冲输出流
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedInputStream != null) {
                try {
                    // 关闭流
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void zipFolderContents(String sourceFolderPath, String zipFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFilePath);
        ZipOutputStream zos = new ZipOutputStream(fos);
        File sourceFolder = new File(sourceFolderPath);

        addFolderContentsToZip(sourceFolder, "", zos);
        zos.close();
        fos.close();
    }

    private static void addFolderContentsToZip(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderContentsToZip(file, parentFolder + file.getName() + "/", zos);
                continue;
            }

            FileInputStream fis = new FileInputStream(file);
            String entryName = parentFolder + file.getName();
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            fis.close();
        }
    }

    public static void deleteFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file.getAbsolutePath());
                }
            }
        }
        folder.delete();
    }
}
