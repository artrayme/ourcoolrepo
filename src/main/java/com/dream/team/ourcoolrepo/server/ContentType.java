package com.dream.team.ourcoolrepo.server;

import com.dream.team.ourcoolrepo.server.reader.FileCoolReader;
import com.dream.team.ourcoolrepo.server.reader.ImageCoolReader;
import com.dream.team.ourcoolrepo.server.reader.CoolReader;

import java.util.Arrays;

public enum ContentType {
    PLAIN("text/plain", "txt", new FileCoolReader()),
    HTML("text/html", "html", new FileCoolReader()),
    CSS("text/css", "css", new FileCoolReader()),
    JS("application/javascript", "js", new FileCoolReader()),
    PNG("image/png", "png", new ImageCoolReader()),
    JPEG("image/jpeg", "jpeg", new ImageCoolReader()),
    SVG("image/svg+xml", "svg", new FileCoolReader());

    private final String text;
    private final String extension;
    private final CoolReader reader;


    ContentType(String text, String extension, CoolReader reader){
        this.text = text;
        this.extension = extension;
        this.reader = reader;
    }

    public String getText(){
        return text;
    }

    public String getExtension(){
        return extension;
    }


    public CoolReader getReader() {
        return reader;
    }

    public static ContentType findByFileName(String fileName){
        String extension = fileName.substring(fileName.lastIndexOf(".")+1);
        return Arrays.stream(ContentType.values())
                .filter(x -> x.getExtension().equalsIgnoreCase(extension))
                .findFirst()
                .orElse(PLAIN);
    }
}
