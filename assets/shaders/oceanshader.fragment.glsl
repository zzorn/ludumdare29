
#ifdef GL_ES
precision mediump float;
#endif

uniform vec4 ObjectColor;

uniform mat3 NormalMatrix;
uniform vec3 SunDirection;
uniform vec4 SunLightColor;
uniform vec3 CameraPosition;
uniform float SeaLevel;

uniform int SpecialType;
const int ENTITY_TYPE = 0;
const int SKY_TYPE = 1;
const int WATER_SURFACE_TYPE = 2;
const int WATER_UNDERSIDE_TYPE = 3;
const int AIR_BUBBLE_TYPE = 4;

varying vec4 vertexColor;
varying vec2 texCoord0;
varying vec3 normal;
varying vec4 worldPos;
varying vec4 screenPos;
varying float airFogFactor;
varying float waterFogFactor;
varying float airDistance;
varying float waterDistance;


const vec3 SUN_COLOR = vec3(0.8, 0.75, 0.7);
const float SUN_INTENSITY = 3;

const vec4 AIR_FOG_COLOR = vec4(0.4, 0.5, 0.6, 1.0);
const vec4 WATER_FOG_COLOR = vec4(0.001, 0.03, 0.1, 1.0);

const float COLOR_DROPOFF_SHARPNESS = 1.4;
const float RED_HALF_DEPTH = 2*30.0;
const float GREEN_HALF_DEPTH = 2*80.0;
const float BLUE_HALF_DEPTH = 2*150.0;
const float SUNLIGHT_BOOST = 3;
const float EXTRA_SUNLIGHT_BOOST_ABOVE_SURFACE = 1.5;
const vec4 AMBIENT_LIGHT = vec4(0.04, 0.06, 0.2, 1.0);
const vec4 SURFACE_AMBIENT_LIGHT = vec4(0.1, 0.2, 0.3, 1.0);
const float REFLECTION_STRENGTH = 1.5;
const float SURFACE_LIGHT_BOUNDARY_DEPTH = 0.5;

float calculateDepth(float y) {
    float d = SeaLevel - y;
    if (d < 0.0) d = 0.0;
    return d;
}

float map(float srcPos, float srcStart, float srcEnd, float targetStart, float targetEnd) {
    float relPos = (srcPos - srcStart) / (srcEnd - srcStart);
    return targetStart + relPos * (targetEnd - targetStart);
}

float stretch(float value, float midPoint) {
    value    = pow(value,    COLOR_DROPOFF_SHARPNESS);
    midPoint = pow(midPoint, COLOR_DROPOFF_SHARPNESS);
	float v = value / (value + midPoint);
    if (v < 0.0) v = 0.0;
    return 1.0 - v;
}

vec3 waterColorFactor(float distance) {
    float rAdjust = stretch(distance, RED_HALF_DEPTH);
    float gAdjust = stretch(distance, GREEN_HALF_DEPTH);
    float bAdjust = stretch(distance, BLUE_HALF_DEPTH);
    vec3 adjust = vec3(rAdjust, gAdjust, bAdjust);

    /*
    if (depth <= SURFACE_LIGHT_BOUNDARY_DEPTH) {
        adjust *= map(depth, 0.0, SURFACE_LIGHT_BOUNDARY_DEPTH, EXTRA_SUNLIGHT_BOOST_ABOVE_SURFACE, 1.0);
    }
    */

    return adjust;
}

/*
void renderObject(vec3 norm) {
    // Depth effect
    float depth = calculateDepth(worldPos.y);
    vec4 depthAdjust = depthColorAdjust(depth);

    // Sunlight
    float sunlightAmount = max(0.0, dot(norm, SunDirection));
    vec4 adjustedSunlightColor = vec4(SunLightColor.rgb * SunLightColor.a, 1.0);
    vec4 boostedSunshine = vec4(SUNLIGHT_BOOST * adjustedSunlightColor.rgb, 1.0);
    gl_FragColor += u_color * depthAdjust * sunlightAmount * boostedSunshine;

    // Reflected sunlight from water below
    vec4 deeperDepthAdjust = depthColorAdjust(depth * depth + 30.0);
    float reflectedAmount = (dot(norm, DOWN) + 1.0) * 0.5;
    gl_FragColor += u_color * REFLECTION_STRENGTH * deeperDepthAdjust * reflectedAmount * adjustedSunlightColor;

    // Ambient light from surrounding water
    gl_FragColor += u_color * AMBIENT_LIGHT * depthAdjust;

    // Ambient light near surface
    float surfaceAmbientAmount = 1.0 - depth * depth * 0.00001f;
    if (surfaceAmbientAmount > 0) {
        gl_FragColor += u_color * SURFACE_AMBIENT_LIGHT * surfaceAmbientAmount;
    }

}

void renderSky(vec3 norm) {

    float angle = norm.y;

    if (angle > 0.0) {
        // Sky
        gl_FragColor.rgb = mix(vec3(0.7, 0.75, 0.75), vec3(0.05, 0.3, 0.5), angle);
    }
    else {
        // Sea
        float virtualDepth = calculateDepth(worldPos.y);
        vec4 depthColor = depthColorAdjust(virtualDepth);
        gl_FragColor.rgb = depthColor;
        //gl_FragColor.rgb = mix(vec3(0.2, 0.5, 0.5), vec3(0, 0.05, 0.2), -angle*1000);

    }

}

*/

const float MAX_WATER_SAMPLING_DISTANCE = 100;

const int WATER_STEPS = 3;

const vec3 UP = vec3(0, 1, 0);
const vec3 DOWN = vec3(0, -1, 0);

void main() {

    vec3 worldNormal = normalize(NormalMatrix * normal);

    float pointDepth = calculateDepth(worldPos.y);

    float cameraPointDistance = screenPos.z;

    float alpha = 1.0;

    vec3 objectColor = ObjectColor.rgb * vertexColor;

    // TODO: Reflect sky / underwater from water surface, or see through, depending on angle
    if (SpecialType == WATER_SURFACE_TYPE) {
        alpha = min(1, cameraPointDistance * 0.01);
        objectColor = vec3(0.5, 0.6, 0.7);
    }
    else if (SpecialType == WATER_UNDERSIDE_TYPE) {
        alpha = min(1, cameraPointDistance * 0.01);
        objectColor = vec3(0.2, 0.8, 0.4);
    }

    if (SpecialType == SKY_TYPE) cameraPointDistance = MAX_WATER_SAMPLING_DISTANCE;

    vec3 ambientLightAtPoint = SUN_INTENSITY * SUN_COLOR * waterColorFactor(pointDepth);

    vec3 sunlightAtPoint = max(0, dot(worldNormal, UP)) * SUN_INTENSITY * SUN_COLOR * waterColorFactor(pointDepth);


    vec3 color = ambientLightAtPoint * objectColor +
                 sunlightAtPoint * objectColor;

    float distanceStep = cameraPointDistance / WATER_STEPS + 0.01;
    vec3 stepWaterAttenuation = waterColorFactor(cameraPointDistance);
    float stepAmbientLightAmount = 1.0 / WATER_STEPS;
    for (float d = cameraPointDistance; d > 0; d -= distanceStep) {
        // Apply water color attenuation
        color *= stepWaterAttenuation;

        // Add ambient light from surface

    }


    if (SpecialType == AIR_BUBBLE_TYPE) {
        float lum = (color.r + color.g + color.b) / 3.0;
        float visibility = abs(lum - 0.5) * 2;
        alpha = visibility * visibility * visibility;
    }


    //vec3 color = pointColor * waterColorFactor(cameraPointDistance);




/*
    if (IsSky == 0) renderObject(norm);
    else renderSky(norm);
*/

//    float angle = norm.y;
//    float yPos = worldPos.y;
//    if (IsSky == 1) yPos += norm.y * 100000.0;
//    vec4 depthColor = depthColorAdjust(depth) * WATER_FOG_COLOR;
//    vec3 seaFogColor = depthColor.rgb;

    // Fog with distance
    //gl_FragColor.rgb = mix(depthColor.rgb, gl_FragColor.rgb, waterFogFactor);
    //gl_FragColor.rgb = mix(WATER_FOG_COLOR.rgb, gl_FragColor.rgb, waterFogFactor);
    //gl_FragColor = mix(AIR_FOG_COLOR, gl_FragColor, airFogFactor);
    //gl_FragColor.r = waterFogFactor;


    gl_FragColor = vec4(color, alpha);
}




