//
//  FUBeautyView.h
//  FUAPIDemoBar
//
//  Created by L on 2018/6/27.
//  Copyright © 2018年 L. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FUDemoBarDefine.h"
#import "FUBaseModel.h"
#import "FUBaseCollectionView.h"
@class FUBeautyView;
@protocol FUBeautyViewDelegate <NSObject>

- (void)beautyCollectionView:(FUBeautyView *)beautyView didSelectedParam:(FUBaseModel *)param;

@end

@interface FUBeautyView : FUBaseCollectionView

@property (nonatomic, assign) id<FUBeautyViewDelegate>mDelegate ;

@end


@interface FUBeautyCell : UICollectionViewCell
@property (nonatomic, strong) UIImageView *imageView ;
@property (nonatomic, strong) UILabel *titleLabel ;
@end
