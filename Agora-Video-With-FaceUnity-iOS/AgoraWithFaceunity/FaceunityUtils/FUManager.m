//
//  FUManager.m
//  FULiveDemo
//
//  Created by yangliu on 2017/8/18.
//  Copyright © 2017年 yangliu. All rights reserved.
//

#import "FUManager.h"
#import "authpack.h"
#import "FULiveModel.h"
#import <sys/utsname.h>
#import <CoreMotion/CoreMotion.h>
#import "FUMusicPlayer.h"

@interface FUManager ()
{
    //MARK: Faceunity
    int items[4];
    int frameID;
    
    NSDictionary *hintDic;
    
    NSDictionary *alertDic ;
}

@property (nonatomic, strong) CMMotionManager *motionManager;
@property (nonatomic) int deviceOrientation;
/* 带屏幕方向的道具 */
@property (nonatomic, strong)NSArray *deviceOrientationItems;
@end

static FUManager *shareManager = NULL;

@implementation FUManager

+ (FUManager *)shareManager
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        shareManager = [[FUManager alloc] init];
    });
    
    return shareManager;
}

- (instancetype)init
{
    if (self = [super init]) {
        
        NSString *path = [[NSBundle mainBundle] pathForResource:@"v3.bundle" ofType:nil];
        
        [[FURenderer shareRenderer] setupWithDataPath:path authPackage:&g_auth_package authSize:sizeof(g_auth_package) shouldCreateContext:YES];
        
        [self setBeautyDefaultParameters];
        
        
        // 加载ai bundle
        [self loadAIModle];
        
        //animoji 添加抗锯齿，不用animoji不需要添加
        [self loadAnimojiFaxxBundle];
        NSLog(@"faceunitySDK version:%@",[FURenderer getVersion]);
        
        hintDic = @{
                    @"future_warrior":@"Open Your Mouth",
                    @"jet_mask":@"Puff Your Cheeks",
                    @"sdx2":@"Frown",
                    @"luhantongkuan_ztt_fu":@"Blink",
                    @"qingqing_ztt_fu":@"Give me a kiss",
                    @"xiaobianzi_zh_fu":@"Have a Smile",
                    @"xiaoxueshen_ztt_fu":@"Whistle",
                    @"hez_ztt_fu":@"Swivel",
                    @"fu_lm_koreaheart":@"Heart_a",
                    @"fu_zh_baoquan":@"Fist",
                    @"fu_zh_hezxiong":@"Namaste",
                    @"fu_ztt_live520":@"Heart_b",
                    @"ssd_thread_thumb":@"Thumb_up",
                    @"ssd_thread_six":@"Shaka sign",
                    @"ssd_thread_cute":@"Be cute",
                    };
        
        alertDic = @{
                     @"armesh_ex":@"High-precision version of AR marsk",
                     };
        
        [self loadItemDataSource];
        
        /* 带屏幕方向的道具 */
        self.deviceOrientationItems = @[@"ctrl_rain",@"ctrl_snow",@"ctrl_flower",@"ssd_thread_six",@"wobushi",@"gaoshiqing"];
        
        // init motion manager
        self.motionManager = [[CMMotionManager alloc] init];
        self.motionManager.accelerometerUpdateInterval = 0.5;// refresh every 1 second
        
        if ([self.motionManager isDeviceMotionAvailable]) {
            [self.motionManager startAccelerometerUpdates];
        }
        
        // default orientation is 0
        self.deviceOrientation = 0 ;
        fuSetDefaultOrientation(self.deviceOrientation) ;
        
        // default is NO
        self.performance = NO ;
    }
    
    return self;
}


-(void)loadAIModle{
    NSData *ai_bgseg = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"ai_bgseg.bundle" ofType:nil]];
    [FURenderer loadAIModelFromPackage:(void *)ai_bgseg.bytes size:(int)ai_bgseg.length aitype:FUAITYPE_BACKGROUNDSEGMENTATION];
    
    NSData *ai_facelandmarks75 = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"ai_facelandmarks75.bundle" ofType:nil]];
    [FURenderer loadAIModelFromPackage:(void *)ai_facelandmarks75.bytes size:(int)ai_facelandmarks75.length aitype:FUAITYPE_FACELANDMARKS75];

    NSData *ai_gesture = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"ai_gesture.bundle" ofType:nil]];
    [FURenderer loadAIModelFromPackage:(void *)ai_gesture.bytes size:(int)ai_gesture.length aitype:FUAITYPE_HANDGESTURE];
    NSData *ai_hairseg = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"ai_hairseg.bundle" ofType:nil]];
    [FURenderer loadAIModelFromPackage:(void *)ai_hairseg.bytes size:(int)ai_hairseg.length aitype:FUAITYPE_HAIRSEGMENTATION];

    NSData *ai_face_processor = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"ai_face_processor.bundle" ofType:nil]];
    [FURenderer loadAIModelFromPackage:(void *)ai_face_processor.bytes size:(int)ai_face_processor.length aitype:FUAITYPE_FACEPROCESSOR];
    
}


/** Judging authority based on certificate
 *  with permissions are ranked first,  without permissions behind
 */
- (void)loadItemDataSource {
    
    NSMutableArray *modesArray = [NSMutableArray arrayWithCapacity:1];
    NSArray *dataArray = [NSArray arrayWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"dataSource.plist" ofType:nil]];
    
    for (NSDictionary *dict in dataArray) {
        
        FULiveModel *model = [[FULiveModel alloc] init];
        
        NSString *itemName = dict[@"itemName"] ;
        model.title = itemName ;
        model.maxFace = [dict[@"maxFace"] integerValue] ;
        model.enble = NO;
        model.type = [dict[@"itemType"] integerValue];
        model.modules = dict[@"modules"] ;
        model.items = dict[@"items"] ;
        [modesArray addObject:model];
    }
    
    int module = fuGetModuleCode(0) ;
    
    if (!module) {
        
        _dataSource = [NSMutableArray arrayWithCapacity:1];
        
        for (FULiveModel *model in modesArray) {
            
            model.enble = YES ;
            [_dataSource addObject:model] ;
        }
        
        return ;
    }
    
    int insertIndex = 0;
    _dataSource = [modesArray mutableCopy];
    
    for (FULiveModel *model in modesArray) {
        
        if ([model.title isEqualToString:@"背景分割"] || [model.title isEqualToString:@"手势识别"]) {
            if ([self isLiteSDK]) {
                continue ;
            }
        }
        
        for (NSNumber *num in model.modules) {
            
            BOOL isEable = module & [num intValue] ;
            
            if (isEable) {
                
                [_dataSource removeObject:model];
                
                model.enble = YES ;
                
                [_dataSource insertObject:model atIndex:insertIndex] ;
                insertIndex ++ ;
                
                break ;
            }
        }
    }
}

/*set default params */
- (void)setBeautyDefaultParameters {
    
    self.filtersDataSource = @[@"origin", @"delta", @"electric", @"slowlived", @"tokyo", @"warm"];
    
    self.beautyFiltersDataSource = @[@"ziran", @"danya", @"fennen", @"qingxin", @"hongrun"];
    self.filtersCHName = @{@"origin" : @"原图", @"ziran":@"自然", @"danya":@"淡雅", @"fennen":@"粉嫩", @"qingxin":@"清新", @"hongrun":@"红润"};
    
    
    // chech the meaning of every param in FUManager.h
    self.selectedFilter         = self.filtersDataSource[0] ;
    self.selectedFilterLevel    = 0.5 ;
    
    self.skinDetectEnable       = YES ;
    self.blurShape              = 0 ;
    self.blurLevel              = 0.7 ;
    self.whiteLevel             = 0.5 ;
    self.redLevel               = 0.5 ;
    
    self.eyelightingLevel       = 0.7 ;
    self.beautyToothLevel       = 0.7 ;
    
    self.faceShape              = 4 ;
    self.enlargingLevel         = 0.4 ;
    self.thinningLevel          = 0.4 ;
    
    self.enlargingLevel_new     = 0.4 ;
    self.thinningLevel_new      = 0.4 ;
    self.jewLevel               = 0.3 ;
    self.foreheadLevel          = 0.3 ;
    self.noseLevel              = 0.5 ;
    self.mouthLevel             = 0.4 ;
    
    self.enableGesture = NO;
    self.enableMaxFaces = NO;
}

-(NSArray<FULiveModel *> *)dataSource {
    
    return _dataSource ;
}


- (void)loadItems
{
    /** load default item*/
    [self loadItem:self.selectedItem];
    
    /** load default beauty item*/
    [self loadFilterLandmarksType:FUAITYPE_FACELANDMARKS75];
}

- (void)setEnableGesture:(BOOL)enableGesture
{
    _enableGesture = enableGesture;
    if (_enableGesture) {
        [self loadGesture];
    }else{
        if (items[2] != 0) {
            
            NSLog(@"faceunity: destroy gesture");
            
            [FURenderer destroyItem:items[2]];
            
            items[2] = 0;
        }
    }
}

/** max face num could recognition, suggested is within 4, could not large than 8*/
- (void)setEnableMaxFaces:(BOOL)enableMaxFaces
{
    if (_enableMaxFaces == enableMaxFaces) {
        return;
    }
    
    _enableMaxFaces = enableMaxFaces;
    
    if (_enableMaxFaces) {
        [FURenderer setMaxFaces:4];
    }else{
        [FURenderer setMaxFaces:1];
    }
    
}

- (void)setConnector:(id<Connector>)connector {
    self.connector = connector;
}

/** destroy all items */
- (void)destoryItems
{
    [FURenderer destroyAllItems];
    
    /** set all handles 0*/
    for (int i = 0; i < sizeof(items) / sizeof(int); i++) {
        items[i] = 0;
    }
    
    /** clear buffer data*/
    [FURenderer OnDeviceLost];
}

/** get hint of current item  */
- (NSString *)hintForItem:(NSString *)item
{
    return hintDic[item];
}

- (NSString *)alertForItem:(NSString *)item {
    return alertDic[item] ;
}

- (void)setCalibrating {
    
    fuSetExpressionCalibration(1) ;
}

- (void)removeCalibrating {
    
    fuSetExpressionCalibration(0) ;
}

- (BOOL)isCalibrating{
    float is_calibrating[1] = {0.0};
    
    fuGetFaceInfo(0, "is_calibrating", is_calibrating, 1);
    return is_calibrating[0] == 1.0;
}

- (void)loadAnimojiFaxxBundle {
    
    /** create handle first */
    NSString *path = [[NSBundle mainBundle] pathForResource:@"fxaa.bundle" ofType:nil];
    int itemHandle = [FURenderer itemWithContentsOfFile:path];
    
    /** destroy old one */
    if (items[3] != 0) {
        NSLog(@"faceunity: destroy item");
        [FURenderer destroyItem:items[3]];
    }
    
    /** put the handle wo just created in items[3]*/
    items[3] = itemHandle;
}

- (void)destoryAnimojiFaxxBundle {
    
    // destroy faxx bundle
    if (items[3] != 0) {
        NSLog(@"faceunity: destroy item");
        [FURenderer destroyItem:items[3]];
        items[3] = 0 ;
    }
    
}

#pragma -Faceunity Load Data
/** load common items */
- (void)loadItem:(NSString *)itemName
{
    self.selectedItem = itemName ;
    
    int destoryItem = items[1];
    
    if (itemName != nil && ![itemName isEqual: @"noitem"]) {
        
        /** create handle first */
        NSString *path = [[NSBundle mainBundle] pathForResource:[itemName stringByAppendingString:@".bundle"] ofType:nil];
        int itemHandle = [FURenderer itemWithContentsOfFile:path];
        
        // set 3D FlipH
        BOOL isPortraitDrive = [itemName hasPrefix:@"picasso_e"];
        BOOL isAnimoji = [itemName hasSuffix:@"_Animoji"];
        
        if (isPortraitDrive || isAnimoji) {
            [FURenderer itemSetParam:itemHandle withName:@"{\"thing\":\"<global>\",\"param\":\"follow\"}" value:@(1)];
            [FURenderer itemSetParam:itemHandle withName:@"is3DFlipH" value:@(1)];
            [FURenderer itemSetParam:itemHandle withName:@"isFlipExpr" value:@(1)];
            [FURenderer itemSetParam:itemHandle withName:@"isFlipTrack" value:@(1)];
            [FURenderer itemSetParam:itemHandle withName:@"isFlipLight" value:@(1)];
        }
        
        if ([itemName isEqualToString:@"luhantongkuan_ztt_fu"]) {
            [FURenderer itemSetParam:itemHandle withName:@"flip_action" value:@(1)];
        }
        
        if ([self.deviceOrientationItems containsObject:itemName]) {//带重力感应道具
            [FURenderer itemSetParam:itemHandle withName:@"rotMode" value:@(self.deviceOrientation)];

        }else{
        }
        /** put the handle wo just created in items[1]*/
        items[1] = itemHandle;
    }else{
        /** set the old handle 0*/
        items[1] = 0;
    }
    NSLog(@"faceunity: load item");
    
    /** destroy old item */
    if (destoryItem != 0)
    {
        NSLog(@"faceunity: destroy item");
        [FURenderer destroyItem:destoryItem];
    }
}

/** load filter bundle */
- (void)loadFilterLandmarksType:(FUAITYPE)landmarksType
{
    if (items[0] == 0) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"face_beautification.bundle" ofType:nil];
        items[0] = [FURenderer itemWithContentsOfFile:path];
        [FURenderer itemSetParam:items[0] withName:@"landmarks_type" value:@(landmarksType)];
    }
}

/** load guester recognition bundle , default is NOT load*/
- (void)loadGesture
{
    if (items[2] != 0) {
        
        NSLog(@"faceunity: destroy gesture");
        
        [FURenderer destroyItem:items[2]];
        
        items[2] = 0;
    }
    
    NSString *path = [[NSBundle mainBundle] pathForResource:@"heart_v2.bundle" ofType:nil];
    
    items[2] = [FURenderer itemWithContentsOfFile:path];
}

/** set the params */
- (void)setBeautyParams {
    
    /*   check the meaning of every param in FUManager.h   */
    [FURenderer itemSetParam:items[0] withName:@"skin_detect" value:@(self.skinDetectEnable)]; //Whether to turn on skin detection
    [FURenderer itemSetParam:items[0] withName:@"heavy_blur" value:@(self.blurShape)]; // Skin type (0、1、) Light：0，Heavy：1
    [FURenderer itemSetParam:items[0] withName:@"blur_level" value:@(self.blurLevel * 6.0 )]; //Blur (0.0 - 6.0)
    [FURenderer itemSetParam:items[0] withName:@"color_level" value:@(self.whiteLevel)]; //Whiten (0~1)
    [FURenderer itemSetParam:items[0] withName:@"red_level" value:@(self.redLevel)]; //Rosy (0~1)
    [FURenderer itemSetParam:items[0] withName:@"eye_bright" value:@(self.eyelightingLevel)]; // Eye Bright
    [FURenderer itemSetParam:items[0] withName:@"tooth_whiten" value:@(self.beautyToothLevel)];// Tooth Whiten
    
    [FURenderer itemSetParam:items[0] withName:@"face_shape" value:@(self.faceShape)]; //Style (0、1、2、3、4)Fairy：0，Belle：1，Nature：2，Default：3，Customize：4
    
    [FURenderer itemSetParam:items[0] withName:@"eye_enlarging" value:self.faceShape == 4 ? @(self.enlargingLevel_new) : @(self.enlargingLevel)]; //Eye Enlarge (0~1)
    [FURenderer itemSetParam:items[0] withName:@"cheek_thinning" value:self.faceShape == 4 ? @(self.thinningLevel_new) : @(self.thinningLevel)]; //Cheek Thin (0~1)
    [FURenderer itemSetParam:items[0] withName:@"intensity_chin" value:@(self.jewLevel)]; /**Chin (0~1)*/
    [FURenderer itemSetParam:items[0] withName:@"intensity_nose" value:@(self.noseLevel)];/**Nose (0~1)*/
    [FURenderer itemSetParam:items[0] withName:@"intensity_forehead" value:@(self.foreheadLevel)];/**Forehead (0~1)*/
    [FURenderer itemSetParam:items[0] withName:@"intensity_mouth" value:@(self.mouthLevel)];/**Mouth (0~1)*/
    
    [FURenderer itemSetParam:items[0] withName:@"filter_name" value:self.selectedFilter]; //filter name
    [FURenderer itemSetParam:items[0] withName:@"filter_level" value:@(self.selectedFilterLevel)]; //filter level
}

- (CVPixelBufferRef)renderItemsToPixelBuffer:(CVPixelBufferRef)pixelBuffer
{
    // set the orientation based on motion when there is no faces
//    if (![FURenderer isTracking]) {
        
        CMAcceleration acceleration = self.motionManager.accelerometerData.acceleration ;
        
        int orientation = 0;
//        if (acceleration.x >= 0.75) {
//            orientation = 3;
//        } else if (acceleration.x <= -0.75) {
//            orientation = 1;
//        } else if (acceleration.y <= -0.75) {
//            orientation = 0;
//        } else if (acceleration.y >= 0.75) {
//            orientation = 2;
//        }
        
        if (self.deviceOrientation != orientation) {
            self.deviceOrientation = orientation ;
            
            //针对带重力道具
            [FURenderer itemSetParam:items[1] withName:@"rotMode" value:@(self.deviceOrientation)];
            /* 手势识别里 666 道具，带有全屏元素 */
            [FURenderer itemSetParam:items[1] withName:@"rotationMode" value:@(self.deviceOrientation)];
            
            fuSetDefaultOrientation(self.deviceOrientation);
            fuSetDefaultRotationMode(self.deviceOrientation);
        }
//    }
    
    [self setBeautyParams];
    
    //flipx ,when YES ,can mirrer the item image
    CVPixelBufferRef buffer = [[FURenderer shareRenderer] renderPixelBuffer:pixelBuffer withFrameId:frameID items:items itemCount:sizeof(items)/sizeof(int) flipx:YES];
    frameID += 1;
    
    return buffer;
}

- (void)set3DFlipH {
    
    [FURenderer itemSetParam:items[1] withName:@"is3DFlipH" value:@(1)];
    [FURenderer itemSetParam:items[1] withName:@"isFlipExpr" value:@(1)];
}

- (void)setLoc_xy_flip {
    
    [FURenderer itemSetParam:items[1] withName:@"loc_x_flip" value:@(1)];
    [FURenderer itemSetParam:items[1] withName:@"loc_y_flip" value:@(1)];
}

- (void)musicFilterSetMusicTime {
    
    [FURenderer itemSetParam:items[1] withName:@"music_time" value:@([FUMusicPlayer sharePlayer].currentTime * 1000 + 50)];//需要加50ms的延迟
}

- (CGPoint)getFaceCenterInFrameSize:(CGSize)frameSize{
    
    static CGPoint preCenter;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        preCenter = CGPointMake(0.49, 0.5);
    });
    
    float faceRect[4];
    int ret = [FURenderer getFaceInfo:0 name:@"face_rect" pret:faceRect number:4];
    
    if (ret == 0) {
        return preCenter;
    }
    
    CGFloat centerX = (faceRect[0] + faceRect[2]) * 0.5;
    CGFloat centerY = (faceRect[1] + faceRect[3]) * 0.5;
    
    centerX = frameSize.width - centerX;
    centerX = centerX / frameSize.width;
    
    centerY = frameSize.height - centerY;
    centerY = centerY / frameSize.height;
    
    CGPoint center = CGPointMake(centerX, centerY);
    
    preCenter = center;
    
    return center;
}

- (void)getLandmarks:(float *)landmarks
{
    int ret = [FURenderer getFaceInfo:0 name:@"landmarks" pret:landmarks number:150];
    
    if (ret == 0) {
        memset(landmarks, 0, sizeof(float)*150);
    }
}

- (BOOL)isTracking
{
    return [FURenderer isTracking] > 0;
}

- (void)onCameraChange
{
    [FURenderer onCameraChange];
}

- (NSString *)getError
{
    // error code
    int errorCode = fuGetSystemError();
    
    if (errorCode != 0) {
        
        NSString *errorStr = [NSString stringWithUTF8String:fuGetSystemErrorString(errorCode)];
        
        return errorStr;
    }
    
    return nil;
}


- (BOOL)isLiteSDK {
    NSString *version = [FURenderer getVersion];
    return [version containsString:@"lite"];
}


- (NSString *)getPlatformtype {
    
    struct utsname systemInfo;
    uname(&systemInfo);
    
    NSString *platform = [NSString stringWithCString:systemInfo.machine encoding:NSASCIIStringEncoding];
    if ([platform isEqualToString:@"iPhone1,1"]) return @"iPhone 2G";
    if ([platform isEqualToString:@"iPhone1,2"]) return @"iPhone 3G";
    if ([platform isEqualToString:@"iPhone2,1"]) return @"iPhone 3GS";
    if ([platform isEqualToString:@"iPhone3,1"]) return @"iPhone 4";
    if ([platform isEqualToString:@"iPhone3,2"]) return @"iPhone 4";
    if ([platform isEqualToString:@"iPhone3,3"]) return @"iPhone 4";
    if ([platform isEqualToString:@"iPhone4,1"]) return @"iPhone 4S";
    if ([platform isEqualToString:@"iPhone5,1"]) return @"iPhone 5";
    if ([platform isEqualToString:@"iPhone5,2"]) return @"iPhone 5";
    if ([platform isEqualToString:@"iPhone5,3"]) return @"iPhone 5c";
    if ([platform isEqualToString:@"iPhone5,4"]) return @"iPhone 5c";
    if ([platform isEqualToString:@"iPhone6,1"]) return @"iPhone 5s";
    if ([platform isEqualToString:@"iPhone6,2"]) return @"iPhone 5s";
    if ([platform isEqualToString:@"iPhone7,1"]) return @"iPhone 6 Plus";
    if ([platform isEqualToString:@"iPhone7,2"]) return @"iPhone 6";
    if ([platform isEqualToString:@"iPhone8,1"]) return @"iPhone 6s";
    if ([platform isEqualToString:@"iPhone8,2"]) return @"iPhone 6s Plus";
    if ([platform isEqualToString:@"iPhone8,4"]) return @"iPhone SE";
    if ([platform isEqualToString:@"iPhone9,1"]) return @"iPhone 7";
    if ([platform isEqualToString:@"iPhone9,2"]) return @"iPhone 7 Plus";
    if ([platform isEqualToString:@"iPhone10,1"]) return @"iPhone 8";
    if ([platform isEqualToString:@"iPhone10,2"]) return @"iPhone 8 Plus";
    if ([platform isEqualToString:@"iPhone10,3"]) return @"iPhone X";
    
    if ([platform isEqualToString:@"iPod1,1"])   return @"iPod Touch 1G";
    if ([platform isEqualToString:@"iPod2,1"])   return @"iPod Touch 2G";
    if ([platform isEqualToString:@"iPod3,1"])   return @"iPod Touch 3G";
    if ([platform isEqualToString:@"iPod4,1"])   return @"iPod Touch 4G";
    if ([platform isEqualToString:@"iPod5,1"])   return @"iPod Touch 5G";
    if ([platform isEqualToString:@"iPod7,1"])   return @"iPod Touch 6G";
    
    if ([platform isEqualToString:@"iPad1,1"])   return @"iPad 1G";
    
    if ([platform isEqualToString:@"iPad2,1"])   return @"iPad 2";
    if ([platform isEqualToString:@"iPad2,2"])   return @"iPad 2";
    if ([platform isEqualToString:@"iPad2,3"])   return @"iPad 2";
    if ([platform isEqualToString:@"iPad2,4"])   return @"iPad 2";
    if ([platform isEqualToString:@"iPad2,5"])   return @"iPad Mini 1G";
    if ([platform isEqualToString:@"iPad2,6"])   return @"iPad Mini 1G";
    if ([platform isEqualToString:@"iPad2,7"])   return @"iPad Mini 1G";
    
    if ([platform isEqualToString:@"iPad3,1"])   return @"iPad 3";
    if ([platform isEqualToString:@"iPad3,2"])   return @"iPad 3";
    if ([platform isEqualToString:@"iPad3,3"])   return @"iPad 3";
    if ([platform isEqualToString:@"iPad3,4"])   return @"iPad 4";
    if ([platform isEqualToString:@"iPad3,5"])   return @"iPad 4";
    if ([platform isEqualToString:@"iPad3,6"])   return @"iPad 4";
    
    if ([platform isEqualToString:@"iPad4,1"])   return @"iPad Air";
    if ([platform isEqualToString:@"iPad4,2"])   return @"iPad Air";
    if ([platform isEqualToString:@"iPad4,3"])   return @"iPad Air";
    
    if ([platform isEqualToString:@"iPad4,4"])   return @"iPad Mini 2G";
    if ([platform isEqualToString:@"iPad4,5"])   return @"iPad Mini 2G";
    if ([platform isEqualToString:@"iPad4,6"])   return @"iPad Mini 2G";
    if ([platform isEqualToString:@"iPad4,7"])   return @"iPad Mini 3G";
    if ([platform isEqualToString:@"iPad4,8"])   return @"iPad Mini 3G";
    if ([platform isEqualToString:@"iPad4,9"])   return @"iPad Mini 3G";
    if ([platform isEqualToString:@"iPad5,1"])   return @"iPad Mini 4G";
    if ([platform isEqualToString:@"iPad5,2"])   return @"iPad Mini 4G";
    if ([platform isEqualToString:@"iPad5,3"])   return @"iPad Air 2";
    if ([platform isEqualToString:@"iPad5,4"])   return @"iPad Air 2";
    if ([platform isEqualToString:@"iPad6,3"])   return @"iPad Pro (9.7 inch)";
    if ([platform isEqualToString:@"iPad6,4"])   return @"iPad Pro (9.7 inch)";
    if ([platform isEqualToString:@"iPad6,7"])   return @"iPad Pro (12.9 inch)";
    if ([platform isEqualToString:@"iPad6,8"])   return @"iPad Pro (12.9 inch)";
    
    if ([platform isEqualToString:@"i386"])      return @"iPhone Simulator";
    if ([platform isEqualToString:@"x86_64"])    return @"iPhone Simulator";
    
    return platform;
}


-(BOOL)isHaveTrackFaceItemsRendering{
    if (items[0] || items[1]) {
        return YES;
    }
    return NO;
}

//- (void)didOutputFrame:(VideoFrame *)frame {
//    if (![frame.buffer isKindOfClass:[CustomCVPixelBuffer class]]) {
//        return;
//    }
//
//    CustomCVPixelBuffer* buffer = frame.buffer;
//    CVPixelBufferRef pixelBuffer = buffer.pixelBuffer;
//    
//    CVPixelBufferLockBaseAddress(pixelBuffer, 0);
//    
//    [self renderItemsToPixelBuffer:pixelBuffer];
//    
////    if ([self.connector respondsToSelector:@selector(didOutputFrame:)]) {
////        [self.connector didOutputFrame:frame];
////    }
//    
//    CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
//}

//- (void)didOutputPixelBuffer:(CVPixelBufferRef)pixelBuffer withTimeStamp:(CMTime)timeStamp rotation:(VideoRotation)rotation {
//    CVPixelBufferLockBaseAddress(pixelBuffer, 0);
//
//    [self renderItemsToPixelBuffer:pixelBuffer];
//
//    if ([self.connector respondsToSelector:@selector(didOutputPixelBuffer:withTimeStamp:rotation:)]) {
//        [self.connector didOutputPixelBuffer:pixelBuffer withTimeStamp:timeStamp rotation:rotation];
//    }
//
//    CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
//}
@end
