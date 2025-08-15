package com.wamteavm.loaders.externalloaders;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

public class BrowserIO implements JSObject {

    @JSBody(script = """
        if (!window.__libgdxFileInput) {
            var input = document.createElement('input');
            input.type = 'file';
            input.multiple = true;
            input.accept = 'image/*';
            input.style.display = 'none';
            document.body.appendChild(input);
            window.__libgdxFileInput = input;
        }
    """)
    public static native void initHiddenFileInput();

    @JSBody(params = {"callback"}, script = """
        var input = window.__libgdxFileInput;
        if (!input.files || input.files.length === 0) return;

        const files = Array.from(input.files);
        const entries = [];
        let loadedCount = 0;

        input.onchange = function() {
            files.forEach(file => {
                const reader = new FileReader();
                reader.onload = function () {
                    entries.push({ key: file.name, value: reader.result });
                    loadedCount++;

                    // When all files are read, call the callback once
                    if (loadedCount === files.length) {
                        callback(entries);
                    }
                };
                reader.readAsDataURL(file);
            });
            input.value = '';
        };
        input.click();
    """)
    public static native void pickImage(ImageCallback callback);

    @JSBody(params = { "database", "storeName", "key", "base64Data" }, script = """
        const prefixIndex = base64Data.indexOf('base64,');

        let base64String;
        if (prefixIndex !== -1) {
            // Extract substring after "base64,"
            base64String = base64Data.substring(prefixIndex + 7);
        } else {
            // No prefix, assume entire string is base64
            base64String = base64Data;
        }

        // Clean up whitespace/newlines
        base64String = base64String.trim().replace(/\\s+/g, '');

        // Convert URL-safe Base64 to standard Base64 (if needed)
        base64String = base64String.replace(/-/g, '+').replace(/_/g, '/');

        base64Data = base64String;

        const db = window.__myIndexedDBInstance;;
        var tx = db.transaction(storeName, "readwrite");
        var store = tx.objectStore(storeName);
        var putRequest = store.put(base64Data, key);

        putRequest.onsuccess = function() {
            console.log("Data saved with key:", key);
        };

        putRequest.onerror = function(e) {
            console.error("Error saving data:", e.target.error);
        };

        tx.onerror = function(e) {
            console.error("Transaction error:", e.target.error);
        };
    """)
    public static native void saveBase64ToIndexedDB(String database, String storeName, String key, String base64Data);

    @JSBody(params = {"database", "storeName", "callback"}, script = """
        console.log('storeName:', storeName, typeof storeName);
        const db = window.__myIndexedDBInstance;
        const transaction = db.transaction(storeName, "readonly");
        const store = transaction.objectStore(storeName);

        const entries = [];
        store.openCursor().onsuccess = function(e) {
            const cursor = e.target.result;
            if (cursor) {
                entries.push({ key: cursor.key, value: cursor.value });
                cursor.continue();
            } else {
                // Send the array of { key, value } to Kotlin
                callback(entries);
            }
        };
        """)
    public static native void loadIndexedDB(String database, String storeName, ImageCallback callback);

    @JSBody(params = {"databaseName", "version", "storeNames"}, script = """
        const request = indexedDB.open(databaseName, version);

        request.onupgradeneeded = event => {
          const db = event.target.result;
          storeNames.forEach(storeName => {
            if (!db.objectStoreNames.contains(storeName)) {
              db.createObjectStore(storeName);
            }
          });
        };

        request.onsuccess = event => {
          window.__myIndexedDBInstance = event.target.result;
          console.log(window.__myIndexedDBInstance);
        };
        """)
    public static native void openDatabase(String database, int version, String[] storeNames);
}
