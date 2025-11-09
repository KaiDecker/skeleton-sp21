package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/**
 * 表示 Gitlet 中的文件快照 blob 对象
 * 为 Gitlet 中的最小数据单元
 * 代表一个文件在某一时刻的内容快照
 *
 * @author Kai Decker
 */

public class Blob implements Serializable{

    /* 文件内容 */
    private final String content;
    /* blob 对象的 SHA-1 id */
    private final String uid;

    /**
     * 使用文件 f 实例化一个 blob 对象
     * blob 意味着文件的快照
     */
    public Blob(File f) {
        this.content = readContentsAsString(f);
        this.uid = getBlobName(f);
    }

    /**
     * 使用文件内容和文件名来生成 SHA-1 哈希
     *
     * @return blob 的 SHA-1 id
     */
    public static String getBlobName(File f) {
        return sha1(readContentsAsString(f) + f.getName());
    }

    /**
     * 将 blob 对象序列化写入 TEMP_DIR.
     *
     * @return blob 的 40-length uid
     */
    public String makeBlob() {
        File out = Repository.makeObjectDir(this.uid);
        writeObject(out, this);
        return this.uid;
    }

    /* 用于在 checkout 时取出文件的原始内容 */
    public String getContent() {
        return content;
    }
}
