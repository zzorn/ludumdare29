#version 150
 
uniform mat4 viewMatrix, projMatrix;
 
in vec4 position;
in vec3 color;
 
out vec3 Color;
 
void main()
{
    Color = position.xyz;
    gl_Position = projMatrix * viewMatrix * position ;
}