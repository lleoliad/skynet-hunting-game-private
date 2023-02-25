package org.skynet.service.provider.hunting.obsolete.service;

import java.io.IOException;

/**
 * 游戏资源操作
 */
public interface GameResourceService {

    /**
     * 将数据库中的数据导入游戏静态资源
     *
     * @param version
     * @param zipData
     * @param tableName
     */
    void inputGameResource(String version, String zipData, String tableName) throws IOException;

    void inputContent(String version, String content, String tableName) throws IOException;
}
