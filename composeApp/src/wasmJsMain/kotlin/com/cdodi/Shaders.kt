@file:Suppress("unused")

package com.cdodi

internal object Shaders {
    const val testShaderStr = """
        uniform int width;
        uniform int height;
        uniform float iTime;

        half4 main(vec2 fragCoord) {
            vec2 iResolution = vec2(width, height);
            vec2 uv = (fragCoord - iResolution.xy * 0.5) / iResolution.y;

            return half4(vec3(length(uv)), abs(sin(iTime)));
        }
    """

    const val wetNeuralNetwork = """
        // Original shader: https://www.shadertoy.com/view/mlBXRK

        uniform int width;
        uniform int height;
        uniform float iTime;

        mat2 rotate2D(float r) {
            return mat2(cos(r), sin(r), -sin(r), cos(r));
        }


        half4 main(vec2 fragCoord) {
            vec2 iResolution = vec2(width, height);
            vec2 uv = (fragCoord - iResolution.xy * 0.5) / iResolution.y;
//            uv *= 0.25;
            vec3 col = vec3(0.0);
            float t = iTime;

            vec2 n = vec2(0), q;
            vec2 N = vec2(0);
            vec2 p = uv + sin(t * 0.1) / 10.0;
            float S = 10.0;
            mat2 m = rotate2D(1.0);

            for(float j = 0.0; j < 30.0; j++) {
              p *= m;
              n *= m;
              q = p * S + j + n + t;
              n += sin(q);
              N += cos(q) / S;
              S *= 1.2;
            }

            col = vec3(1, 2, 4) * pow((N.x + N.y + 0.2) + 0.005 / length(N), 2.1);

            return half4(col, 1.0);
        }
    """

    const val bokhe = """
        uniform float2 size;
        uniform float time;
        const float loopStep = 0.1;
        uniform shader composable;

        struct Ray {
            float3 origin;
            float3 direction;
        };

        Ray getRay(float2 uv, float3 cameraPos, float3 lookAt, float zoom) {
            Ray ray;
            ray.origin = cameraPos;

            float3 forwardV = normalize(lookAt - cameraPos);
            float3 rightV = cross(float3(0.0, 1.0, 0.0), forwardV);
            float3 upV = cross(forwardV, rightV);
            float3 center = ray.origin + forwardV * zoom;
            float3 intersectionP = center + uv.x * rightV + uv.y * upV;

            ray.direction = normalize(intersectionP - ray.origin);

            return ray;
        }

        float noise(float num) {
            return fract(sin(num * 3456.0) * 6547.0);
        }

        float4 noiseVec4(float num) {
            return fract(sin(num * float4(123.0, 1024.0, 3456.0, 9564.0)) * float4(6547.0, 345.0, 8799.0, 1564.0));
        }

        float3 closestPoint(Ray ray, float3 point) {
            return ray.origin + max(0, dot(point - ray.origin, ray.direction)) * ray.direction;
        }

        float distanceToRay(Ray ray, float3 point) {
            return length(point - closestPoint(ray, point));
        }

        float bokeh(Ray ray, float3 point, float size, float blur) {
            size *= length(point);
            float distance = distanceToRay(ray, point);
            float circle = smoothstep(size, size * (1.0 - blur), distance);
            circle *= mix(0.6, 1.0, smoothstep(size * 0.8, size, distance));

            return circle;
        }

        float3 streetLights(Ray ray, float time) {
            float mask = 0.0;
            float side = step(ray.direction.x, 0.0);
            ray.direction.x = abs(ray.direction.x);

            for (float i = 0.0; i < 1.0; i += loopStep) {
                float iTime = fract(i + time + side * loopStep * 0.5);
                float3 point = float3(2.0, 2.0, 100.0 - iTime * 100.0);

                mask += bokeh(ray, point, 0.05, 0.1) * iTime * iTime * iTime;
            }

            return float3(1.0, 0.7, 0.3) * mask;
        }

        float3 headLights(Ray ray, float time) {
            time *= 2.0;

            float mask = 0.0;
            float width0 = 0.25;
            float width1 = width0 * 1.2;

            for (float i = 0.0; i < 1.0; i += 1.0 / 30.0) {
                float n = noise(i);

                if (n > 0.1) continue;

                float iTime = fract(i + time);
                float z = 100.0 - iTime * 100.0;
                float fade = iTime * iTime * iTime * iTime * iTime;
                float focus = smoothstep(0.9, 1.0, iTime);
                float size = mix(0.05, 0.03, focus);

                mask += bokeh(ray, float3(-1.0 - width0, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(-1.0 + width0, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(-1.0 - width1, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(-1.0 + width1, 0.15, z), size, 0.1) * fade;

                float ref = 0.0;
                ref += bokeh(ray, float3(-1.0 - width1, -0.15, z), size * 3.0, 1.0) * fade;
                ref += bokeh(ray, float3(-1.0 + width1, -0.15, z), size * 3.0, 1.0) * fade;

                mask += ref * focus;
            }

            return float3(0.9, 0.9, 1.0) * mask;
        }

        float3 tailLights(Ray ray, float time) {
            time *= 0.25;

            float mask = 0.0;
            float width0 = 0.25;
            float width1 = width0 * 1.2;

            for (float i = 0.0; i < 1.0; i += 1.0 / 10.0) {
                float n = noise(i);

                if (n > 0.3) continue;

                float lane = step(0.25, n);
                float iTime = fract(i + time);
                float z = 100.0 - iTime * 100.0;
                float fade = iTime * iTime * iTime * iTime * iTime;
                float focus = smoothstep(0.9, 1.0, iTime);
                float size = mix(0.05, 0.03, focus);
                float shiftLane = smoothstep(1.0, 0.96, iTime);
                float x = 1.5 - lane * shiftLane;
                float blink = step(0.0, sin(time * 500.0)) * 7.0 * lane * step(0.9, iTime);

                mask += bokeh(ray, float3(x - width0, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(x + width0, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(x - width1, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(x + width1, 0.15, z), size, 0.1) * fade * (1.0 + blink);

                float ref = 0.0;
                ref += bokeh(ray, float3(x - width1, -0.15, z), size * 3.0, 1.0) * fade;
                ref += bokeh(ray, float3(x + width1, -0.15, z), size * 3.0, 1.0) * fade * (1.0 + blink * 0.1);

                mask += ref * focus;
            }

            return float3(1.0, 0.1, 0.03) * mask;
        }

        float3 envLights(Ray ray, float time) {
            float3 color = float3(0.0);
            float side = step(ray.direction.x, 0.0);

            ray.direction.x = abs(ray.direction.x);

            for (float i = 0.0; i < 1.0; i += 0.2) {
                float iTime = fract(i + time + side * 0.2 * 0.5);
                float4 noise = noiseVec4(i + side * 100.0);
                float occlusion = sin(iTime * 6.28 * 10.0 * noise.x) * 0.5 + 0.5;
                float x = mix(2.5, 10.0, noise.x);
                float y = mix(0.1, 2.0, noise.y);
                float3 point = float3(x, y, 50.0 - iTime * 50.0);

                color += bokeh(ray, point, 0.05, 0.1) * occlusion * noise.wzy * 0.5;
            }

            return color;
        }

        float2 distortion(float2 uv, float time) {
            time *= 40.0;

            float2 aspectRatio = float2(3.0, 1.0);
            float2 newUV = uv * aspectRatio;
            float2 id = floor(newUV);

            newUV.y += time * 0.24;
            float noise = fract(sin(id.x * 716.34) * 768.34);
            newUV.y += noise;
            uv.y += noise;
            id = floor(newUV);
            newUV = fract(newUV) - 0.5;
            // TODO add noise to drops X positions
            time += fract(sin(id.x * 76.34 + id.y * 1453.7) * 768.34) * 6.283;

            float y = -sin(time + sin(time + sin(time) * 0.5)) * 0.42;
            float2 pos = float2(0.0, y);
            float2 mainOffset = (newUV - pos) / aspectRatio;
            float distance = length(mainOffset);
            float mask1 = smoothstep(0.07, 0.0, distance);

            float2 tailOffset = (fract(uv * aspectRatio.x * float2(1.0, 2.0)) -.5) / float2(1.0, 2.0);
            distance = length(tailOffset);

            float mask2 = smoothstep(0.25 * (0.5 - newUV.y), 0.0, distance) * smoothstep(-0.1, 0.1, newUV.y - pos.y);

    //        if(newUV.x > 0.46 || newUV.y > 0.49) mask1 = 1.0;           // FOR DEBUGGING

            return float2(mask1 * mainOffset * 30.0 + mask2 * tailOffset * 10.0);
        }

        half4 main(float2 fragCoord) {
            float2 pxCoord = float2(fragCoord.x, size.y - fragCoord.y); // GLSL coords
            float2 uv = pxCoord.xy / size.xy;                           // Normalize

            uv -= .5;                                                   // Center
            uv.x *= size.x / size.y;                                    // Aspect Ratio

            float3 cameraPos = float3(0.0, 0.2, 0.0);
            float3 lookAt = float3(0., 0.2, 1.0);
            float slowed = time * 0.05;
            float2 rainDistortion = distortion(uv * 5.0, slowed) * 0.5;
            rainDistortion += distortion(uv * 7.0, slowed) * 0.5;

            uv.x += sin(uv.y * 70.0) * 0.0045;
            uv.y += sin(uv.x * 170.0) * 0.002;

            Ray ray = getRay(uv - rainDistortion * 0.5, cameraPos, lookAt, 2.0);
            float3 color = streetLights(ray, slowed);

            color += headLights(ray, slowed);
            color += tailLights(ray, slowed);
            color += envLights(ray, slowed);
            color += (ray.direction.y + 0.25) * float3(0.1, 0.1, 0.4);

            return half4(color, 1.0);
        }uniform float2 size;
        uniform float time;
        const float loopStep = 0.1;
        uniform shader composable;

        struct Ray {
            float3 origin;
            float3 direction;
        };

        Ray getRay(float2 uv, float3 cameraPos, float3 lookAt, float zoom) {
            Ray ray;
            ray.origin = cameraPos;

            float3 forwardV = normalize(lookAt - cameraPos);
            float3 rightV = cross(float3(0.0, 1.0, 0.0), forwardV);
            float3 upV = cross(forwardV, rightV);
            float3 center = ray.origin + forwardV * zoom;
            float3 intersectionP = center + uv.x * rightV + uv.y * upV;

            ray.direction = normalize(intersectionP - ray.origin);

            return ray;
        }

        float noise(float num) {
            return fract(sin(num * 3456.0) * 6547.0);
        }

        float4 noiseVec4(float num) {
            return fract(sin(num * float4(123.0, 1024.0, 3456.0, 9564.0)) * float4(6547.0, 345.0, 8799.0, 1564.0));
        }

        float3 closestPoint(Ray ray, float3 point) {
            return ray.origin + max(0, dot(point - ray.origin, ray.direction)) * ray.direction;
        }

        float distanceToRay(Ray ray, float3 point) {
            return length(point - closestPoint(ray, point));
        }

        float bokeh(Ray ray, float3 point, float size, float blur) {
            size *= length(point);
            float distance = distanceToRay(ray, point);
            float circle = smoothstep(size, size * (1.0 - blur), distance);
            circle *= mix(0.6, 1.0, smoothstep(size * 0.8, size, distance));

            return circle;
        }

        float3 streetLights(Ray ray, float time) {
            float mask = 0.0;
            float side = step(ray.direction.x, 0.0);
            ray.direction.x = abs(ray.direction.x);

            for (float i = 0.0; i < 1.0; i += loopStep) {
                float iTime = fract(i + time + side * loopStep * 0.5);
                float3 point = float3(2.0, 2.0, 100.0 - iTime * 100.0);

                mask += bokeh(ray, point, 0.05, 0.1) * iTime * iTime * iTime;
            }

            return float3(1.0, 0.7, 0.3) * mask;
        }

        float3 headLights(Ray ray, float time) {
            time *= 2.0;

            float mask = 0.0;
            float width0 = 0.25;
            float width1 = width0 * 1.2;

            for (float i = 0.0; i < 1.0; i += 1.0 / 30.0) {
                float n = noise(i);

                if (n > 0.1) continue;

                float iTime = fract(i + time);
                float z = 100.0 - iTime * 100.0;
                float fade = iTime * iTime * iTime * iTime * iTime;
                float focus = smoothstep(0.9, 1.0, iTime);
                float size = mix(0.05, 0.03, focus);

                mask += bokeh(ray, float3(-1.0 - width0, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(-1.0 + width0, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(-1.0 - width1, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(-1.0 + width1, 0.15, z), size, 0.1) * fade;

                float ref = 0.0;
                ref += bokeh(ray, float3(-1.0 - width1, -0.15, z), size * 3.0, 1.0) * fade;
                ref += bokeh(ray, float3(-1.0 + width1, -0.15, z), size * 3.0, 1.0) * fade;

                mask += ref * focus;
            }

            return float3(0.9, 0.9, 1.0) * mask;
        }

        float3 tailLights(Ray ray, float time) {
            time *= 0.25;

            float mask = 0.0;
            float width0 = 0.25;
            float width1 = width0 * 1.2;

            for (float i = 0.0; i < 1.0; i += 1.0 / 10.0) {
                float n = noise(i);

                if (n > 0.3) continue;

                float lane = step(0.25, n);
                float iTime = fract(i + time);
                float z = 100.0 - iTime * 100.0;
                float fade = iTime * iTime * iTime * iTime * iTime;
                float focus = smoothstep(0.9, 1.0, iTime);
                float size = mix(0.05, 0.03, focus);
                float shiftLane = smoothstep(1.0, 0.96, iTime);
                float x = 1.5 - lane * shiftLane;
                float blink = step(0.0, sin(time * 500.0)) * 7.0 * lane * step(0.9, iTime);

                mask += bokeh(ray, float3(x - width0, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(x + width0, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(x - width1, 0.15, z), size, 0.1) * fade;
                mask += bokeh(ray, float3(x + width1, 0.15, z), size, 0.1) * fade * (1.0 + blink);

                float ref = 0.0;
                ref += bokeh(ray, float3(x - width1, -0.15, z), size * 3.0, 1.0) * fade;
                ref += bokeh(ray, float3(x + width1, -0.15, z), size * 3.0, 1.0) * fade * (1.0 + blink * 0.1);

                mask += ref * focus;
            }

            return float3(1.0, 0.1, 0.03) * mask;
        }

        float3 envLights(Ray ray, float time) {
            float3 color = float3(0.0);
            float side = step(ray.direction.x, 0.0);

            ray.direction.x = abs(ray.direction.x);

            for (float i = 0.0; i < 1.0; i += 0.2) {
                float iTime = fract(i + time + side * 0.2 * 0.5);
                float4 noise = noiseVec4(i + side * 100.0);
                float occlusion = sin(iTime * 6.28 * 10.0 * noise.x) * 0.5 + 0.5;
                float x = mix(2.5, 10.0, noise.x);
                float y = mix(0.1, 2.0, noise.y);
                float3 point = float3(x, y, 50.0 - iTime * 50.0);

                color += bokeh(ray, point, 0.05, 0.1) * occlusion * noise.wzy * 0.5;
            }

            return color;
        }

        float2 distortion(float2 uv, float time) {
            time *= 40.0;

            float2 aspectRatio = float2(3.0, 1.0);
            float2 newUV = uv * aspectRatio;
            float2 id = floor(newUV);

            newUV.y += time * 0.24;
            float noise = fract(sin(id.x * 716.34) * 768.34);
            newUV.y += noise;
            uv.y += noise;
            id = floor(newUV);
            newUV = fract(newUV) - 0.5;
            // TODO add noise to drops X positions
            time += fract(sin(id.x * 76.34 + id.y * 1453.7) * 768.34) * 6.283;

            float y = -sin(time + sin(time + sin(time) * 0.5)) * 0.42;
            float2 pos = float2(0.0, y);
            float2 mainOffset = (newUV - pos) / aspectRatio;
            float distance = length(mainOffset);
            float mask1 = smoothstep(0.07, 0.0, distance);

            float2 tailOffset = (fract(uv * aspectRatio.x * float2(1.0, 2.0)) -.5) / float2(1.0, 2.0);
            distance = length(tailOffset);

            float mask2 = smoothstep(0.25 * (0.5 - newUV.y), 0.0, distance) * smoothstep(-0.1, 0.1, newUV.y - pos.y);

    //        if(newUV.x > 0.46 || newUV.y > 0.49) mask1 = 1.0;           // FOR DEBUGGING

            return float2(mask1 * mainOffset * 30.0 + mask2 * tailOffset * 10.0);
        }

        half4 main(float2 fragCoord) {
            float2 pxCoord = float2(fragCoord.x, size.y - fragCoord.y); // GLSL coords
            float2 uv = pxCoord.xy / size.xy;                           // Normalize

            uv -= .5;                                                   // Center
            uv.x *= size.x / size.y;                                    // Aspect Ratio

            float3 cameraPos = float3(0.0, 0.2, 0.0);
            float3 lookAt = float3(0., 0.2, 1.0);
            float slowed = time * 0.05;
            float2 rainDistortion = distortion(uv * 5.0, slowed) * 0.5;
            rainDistortion += distortion(uv * 7.0, slowed) * 0.5;

            uv.x += sin(uv.y * 70.0) * 0.0045;
            uv.y += sin(uv.x * 170.0) * 0.002;

            Ray ray = getRay(uv - rainDistortion * 0.5, cameraPos, lookAt, 2.0);
            float3 color = streetLights(ray, slowed);

            color += headLights(ray, slowed);
            color += tailLights(ray, slowed);
            color += envLights(ray, slowed);
            color += (ray.direction.y + 0.25) * float3(0.1, 0.1, 0.4);

            return half4(color, 1.0);
        }
    """
}