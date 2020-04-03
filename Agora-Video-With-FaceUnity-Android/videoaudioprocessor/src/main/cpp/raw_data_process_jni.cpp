#include <jni.h>
#include <malloc.h>
#include <libyuv.h>

#define RTMP_JAVA_PREFIX                                 io_agora_processor_media_internal
#define CONCAT1(prefix, class, function)                CONCAT2(prefix, class, function)
#define CONCAT2(prefix, class, function)                Java_ ## prefix ## _ ## class ## _ ## function
#define RTMP_JAVA_INTERFACE(function)                    CONCAT1(RTMP_JAVA_PREFIX, RawDataProcess, function)

using namespace libyuv;
#ifdef __cplusplus
extern "C" {
#endif

static int FMTtoYUV420Planer(const uint8* src_frame,
                                   int nLen,
                                   int nWidth,
                                   int nHeight,
                                   uint32 pixFmt,
                                   int nDegree,
                                   uint8* des_frame){
    int nYStride = nWidth;
    int nUVStride = nWidth / 2;
    RotationMode rotationMode = kRotate0;
    uint32 format = FOURCC_NV21;
    //XLOGI("FMTtoYUV420Planer %d,%d,%d",nLen,nWidth,nHeight);
    switch (nDegree)
    {
        case 0:
            rotationMode = kRotate0;
            break;
        case 90:
            nYStride = nHeight;
            nUVStride = nHeight/2;
            rotationMode = kRotate90;
            break;
        case 270:
            nYStride = nHeight;
            nUVStride = nHeight/2;
            rotationMode = kRotate270;
            break;
        case 180:
            rotationMode = kRotate180;
            break;
        default:
            break;
    }

    switch (pixFmt)
    {
        case 0:
            format = FOURCC_NV21;
            break;
        case 1:
            format = FOURCC_YV12;
            break;
        default:
            break;
    }

    // I420 ： count I420
    uint8 *pY = (uint8 *) des_frame;
    uint8 *pU = pY + nWidth*nHeight;
    uint8 *pV = pU + nWidth*nHeight/4;

    int nRet = ConvertToI420((uint8 *) src_frame, (int)nLen,
                             pY, nYStride,
                             pU, nUVStride,
                             pV, nUVStride,
                             0, 0,
                             (int)nWidth, nHeight,
                             (int)nWidth, nHeight,
                             rotationMode, format);
    return nRet;
}

static int FmtI420ToNV12(const uint8* src_frame,
                             uint8* des_frame,
                             int nWidth,
                             int nHeight) {
    // I420 ： count I420
    uint8 *srcY = (uint8 *) src_frame;
    uint8 *srcU = srcY + nWidth*nHeight;
    uint8 *srcV = srcU + nWidth*nHeight/4;

    int dest_y_size = nWidth * nHeight;
    uint8 *dest_y = des_frame;
    uint8 *dest_vu = des_frame + dest_y_size;

    int nRet = I420ToNV12(srcY, nWidth,
                            srcU, nWidth>>1,
                            srcV, nWidth>>1,
                            dest_y, nWidth,
                            dest_vu, nWidth,
                            nWidth, nHeight);
    return nRet;
}

//static int FmtI420ToNV12(uint8* src_frame,
//                         uint8* des_frame,
//                         int nWidth,
//                         int nHeight) {
//    uint8 * SrcU = src_frame + nWidth * nHeight;
//    uint8 * SrcV = SrcU + nWidth * nHeight / 4 ;
//    memcpy(des_frame, src_frame, nWidth * nHeight);
//    uint8 * DstU = des_frame + nWidth * nHeight;
//    for(int i = 0 ; i < nWidth * nHeight / 4 ; i++ ){
//        ( *DstU++) = ( *SrcU++);
//        ( *DstU++) = ( *SrcV++);
//    }
//    return 0;
//}

static int FmtI420Mirror(uint8_t *srcYuvData, uint8_t *dstYuvData, int width, int height) {
    uint8 *srcY = (uint8 *) srcYuvData;
    uint8 *srcU = srcY + width*height;
    uint8 *srcV = srcU + width*height/4;

    uint8 *dstY = (uint8 *) dstYuvData;
    uint8 *destU = dstY + width*height;
    uint8 *destV = destU + width*height/4;

    return I420Mirror(
            srcY,width,srcU, width/2,srcV, width/2,
            dstY,width,destU, width/2,destV, width/2,
            width,height
    );
}


JNIEXPORT jint JNICALL
RTMP_JAVA_INTERFACE(formatToI420)(JNIEnv *env, jobject instance, jbyteArray pData_,
                                jint nLen, jint nWidth, jint nHeight, jint pixFmt,
                                jint nDegree, jbyteArray pOu_) {
    jbyte *pData = env->GetByteArrayElements(pData_, NULL);
    jbyte *pOu = env->GetByteArrayElements(pOu_, NULL);
    //XLOGI("FormatToI420 %d,%d,%d", nLen, nWidth, nHeight);
    int result = FMTtoYUV420Planer((uint8 *) pData, nLen, nWidth, nHeight, pixFmt, nDegree, (uint8 *) pOu);
    env->ReleaseByteArrayElements(pData_, pData, 0);
    env->ReleaseByteArrayElements(pOu_, pOu, 0);
    return result;
}


JNIEXPORT jint JNICALL
RTMP_JAVA_INTERFACE(I420toNV12)(JNIEnv *env, jobject instance,
                                jbyteArray i420_src,
                                jbyteArray nv12_dst, int width,
                                int height) {
    jbyte *src = env->GetByteArrayElements(i420_src, NULL);
    jbyte *dst = env->GetByteArrayElements(nv12_dst, NULL);
    // 执行转换 I420 -> NV12 的转换
    int result = FmtI420ToNV12((uint8 *) src, (uint8 *) dst, width, height);
    // 释放资源
    env->ReleaseByteArrayElements(i420_src, src, 0);
    env->ReleaseByteArrayElements(nv12_dst, dst, 0);
    return result;
}

JNIEXPORT jint JNICALL
RTMP_JAVA_INTERFACE(I420Mirror)(JNIEnv *env, jobject instance,
                                jbyteArray data_src,
                                jbyteArray data_dst, int width,
                                int height) {
    jbyte *src = env->GetByteArrayElements(data_src, NULL);
    jbyte *dst = env->GetByteArrayElements(data_dst, NULL);
    // 执行转换 I420 -> NV12 的转换
    int result = FmtI420Mirror((uint8 *) src, (uint8 *) dst, width, height);
    // 释放资源
    env->ReleaseByteArrayElements(data_src, src, 0);
    env->ReleaseByteArrayElements(data_dst, dst, 0);
    return result;
}

#ifdef __cplusplus
}
#endif

