import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Shape1 {
    List<Triangle> tris = new ArrayList<>();

    // Method to add triangles
    public void addTriangles() {
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
    }
}
