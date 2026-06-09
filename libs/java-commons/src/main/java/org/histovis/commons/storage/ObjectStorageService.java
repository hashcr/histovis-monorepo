package org.histovis.commons.storage;

import java.io.InputStream;

public interface ObjectStorageService {

    String upload(String key, InputStream data, long size, String contentType);

    void delete(String key);

    String getUrl(String key);
}
