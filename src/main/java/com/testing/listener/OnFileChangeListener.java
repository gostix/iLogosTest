package com.testing.listener;

/**
 * Interface definition for a callback to be invoked when a file under
 * watch is changed.
 */
public interface OnFileChangeListener
{
    /**
     * Called when the file is changed.
     *
     * @param filePath The file path.
     */
    void onFileChange(String filePath);
}
