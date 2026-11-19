package dev.disc;

import javax.imageio.ImageIO;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextureTest {
    @Test void nativeTexturesMatchEverySuppliedPixel() throws Exception {
        for (String tier : new String[]{"iron","gold","diamond","netherite"}) {
            var source=ImageIO.read(Path.of("art/supplied/"+tier+".png").toFile());
            try(var in=getClass().getResourceAsStream("/assets/throwing_disc/textures/item/"+tier+"_throwing_disc.png")) {
                assertNotNull(in);
                var image=ImageIO.read(in);
                assertEquals(16,image.getWidth());assertEquals(16,image.getHeight());
                for(int y=0;y<16;y++) for(int x=0;x<16;x++) {
                    int pixel=image.getRGB(x,y);
                    assertEquals(source.getRGB((2*x+1)*source.getWidth()/32,(2*y+1)*source.getHeight()/32),pixel);
                    assertTrue((pixel>>>24)==0 || (pixel>>>24)==255,"No antialiased alpha");
                }
                assertEquals(0,image.getRGB(8,8)>>>24,"Transparent central hole");
            }
        }
    }
}
