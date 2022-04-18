package com.dream.team.ourcoolrepo.server.reader;

import java.io.IOException;
import java.io.InputStream;

public interface CoolReader {
    byte[] read(InputStream inputStream) throws IOException;
}
