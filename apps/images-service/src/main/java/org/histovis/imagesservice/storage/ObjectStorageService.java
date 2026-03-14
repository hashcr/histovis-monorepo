package org.histovis.imagesservice.storage;

import java.io.InputStream;

public interface ObjectStorageService {

    /**
     * Uploads an object and returns its public URL.
     *
     * @param key         the storage key (path within the bucket)
     * @param data        the input stream of the object data
     * @param size        the size in bytes (-1 if unknown)
     * @param contentType the MIME type of the object
     * @return the public URL of the uploaded object
     */
    String upload(String key, InputStream data, long size, String contentType);

    /**
     * Deletes an object from storage.
     *
     * @param key the storage key of the object to delete
     */
    void delete(String key);

    /**
     * Returns the public URL for a given storage key.
     *
     * @param key the storage key
     * @return the public URL
     */
    String getUrl(String key);
}
