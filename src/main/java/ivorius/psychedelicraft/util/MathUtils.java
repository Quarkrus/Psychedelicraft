package ivorius.psychedelicraft.util;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public interface MathUtils {
    static float nearValue(float from, float to, float delta, float adjustmentRate) {
        return approach(MathHelper.lerp(delta, from, to), to, adjustmentRate);
    }

    static float approach(float value, float target, float adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }

    static double nearValue(double from, double to, double delta, double adjustmentRate) {
        return approach(MathHelper.lerp(delta, from, to), to, adjustmentRate);
    }

    static double approach(double value, double target, double adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }

    static Vector3f unpackRgb(int c) {
        return new Vector3f(r(c), g(c), b(c));
    }

    static Vector4f unpackArgb(int c) {
        return new Vector4f(a(c), r(c), g(c), b(c));
    }

    static float a(int c) {
        return ColorHelper.Argb.getAlpha(c) / 255F;
    }

    static float r(int c) {
        return ColorHelper.Argb.getRed(c) / 255F;
    }

    static float g(int c) {
        return ColorHelper.Argb.getGreen(c) / 255F;
    }

    static float b(int c) {
        return ColorHelper.Argb.getBlue(c) / 255F;
    }

    static int withAlpha(int color, float alpha) {
        return (color & 0xFFFFFF) | ((int)(alpha * 255) & 0xFF) << 24;
    }

    static int mixColors(int left, int right, float progress) {
        return ColorHelper.Argb.fromFloats(
                MathHelper.lerp(a(left), a(right), progress),
                MathHelper.lerp(r(left), r(right), progress),
                MathHelper.lerp(g(left), g(right), progress),
                MathHelper.lerp(b(left), b(right), progress)
        );
    }

    @Deprecated
    static int packArgb(float a, float r, float g, float b) {
        return ColorHelper.Argb.fromFloats(a, r, g, b);
    }

    static float[] mixColorsDynamic(float[] color, float[] colorBase, float alpha, boolean fixAlpha) {
        if (alpha > 0) {
            float max = alpha + colorBase[3];
            colorBase[0] = MathHelper.lerp(alpha / max, colorBase[0], color[0]);
            colorBase[1] = MathHelper.lerp(alpha / max, colorBase[1], color[1]);
            colorBase[2] = MathHelper.lerp(alpha / max, colorBase[2], color[2]);
            colorBase[3] = max;
        }
        if (fixAlpha) {
            colorBase[3] = MathHelper.clamp(colorBase[3], 0, 1);
        }
        return colorBase;
    }

    static Vector4fc mixColorsDynamic(Vector3fc color, Vector4f colorBase, float alpha, boolean fixAlpha) {
        if (alpha > 0) {
            float max = alpha + colorBase.w;
            colorBase.set(
                    MathHelper.lerp(alpha / max, colorBase.x, color.x()),
                    MathHelper.lerp(alpha / max, colorBase.y, color.y()),
                    MathHelper.lerp(alpha / max, colorBase.z, color.z()),
                    max
            );
        }
        if (fixAlpha) {
            colorBase.w = MathHelper.clamp(colorBase.w, 0, 1);
        }
        return colorBase;
    }

    static Vector3f lerp(float delta, Vector3f a, Vector3fc b) {
        return a.set(
                MathHelper.lerp(delta, a.x(), b.x()),
                MathHelper.lerp(delta, a.y(), b.y()),
                MathHelper.lerp(delta, a.z(), b.z())
        );
    }

    static float randomColor(Random random, int ticksExisted, float base, float sway, float... speed) {
        for (float s : speed) {
            base *= 1.0f + MathHelper.sin(ticksExisted * s) * sway;
        }
        return base;
    }

    static float progress(float metric, float delta) {
        return 1 - (delta / (1 + metric));
    }

    static float progress(float metric) {
        return progress(metric, 1F);
    }

    static float mixEaseInOut(float v1, float v2, float delta) {
        return cubicMix(v1, v1, v2, v2, delta);
    }

    static double mixEaseInOut(double v1, double v2, double delta) {
        return cubicMix(v1, v1, v2, v2, delta);
    }

    static double easeZeroToOne(double delta) {
        return cubicMix(0, 0, 1, 1, MathHelper.clamp(delta, 0, 1));
    }

    static float easeZeroToOne(float delta) {
        return cubicMix(0, 0, 1, 1, MathHelper.clamp(delta, 0, 1));
    }

    static float inverseLerp(float value, float start, float end) {
        return MathHelper.clamp(MathHelper.getLerpProgress(value, start, end), 0, 1);
    }

    static Vector3d cubicMix(Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4, double delta, Vector3d dest) {
        return dest.set(
            cubicMix(v1.x, v2.x, v3.x, v4.x, delta),
            cubicMix(v1.y, v2.y, v3.y, v4.y, delta),
            cubicMix(v1.z, v2.z, v3.z, v4.z, delta)
        );
    }

    static float cubicMix(float v1, float v2, float v3, float v4, float delta) {
        return (float)MathHelper.lerp3(delta, delta, delta, v1, v2, v2, v3, v2, v3, v3, v4);
    }

    static double cubicMix(double v1, double v2, double v3, double v4, double delta) {
        return MathHelper.lerp3(delta, delta, delta, v1, v2, v2, v3, v2, v3, v3, v4);
    }

    static Vector3f apply(Vector3f vector, Float2FloatFunction function) {
        return vector.set(
                function.get(vector.x),
                function.get(vector.y),
                function.get(vector.z)
        );
    }

    static Vector3d apply(Vector3d vector, Double2DoubleFunction function) {
        return new Vector3d(
                function.applyAsDouble(vector.x),
                function.applyAsDouble(vector.y),
                function.applyAsDouble(vector.z)
        );
    }
}
