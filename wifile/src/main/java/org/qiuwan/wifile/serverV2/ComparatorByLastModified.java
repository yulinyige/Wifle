package org.qiuwan.wifile.serverV2;

import java.io.File;
import java.util.Comparator;

/**
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class ComparatorByLastModified implements Comparator<File> {

    @Override
    public int compare(File file1, File file2) {
        long l = file1.lastModified() - file2.lastModified();
        if (l > 0) {
            return -1;
        }
        if (l == 0) {
            return 0;
        }
        return 1;
    }

}
