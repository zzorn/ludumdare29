
const float LOG2 = 1.442695;
const float WATER_FOG_DENSITY = 0.001;
const float AIR_FOG_DENSITY = 0.0001;

uniform int SpecialType;
const int ENTITY_TYPE = 0;
const int SKY_TYPE = 1;
const int WATER_SURFACE_TYPE = 2;
const int WATER_UNDERSIDE_TYPE = 3;

uniform vec4 ObjectColor;
uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;

uniform mat3 NormalMatrix;
uniform vec3 CameraPosition;
uniform float SeaLevel;


attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
attribute vec4 a_color;


varying vec4 vertexColor;
varying vec2 texCoord0;
varying vec3 normal;
varying vec4 worldPos;
varying vec4 screenPos;
varying float airFogFactor;
varying float waterFogFactor;

varying float airDistance;
varying float waterDistance;


float calculateFogFactor(float density, float distance) {
    return exp2( -LOG2 *
                  density * density*
                  distance * distance);
}

void main() {
    worldPos = u_worldTrans * vec4(a_position, 1.0);

    gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);

    screenPos = gl_Position;


    vertexColor = vec4(a_color);
    texCoord0 = a_texCoord0;
    normal = a_normal;


    float z = screenPos.z;
    if (SpecialType == SKY_TYPE) {
        z = 1000000.0; // High distance
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

            airDistance = (1.0 - waterPortion) * z;
            waterDistance = waterPortion * z;
       }
        else {
            waterFogFactor = 0.0;
            airFogFactor = 0.0;
        }
    }
    else {
        // Line completely on either side
        if (worldPos.y > SeaLevel) {
            // In air
            airDistance = z;
            waterDistance = 0.0;
        }
        else {
            // In water
            airDistance = 0.0;
            waterDistance = z;
        }
    }

    airFogFactor = calculateFogFactor(AIR_FOG_DENSITY, airDistance);
    waterFogFactor = calculateFogFactor(WATER_FOG_DENSITY, waterDistance);

    airFogFactor = clamp(airFogFactor, 0.0, 1.0);
    waterFogFactor = clamp(waterFogFactor, 0.0, 1.0);


}