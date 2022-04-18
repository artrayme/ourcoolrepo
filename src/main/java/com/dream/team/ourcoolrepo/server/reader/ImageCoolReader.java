package com.dream.team.ourcoolrepo.server.reader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageCoolReader implements CoolReader {

    @Override
    public byte[] read(InputStream inputStream) throws IOException {
        BufferedImage image;
        try (inputStream) {
            image = ImageIO.read(inputStream);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();

    }
}
