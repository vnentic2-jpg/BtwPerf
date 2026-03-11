package com.legacyperf.util;

/**
 * A lightweight Axis-Aligned Bounding Box (AABB) frustum tester.
 *
 * Updated every frame from ChunkRenderCullingMixin and EntityCullingMixin.
 * Uses the raw OpenGL modelview+projection matrices that are already on the
 * GL stack, so it has zero extra rendering cost.
 *
 * The frustum is stored as 6 planes in the form (nx, ny, nz, d) where the
 * plane equation is: nx*x + ny*y + nz*z + d >= 0 means "inside".
 */
public final class PerfFrustum {

    // 6 planes × 4 floats (nx, ny, nz, d)
    private static final float[][] PLANES = new float[6][4];

    // The combined clip matrix (modelview × projection), column-major
    private static final float[] CLIP = new float[16];

    private PerfFrustum() {}

    /**
     * Rebuild the frustum from raw GL matrices. Call once per frame before
     * any culling checks, e.g. from the start of world rendering.
     *
     * @param modelview  16-element column-major modelview matrix
     * @param projection 16-element column-major projection matrix
     */
    public static void update(float[] modelview, float[] projection) {
        // clip = projection * modelview
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                float sum = 0f;
                for (int k = 0; k < 4; k++) {
                    sum += projection[r + k * 4] * modelview[k + c * 4];
                }
                CLIP[r + c * 4] = sum;
            }
        }

        // Extract planes from the clip matrix (Gribb/Hartmann method)
        extractPlane(0, CLIP[3]  - CLIP[0],  CLIP[7]  - CLIP[4],  CLIP[11] - CLIP[8],  CLIP[15] - CLIP[12]); // right
        extractPlane(1, CLIP[3]  + CLIP[0],  CLIP[7]  + CLIP[4],  CLIP[11] + CLIP[8],  CLIP[15] + CLIP[12]); // left
        extractPlane(2, CLIP[3]  - CLIP[1],  CLIP[7]  - CLIP[5],  CLIP[11] - CLIP[9],  CLIP[15] - CLIP[13]); // top
        extractPlane(3, CLIP[3]  + CLIP[1],  CLIP[7]  + CLIP[5],  CLIP[11] + CLIP[9],  CLIP[15] + CLIP[13]); // bottom
        extractPlane(4, CLIP[3]  - CLIP[2],  CLIP[7]  - CLIP[6],  CLIP[11] - CLIP[10], CLIP[15] - CLIP[14]); // far
        extractPlane(5, CLIP[3]  + CLIP[2],  CLIP[7]  + CLIP[6],  CLIP[11] + CLIP[10], CLIP[15] + CLIP[14]); // near
    }

    private static void extractPlane(int i, float a, float b, float c, float d) {
        float len = (float) Math.sqrt(a * a + b * b + c * c);
        if (len == 0f) len = 1f;
        PLANES[i][0] = a / len;
        PLANES[i][1] = b / len;
        PLANES[i][2] = c / len;
        PLANES[i][3] = d / len;
    }

    /**
     * Returns true if the AABB (minX..maxX, minY..maxY, minZ..maxZ) is
     * entirely OUTSIDE the frustum (i.e. safe to skip rendering).
     */
    public static boolean isAabbOutside(double minX, double minY, double minZ,
                                        double maxX, double maxY, double maxZ) {
        for (float[] p : PLANES) {
            // For each plane, find the "positive vertex" – the corner that is
            // furthest in the direction of the plane normal. If even that is
            // behind the plane, the whole box is outside.
            float px = (p[0] >= 0) ? (float) maxX : (float) minX;
            float py = (p[1] >= 0) ? (float) maxY : (float) minY;
            float pz = (p[2] >= 0) ? (float) maxZ : (float) minZ;
            if (p[0] * px + p[1] * py + p[2] * pz + p[3] < 0f) {
                return true; // entire box is outside this plane → cull
            }
        }
        return false;
    }

    /**
     * Convenience overload with a uniform padding value (added on all sides).
     */
    public static boolean isAabbOutside(double minX, double minY, double minZ,
                                        double maxX, double maxY, double maxZ,
                                        double padding) {
        return isAabbOutside(minX - padding, minY - padding, minZ - padding,
                             maxX + padding, maxY + padding, maxZ + padding);
    }
}
