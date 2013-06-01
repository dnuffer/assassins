package com.nbs.client.assassins.sensors;

public class SensorManagerStub implements Spatial {

	private static final float[] mTempMatrix = new float[16];
	
	public SensorManagerStub(){}
	
	/**
     * Computes the inclination matrix <b>I</b> as well as the rotation
     * matrix <b>R</b> transforming a vector from the
     * device coordinate system to the world's coordinate system which is
     * defined as a direct orthonormal basis, where:
     * 
     * <li>X is defined as the vector product <b>Y.Z</b> (It is tangential to
     * the ground at the device's current location and roughly points East).</li>
     * <li>Y is tangential to the ground at the device's current location and
     * points towards the magnetic North Pole.</li>
     * <li>Z points towards the sky and is perpendicular to the ground.</li>
     * <p>
     * <hr>
     * <p>By definition:
     * <p>[0 0 g] = <b>R</b> * <b>gravity</b> (g = magnitude of gravity)
     * <p>[0 m 0] = <b>I</b> * <b>R</b> * <b>geomagnetic</b>
     * (m = magnitude of geomagnetic field)
     * <p><b>R</b> is the identity matrix when the device is aligned with the
     * world's coordinate system, that is, when the device's X axis points
     * toward East, the Y axis points to the North Pole and the device is facing
     * the sky.
     *
     * <p><b>I</b> is a rotation matrix transforming the geomagnetic
     * vector into the same coordinate space as gravity (the world's coordinate
     * space). <b>I</b> is a simple rotation around the X axis.
     * The inclination angle in radians can be computed with
     * {@link #getInclination}.
     * <hr>
     * 
     * <p> Each matrix is returned either as a 3x3 or 4x4 row-major matrix
     * depending on the length of the passed array:
     * <p><u>If the array length is 16:</u>
     * <pre>
     *   /  M[ 0]   M[ 1]   M[ 2]   M[ 3]  \
     *   |  M[ 4]   M[ 5]   M[ 6]   M[ 7]  |
     *   |  M[ 8]   M[ 9]   M[10]   M[11]  |
     *   \  M[12]   M[13]   M[14]   M[15]  /
     *</pre>
     * This matrix is ready to be used by OpenGL ES's 
     * {@link javax.microedition.khronos.opengles.GL10#glLoadMatrixf(float[], int) 
     * glLoadMatrixf(float[], int)}. 
     * <p>Note that because OpenGL matrices are column-major matrices you must
     * transpose the matrix before using it. However, since the matrix is a 
     * rotation matrix, its transpose is also its inverse, conveniently, it is
     * often the inverse of the rotation that is needed for rendering; it can
     * therefore be used with OpenGL ES directly.
     * <p>
     * Also note that the returned matrices always have this form:
     * <pre>
     *   /  M[ 0]   M[ 1]   M[ 2]   0  \
     *   |  M[ 4]   M[ 5]   M[ 6]   0  |
     *   |  M[ 8]   M[ 9]   M[10]   0  |
     *   \      0       0       0   1  /
     *</pre>
     * <p><u>If the array length is 9:</u>
     * <pre>
     *   /  M[ 0]   M[ 1]   M[ 2]  \
     *   |  M[ 3]   M[ 4]   M[ 5]  |
     *   \  M[ 6]   M[ 7]   M[ 8]  /
     *</pre>
     *
     * <hr>
     * <p>The inverse of each matrix can be computed easily by taking its
     * transpose.
     *
     * <p>The matrices returned by this function are meaningful only when the
     * device is not free-falling and it is not close to the magnetic north.
     * If the device is accelerating, or placed into a strong magnetic field,
     * the returned matrices may be inaccurate.
     *
     * @param R is an array of 9 floats holding the rotation matrix <b>R</b>
     * when this function returns. R can be null.<p>
     * @param I is an array of 9 floats holding the rotation matrix <b>I</b>
     * when this function returns. I can be null.<p>
     * @param gravity is an array of 3 floats containing the gravity vector
     * expressed in the device's coordinate. You can simply use the
     * {@link android.hardware.SensorEvent#values values}
     * returned by a {@link android.hardware.SensorEvent SensorEvent} of a
     * {@link android.hardware.Sensor Sensor} of type
     * {@link android.hardware.Sensor#TYPE_ACCELEROMETER TYPE_ACCELEROMETER}.<p>
     * @param geomagnetic is an array of 3 floats containing the geomagnetic
     * vector expressed in the device's coordinate. You can simply use the
     * {@link android.hardware.SensorEvent#values values}
     * returned by a {@link android.hardware.SensorEvent SensorEvent} of a
     * {@link android.hardware.Sensor Sensor} of type
     * {@link android.hardware.Sensor#TYPE_MAGNETIC_FIELD TYPE_MAGNETIC_FIELD}.
     * @return
     *   true on success<p>
     *   false on failure (for instance, if the device is in free fall).
     *   On failure the output matrices are not modified.
     */
	@Override
    public boolean getRotationMatrix(float[] R, float[] I,
            float[] gravity, float[] geomagnetic) {
        // TODO: move this to native code for efficiency
        float Ax = gravity[0];
        float Ay = gravity[1];
        float Az = gravity[2];
        final float Ex = geomagnetic[0];
        final float Ey = geomagnetic[1];
        final float Ez = geomagnetic[2];
        float Hx = Ey*Az - Ez*Ay;
        float Hy = Ez*Ax - Ex*Az;
        float Hz = Ex*Ay - Ey*Ax;
        final float normH = (float)Math.sqrt(Hx*Hx + Hy*Hy + Hz*Hz);
        if (normH < 0.1f) {
            // device is close to free fall (or in space?), or close to
            // magnetic north pole. Typical values are  > 100.
            return false;
        }
        final float invH = 1.0f / normH;
        Hx *= invH;
        Hy *= invH;
        Hz *= invH;
        final float invA = 1.0f / (float)Math.sqrt(Ax*Ax + Ay*Ay + Az*Az);
        Ax *= invA;
        Ay *= invA;
        Az *= invA;
        final float Mx = Ay*Hz - Az*Hy;
        final float My = Az*Hx - Ax*Hz;
        final float Mz = Ax*Hy - Ay*Hx;
        if (R != null) {
            if (R.length == 9) {
                R[0] = Hx;     R[1] = Hy;     R[2] = Hz;
                R[3] = Mx;     R[4] = My;     R[5] = Mz;
                R[6] = Ax;     R[7] = Ay;     R[8] = Az;
            } else if (R.length == 16) {
                R[0]  = Hx;    R[1]  = Hy;    R[2]  = Hz;   R[3]  = 0;
                R[4]  = Mx;    R[5]  = My;    R[6]  = Mz;   R[7]  = 0;
                R[8]  = Ax;    R[9]  = Ay;    R[10] = Az;   R[11] = 0;
                R[12] = 0;     R[13] = 0;     R[14] = 0;    R[15] = 1;
            }
        }
        if (I != null) {
            // compute the inclination matrix by projecting the geomagnetic
            // vector onto the Z (gravity) and X (horizontal component
            // of geomagnetic vector) axes.
            final float invE = 1.0f / (float)Math.sqrt(Ex*Ex + Ey*Ey + Ez*Ez);
            final float c = (Ex*Mx + Ey*My + Ez*Mz) * invE;
            final float s = (Ex*Ax + Ey*Ay + Ez*Az) * invE;
            if (I.length == 9) {
                I[0] = 1;     I[1] = 0;     I[2] = 0;
                I[3] = 0;     I[4] = c;     I[5] = s;
                I[6] = 0;     I[7] =-s;     I[8] = c;
            } else if (I.length == 16) {
                I[0] = 1;     I[1] = 0;     I[2] = 0;
                I[4] = 0;     I[5] = c;     I[6] = s;
                I[8] = 0;     I[9] =-s;     I[10]= c;
                I[3] = I[7] = I[11] = I[12] = I[13] = I[14] = 0;
                I[15] = 1;
            }
        }
        return true;
    }

    /**
     * Computes the geomagnetic inclination angle in radians from the
     * inclination matrix <b>I</b> returned by {@link #getRotationMatrix}.
     * @param I inclination matrix see {@link #getRotationMatrix}.
     * @return The geomagnetic inclination angle in radians.
     */
    public static float getInclination(float[] I) {
        if (I.length == 9) {
            return (float)Math.atan2(I[5], I[4]);
        } else {
            return (float)Math.atan2(I[6], I[5]);            
        }
    }

    /**
     * Rotates the supplied rotation matrix so it is expressed in a
     * different coordinate system. This is typically used when an application
     * needs to compute the three orientation angles of the device (see
     * {@link #getOrientation}) in a different coordinate system.
     * 
     * <p>When the rotation matrix is used for drawing (for instance with 
     * OpenGL ES), it usually <b>doesn't need</b> to be transformed by this 
     * function, unless the screen is physically rotated, such as when used
     * in landscape mode. 
     *
     * <p><u>Examples:</u><p>
     *
     * <li>Using the camera (Y axis along the camera's axis) for an augmented 
     * reality application where the rotation angles are needed :</li><p>
     *
     * <code>remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR);</code><p>
     *
     * <li>Using the device as a mechanical compass in landscape mode:</li><p>
     *
     * <code>remapCoordinateSystem(inR, AXIS_Y, AXIS_MINUS_X, outR);</code><p>
     *
     * Beware of the above example. This call is needed only if the device is
     * physically used in landscape mode to calculate the rotation angles (see 
     * {@link #getOrientation}).
     * If the rotation matrix is also used for rendering, it may not need to 
     * be transformed, for instance if your {@link android.app.Activity
     * Activity} is running in landscape mode.
     *
     * <p>Since the resulting coordinate system is orthonormal, only two axes
     * need to be specified.
     *
     * @param inR the rotation matrix to be transformed. Usually it is the
     * matrix returned by {@link #getRotationMatrix}.
     * @param X defines on which world axis and direction the X axis of the
     *        device is mapped.
     * @param Y defines on which world axis and direction the Y axis of the
     *        device is mapped.
     * @param outR the transformed rotation matrix. inR and outR can be the same
     *        array, but it is not recommended for performance reason.
     * @return true on success. false if the input parameters are incorrect, for
     * instance if X and Y define the same axis. Or if inR and outR don't have 
     * the same length.
     */

    public boolean remapCoordinateSystem(float[] inR, int X, int Y,
            float[] outR)
    {
        if (inR == outR) {
            final float[] temp = mTempMatrix;
            synchronized(temp) {
                // we don't expect to have a lot of contention
                if (remapCoordinateSystemImpl(inR, X, Y, temp)) {
                    final int size = outR.length;
                    for (int i=0 ; i<size ; i++)
                        outR[i] = temp[i];
                    return true;
                }
            }
        }
        return remapCoordinateSystemImpl(inR, X, Y, outR);
    }

    private boolean remapCoordinateSystemImpl(float[] inR, int X, int Y,
            float[] outR)
    {
        /*
         * X and Y define a rotation matrix 'r':
         *
         *  (X==1)?((X&0x80)?-1:1):0    (X==2)?((X&0x80)?-1:1):0    (X==3)?((X&0x80)?-1:1):0
         *  (Y==1)?((Y&0x80)?-1:1):0    (Y==2)?((Y&0x80)?-1:1):0    (Y==3)?((X&0x80)?-1:1):0
         *                              r[0] ^ r[1]
         *
         * where the 3rd line is the vector product of the first 2 lines
         *
         */

        final int length = outR.length;
        if (inR.length != length)
            return false;   // invalid parameter
        if ((X & 0x7C)!=0 || (Y & 0x7C)!=0)
            return false;   // invalid parameter
        if (((X & 0x3)==0) || ((Y & 0x3)==0))
            return false;   // no axis specified
        if ((X & 0x3) == (Y & 0x3))
            return false;   // same axis specified

        // Z is "the other" axis, its sign is either +/- sign(X)*sign(Y)
        // this can be calculated by exclusive-or'ing X and Y; except for
        // the sign inversion (+/-) which is calculated below.
        int Z = X ^ Y;

        // extract the axis (remove the sign), offset in the range 0 to 2.
        final int x = (X & 0x3)-1;
        final int y = (Y & 0x3)-1;
        final int z = (Z & 0x3)-1;

        // compute the sign of Z (whether it needs to be inverted)
        final int axis_y = (z+1)%3;
        final int axis_z = (z+2)%3;
        if (((x^axis_y)|(y^axis_z)) != 0)
            Z ^= 0x80;

        final boolean sx = (X>=0x80);
        final boolean sy = (Y>=0x80);
        final boolean sz = (Z>=0x80);

        // Perform R * r, in avoiding actual muls and adds.
        final int rowLength = ((length==16)?4:3);
        for (int j=0 ; j<3 ; j++) {
            final int offset = j*rowLength;
            for (int i=0 ; i<3 ; i++) {
                if (x==i)   outR[offset+i] = sx ? -inR[offset+0] : inR[offset+0];
                if (y==i)   outR[offset+i] = sy ? -inR[offset+1] : inR[offset+1];
                if (z==i)   outR[offset+i] = sz ? -inR[offset+2] : inR[offset+2];
            }
        }
        if (length == 16) {
            outR[3] = outR[7] = outR[11] = outR[12] = outR[13] = outR[14] = 0;
            outR[15] = 1;
        }
        return true;
    }

    /**
     * Computes the device's orientation based on the rotation matrix.
     * <p> When it returns, the array values is filled with the result:
     * <li>values[0]: <i>azimuth</i>, rotation around the Z axis.</li>
     * <li>values[1]: <i>pitch</i>, rotation around the X axis.</li>
     * <li>values[2]: <i>roll</i>, rotation around the Y axis.</li>
     * <p>
     *
     * @param R rotation matrix see {@link #getRotationMatrix}.
     * @param values an array of 3 floats to hold the result.
     * @return The array values passed as argument.
     */
    @Override
    public float[] getOrientation(float[] R, float values[]) {
        /*
         * 4x4 (length=16) case:
         *   /  R[ 0]   R[ 1]   R[ 2]   0  \
         *   |  R[ 4]   R[ 5]   R[ 6]   0  |
         *   |  R[ 8]   R[ 9]   R[10]   0  |
         *   \      0       0       0   1  /
         *   
         * 3x3 (length=9) case:
         *   /  R[ 0]   R[ 1]   R[ 2]  \
         *   |  R[ 3]   R[ 4]   R[ 5]  |
         *   \  R[ 6]   R[ 7]   R[ 8]  /
         * 
         */
        if (R.length == 9) {
            values[0] = (float)Math.atan2(R[1], R[4]);
            values[1] = (float)Math.asin(-R[7]);
            values[2] = (float)Math.atan2(-R[6], R[8]);
        } else {
            values[0] = (float)Math.atan2(R[1], R[5]);
            values[1] = (float)Math.asin(-R[9]);
            values[2] = (float)Math.atan2(-R[8], R[10]);
        }
        return values;
    }


}
