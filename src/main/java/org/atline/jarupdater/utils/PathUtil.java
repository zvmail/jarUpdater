package org.atline.jarupdater.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.log4j.Logger;

public class PathUtil {
	static Logger logger = Logger.getLogger(PathUtil.class);
	
    private static String path = "";

    public static String getPath() {
        if ("".equals(path)) {
            return getPath(PathUtil.class);
        }

        return path;
    }

    public static String getPath(Class<?> clazz) {
        String filePath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();

        try {
            filePath = URLDecoder.decode(filePath, "utf-8");

            if (filePath.endsWith((".jar"))) {
                filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            }

            File file = new File(filePath);
            filePath = file.getAbsolutePath();
        } catch (UnsupportedEncodingException e) {
        	logger.error(e,e);
            filePath = "";
        }

        path = filePath;

        return filePath;
    }
}
