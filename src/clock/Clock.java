package clock;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Clock
{
    private static Font font = new Font("TDBerth DM", 0, 128);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private static String currentTime = "||:||:||";

    private static Point initialClick;

    public static void main(String[] args)
    {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) { e.printStackTrace(); }

        try
        {
            EventQueue.invokeAndWait(() ->
            {
                final JFrame frame = new JFrame();
                frame.setTitle("Clock");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                //frame.setUndecorated(true);
                frame.setAlwaysOnTop(true);
                //frame.setType(JFrame.Type.UTILITY);
                frame.getContentPane().setBackground(new Color(0, 0, 0));

                final JComponent clockDigital = new JComponent()
                {
                    @Override
                    protected void paintComponent(Graphics g)
                    {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                        //g.setFont(font.deriveFont(128f));

                        g2d.drawString(currentTime, ((g2d.getClipBounds().width - g2d.getFontMetrics(font).stringWidth(currentTime)) / 2), ((g2d.getClipBounds().height - g2d.getFontMetrics(font).getHeight()) / 2) + g2d.getFontMetrics(font).getHeight());
                    }
                };
                clockDigital.setForeground(new Color(0, 153, 0, 255));
                clockDigital.setFont(font);
                clockDigital.setPreferredSize(new Dimension(780, 125));

                final JComponent clockAnalog = new JComponent()
                {
                    @Override
                    public void paintComponent(Graphics g)
                    {
                        Graphics2D g2d = (Graphics2D) g.create();
                        int diameter = Math.max(0, Math.min(g2d.getClipBounds().width, g2d.getClipBounds().height) - 20);

                        if (diameter >= 10)
                        {
                            int yBorder = Math.max(1, g2d.getClipBounds().height - diameter) / 2;
                            int xBorder = Math.max(1, g2d.getClipBounds().width - diameter) / 2;

                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                            Color faceColour = new Color(255, 255, 255);
                            g2d.setColor(faceColour);
                            g2d.fillOval(xBorder, yBorder, diameter, diameter);

                            g2d.setColor(Color.BLACK);
                            for (int i = 0; i < 60; i++)
                            {
                                if (i % 5 != 0)
                                {
                                    double angle = (Math.PI * i) / 30;
                                    int tickLength = diameter * 10 / 760;

                                    int tx = diameter/2 + (int) (Math.cos(angle) * (diameter/2));
                                    int ty = diameter/2 + (int) (Math.sin(angle) * (diameter/2));
                                    g2d.drawLine(xBorder+tx, yBorder+ty, xBorder+tx - (int) (Math.cos(angle) * tickLength), yBorder+ty - (int) (Math.sin(angle) * tickLength));
                                }
                            }

                            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                            g2d.setColor(Color.BLACK);
                            for (int i = 0; i <= 12; i++)
                            {
                                double angle = (i * Math.PI) / 6;
                                int tickLength = diameter * 20 / 760;

                                int tx = diameter/2 + (int) (Math.cos(angle) * (diameter/2));
                                int ty = diameter/2 + (int) (Math.sin(angle) * (diameter/2));
                                g2d.drawLine(xBorder+tx, yBorder+ty, xBorder+tx - (int) (Math.cos(angle) * tickLength), yBorder+ty - (int) (Math.sin(angle) * tickLength));
                            }

                            Calendar c = new GregorianCalendar();
                            int mils = c.get(Calendar.MILLISECOND);
                            int secs = c.get(Calendar.SECOND);
                            int mins = c.get(Calendar.MINUTE);
                            int hrs  = c.get(Calendar.HOUR);
                            { // Seconds
                                double angle = Math.PI/30 * secs - Math.PI/2;
                                int tx = diameter/2 + (int) ((diameter/2-(diameter*25/760)) * Math.cos(angle));
                                int ty = diameter/2 + (int) ((diameter/2-(diameter*25/760)) * Math.sin(angle));

                                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
                                g2d.drawLine(xBorder + diameter/2, yBorder + diameter/2, xBorder+tx, yBorder+ty);
                            }{ // Minutes
                                double angle = Math.PI/30 * (mins+(secs/60f)+(mils/60000f)) - Math.PI/2;
                                int tx = diameter/2 + (int) ((diameter/2-(diameter*30/760)) * Math.cos(angle));
                                int ty = diameter/2 + (int) ((diameter/2-(diameter*30/760)) * Math.sin(angle));

                                g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
                                g2d.drawLine(xBorder + diameter/2, yBorder + diameter/2, xBorder+tx, yBorder+ty);
                            }{ // Hours
                                double angle = Math.PI/6 * (hrs+(mins/60f)+(secs/3600f)+(mils/3600000f)) - Math.PI/2;
                                int tx = diameter/2 + (int) ((diameter/2-(diameter*150/760)) * Math.cos(angle));
                                int ty = diameter/2 + (int) ((diameter/2-(diameter*150/760)) * Math.sin(angle));

                                g2d.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
                                g2d.drawLine(xBorder + diameter/2, yBorder + diameter/2, xBorder+tx, yBorder+ty);
                            }

                        }

                        g2d.dispose();
                    }
                };
                clockAnalog.setForeground(new Color(0, 0, 0, 255));
                clockAnalog.setPreferredSize(new Dimension(780, 780));

                /*MouseAdapter ma = new MouseAdapter()
                {
                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        if (e.isShiftDown() && e.getButton() == MouseEvent.BUTTON1)
                        {
                            if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH)
                                frame.setExtendedState(JFrame.NORMAL);
                            else
                                frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
                        }
                        else if (e.isControlDown() || e.getButton() != MouseEvent.BUTTON1)
                        {
                            frame.dispose();
                            System.exit(0);
                        }
                        else
                        {
                            initialClick = e.getPoint();
                            frame.getComponentAt(initialClick);
                        }
                    }
                };
                MouseMotionAdapter mma = new MouseMotionAdapter()
                {
                    @Override
                    public void mouseDragged(MouseEvent e)
                    {
                        if ((frame.getExtendedState()|JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH)
                        {
                            int thisX = frame.getLocation().x;
                            int thisY = frame.getLocation().y;

                            frame.setLocation(thisX + (thisX + e.getX()) - (thisX + initialClick.x), thisY + (thisY + e.getY()) - (thisY + initialClick.y));
                        }
                    }
                };
                clockDigital.addMouseListener(ma);
                frame.addMouseListener(ma);
                clockDigital.addMouseMotionListener(mma);
                frame.addMouseMotionListener(mma);*/

                frame.add(clockAnalog, BorderLayout.CENTER);
                frame.add(clockDigital, BorderLayout.SOUTH);

                try
                {
                    font = Font.createFont(Font.TRUETYPE_FONT, Clock.class.getResourceAsStream("/clock/resources/TDBerth-DM.ttf")).deriveFont(128f);
                }
                catch (FontFormatException | IOException e)
                {
                    font = new Font("Monospaced", 0, 128);
                    e.printStackTrace();
                }

                frame.pack();
                frame.setVisible(true);

                Timer timer = new Timer(100, (ActionEvent e) ->
                {
                    currentTime = sdf.format(new Date());
                    frame.setTitle("Clock - " + currentTime);
                    clockDigital.repaint();
                    clockAnalog.repaint();
                });
                timer.start();
            });
        }
        catch (InterruptedException | InvocationTargetException e) { e.printStackTrace(); }
    }
}