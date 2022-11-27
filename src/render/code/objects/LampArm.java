package render.code.objects;

public final class LampArm {
 
  private static final int XLONG = 30;
  private static final int YLAT = 30;
  
  public static final float[] vertices = createVertices();
  public static final int[] indices = createIndices();

  private static float[] createVertices() {
    double r = 0.5;
    int step = 8;
    float[] vertices = new float[XLONG*YLAT*step];
    for (int j = 0; j<YLAT; ++j) {
      double b = Math.toRadians(-90+180*(double)(j)/(YLAT-1));
      for (int i = 0; i<XLONG; ++i) {
        double a = Math.toRadians(360 * (double) (i) / (XLONG - 1));
        double z = Math.cos(b) * Math.cos(a)/4;
        double x = Math.cos(b) * Math.sin(a)/4;
        double y = Math.sin(b)*2;
        int base = j * XLONG * step;
        vertices[base + i * step] = (float) (r * x);
        vertices[base + i * step + 1] = (float) (r * y);
        vertices[base + i * step + 2] = (float) (r * z);
        vertices[base + i * step + 3] = (float) x;
        vertices[base + i * step + 4] = (float) y;
        vertices[base + i * step + 5] = (float) z;
        vertices[base + i * step + 6] = (float) (i) / (float) (XLONG - 1);
        vertices[base + i * step + 7] = (float) (j) / (float) (YLAT - 1);
      }
    }
    return vertices;
  }
  
  private static int[] createIndices() {
    int[] indices = new int[(XLONG-1)*(YLAT-1)*6];
    for (int j = 0; j<YLAT-1; ++j) {
      for (int i = 0; i<XLONG-1; ++i) {
        int base = j*(XLONG-1)*6;
        indices[base + i * 6] = j*XLONG+i;
        indices[base + i*6+1] = j*XLONG+i+1;
        indices[base + i*6+2] = (j+1)*XLONG+i+1;
        indices[base + i*6+3] = j*XLONG+i;
        indices[base + i*6+4] = (j+1)*XLONG+i+1;
        indices[base + i*6+5] = (j+1)*XLONG+i;
      }
    }
    return indices;
  }
  
}