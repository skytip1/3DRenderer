import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;


public class main {
    public static void main(String[] args ) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        JSlider headingSlider = new JSlider(0,360,180);
        pane.add(headingSlider, BorderLayout.SOUTH);

        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL,-90,90,0);
        pane.add(pitchSlider, BorderLayout.EAST);


        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0,0,getWidth(),getHeight());

                //rendering stuff

                List<Triangle> tris = new ArrayList<>();
                tris.add(new Triangle(new Vertex(100, 100, 100),
                                    new Vertex(-100, -100, 100),
                                    new Vertex(-100, 100, -100),
                                    Color.WHITE));
                tris.add(new Triangle(new Vertex(100, 100, 100),
                                    new Vertex(-100, -100, 100),
                                    new Vertex(100, -100, -100),
                                    Color.RED));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                                    new Vertex(100, -100, -100),
                                    new Vertex(100, 100, 100),
                                    Color.GREEN));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                                    new Vertex(100, -100, -100),
                                    new Vertex(-100, -100, 100),
                                    Color.BLUE));

                
                


            
                double heading = Math.toRadians(headingSlider.getValue());
                
                
                Matrix3D headingTransform = new Matrix3D(new double[] {
                    Math.cos(heading), 0, -Math.sin(heading),
                    0,1,0,
                    Math.sin(heading), 0, Math.cos(heading)
                });
                double pitch = Math.toRadians(pitchSlider.getValue());
                Matrix3D pitchTransform = new Matrix3D(new double[] {
                    1,0,0,
                    0,Math.cos(pitch),Math.sin(pitch),
                    0, -Math.sin(pitch), Math.cos(pitch)
                });
                Matrix3D transform = headingTransform.multiply(pitchTransform);
                
                






                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);

                BufferedImage img = 
                new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                


            
                for (Triangle t : tris) {
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);


                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;



                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1, 
                                            Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1,
                                            Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));
                    

                    double triangleArea =
                    (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);



                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double b1 = 
                              ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 =
                              ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 =
                              ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                img.setRGB(x, y, t.color.getRGB());
                            }
                        }
                    }


                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x,v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x,v3.y);
                    path.closePath();

                    g2.drawImage(img, -300,-300,null);
                }

            }

        };



        

        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
        pane.add(renderPanel,BorderLayout.CENTER);
        frame.setSize(600,600);
        frame.setVisible(true);

    }
}