package com.dream.team.ourcoolrepo.server.reader;

import java.io.IOException;
import java.io.InputStream;

public class FileCoolReader implements CoolReader {

    @Override
    public byte[] read(InputStream inputStream) throws IOException {
        byte[] fileData = new byte[inputStream.available()];
        try (inputStream) {
            inputStream.read(fileData);
        }
        return fileData;
    }
}
