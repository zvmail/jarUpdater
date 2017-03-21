package org.atline.jarupdater.utils;

import java.io.File;

public class Finder {
    public static String findSettings() {
        String str = PathUtil.getPath(Finder.class);
        if (!"".equals(str)) {
            String file = str + "/settings.conf";
            if ((new File(file)).exists()) {
                return file;
            }
            return "";
        } else {
            return "";
        }
    }

    public static String findVersions() {
        String str = PathUtil.getPath(Finder.class);
        if (!"".equals(str)) {
            String file = str + "/version.txt";
            if ((new File(file)).exists()) {
                return file;
            }
            return "";
        } else {
            return "";
        }
    }
}
