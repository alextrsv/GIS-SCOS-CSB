package gisscos.studentcard.services.Impl;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Converter {
    public static Optional<Resource> getResource(BufferedImage qrCodeImage) {
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(convertToBytes(qrCodeImage));
            return Optional.of(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static byte[] convertToBytes(BufferedImage qrCodeImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrCodeImage, "png", baos);
        return baos.toByteArray();
    }

    public static InputStream bufferedImageToInputStream(BufferedImage image){
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(convertToBytes(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }
}
