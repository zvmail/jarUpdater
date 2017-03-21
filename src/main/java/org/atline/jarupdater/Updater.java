package org.atline.jarupdater;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.atline.jarupdater.utils.Finder;
import org.atline.jarupdater.utils.HttpClientUtil;
import org.atline.jarupdater.utils.PathUtil;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Updater {
    /**
     * 更新本地的版本号
     * @param properties
     */
	private static void updateLocalVersion(Properties properties) {
        try {
            OutputStream os = new FileOutputStream(PathUtil.getPath() + "/version.txt");
            properties.store(os, "This file is auto generated, do not modify it.");
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * 从远程下载指定文件
	 * @param updateSite
	 * @param jarRepo
	 * @param jarString
	 */
    private static boolean downloadJar(String updateSite, String jarRepo, String jarString) {
        try {
            String jarRepoPath = "";
            if ("".equals(jarRepo)) {
                jarRepoPath = PathUtil.getPath();
            } else {
                jarRepoPath = PathUtil.getPath() + "/" + jarRepo;
                File f = new File(jarRepoPath);
                if (!f.exists() && !f.isDirectory()) {
                    f.mkdirs();
                }
            }

            FileOutputStream fos = new FileOutputStream(jarRepoPath + "/" + jarString);
            System.out.println("Download from "+updateSite+ "/" + jarString);
            HttpClientUtil.downloadBinary(updateSite + "/" + jarString, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    final static String UPDATE = "-UPDATE"; //成功下载，待更新
    final static String ERROR = "DOWNLOADERROR"; //下载失败
    final static String VERSION = "version";
    
    /**
     * 下载远程文件到本地文件夹
     * @param updateSite
     * @param jarRepo
     * @return
     */
    public static String download(String updateSite, String jarRepo) {
        // get remote version info
        String remoteVersion = "";
        HashMap<String , String> remoteJarMap = new HashMap<String , String>();
        
        String str = HttpClientUtil.getInfo(updateSite + "/version.txt");

        if(str.isEmpty())return "";
        
        try {
        	Properties pRemote = new Properties();
            pRemote.load(new StringReader(str));
            Enumeration<?> en = pRemote.propertyNames();
            while (en.hasMoreElements()){
                String k = (String) en.nextElement();
                if (VERSION.equals(k)) {
                    remoteVersion = pRemote.getProperty(k);
                } else {
                    remoteJarMap.put(k, pRemote.getProperty(k));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in Get updateSite version info.");
        }

        // get local version info
        String found = Finder.findVersions();

        if ("".equals(found)) {
            found = PathUtil.getPath() + "/version.txt";
            File f = new File(found);
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Properties pLocal = new Properties();
            pLocal.load(new FileInputStream(found));
            String localVersion = pLocal.getProperty(VERSION, "") ;
            HashMap<String , String> localJarMap = new HashMap<String , String>();
            
            //检查2边版本不相同时，开始下载更新文件
            if (!localVersion.equals(remoteVersion)) {
                System.out.println("Download update site changes, waiting ...");
                
                Properties pOutput = new Properties();
                pOutput.setProperty(VERSION,localVersion);
                
                //得到本地文件列表
                Enumeration<?> en = pLocal.propertyNames();
                while (en.hasMoreElements()){
                    String k = (String) en.nextElement();
                    if (!VERSION.equals(k)) {
                        localJarMap.put(k, pLocal.getProperty(k));
                    }
                }

                //循环待更新文件，比较本地文件下载情况
                // md5 compare and download jar if needed
                Iterator remoteIter = remoteJarMap.entrySet().iterator();
                while (remoteIter.hasNext()) {
                    Map.Entry entry = (Map.Entry) remoteIter.next();
                    String key = (String)entry.getKey();
                    String val = (String)entry.getValue();
                    String lovalval=localJarMap.get(key);
                    if(lovalval==null)lovalval="";
                    
                    //如果版本不相同
                    if (checkVersion(val,lovalval)) {
                        if(downloadJar(updateSite, jarRepo,  key)) {
                        	pOutput.setProperty(key,val + UPDATE);
                        }
                        else{
                        	//失败时，下次会继续更新
                        	pOutput.setProperty(key,ERROR);
                        }
                    }else{
                    	pOutput.setProperty(key,lovalval);
                    }
                    //用于删除unused jar
                    //localJarMap.remove(key);
                }

//                // delete unused jar
//                Iterator localIter = localJarMap.entrySet().iterator();
//                while (localIter.hasNext()) {
//                    Map.Entry entry = (Map.Entry) localIter.next();
//                    Object key = entry.getKey();
//
//                    String jarRepoPath = "";
//                    if ("".equals(jarRepo)) {
//                        jarRepoPath = PathUtil.getPath();
//                    } else {
//                        jarRepoPath = PathUtil.getPath() + "/" + jarRepo;
//                    }
//                    File f = new File(jarRepoPath + "/" + key);
//                    if (f.exists()) {
//                        f.delete();
//                    }
//                }

                System.out.println("Update local version.txt");
                // update local versions
                Updater.updateLocalVersion(pOutput);
                
                return remoteVersion;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * 检查2边文件版本，先去掉PENDING
     * @param val
     * @param str
     * @return
     */
    private static boolean checkVersion(String val, String str) {
    	String tmp = val;
		if(str.endsWith(UPDATE)){
			tmp = val + UPDATE;
		}
		
		return !tmp.equals(str);
	}

	/**
     * 检查文件更新成功
     * @return
     */
    public static Set<String> getUpdateFiles(Properties pLocal){
    	HashSet<String> localJarMap = new HashSet<String>();

        Enumeration<?> en = pLocal.propertyNames();
        while (en.hasMoreElements()){
            String k = (String) en.nextElement();
            String v = pLocal.getProperty(k);
            
            if (v.equals(ERROR)) {
                localJarMap.clear();
                break;
            }
            if (v.contains(UPDATE)) {
                localJarMap.add(k);
            }
        }

        return localJarMap;
    }
    /**
     * 循环需要更新的文件，依次复制到真实目录，保证复制成功
     * @param temRepo
     * @param jarRepo
     */
    public static void update(String newVersion, String temRepo, String jarRepo){
    	
    	// get local version info
    	String found = Finder.findVersions();
        Properties pLocal = new Properties();
        try {
        	pLocal.load(new FileInputStream(found));
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        
    	//得到更新文件列表
        HashSet<String> list = new HashSet<String>();

        Enumeration<?> en = pLocal.propertyNames();
        while (en.hasMoreElements()){
            String k = (String) en.nextElement();
            String v = pLocal.getProperty(k);
            
            if (v.equals(ERROR)) {
            	list.clear();
                break;
            }
            if (v.contains(UPDATE)) {
            	list.add(k);
            	pLocal.setProperty(k, v.substring(0, v.length() - UPDATE.length()));
            }
        }

        if(list.isEmpty()){
        	//no need update
        	return;
        }else{
        	//如果下载成功，读取标记，复制tmp文件到正式目录
            if(updateFiles(list, temRepo, jarRepo)){
            	//文件更新成功，修改local version
            	pLocal.setProperty(VERSION, newVersion);
            	Updater.updateLocalVersion(pLocal);
            }else{
            	//试图恢复原来的文件（未实现）
            }
        }
    }
    
    static boolean updateFiles(Set<String>updateFileList, String temRepo, String jarRepo){
    	//the file will first be copied to the update directory
		System.out.println("Replace obsolete files...");
		
    	for(String fileName : updateFileList){
			String tmpFileName = PathUtil.getPath()+ "/" + temRepo + "/" + fileName;
			String newFileName = PathUtil.getPath()+ "/" + jarRepo + "/" +fileName;
			
			File uploadFile = new File(newFileName);
			if(uploadFile.exists()){
//				//备份文件，暂时未实现				
//				File bakFile = new File(getBakFileName(newFileName));
//				try{
//					bakFile.delete();
//				}catch(Exception ex){
//					ex.printStackTrace();
//				}
//				
//				if(!uploadFile.renameTo(bakFile))
//					return false;
				
				if(!uploadFile.delete())
					return false;
			}

			try {
				Files.copy(Paths.get(tmpFileName), Paths.get(newFileName), REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			System.out.println(newFileName + " updated.");

    	}
    	
    	return true;
    }

    /*
     * 返回备份文件名
     */
	private static String getBakFileName(String fileName) {
		String bak = ".bak";
	
		int dot = fileName.lastIndexOf('.');   
        if ((dot > -1) && (dot < (fileName.length()))) {   
            return fileName.substring(0, dot) + bak;   
        }else{
        	return fileName + bak;
        }

	}
	
	/**
	 * 返回文件的MD5
	 * @param fileName
	 * @return
	 */
	public static String getMD5(String fileName){
        FileInputStream fis=null;
        String md5 = "";
		try {
			fis = new FileInputStream(fileName);
			md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fis));    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(fis!=null)IOUtils.closeQuietly(fis);  
		}
        
        System.out.println("MD5:"+md5);
        return md5;
	}
	
}
