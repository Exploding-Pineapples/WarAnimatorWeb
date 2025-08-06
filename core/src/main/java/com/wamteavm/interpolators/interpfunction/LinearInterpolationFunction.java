package com.wamteavm.interpolators.interpfunction;

import java.util.Arrays;

public class LinearInterpolationFunction<I extends Number> extends InterpolationFunction<I, Double> {

    public LinearInterpolationFunction(I[] x, double[] y) {
        super(x, Arrays.stream(y).boxed().toArray(Double[]::new));
    }

    @Override
    public Double evaluate(I at) {
        double atDouble = at.doubleValue();
        I[] genericI = getI();
        double[] i = new double[genericI.length];
        for (int index = 0; index < i.length; index++) {
            i[index] = genericI[index].doubleValue();
        }
        double[] y = new double[getO().length];

        for (int index = 0; index < getO().length; index++) {
            y[index] = getO()[index];
        }

        if (atDouble <= i[0]) {
            return y[0];
        }
        if (atDouble >= i[i.length - 1]) {
            return y[y.length - 1];
        }

        int index = 0;
        while (atDouble > i[index+1]) {
            index++;
        }

        double x0 = i[index];
        double x1 = i[index+1];
        double y0 = y[index];
        double y1 = y[index+1];

        return y0 + (y1 - y0) * (atDouble - x0) / (x1 - x0);
    }

    @Override
    public void init() {

    }
}
