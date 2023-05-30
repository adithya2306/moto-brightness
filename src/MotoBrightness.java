import java.io.FileWriter;
import java.io.IOException;

public class MotoBrightness {

    private static final boolean DEBUG = false;

    /* values for moto edge 30 (dubai) */

    private static final float MAX_BL = 3514f;
    private static final float MAX_BL_HBM = 4095f;
    // private static final int MAX_NITS = 500;
    private static final int MAX_NITS_HBM = 700;
    private static final float MIN_BL = 9f;
    // private static final int MIN_NITS = 2;

    private static final float bl_x[] = {
        1, 4, 5, 6, 12, 48, 57, 76, 98, 129, 255
    }; // config_screenBrightnessBacklight

    private static final float bl_y[] = {
        1, 31, 39, 43, 61, 119, 129, 148, 164, 187, 255
    }; // config_screenExponentBacklight

    private static final float nits[] = {
        2 /* MIN_NITS */, 7, 10, 12, 24, 95, 111, 149, 192, 252, /* MAX_NITS */ 500
    }; // config_screenBrightnessNits

    /* device-specific values end */

    private static void generateBlLut(float[] x, float[] y) throws IOException {
        Spline s = Spline.createMonotoneCubicSpline(x, y);
        if (DEBUG)
            System.out.println(s);
        
        FileWriter fw = new FileWriter("bl_lut.txt");
        fw.write("");
        for (int b = 0; b <= MAX_BL_HBM; b++) {
            float f = b / MAX_BL_HBM;
            int i = Math.round(Math.max(f, s.interpolate(f)) * MAX_BL_HBM);
            if (DEBUG)
                System.out.println(b + " -> " + i);
            fw.append(i + (b == MAX_BL_HBM ? "" : ", "));
        }
        fw.close();
        System.out.println("wrote lut to bl_lut.txt");
    }

    private static void printBlNitMapping(float bl[], float n[]) {
        for (int i = 1; i < n.length; i++) {
            final float f = (i == 1)
                ? (MIN_BL / MAX_BL_HBM) // use known lowest min bl-nit
                : bl[i];
            System.out.println("<point>");
            System.out.println(String.format("    <value>%.8f</value>", f));
            System.out.println(String.format("    <nits>%.1f</nits>", n[i]));
            System.out.println("</point>");
        }
    }

    public static void main(String[] args) throws Exception {
        final int len = bl_x.length;
        if (len != bl_y.length || len != nits.length)
            throw new IllegalArgumentException("length of all 3 arrays must be same!");

        float x[] = new float[len + 2];
        float y[] = new float[len + 2];
        float n[] = new float[len + 2];

        // begin all arrays with 0
        x[0] = y[0] = n[0] = 0f;

        // convert to float scale (0.0f - 1.0f)
        final float scale = (MAX_BL / MAX_BL_HBM) / 255f;
        for (int i = 0; i < len; i++) {
            x[i + 1] = bl_x[i] * scale;
            y[i + 1] = bl_y[i] * scale;
            n[i + 1] = nits[i];
        }

        // add hbm brightness and nit
        x[len + 1] = y[len + 1] = 1.0f;
        n[len + 1] = MAX_NITS_HBM;

        if (DEBUG) {
            for (int i = 0; i <= len + 1; i++) {
                System.out.println(x[i] + " " + y[i] + " " + n[i]);
            }
        }
        
        generateBlLut(x, y);
        printBlNitMapping(x, n);
    }
}
