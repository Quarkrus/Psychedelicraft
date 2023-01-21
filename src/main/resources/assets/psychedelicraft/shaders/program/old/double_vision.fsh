#version 120

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform float distance;
uniform float stretch;
uniform float totalAlpha;

out vec4 fragColor;

void main() {
    vec4 texel = texture(DiffuseSampler, texCoord);
    vec4 newColor = texel.rgb * 0.35;

    newColor += texture(tex0, vec2(0.5 + (texCoord.s - 0.5) / stretch + distance, texCoord.t)) * 0.325;
    newColor += texture(tex0, vec2(0.5 + (texCoord.s - 0.5) / stretch - distance, texCoord.t)) * 0.325;

    fragColor = vec4(mix(texel.rgb, newColor, totalAlpha), 1.0);
}
