
const float LOG2 = 1.442695;
const float WATER_FOG_DENSITY = 0.002;
const float AIR_FOG_DENSITY = 0.00001;

uniform int IsSky;

uniform vec4 u_color;
uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;

uniform mat3 NormalMatrix;
uniform vec3 CameraPosition;
uniform float SeaLevel;


attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
attribute vec4 a_color;


varying vec4 surfaceColor;
varying vec2 texCoord0;
varying vec3 normal;
varying vec4 worldPos;
varying vec4 screenPos;
varying float airFogFactor;
varying float waterFogFactor;

float calculateFogFactor(float density, float distance) {
    return exp2( -LOG2 *
                  density * density*
                  distance * distance);
}

void main() {
    worldPos = u_worldTrans * vec4(a_position, 1.0);

    gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);

    screenPos = gl_Position;


    surfaceColor = vec4(a_color);
    texCoord0 = a_texCoord0;
    normal = a_normal;


    float z = screenPos.z;
    if (IsSky == 1) {
        z = 100000; // Near infinite
    }

    if ((CameraPosition.y <= SeaLevel && worldPos.y > SeaLevel) ||
        (CameraPosition.y > SeaLevel && worldPos.y <= SeaLevel)) {
        // Line crosses through water surface
        float yDist = abs(CameraPosition.y - worldPos.y);
        if (yDist > 0) {
            float cameraSidePortion = abs(CameraPosition.y - SeaLevel) / yDist;
            float waterPortion = cameraSidePortion;
            if (CameraPosition.y > SeaLevel) {
                waterPortion = 1.0 - cameraSidePortion;
            }

            airFogFactor = calculateFogFactor(AIR_FOG_DENSITY, (1.0 - waterPortion) * z);
            waterFogFactor = calculateFogFactor(WATER_FOG_DENSITY, waterPortion * z);
        }
        else {
            waterFogFactor = 1.0;
            airFogFactor = 1.0;
        }
    }
    else {
        // Line completely on either side
        if (worldPos.y > SeaLevel) {
            // In air
            airFogFactor = calculateFogFactor(AIR_FOG_DENSITY, z);
            waterFogFactor = 1.0;
        }
        else {
            // In water
            airFogFactor = 1.0;
            waterFogFactor = calculateFogFactor(WATER_FOG_DENSITY, z);
        }

    }

    airFogFactor = clamp(airFogFactor, 0.0, 1.0);
    waterFogFactor = clamp(waterFogFactor, 0.0, 1.0);


}