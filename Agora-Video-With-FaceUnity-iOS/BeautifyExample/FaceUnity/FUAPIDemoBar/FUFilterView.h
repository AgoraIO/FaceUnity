//
//  FUFilterView.h
//  FUAPIDemoBar
//
//  Created by L on 2018/6/27.
//  Copyright © 2018年 L. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FUDemoBarDefine.h"
#import "FUBaseModel.h"
#import "FUBaseCollectionView.h"

@protocol FUFilterViewDelegate <NSObject>

// 开启滤镜
- (void)filterViewDidSelectedFilter:(FUBaseModel *)param;
@end


@interface FUFilterView : FUBaseCollectionView

@property (nonatomic, assign) FUFilterViewType type ;

@property (nonatomic, assign) id<FUFilterViewDelegate>mDelegate ;

-(void)setDefaultFilter:(FUBaseModel *)filter;

@end

@interface FUFilterCell : UICollectionViewCell

@property (nonatomic, strong) UIImageView *imageView ;
@property (nonatomic, strong) UILabel *titleLabel ;
@end
