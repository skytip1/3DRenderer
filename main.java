import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class main {
    private static double heading = 180;
    private static double pitch = 0;
    private static Point lastMousePosition;
    private static boolean isSphere = false; // Flag to toggle between sphere and tetrahedron
    private static boolean gravityEnabled = false; // Toggle gravity
    private static double velocityY = 0; // Object's vertical velocity
    private static double objectY = 0; // Object's current Y position
    private static final double gravity = 0.5; // Gravity constant
    private static final double groundLevel = 145; // Ground level position (bottom of screen)

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Button to toggle between sphere and tetrahedron
        JButton toggleButton = new JButton("Toggle Sphere/Tetrahedron");
        pane.add(toggleButton, BorderLayout.NORTH);

        // Button to toggle gravity and physics
        JButton gravityButton = new JButton("Toggle Gravity");
        pane.add(gravityButton, BorderLayout.SOUTH);

        JPanel renderPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Generate either a tetrahedron or a sphere based on the current mode
                List<Triangle> tris = isSphere ? generateSphere() : generateTetrahedron();

                // Apply gravity if enabled
                if (gravityEnabled) {
                    velocityY += gravity; // Apply gravity to the velocity
                    objectY += velocityY; // Apply velocity to the object's Y position

                    // Stop the object when it hits the ground
                    if (objectY >= groundLevel) {
                        objectY = groundLevel;
                        velocityY = 0; // Stop the object
                    }
                }

                // Transformations for heading and pitch
                Matrix3D headingTransform = new Matrix3D(new double[]{
                        Math.cos(Math.toRadians(heading)), 0, -Math.sin(Math.toRadians(heading)),
                        0, 1, 0,
                        Math.sin(Math.toRadians(heading)), 0, Math.cos(Math.toRadians(heading))
                });

                Matrix3D pitchTransform = new Matrix3D(new double[]{
                        1, 0, 0,
                        0, Math.cos(Math.toRadians(pitch)), Math.sin(Math.toRadians(pitch)),
                        0, -Math.sin(Math.toRadians(pitch)), Math.cos(Math.toRadians(pitch))
                });

                Matrix3D transform = headingTransform.multiply(pitchTransform);

                // Set up rendering environment
                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);

                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                for (int i = 0; i < zBuffer.length; i++) {
                    zBuffer[i] = Double.NEGATIVE_INFINITY; // Initialize Z-buffer
                }

                // Iterate through each triangle
                for (Triangle t : tris) {
                    // Apply vertical position (including gravity effect)
                    Vertex v1 = transform.transform(new Vertex(t.v1.x, t.v1.y + objectY, t.v1.z));
                    Vertex v2 = transform.transform(new Vertex(t.v2.x, t.v2.y + objectY, t.v2.z));
                    Vertex v3 = transform.transform(new Vertex(t.v3.x, t.v3.y + objectY, t.v3.z));

                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;

                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                    // Rasterize each pixel within the triangle bounding box
                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;

                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z; // Calculate depth using barycentric coordinates
                                int zIndex = y * img.getWidth() + x;

                                // Check Z-buffer and update if necessary
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, t.color.getRGB()); // Set the color of the pixel
                                    zBuffer[zIndex] = depth; // Update Z-buffer
                                }
                            }
                        }
                    }
                }

                g2.drawImage(img, -getWidth() / 2, -getHeight() / 2, null); // Draw the image with depth handling
            }
        };

        // Mouse listener for dragging
        renderPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePosition != null) {
                    int dx = e.getX() - lastMousePosition.x;
                    int dy = e.getY() - lastMousePosition.y;

                    heading += dx * 0.5;
                    pitch -= dy * 0.5; // Invert pitch for natural feel
                    pitch = Math.max(-90, Math.min(90, pitch)); // Clamp pitch to [-90, 90]
                }
                lastMousePosition = e.getPoint();
                renderPanel.repaint();
            }
        });

        // Mouse listener to reset position when dragging starts
        renderPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePosition = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastMousePosition = null;
            }
        });

        // Toggle button listener
        toggleButton.addActionListener(e -> {
            isSphere = !isSphere; // Toggle between sphere and tetrahedron
            renderPanel.repaint(); // Repaint the panel with the new shape
        });

        // Gravity button listener
        gravityButton.addActionListener(e -> {
            gravityEnabled = !gravityEnabled; // Toggle gravity on or off
            velocityY = 0; // Reset velocity to ensure smooth start
            renderPanel.repaint(); // Repaint the panel
        });

        pane.add(renderPanel, BorderLayout.CENTER);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Animation loop for gravity (if enabled)
        Timer timer = new Timer(16, e -> {
            if (gravityEnabled) {
                renderPanel.repaint();
            }
        });
        timer.start();
    }

    // Method to generate a tetrahedron
    public static List<Triangle> generateTetrahedron() {
        List<Triangle> tris = new ArrayList<>();
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100), Color.RED));
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100), Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100), Color.BLUE));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100), Color.WHITE));
        return tris;
    }

    // Method to generate a sphere by inflating a tetrahedron
    public static List<Triangle> generateSphere() {
        List<Triangle> tris = generateTetrahedron();
        for (int i = 0; i < 5; i++) {
            tris = inflate(tris);
        }
        return tris;
    }

    // Inflate a list of triangles to form a more spherical shape
    public static List<Triangle> inflate(List<Triangle> tris) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            Vertex m1 = new Vertex((t.v1.x + t.v2.x) / 2, (t.v1.y + t.v2.y) / 2, (t.v1.z + t.v2.z) / 2);
            Vertex m2 = new Vertex((t.v2.x + t.v3.x) / 2, (t.v2.y + t.v3.y) / 2, (t.v2.z + t.v3.z) / 2);
            Vertex m3 = new Vertex((t.v1.x + t.v3.x) / 2, (t.v1.y + t.v3.y) / 2, (t.v1.z + t.v3.z) / 2);

            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }

        // Normalize the vertices to form a sphere
        for (Triangle t : result) {
            for (Vertex v : new Vertex[]{t.v1, t.v2, t.v3}) {
                double length = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= length;
                v.y /= length;
                v.z /= length;
            }
        }

        return result;
    }
}

// Supporting classes

class Vertex {
    double x, y, z;

    Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

class Triangle {
    Vertex v1, v2, v3;
    Color color;

    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }
}

class Matrix3D {
    double[] matrix;

    Matrix3D(double[] matrix) {
        this.matrix = matrix;
    }

    public Vertex transform(Vertex v) {
        double x = matrix[0] * v.x + matrix[1] * v.y + matrix[2] * v.z;
        double y = matrix[3] * v.x + matrix[4] * v.y + matrix[5] * v.z;
        double z = matrix[6] * v.x + matrix[7] * v.y + matrix[8] * v.z;
        return new Vertex(x, y, z);
    }

    public Matrix3D multiply(Matrix3D other) {
        double[] result = new double[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                result[row * 3 + col] = matrix[row * 3] * other.matrix[col]
                        + matrix[row * 3 + 1] * other.matrix[3 + col]
                        + matrix[row * 3 + 2] * other.matrix[6 + col];
            }
        }
        return new Matrix3D(result);
    }
}
