//
//  FaceUnityVideoFilter.h
//  BeautifyExample
//
//  Created by LSQ on 2020/8/6.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "VideoFilterDelegate.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, FUNamaHandleType) {
    FUNamaHandleTypeBeauty = 0,   /* items[0] ------ 放置 美颜道具句柄 */
    FUNamaHandleTypeItem = 1,     /* items[1] ------ 放置 普通道具句柄（包含很多，如：贴纸，aoimoji...若不单一存在，可放句柄集其他位置） */
    FUNamaHandleTypeFxaa = 2,     /* items[2] ------ fxaa抗锯齿道具句柄 */
    FUNamaHandleTypeGesture = 3,    /* items[3] ------ 手势识别道具句柄 */
    FUNamaHandleTypeChangeface = 4, /* items[4] ------ 海报换脸道具句柄 */
    FUNamaHandleTypeComic = 5,      /* items[5] ------ 动漫道具句柄 */
    FUNamaHandleTypeMakeup = 6,     /* items[6] ------ 美妆道具句柄 */
    FUNamaHandleTypePhotolive = 7,  /* items[7] ------ 异图道具句柄 */
    FUNamaHandleTypeAvtarHead = 8,  /* items[8] ------ Avtar头*/
    FUNamaHandleTypeAvtarHiar = 9,  /* items[9] ------ Avtar头发 */
    FUNamaHandleTypeAvtarbg = 10,  /* items[10] ------ Avtar背景 */
    FUNamaHandleTypeBodySlim = 11,  /* items[11] ------ 美体道具 */
    FUNamaHandleTypeBodyAvtar = 12,  /* 全身avtar */
    FUNamaHandleTotal = 13,
};

@interface FaceUnityVideoFilter : NSObject <VideoFilterDelegate>

@property (nonatomic, strong) dispatch_queue_t asyncLoadQueue;
@property (nonatomic, assign) BOOL enabled;
@end

NS_ASSUME_NONNULL_END
