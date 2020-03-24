//
//  FUManager.h
//  FULiveDemo
//
//  Created by yangliu on 2017/8/18.
//  Copyright © 2017年 yangliu. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "FURenderer.h"

@class FULiveModel ;

@protocol Connector;

@interface FUManager : NSObject

@property (nonatomic, assign)               BOOL enableGesture;/** gesture recognition, default is NO*/
@property (nonatomic, assign)               BOOL enableMaxFaces;/** max recognition faces, NO for 1, Yes for 4*/

@property (nonatomic, assign) BOOL skinDetectEnable ;   // skin detect
@property (nonatomic, assign) NSInteger blurShape;      // blur type (0、1、) clear：0，not clear：1
@property (nonatomic, assign) double blurLevel;         // blur (0.0 - 6.0)
@property (nonatomic, assign) double whiteLevel;        // skin whiten
@property (nonatomic, assign) double redLevel;          // skin red
@property (nonatomic, assign) double eyelightingLevel;  // eye bright
@property (nonatomic, assign) double beautyToothLevel;  // tooth whiten

@property (nonatomic, assign) NSInteger faceShape;        //face type (0、1、2、3、4)girlish：0，model：1，origin：2，default：3，custom：4
@property (nonatomic, assign) double enlargingLevel;      /** eye enlarging (0~1)*/
@property (nonatomic, assign) double thinningLevel;       /** cheek thinning (0~1)*/
@property (nonatomic, assign) double enlargingLevel_new;  /** new version of eye enlarging (0~1)*/
@property (nonatomic, assign) double thinningLevel_new;   /** new version of cheek thinning (0~1)*/

@property (nonatomic, assign) double jewLevel;            /** chin (0~1)*/
@property (nonatomic, assign) double foreheadLevel;       /** forehead (0~1)*/
@property (nonatomic, assign) double noseLevel;           /** nose (0~1)*/
@property (nonatomic, assign) double mouthLevel;          /** mouth (0~1)*/

@property (nonatomic, strong) NSArray<NSString *> *filtersDataSource;     /** filter array */
@property (nonatomic, strong) NSArray<NSString *> *beautyFiltersDataSource;     /** beauty filter array */
@property (nonatomic, strong) NSDictionary<NSString *,NSString *> *filtersCHName;       /** Chinese character of filter array */
@property (nonatomic, strong) NSString *selectedFilter; /* current filter */
@property (nonatomic, assign) double selectedFilterLevel; /* level of current filter */

@property (nonatomic, strong)               NSMutableArray<FULiveModel *> *dataSource;  /** items array */
@property (nonatomic, strong)               NSString *selectedItem;     /**current item name */

@property (nonatomic, assign) BOOL performance ;

@property(nonatomic, weak) id<Connector> connector;

+ (FUManager *)shareManager;

// set default beauty params
- (void)setBeautyDefaultParameters ;

/** load default items */
- (void)loadItems;

/** load beauty face item */
- (void)loadFilterLandmarksType:(FUAITYPE)landmarksType;

/** destroy all items */
- (void)destoryItems;

/** load common item with name */
- (void)loadItem:(NSString *)itemName;

// load Bundle of @"fxaa.bundle"
- (void)loadAnimojiFaxxBundle ;

// destroy bundle of @"fxaa.bundle"
- (void)destoryAnimojiFaxxBundle ;

// set music time for items with music
- (void)musicFilterSetMusicTime ;

/** set  Facial calibration **/
- (void)setCalibrating ;

/** remove  Facial calibration **/
- (void)removeCalibrating ;

/** is  Facial calibration now?**/
- (BOOL)isCalibrating ;

/** get hint of current item */
- (NSString *)hintForItem:(NSString *)item;

/** get alert of current item */
- (NSString *)alertForItem:(NSString *)item ;

/** render items effect to pixelBuffer */
- (CVPixelBufferRef)renderItemsToPixelBuffer:(CVPixelBufferRef)pixelBuffer;

// set 3D flip
- (void)set3DFlipH ;

// set flip
- (void)setLoc_xy_flip ;

/** get 75 landmarks of current face*/
- (void)getLandmarks:(float *)landmarks;

/** get center point of current face rectangle*/
- (CGPoint)getFaceCenterInFrameSize:(CGSize)frameSize;

/** is there human face in current image */
- (BOOL)isTracking;

/** change camera to back/front camera */
- (void)onCameraChange;

/** get error string*/
- (NSString *)getError;

/** is the SDK lite version **/
- (BOOL)isLiteSDK ;

/** get current device platforma**/
- (NSString *)getPlatformtype ;


/* isHaveItem in rendering */
-(BOOL)isHaveTrackFaceItemsRendering;

@end
