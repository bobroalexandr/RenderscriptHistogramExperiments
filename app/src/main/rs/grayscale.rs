#pragma version(1)
#pragma rs java_package_name(com.xrigau.renderscripting)
#pragma rs_fp_relaxed

static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};

uchar4 RS_KERNEL grayscale(uchar4 in, uint32_t x, uint32_t y) {
    const float4 val = rsUnpackColor8888(in);
    const float dotVal = dot(val, weight);
    return rsPackColorTo8888(dotVal, dotVal, dotVal);
}