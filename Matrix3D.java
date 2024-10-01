public class Matrix3D {
    double[] values;
    Matrix3D(double[] values) {
        this.values = values;
    }
    Matrix3D multiply (Matrix3D other) {
        double[] result = new double[9];
        for(int row = 0; row <3; row++) {
            for (int col = 0; col <3; col++) {
                for (int i = 0; i<3; i++) {
                    result[row *3 +col ] += 
                        this.values[row *3 + i] * other.values[i*3+col];
                }
            }
        }
        return new Matrix3D(result);
    }



    Vertex transform(Vertex in) {
        return new Vertex( 
            in.x * values[0] + in.y * values[3] + in.z * values[6],
            in.x * values[1] + in.y * values[4] + in.z * values[7],
            in.x * values[2] + in.y * values[5] + in.z * values[8]
        );
    }
    
}
