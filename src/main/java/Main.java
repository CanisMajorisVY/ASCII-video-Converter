import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main extends JFrame {
    private final boolean NEGATIVE = false;
    private final int WINDOW_WIDTH = 1366;
    private final int WINDOW_HEIGHT = 768;
    private final int NUM_OF_CH_IN_LINE = WINDOW_WIDTH / 10;
    private final int NUM_OF_CH_IN_COL = WINDOW_HEIGHT / 10;

    public Main(String s) throws IOException, JCodecException {
        super(s);
        Screen panel = new Screen();
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT + 5));
        add(panel);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = null;
                try {
                    frame = new Main("ASCII");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JCodecException e) {
                    e.printStackTrace();
                }
                frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            }
        });
    }

    public class Screen extends JComponent implements Runnable {
        String ascii;
        BufferedImage image;
        File file = new File("video1.mp4");
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        Picture picture;

        public Screen() throws IOException, JCodecException {
            super();
            try {
                PrintWriter pw = new PrintWriter(new FileWriter(("ASCIIres.txt")));
                pw.print(ascii);
                pw.close();
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Thread(this).start();
        }

        public void run() {
            long t = System.currentTimeMillis();
            while (true) {
                repaint();
                try {
                    t = System.currentTimeMillis() - t;
                    t = 33 - t;
                    if (t < 0) {
                        t = 0;
                    }
                    System.out.println(t);
                    Thread.sleep(t);
                } catch (InterruptedException ex) {
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                if (null != (picture = grab.getNativeFrame())) {
                    image = AWTUtil.toBufferedImage(picture);
                    this.ascii = convert(image);
                    g.setFont(new Font("Monospaced", Font.BOLD, 9));
                    try {
                        if (ascii != null) {
                            String[] s = ascii.split("\n");
                            for (int i = 0; i < s.length; i++) {
                                g.drawString(s[i], 0, i * 10 + 10);
                            }
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.exit(0);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        public String convert(final BufferedImage image) {
            StringBuilder sb = new StringBuilder((image.getWidth() + 1) * image.getHeight());

            for (int y = 0; y < NUM_OF_CH_IN_COL; y++) {
                if (sb.length() != 0) sb.append("\n");
                for (int x = 0; x < NUM_OF_CH_IN_LINE; x++) {
                    double r = 0;
                    double g = 0;
                    double b = 0;
                    int sumC = 0;

                    for (int i = y * image.getHeight() / NUM_OF_CH_IN_COL; i < (y + 1) * image.getHeight() / NUM_OF_CH_IN_COL; i++) {
                        for (int j = x * image.getWidth() / NUM_OF_CH_IN_LINE; j < (x + 1) * image.getWidth() / NUM_OF_CH_IN_LINE; j++) {

                            Color pixelColor = new Color(image.getRGB(j, i));
                            r += (double) pixelColor.getRed();
                            g += (double) pixelColor.getGreen();
                            b += (double) pixelColor.getBlue();
                            sumC++;
                        }
                    }
                    r = r / sumC * 0.2989;
                    g = g / sumC * 0.5870;
                    b = b / sumC * 0.1140;

                    double gValue = r + g + b;
                    final char s = NEGATIVE ? returnStrNeg(gValue) : returnStrPos(gValue);
                    sb.append(s).append(" ");
                }
            }
            return sb.toString();
        }

        private char returnStrPos(double g) {
            final char str;

            if (g >= 230.0) {
                str = ' ';
            } else if (g >= 200.0) {
                str = '.';
            } else if (g >= 180.0) {
                str = '*';
            } else if (g >= 160.0) {
                str = ':';
            } else if (g >= 130.0) {
                str = 'o';
            } else if (g >= 100.0) {
                str = '&';
            } else if (g >= 70.0) {
                str = '8';
            } else if (g >= 50.0) {
                str = '#';
            } else {
                str = '@';
            }
            return str;

        }

        private char returnStrNeg(double g) {
            final char str;

            if (g >= 230.0) {
                str = '@';
            } else if (g >= 200.0) {
                str = '#';
            } else if (g >= 180.0) {
                str = '8';
            } else if (g >= 160.0) {
                str = '&';
            } else if (g >= 130.0) {
                str = 'o';
            } else if (g >= 100.0) {
                str = ':';
            } else if (g >= 70.0) {
                str = '*';
            } else if (g >= 50.0) {
                str = '.';
            } else {
                str = ' ';
            }
            return str;

        }

    }
}