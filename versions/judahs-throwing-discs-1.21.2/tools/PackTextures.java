import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Packs the user's supplied enlarged sprites into native 16x16 PNGs without smoothing. */
public class PackTextures {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length == 0 ? "." : args[0]);
        Path dest = root.resolve("src/main/resources/assets/throwing_disc/textures/item");
        Files.createDirectories(dest);
        String[] tiers = {"copper","iron","gold","diamond","netherite"};
        BufferedImage preview = new BufferedImage(900,220,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=preview.createGraphics();
        g.setColor(new Color(0x20242c));g.fillRect(0,0,900,220);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));
        for(int i=0;i<tiers.length;i++) {
            BufferedImage source=ImageIO.read(root.resolve("art/supplied/"+tiers[i]+".png").toFile());
            if(source.getWidth()%16!=0 || source.getHeight()%16!=0) throw new IllegalArgumentException("Source grid must be an integer multiple of 16");
            BufferedImage out=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<16;y++) for(int x=0;x<16;x++)
                out.setRGB(x,y,source.getRGB((2*x+1)*source.getWidth()/32,(2*y+1)*source.getHeight()/32));
            ImageIO.write(out,"PNG",dest.resolve(tiers[i]+"_throwing_disc.png").toFile());
            g.drawImage(out,i*180+10,10,160,160,null);
            g.setColor(Color.WHITE);g.drawString(tiers[i].toUpperCase(),i*180+40,200);
        }
        g.dispose();ImageIO.write(preview,"PNG",root.resolve("texture-preview.png").toFile());
    }
}
