/*
 * -----------------------------------------------------------------------------
 * © Swisslog AG
 * Swisslog is not liable for any usage of this source code that is not agreed on between Swisslog and the other party.
 * The mandatory legal liability remains unaffected.
 * -----------------------------------------------------------------------------
 */
package com.example.bookstore.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

public class ImageUtils {

    private ImageUtils() {}

    public static byte[] processImageData(MultipartFile imageData) {
        if (!imageData.isEmpty()) {
            try {
                return imageData.getBytes();
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image upload.", e);
            }
        }
        return new byte[0];
    }
}
