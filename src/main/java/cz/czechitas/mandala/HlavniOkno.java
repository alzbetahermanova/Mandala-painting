package cz.czechitas.mandala;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import net.miginfocom.swing.*;
import net.sevecek.util.*;
import javax.swing.JColorChooser;

public class HlavniOkno extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    JMenuBar menuBar1;
    JMenu menuObrazek;
    JMenuItem menuINahrajObrazek;
    JMenuItem menuIUlozObrazekJako;
    JMenu menuBarva;
    JMenuItem menuItemVlastniBarva;
    JLabel labBarva;
    JLabel labBarvaA;
    JLabel labBarvaB;
    JLabel labBarvaC;
    JLabel labBarvaD;
    JLabel labBarvaE;
    JLabel labBarvaF;
    JLabel labVybranaBarva;
    JLabel labObrazek;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    JPanel contentPane;
    MigLayout migLayoutManager;
    File soubor;
    BufferedImage obrazek;
    Color vybranaBarva;
    File otevrenySoubor;

    public HlavniOkno() {
        initComponents();
        File soubor = new File("Mandala1.png");
        nahrajObrazek(soubor);
    }

    private void nahrajObrazek(File soubor) {
        if (soubor == null) {
            try {
                obrazek = ImageIO.read(getClass().getResourceAsStream("/cz/czechitas/mandala/Mandala1.png"));
            } catch (IOException ex) {
                throw new ApplicationPublicException(ex, "Nepodařilo se nahrát zabudovaný obrázek mandaly");
            }
        } else {
            try {
                obrazek = ImageIO.read(soubor);
            } catch (IOException ex) {
                throw new ApplicationPublicException(ex, "Nepodařilo se nahrát obrázek mandaly ze souboru " + soubor.getAbsolutePath());
            }
        }
        labObrazek.setIcon(new ImageIcon(obrazek));
        labObrazek.setMinimumSize(new Dimension(obrazek.getWidth(), obrazek.getHeight()));
        pack();
        setMinimumSize(getSize());
    }
    
    private void priStiskuMysiNaLabBarvy(MouseEvent e) {
        JLabel Lab = (JLabel) e.getSource();
        this.vybranaBarva = Lab.getBackground();
        smazOVsemLabBarva();
        Lab.setText("O");
    }

    private void butVyberBarvuActionPerformed(ActionEvent e) {
        smazOVsemLabBarva();
        this.vybranaBarva = JColorChooser.showDialog(null, "Choose a color", Color.RED);
        labVybranaBarva.setBackground(vybranaBarva);
        labVybranaBarva.setText("O");
    }
    
    private void priStiskuMysiNaContentPane(MouseEvent e) {

        int x = e.getX();
        int y = e.getY();

        vyplnObrazek(obrazek, x, y, vybranaBarva);
        labObrazek.repaint();
    }

    private void smazOVsemLabBarva() {
        labBarvaA.setText("");
        labBarvaB.setText("");
        labBarvaC.setText("");
        labBarvaD.setText("");
        labBarvaE.setText("");
        labBarvaF.setText("");
        labVybranaBarva.setText("");
    }

    private void butNahrajObrazek(ActionEvent e) {

        JFileChooser dialog;
        if (otevrenySoubor == null) {
            dialog = new JFileChooser(".");
        } else {
            dialog = new JFileChooser(otevrenySoubor.getParentFile());
        }
        dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        dialog.setMultiSelectionEnabled(false);
        dialog.addChoosableFileFilter(new FileNameExtensionFilter("Obrázky (*.png)", "png"));
        int vysledek = dialog.showOpenDialog(this);
        if (vysledek != JFileChooser.APPROVE_OPTION) {
            return;
        }

        otevrenySoubor = dialog.getSelectedFile();
        nahrajObrazek(otevrenySoubor);
    }

    private void butUlozObrazekActionPerformed(ActionEvent e) {
        ulozitJako();
    }

    private void ulozObrazek(File soubor) {
        try {
            ImageIO.write(obrazek, "png", soubor);
        } catch (IOException ex) {
            throw new ApplicationPublicException(ex, "Nepodařilo se uložit obrázek mandaly do souboru " + soubor.getAbsolutePath());
        }
    }

    private void ulozitJako() {
        JFileChooser dialog;
        dialog = new JFileChooser(".");

        int vysledek = dialog.showSaveDialog(this);
        if (vysledek != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File soubor = dialog.getSelectedFile();
        if (!soubor.getName().contains(".") && !soubor.exists()) {
            soubor = new File(soubor.getParentFile(), soubor.getName() + ".png");
        }
        if (soubor.exists()) {
            int potvrzeni = JOptionPane.showConfirmDialog(this, "Soubor " + soubor.getName() + " už existuje.\nChcete jej přepsat?", "Přepsat soubor?", JOptionPane.YES_NO_OPTION);
            if (potvrzeni == JOptionPane.NO_OPTION) {
                return;
            }
        }
        ulozObrazek(soubor);
    }

    public void vyplnObrazek(BufferedImage obrazek, int x, int y, Color barva) {
        if (barva == null) {
            barva = new Color(255, 255, 255);
        }

        // Zamez vyplnovani mimo rozsah
        if (x < 0 || x >= obrazek.getWidth() || y < 0 || y >= obrazek.getHeight()) {
            return;
        }

        WritableRaster pixely = obrazek.getRaster();
        int[] novyPixel = new int[] {barva.getRed(), barva.getGreen(), barva.getBlue(), barva.getAlpha()};
        int[] staryPixel = new int[] {255, 255, 255, 255};
        staryPixel = pixely.getPixel(x, y, staryPixel);

        // Pokud uz je pocatecni pixel obarven na cilovou barvu, nic nemen
        if (pixelyMajiStejnouBarvu(novyPixel, staryPixel)) {
            return;
        }

        // Zamez prebarveni cerne cary
        int[] cernyPixel = new int[] {0, 0, 0, 0};
        if (pixelyMajiStejnouBarvu(cernyPixel, staryPixel)) {
            return;
        }

        vyplnovaciSmycka(pixely, x, y, novyPixel, staryPixel);
    }

    /**
     * Provede skutecne vyplneni pomoci zasobniku
     */
    private void vyplnovaciSmycka(WritableRaster raster, int x, int y, int[] novaBarva, int[] nahrazovanaBarva) {
        Rectangle rozmery = raster.getBounds();
        int[] aktualniBarva = new int[] {255, 255, 255, 255};

        Deque<Point> zasobnik = new ArrayDeque<>(rozmery.width * rozmery.height);
        zasobnik.push(new Point(x, y));
        while (zasobnik.size() > 0) {
            Point point = zasobnik.pop();
            x = point.x;
            y = point.y;
            if (!pixelyMajiStejnouBarvu(raster.getPixel(x, y, aktualniBarva), nahrazovanaBarva)) {
                continue;
            }

            // Najdi levou zed, po ceste vyplnuj
            int levaZed = x;
            do {
                raster.setPixel(levaZed, y, novaBarva);
                levaZed--;
            }
            while (levaZed >= 0 && pixelyMajiStejnouBarvu(raster.getPixel(levaZed, y, aktualniBarva), nahrazovanaBarva));
            levaZed++;

            // Najdi pravou zed, po ceste vyplnuj
            int pravaZed = x;
            do {
                raster.setPixel(pravaZed, y, novaBarva);
                pravaZed++;
            }
            while (pravaZed < rozmery.width && pixelyMajiStejnouBarvu(raster.getPixel(pravaZed, y, aktualniBarva), nahrazovanaBarva));
            pravaZed--;

            // Pridej na zasobnik body nahore a dole
            for (int i = levaZed; i <= pravaZed; i++) {
                if (y > 0 && pixelyMajiStejnouBarvu(raster.getPixel(i, y - 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y - 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y - 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y - 1));
                    }
                }
                if (y < rozmery.height - 1 && pixelyMajiStejnouBarvu(raster.getPixel(i, y + 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y + 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y + 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y + 1));
                    }
                }
            }
        }
    }

    /**
     * Vrati true pokud RGB hodnoty v polich jsou stejne. Pokud jsou ruzne, vraci false
     */

    private boolean pixelyMajiStejnouBarvu(int[] barva1, int[] barva2) {
        return barva1[0] == barva2[0] && barva1[1] == barva2[1] && barva1[2] == barva2[2];
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        menuBar1 = new JMenuBar();
        menuObrazek = new JMenu();
        menuINahrajObrazek = new JMenuItem();
        menuIUlozObrazekJako = new JMenuItem();
        menuBarva = new JMenu();
        menuItemVlastniBarva = new JMenuItem();
        labBarva = new JLabel();
        labBarvaA = new JLabel();
        labBarvaB = new JLabel();
        labBarvaC = new JLabel();
        labBarvaD = new JLabel();
        labBarvaE = new JLabel();
        labBarvaF = new JLabel();
        labVybranaBarva = new JLabel();
        labObrazek = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mandala");
        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "insets rel,hidemode 3",
            // columns
            "[36,fill]" +
            "[35,fill]" +
            "[35,fill]" +
            "[35,fill]" +
            "[35,fill]" +
            "[35,fill]" +
            "[35,fill]" +
            "[35,fill]",
            // rows
            "[0]" +
            "[35]" +
            "[0]" +
            "[0]" +
            "[0]" +
            "[grow,fill]" +
            "[]"));
        this.contentPane = (JPanel) this.getContentPane();
        this.contentPane.setBackground(this.getBackground());
        LayoutManager layout = this.contentPane.getLayout();
        if (layout instanceof MigLayout) {
            this.migLayoutManager = (MigLayout) layout;
        }

        //======== menuBar1 ========
        {

            //======== menuObrazek ========
            {
                menuObrazek.setText("Obr\u00e1zek");

                //---- menuINahrajObrazek ----
                menuINahrajObrazek.setText("Nahraj obr\u00e1zek");
                menuINahrajObrazek.addActionListener(e -> butNahrajObrazek(e));
                menuObrazek.add(menuINahrajObrazek);

                //---- menuIUlozObrazekJako ----
                menuIUlozObrazekJako.setText("Ulo\u017e obr\u00e1zek jako");
                menuIUlozObrazekJako.addActionListener(e -> butUlozObrazekActionPerformed(e));
                menuObrazek.add(menuIUlozObrazekJako);
            }
            menuBar1.add(menuObrazek);

            //======== menuBarva ========
            {
                menuBarva.setText("Barva");

                //---- menuItemVlastniBarva ----
                menuItemVlastniBarva.setText("Vlastn\u00ed barva");
                menuItemVlastniBarva.addActionListener(e -> butVyberBarvuActionPerformed(e));
                menuBarva.add(menuItemVlastniBarva);
            }
            menuBar1.add(menuBarva);
        }
        setJMenuBar(menuBar1);

        //---- labBarva ----
        labBarva.setText("Barva");
        contentPane.add(labBarva, "cell 0 1");

        //---- labBarvaA ----
        labBarvaA.setBackground(new Color(255, 255, 204));
        labBarvaA.setOpaque(true);
        labBarvaA.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaA.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priStiskuMysiNaLabBarvy(e);
            }
        });
        contentPane.add(labBarvaA, "cell 1 1,grow");

        //---- labBarvaB ----
        labBarvaB.setBackground(new Color(204, 255, 204));
        labBarvaB.setOpaque(true);
        labBarvaB.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaB.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priStiskuMysiNaLabBarvy(e);
            }
        });
        contentPane.add(labBarvaB, "cell 2 1,growy");

        //---- labBarvaC ----
        labBarvaC.setBackground(new Color(255, 204, 204));
        labBarvaC.setOpaque(true);
        labBarvaC.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaC.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priStiskuMysiNaLabBarvy(e);
            }
        });
        contentPane.add(labBarvaC, "cell 3 1,growy");

        //---- labBarvaD ----
        labBarvaD.setBackground(new Color(204, 204, 255));
        labBarvaD.setOpaque(true);
        labBarvaD.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaD.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priStiskuMysiNaLabBarvy(e);
            }
        });
        contentPane.add(labBarvaD, "cell 4 1,growy");

        //---- labBarvaE ----
        labBarvaE.setBackground(new Color(255, 153, 204));
        labBarvaE.setOpaque(true);
        labBarvaE.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaE.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priStiskuMysiNaLabBarvy(e);
            }
        });
        contentPane.add(labBarvaE, "cell 5 1,growy");

        //---- labBarvaF ----
        labBarvaF.setOpaque(true);
        labBarvaF.setBackground(Color.white);
        labBarvaF.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaF.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priStiskuMysiNaLabBarvy(e);
            }
        });
        contentPane.add(labBarvaF, "cell 6 1,growy");

        //---- labVybranaBarva ----
        labVybranaBarva.setOpaque(true);
        labVybranaBarva.setHorizontalAlignment(SwingConstants.CENTER);
        labVybranaBarva.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priStiskuMysiNaLabBarvy(e);
            }
        });
        contentPane.add(labVybranaBarva, "cell 7 1,grow");

        //---- labObrazek ----
        labObrazek.setOpaque(true);
        labObrazek.setBackground(Color.white);
        labObrazek.setVerticalAlignment(SwingConstants.TOP);
        labObrazek.setHorizontalAlignment(SwingConstants.LEFT);
        labObrazek.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priStiskuMysiNaContentPane(e);
            }
        });
        contentPane.add(labObrazek, "cell 0 5 8 1");
        setSize(400, 360);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
