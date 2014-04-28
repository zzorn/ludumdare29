
#ifdef GL_ES
precision mediump float;
#endif

uniform vec4 u_color;

uniform mat3 NormalMatrix;
uniform vec3 SunDirection;
uniform vec4 SunLightColor;
uniform vec3 CameraPosition;
uniform float SeaLevel;

uniform int IsSky;

varying vec4 surfaceColor;
varying vec2 texCoord0;
varying vec3 normal;
varying vec4 worldPos;
varying vec4 screenPos;
varying float airFogFactor;
varying float waterFogFactor;

const vec4 AIR_FOG_COLOR = vec4(0.4, 0.5, 0.6, 1.0);
const vec4 WATER_FOG_COLOR = vec4(0.0, 0.03,0.1, 1.0);

const float COLOR_DROPOFF_SHARPNESS = 1.4;
const float RED_HALF_DEPTH = 30.0;
const float GREEN_HALF_DEPTH = 80.0;
const float BLUE_HALF_DEPTH = 150.0;
const float SUNLIGHT_BOOST = 3.0;
const float EXTRA_SUNLIGHT_BOOST_ABOVE_SURFACE = 1.5;
const vec4 AMBIENT_LIGHT = vec4(0.02, 0.03, 0.1, 1.0);
const vec3 DOWN = vec3(0.0, -1.0, 0.0);
const float REFLECTION_STRENGTH = 1.5;
const float SURFACE_LIGHT_BOUNDARY_DEPTH = 0.5;

float stretch(float value, float midPoint) {
    value    = pow(value,    COLOR_DROPOFF_SHARPNESS);
    midPoint = pow(midPoint, COLOR_DROPOFF_SHARPNESS);
	float v = value / (value + midPoint);
    if (v < 0.0) v = 0.0;
    return 1.0 - v;
}

float calculateDepth(float y) {
    float d = SeaLevel - y;
    if (d < 0.0) d = 0.0;
    return d;
}

float map(float srcPos, float srcStart, float srcEnd, float targetStart, float targetEnd) {
    float relPos = (srcPos - srcStart) / (srcEnd - srcStart);
    return targetStart + relPos * (targetEnd - targetStart);
}

vec4 depthColorAdjust(float depth) {
    float rAdjust = stretch(depth, RED_HALF_DEPTH);
    float gAdjust = stretch(depth, GREEN_HALF_DEPTH);
    float bAdjust = stretch(depth, BLUE_HALF_DEPTH);
    vec4 adjust = vec4(rAdjust, gAdjust, bAdjust, 1.0);

    if (depth <= SURFACE_LIGHT_BOUNDARY_DEPTH) {
        adjust.rgb *= map(depth, 0.0, SURFACE_LIGHT_BOUNDARY_DEPTH, EXTRA_SUNLIGHT_BOOST_ABOVE_SURFACE, 1.0);
    }
    return adjust;
}


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

}

void renderSky(vec3 norm) {

    float angle = norm.y;

    if (angle > 0.0) {
        // Sky
        gl_FragColor.rgb = mix(vec3(0.7, 0.75, 0.75), vec3(0.05, 0.3, 0.5), angle);
    }
    else {
        // Sea
        //gl_FragColor.rgb = mix(vec3(0.2, 0.5, 0.5), vec3(0, 0.05, 0.2), -angle);

    }

}

void main() {

    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);

    vec3 norm = normalize(NormalMatrix * normal);

    if (IsSky == 0) {
        renderObject(norm);
    }
    else {
        renderSky(norm);
    }


    float angle = norm.y;
    float yPos = worldPos.y;
    if (IsSky == 1) yPos += norm.y * 100000.0;
    float depth = calculateDepth(yPos);
    vec4 depthColor = depthColorAdjust(depth) * WATER_FOG_COLOR;
    vec3 seaFogColor = depthColor.rgb;

    // Fog with distance
    //gl_FragColor.rgb = mix(depthColor.rgb, gl_FragColor.rgb, waterFogFactor);
    gl_FragColor.rgb = mix(WATER_FOG_COLOR.rgb, gl_FragColor.rgb, waterFogFactor);
    //gl_FragColor = mix(AIR_FOG_COLOR, gl_FragColor, airFogFactor);
    //gl_FragColor.r = waterFogFactor;


    gl_FragColor.a = 1.0;
}




