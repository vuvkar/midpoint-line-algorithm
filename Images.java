import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Images {

    private static BufferedImage img;
    private static int[] texelColor = { 255, 255, 255 };
    private static Point[] keptPoints = new Point[2];

    private static final int imageWidth = 500;
    private static final int imageHeight = 500;

    private static class Point {
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static BufferedImage gradientSetRaster(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        WritableRaster raster = img.getRaster();
        int[] pixel = new int[3]; // RGB

        for (int y = 0; y < height; y++) {
            int val = (int) (y * 255f / height);
            for (int shift = 1; shift < 3; shift++) {
                pixel[shift] = val;
            }

            for (int x = 0; x < width; x++) {
                raster.setPixel(x, y, pixel);
            }
        }

        return img;
    }

    public static void handleNewPoint() {
        Point start = keptPoints[0];
        if (start == null) {
            // Call this function once you have at least one point
            return;
        }

        Point end = keptPoints[1];
        if (keptPoints[1] == null) {
            drawPoint(start);
        } else {
            preparePointsAndDraw(start, end);
            keptPoints[0] = null;
            keptPoints[1] = null;
        }
    }

    private static void drawPoint(Point p) {
        drawTexel(p.x, p.y);
    }

    private static void drawTexel(int x, int y) {
        img.getRaster().setPixel(x, imageHeight - y, texelColor);
    }

    private static void preparePointsAndDraw(Point start, Point end) {
        int x0 = start.x;
        int y0 = start.y;

        int x1 = end.x;
        int y1 = end.y;

        float m = (y1 - y0) * 1f / (x1 - x0) * 1f;

        drawPoint(start);

        if (y0 < y1) {
            if (x0 < x1) {
                if (m <= 1 && m >= 0) {
                    // first octant
                    drawLine(start, end, false, false, false);
                } else {
                    // second octant
                    drawLine(new Point(start.y, start.x), new Point(end.y, end.x), true, false, false);
                }
            } else {
                if (m <= -1 || Float.isInfinite(m)) {
                    // third octant
                    drawLine(new Point(end.y, end.x), new Point(start.y, start.x), true, true, true);
                } else {
                    // fourth octant
                    drawLine(new Point(start.x, start.y), new Point(end.x, end.y), false, true, true);
                }
            }
        } else {
            System.out.println(m);
            if (x1 < x0) {
                if (m <= 1 && m >= 0) {
                    // fifth octant
                    drawLine(new Point(start.x, start.y), new Point(end.x, end.y), false, true, false);
                } else {
                    // sixth octant
                    drawLine(new Point(start.y, start.x), new Point(end.y, end.x), true, true, false);
                }
            } else {
                if (m <= -1 || Float.isInfinite(m)) {
                    // seventh octant
                    drawLine(new Point(start.y, start.x), new Point(end.y, end.x), true, true, true);
                } else {
                    // eighth octant
                    drawLine(start, end, false, false, true);
                }
            }
        }

        drawPoint(end);
    }

    // this function is only responsible for first octant
    private static void drawLine(Point start, Point end, boolean swappedAxes, boolean swappedPoints,
            boolean decrementY) {
        int x0 = start.x;
        int y0 = start.y;

        int x1 = end.x;
        int y1 = end.y;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int d = 2 * dy - dx;

        int toE = 2 * dy;
        int toNE = 2 * (dy - dx);
        int y = !swappedPoints ? y0 : y1;

        for (int x = swappedPoints ? x1 : x0; x < (swappedPoints ? x0 : x1); x++) {
            if (d <= 0) {
                d += toE;
            } else {
                d += toNE;
                y = y + (decrementY ? -1 : 1);
            }
            drawTexel(!swappedAxes ? x : y, !swappedAxes ? y : x);
        }
    }

    public static void main(String... args) {
        Frame w = new Frame("Raster"); // window

        w.setSize(imageWidth, imageHeight);
        w.setLocation(100, 100);
        w.setVisible(true);

        Graphics g = w.getGraphics();

        img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        gradientSetRaster(img);

        preparePointsAndDraw(new Point(250, 250), new Point(300, 250));
        preparePointsAndDraw(new Point(250, 250), new Point(300, 300));
        preparePointsAndDraw(new Point(250, 250), new Point(250, 300));
        preparePointsAndDraw(new Point(250, 250), new Point(200, 300));
        preparePointsAndDraw(new Point(250, 250), new Point(200, 250));
        preparePointsAndDraw(new Point(250, 250), new Point(200, 200));
        preparePointsAndDraw(new Point(250, 250), new Point(250, 200));
        preparePointsAndDraw(new Point(250, 250), new Point(300, 200));

        g.drawImage(img, 0, 0, (img1, infoflags, x, y, width, height) -> false);

        w.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int index = keptPoints[0] == null ? 0 : 1;
                    // thinking that left bottom is zero, makes life easier
                    keptPoints[index] = new Point(e.getX(), imageHeight - e.getY());
                    handleNewPoint();
                    g.drawImage(img, 0, 0, (img1, infoflags, x, y, width, height) -> false);
                }
            }
        });

        w.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                w.dispose();
                g.dispose();
                System.exit(0);
            }
        });
    }
}
