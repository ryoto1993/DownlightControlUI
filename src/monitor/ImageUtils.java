package monitor;

import javafx.scene.paint.Color;

public class ImageUtils {
     /**
     * Convert color temperature in Kelvins to RGB color for AWT
     * @param temperature
     * @return ready to use color object
     */
    public static Color getRGBFromK(int temperature) {
        // Used this: https://gist.github.com/paulkaplan/5184275 at the beginning
        // based on http://stackoverflow.com/questions/7229895/display-temperature-as-a-color-with-c
        // this answer: http://stackoverflow.com/a/24856307
        // (so, just interpretation of pseudocode in Java)

        double tmp;
        if(temperature<3000) {
            tmp = 4000;
        } else if(temperature>6000) {
            tmp = 10000;
        } else {
            tmp = 4000 + 6000*((double)temperature-3000)/3000;
        }

        double x = tmp / 1000.0;
        if (x > 40) {
            x = 40;
        }
        double red;
        double green;
        double blue;

        // R
        if (tmp < 6527) {
            red = 1;
        } else {
            double[] redpoly = {4.93596077e0, -1.29917429e0,
                1.64810386e-01, -1.16449912e-02,
                4.86540872e-04, -1.19453511e-05,
                1.59255189e-07, -8.89357601e-10};
            red = poly(redpoly, x);

        }
        // G
        if (tmp < 850) {
            green = 0;
        } else if (tmp <= 6600) {
            double[] greenpoly = {-4.95931720e-01, 1.08442658e0,
                -9.17444217e-01, 4.94501179e-01,
                -1.48487675e-01, 2.49910386e-02,
                -2.21528530e-03, 8.06118266e-05};
            green = poly(greenpoly, x);
        } else {
            double[] greenpoly = {3.06119745e0, -6.76337896e-01,
                8.28276286e-02, -5.72828699e-03,
                2.35931130e-04, -5.73391101e-06,
                7.58711054e-08, -4.21266737e-10};

            green = poly(greenpoly, x);
        }
        // B
        if (tmp < 1900) {
            blue = 0;
        } else if (tmp < 6600) {
            double[] bluepoly = {4.93997706e-01, -8.59349314e-01,
                5.45514949e-01, -1.81694167e-01,
                4.16704799e-02, -6.01602324e-03,
                4.80731598e-04, -1.61366693e-05};
            blue = poly(bluepoly, x);
        } else {
            blue = 1;
        }

        red = clamp(red, 0, 1);
        blue = clamp(blue, 0, 1);
        green = clamp(green, 0, 1);

        return Color.rgb((int)(red*255), (int)(green*255), (int)(blue*255));
    }

    public static double poly(double[] coefficients, double x) {
        double result = coefficients[0];
        double xn = x;
        for (int i = 1; i < coefficients.length; i++) {
            result += xn * coefficients[i];
            xn *= x;

        }
        return result;
    }

    public static double clamp(double x, double min, double max) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }
}
