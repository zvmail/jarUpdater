package org.atline.jarupdater;

import org.apache.log4j.Logger;
import org.atline.jarupdater.utils.Finder;

import java.io.*;
import java.util.Properties;

public class AppMain {
	static Logger logger = Logger.getLogger(AppMain.class);
	
    public static void main(String[] args) {
    	logger.info("Start update program ...");
        String settings = "";
        if (0 == args.length) {
            String found = Finder.findSettings();
            if ("".equals(found)) {
                logger.info("Failure to find settings.conf.");
                System.exit(1);
            } else {
                settings = found;
            }
        } else {
        	//利用命令行参数指定conf文件
            settings = args[0];
        }

        try {
        	//读取配置
            InputStream is = new BufferedInputStream(new FileInputStream(settings));
            Properties p = new Properties();
            p.load(is);
            
            String updatesite = p.getProperty("updatesite");
            String jarrepo = p.getProperty("jarrepo", "");
            String tmprepo = "tmp";
            if (null != updatesite) {
            	//触发执行更新检查，并下载需要的文件到tmp目录
                String newVersion = Updater.download(updatesite, tmprepo);
                
                if(!newVersion.isEmpty()){
            	    //如果下载成功，读取标记，复制tmp文件到正式目录
	                Updater.update(newVersion, tmprepo, jarrepo);
                }
                
            } else {
                logger.info("Wrong updatesite settings.");
                System.exit(1);
            }
        } catch (FileNotFoundException e) {
        	logger.error(e,e);
            System.exit(1);
        } catch (IOException e) {
        	logger.error(e,e);
            System.exit(1);
        }
        logger.info("End update program.");
        System.exit(0);
    }
}
